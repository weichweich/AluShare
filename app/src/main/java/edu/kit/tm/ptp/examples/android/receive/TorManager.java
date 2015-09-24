package edu.kit.tm.ptp.examples.android.receive;

import android.content.Context;
import android.os.AsyncTask;

import net.freehaven.tor.control.TorControlConnection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;
import java.util.zip.ZipInputStream;

import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.ptp.utility.Constants;

/**
 * Android-specific manager for the Tor process. Might be included into main PeerTorPeer codebase in the future.
 *
 * @author Simeon Andreev
 *
 */
public class TorManager {

    public static final long controlPortTimeout = 50 * 1000;
    public static final long bootstrapTimeout = 90 * 1000;
    public static final String delimiter = ":";
    public static final String controlPortFile = "controlport";
    public static final String workingSubdirectory = "/ptphome/";

    private static Process torProcess = null;

    public interface Listener {

        void success(String message, int progress);

        void update(String message, int progress);

        void failure(String message, int progress);
    }

    private static class Update {

        public final int result;
        public final String message;
        public final int progress;

        public Update(int result, String message, int progress) {
            this.result = result;
            this.message = message;
            this.progress = progress;
        }
    }

    private static abstract class Task extends AsyncTask<Void, Update, Void> {

        protected final int SUCCESS = 0;
        protected final int FAILURE = 1;
        protected final int UPDATE = 2;

        protected final Context context;
        protected final Listener listener;

        public Task(Context context, Listener listener) {
            this.context = context;
            this.listener = listener;
        }

        protected void onProgressUpdate(Update... update) {
            if (update.length == 0) return;
            final int l = update.length - 1;
            if (update[l].result == SUCCESS) listener.success(update[l].message, update[l].progress);
            else if (update[l].result == UPDATE) listener.update(update[l].message, update[l].progress);
            else if (update[l].result == FAILURE) listener.failure(update[l].message, update[l].progress);
        }
    }

    private static class StartTask extends Task {

        public StartTask(Context context, Listener listener) { super(context, listener); }

        protected Void doInBackground(Void... params) {
            final String directory = context.getFilesDir().getPath();
            final String workingDirectory = directory + workingSubdirectory;
            final String torFile = workingDirectory + "tor";
            final String torrcFile = workingDirectory + "torrc";
            final String configFile = workingDirectory + "/config/ptp.ini";
            final String portFile = workingDirectory + controlPortFile;

            try {
                // Check if Tor is already running.
                int controlPort = getControlPort(directory);
                if (controlPort != -1) {
                    try {
                        Socket s = new Socket(Constants.localhost, controlPort);
                        TorControlConnection conn = new TorControlConnection(s);
                        conn.authenticate(new byte[0]);
                        final int socksPort = parsePort(conn.getInfo("net/listeners/socks").replace("\"", ""));
                        // If so, do not start it again.
                        publishProgress(new Update(SUCCESS, "Tor already running" + delimiter + controlPort + delimiter + socksPort, 0));
                        return null;
                    } catch (IOException ignored) {
                    }
                }
                new File(workingDirectory).mkdirs();
                new File(workingDirectory + "/config/").mkdir();
                copy(context, torFile, R.raw.tor, true);
                copy(context, torrcFile, R.raw.torrc, false);
                copy(context, configFile, R.raw.ptp, false);
                new File(torFile).setExecutable(true);

                /** The parameters for the Tor execution command. */
                final String[] cmd = {
                        /** The Tor executable file to run. */
                        torFile,
                        /** Tell Tor which torrc file to use. */
                        Constants.torrcoption,
                        torrcFile,
                        /** Tell Tor to use a cache directory. */
                        Constants.datadiroption,//"DataDirectory",
                        workingDirectory.toString(),
                        /** Tell Tor to write its control port to a file. */
                        Constants.ctlportoutoption,//"ControlPortWriteToFile",
                        portFile
                };

                torProcess = Runtime.getRuntime().exec(cmd);
                publishProgress(new Update(UPDATE, "Bootstrapping started.", 0));

                // Wait until the control port file is written.
                boolean controlPortFileExists = false;
                long waited = 0;
                while (waited < controlPortTimeout) {
                    try {
                        controlPortFileExists = new File(portFile).exists();
                        if (controlPortFileExists) break;
                        final long start = System.currentTimeMillis();
                        Thread.sleep(1000);
                        waited += System.currentTimeMillis() - start;
                    } catch (InterruptedException e) {
                        // Waiting was interrupted. Do nothing.
                    }
                }
                if (!controlPortFileExists) throw new TimeoutException("Tor did not create the control port file in the given timeout.");

                controlPort = getControlPort(directory);
                if (controlPort == -1) throw new TimeoutException("Could not wasRead the control port output file.");

                Socket socket = new Socket(Constants.localhost, controlPort);
                TorControlConnection conn = new TorControlConnection(socket);
                conn.authenticate(new byte[0]);
                int percent = 0;

                // Wait until the bootstrapping is done.
                boolean done = false;
                waited = 0;
                while (waited < bootstrapTimeout) {
                    try {

                        String notification = conn.getInfo("status/bootstrap-phase");
                        done |= notification.startsWith("NOTICE BOOTSTRAP PROGRESS=100 TAG=done");
                        if (done) break;
                        percent = parsePercent(notification);
                        publishProgress(new Update(UPDATE, "Bootstrap: " + percent, percent));
                        final long start = System.currentTimeMillis();
                        Thread.sleep(1000);
                        waited += System.currentTimeMillis() - start;
                    } catch (InterruptedException e) {
                        // Waiting was interrupted. Do nothing.
                    }
                }
                if (!done) throw new TimeoutException("Tor did not bootstrap in the given timeout.");
                final int socksPort = parsePort(conn.getInfo("net/listeners/socks").replace("\"", ""));

                publishProgress(new Update(SUCCESS, "Bootstrapping done" + delimiter + controlPort + delimiter + socksPort, 100));
            } catch (Exception e) {
                if (torProcess != null) torProcess.destroy();
                new File(portFile).delete();
                publishProgress(new Update(FAILURE, e.getMessage(), 0));
            }
            return null;
        }
    }

