package edu.kit.tm.pseprak2.alushare.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.network.NetworkingService;
import edu.kit.tm.pseprak2.alushare.network.NetworkingServiceConnectionListener;

public class SplashScreen extends AppCompatActivity {
    private boolean serviceBound = false;
    private ProgressBar progressBar;
    private TextView textView;
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            serviceBound = true;


            // We've bound to LocalService, cast the IBinder and get LocalService instance
            final NetworkingService.NetworkBinder binder = (NetworkingService.NetworkBinder) service;
            if (binder.getService().isProtocolConnected()) {
                start();
                Log.d("SplashScreen", "isProtocolConnected");
            }
            binder.getService().setConnectionListener(new NetworkingServiceConnectionListener() {

                @Override
                public void connected() {
                    SplashScreen.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(100);
                            textView.setText("Verbindung hergestellt");
                            start();
                            binder.getService().setConnectionListener(null);
                        }
                    });
                }

                @Override
                public void connectionFailed() {
                    SplashScreen.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText("Verbindung konnte nicht hergestellt werden");
                            Log.d("SplashScreen", "Connection Failed");

                        }
                    });
                    binder.getService().restartNetwork();
                }

                @Override
                public void connectionProgress(final int progress) {
                    SplashScreen.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText("Verbindung zum Tor-Netzwerk wird hergestellt");
                            progressBar.setProgress(progress);
                        }
                    });
                }

                @Override
                public void networkingIDCreated(String nid) {
                    binder.getService().setConnectionListener(null);
                    SplashScreen.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //textView.setText("Netzwerk Adresse erstellt");
                            progressBar.setProgress(100);
                            start();
                        }
                    });
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            serviceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        textView = (TextView) findViewById(R.id.splash_screen_progress_text);
        progressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.accentColor), PorterDuff.Mode.SRC_IN);
        this.getApplicationContext().startService(new Intent(this.getApplicationContext(), NetworkingService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Contact self = HelperFactory.getContacHelper(this).getSelf();
        if (self == null) {
            Intent intent = new Intent(this, NetworkingService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        } else {
            start();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(mConnection);
        }
    }

    private void start() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

}
