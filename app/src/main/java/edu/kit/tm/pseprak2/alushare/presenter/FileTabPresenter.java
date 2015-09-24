package edu.kit.tm.pseprak2.alushare.presenter;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.kit.tm.pseprak2.alushare.model.ASFile;
import edu.kit.tm.pseprak2.alushare.model.Data;
import edu.kit.tm.pseprak2.alushare.model.helper.ASFileHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.AluObserver;
import edu.kit.tm.pseprak2.alushare.model.helper.DataHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.view.adapter.FileTabRecyclerAdapter;
import edu.kit.tm.pseprak2.alushare.view.fragments.FileTabFragment;

/**
 * @author Niklas Sänger
 *         Presenter for the FileTab Fragment
 */
public class FileTabPresenter implements AluObserver<ASFile> {
    private FileTabFragment view;
    private FileTabRecyclerAdapter adapter;
    private ASFileHelper filehelper;
    private DataHelper datahelper;
    private int current_state;

    /**
     * Constructor. Initializies SQLContext
     */
    public FileTabPresenter(FileTabFragment view, FileTabRecyclerAdapter adapter) {
        if (view == null) {
            throw new IllegalArgumentException();
        }
        if (adapter == null) {
            throw new IllegalArgumentException();
        }

        this.view = view;
        this.adapter = adapter;

        filehelper = HelperFactory.getFileHelper(view.getActivity().getApplicationContext());
        datahelper = HelperFactory.getDataHelper(view.getActivity().getApplicationContext());
        filehelper.addObserver(this);

        current_state = -1;
    }

    public void showFiles(int n) {
        switch (n) {
            case 0: //All
                setAll();
                break;
            case 1: //Send
                setSent();
                break;
            case 2: // Received
                setReceived();
                break;
            default:
                setAll();
                break;
        }
    }

    /**
     * Return filterd list of files
     *
     * @param query Parameter to filter the list.
     * @return filtered list.
     */
    public void showFileList(String query) {
        switch (current_state) {
            case 0: //All
                setAll();
                break;
            case 1: //Send
                adapter.updateDataSet(filehelper.getSendFilesByName(query));
                break;
            case 2: // Received
                adapter.updateDataSet(filehelper.getReceivedFilesByName(query));
                break;
            default:
                adapter.updateDataSet(filehelper.getFilesByName(query));
                break;
        }

    }

    public void removeFile(long id) {
        try {
            ASFile file = filehelper.getFileByID(id);
            Data data = datahelper.getDataByID(file.getDataId());

            filehelper.delete(file); // Es existiert ein Text, es muss nur die File gelöscht werden.
            datahelper.delete(data);
        } catch (Exception e) {
            Log.e("Error", e.toString());
        }
    }

    /**
     * Datei umbennen
     * @param id File ID
     * @param name Neuer Name
     */
    public void renameFile(long id, String name) {
        ASFile file = filehelper.getFileByID(id);
        file.setASName(name);
        filehelper.update(file);
    }

    @Override
    public void inserted(ASFile data) {
        update(data);
    }

    @Override
    public void updated(ASFile data) {
        update(data);
    }

    @Override
    public void removed(ASFile data) {
        update(data);
    }

    private void update(ASFile data) {
        if (view != null && view.getActivity() != null) {
            view.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showFiles(current_state);
                }
            });
        }
    }

    private void setReceived() {
        adapter.updateDataSet(filehelper.getReceivedFiles());
    }

    private void setSent() {
        adapter.updateDataSet(filehelper.getSendFiles());
    }

    private void setAll() {
        adapter.updateDataSet(filehelper.getFiles());
    }
}