    private static class StopTask extends Task {

        public StopTask(Context context, Listener listener) { super(context, listener); }

        protected Void doInBackground(Void... params) {
            final String directory = context.getFilesDir().getPath();

            try {
                final int port = getControlPort(directory);
                shutdown(port);
                publishProgress(new Update(SUCCESS, "Signaled Tor to shutdown.", 100));
            } catch (Exception e) {
                publishProgress(new Update(FAILURE, e.getMessage(), 0));
                if (torProcess != null) torProcess.destroy();
                else publishProgress(new Update(FAILURE, "Tor not found!", 0));
            }
            // Just to be sure: kill the process after 2 seconds
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {	}

            if (torProcess != null) torProcess.destroy();
            else publishProgress(new Update(FAILURE, "Tor not found!", 0));


            return null;
        }
    }

    private static Listener dummy = new Listener() {
        @Override
        public void success(String message, int progress) {}
        @Override
        public void update(String message, int progress) {}
        @Override
        public void failure(String message, int progress) {}
    };

    public static void start(Context context, Listener listener) { new StartTask(context, listener).execute(); }

    public static void start(Context context) { new StartTask(context, dummy).execute(); }

    public static void stop(Context context, Listener listener) { new StopTask(context, listener).execute(); }

    public static void stop(Context context) { new StopTask(context, dummy).execute(); }

    public static void shutdown(int controlPort) throws UnknownHostException, IOException {
        Socket s = new Socket(Constants.localhost, controlPort);
        TorControlConnection conn = new TorControlConnection(s);
        conn.authenticate(new byte[0]);
        conn.shutdownTor(Constants.shutdownsignal);
    }

    public static int getControlPort(String directory) {
        final File portFile = new File(directory + workingSubdirectory + controlPortFile);
        int port = -1;
        BufferedReader buffer = null;

        try {
            InputStream file = new FileInputStream(portFile);
            InputStreamReader reader = new InputStreamReader(file);
            buffer = new BufferedReader(reader);
            final String line = buffer.readLine();
            port = parsePort(line);
        } catch (Exception ignored) {

        } finally {
            // pretty Java code
            try { if (buffer != null) buffer.close(); } catch (IOException ignored) { }
        }

        return port;
    }

    public static String getWorkingDirectory(String directory) { return directory + workingSubdirectory; }

    private static int parsePort(String line) { return Integer.valueOf(line.substring(line.lastIndexOf(":") + 1)); }

    private static int parsePercent(String line) {
        int start = line.indexOf("PROGRESS=") + "PROGRESS=".length();
        int end = line.indexOf(" ", start);
        return Integer.valueOf(line.substring(start, end));
    }

    private static void copy(Context context, String location, int resource, boolean zip) throws IOException {
        File destination = new File(location);
        if (destination.exists()) return;

        InputStream in = context.getResources().openRawResource(resource);
        FileOutputStream out = new FileOutputStream(destination);
        ZipInputStream zis = null;

        if (zip) {
            zis = new ZipInputStream(in);
            zis.getNextEntry();
            in = zis;
        }
        byte[] buffer = new byte[4096];
        int bytecount;
        while ((bytecount = in.read(buffer)) > 0)
            out.write(buffer, 0, bytecount);

        out.close();
        in.close();
        if (zip) zis.close();
    }
}
