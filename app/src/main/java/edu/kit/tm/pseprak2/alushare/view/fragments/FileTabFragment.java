package edu.kit.tm.pseprak2.alushare.view.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.internal.view.menu.ActionMenuItemView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.presenter.FileTabPresenter;
import edu.kit.tm.pseprak2.alushare.presenter.IntentGenerator;
import edu.kit.tm.pseprak2.alushare.view.Divider;
import edu.kit.tm.pseprak2.alushare.view.adapter.FileTabRecyclerAdapter;
import edu.kit.tm.pseprak2.alushare.view.adapter.ItemClickListener;

/**
 * FileTab fragment shows list of files.
 */
public class FileTabFragment extends Fragment implements SearchView.OnQueryTextListener {
    private static String TAG = "FileTabFragment";
    private FileTabPresenter presenter;
    private FileTabRecyclerAdapter fileTabRecyclerAdapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager mLayoutManager;

    /**
     * Creates an instance of FileTabFragment.
     *
     * @return Instance of FileTabFragment.
     */
    public static FileTabFragment createInstance() {
        return new FileTabFragment();
    }

    /**
     * Creates the view and initializes the presenter.
     *
     * @param inflater           Inflater
     * @param container          Container
     * @param savedInstanceState Saved Instance
     * @return Returns the new view.
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(
                R.layout.fragment_file_tab, container, false);
        mLayoutManager = new LinearLayoutManager(this.getActivity());


        fileTabRecyclerAdapter = new FileTabRecyclerAdapter(this);
        presenter = new FileTabPresenter(this, fileTabRecyclerAdapter);

        setupRecyclerView(recyclerView);
        registerForContextMenu(recyclerView);

        return recyclerView;
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        int pos = p.getInt("fileTabShow", 0);

        presenter.showFiles(pos);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /**
     * Creates the options menu and the searchaction.
     *
     * @param menu     Menu layout.
     * @param inflater MenuInflater.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_tab_file, menu);
        try {// ActionView kann nicht erstellt werden da
            //Robolectrc einen Bug hat bezüglich MenuCompat.
            //nur fürs Testen
            final MenuItem itemSearch = menu.findItem(R.id.action_search);
            final SearchView searchView = (SearchView) MenuItemCompat.getActionView(itemSearch);
            searchView.setOnQueryTextListener(this);
        } catch (NullPointerException e) {
            Log.e("FileTabFragment", e.toString());
        }


        final MenuItem m = menu.findItem(R.id.filter);
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        int pos = p.getInt("fileTabShow", 0);
        m.setIcon(getIcon(pos));

        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ActionMenuItemView m = (ActionMenuItemView) getActivity().findViewById(R.id.filter);
        Drawable d;
        if (item.getGroupId() == R.id.group_tab_file_sort) {
            SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            SharedPreferences.Editor e = p.edit();
            switch (item.getItemId()) {
                case R.id.filter_send:
                    e.putInt("fileTabShow", 1).commit();
                    presenter.showFiles(1);
                    d = getIcon(1);
                    break;
                case R.id.filter_received:
                    e.putInt("fileTabShow", 2).commit();
                    presenter.showFiles(2);
                    d = getIcon(2);
                    break;
                default:
                    e.putInt("fileTabShow", 0).commit();
                    presenter.showFiles(0);
                    d = getIcon(0);
                    break;
            }
            m.setIcon(d);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        presenter.showFileList(newText);
        recyclerView.scrollToPosition(0);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }


    /**
     * Context Item der Items
     *
     * @param item MenuItem auf das im Contextmenü geklickt wurde.
     * @return
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getGroupId() == R.id.context_tab_file_group) {
            long id = fileTabRecyclerAdapter.getId();

            Log.d(TAG, "" + item.toString() + " " + id);

            if (item.getItemId() == R.id.context_tab_file_delete) {
                presenter.removeFile(id);
                Snackbar.make(this.getView(), getString(R.string.message_file_deleted), Snackbar.LENGTH_LONG).show();
                return true;
            } else if (item.getItemId() == R.id.context_tab_file_rename) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity(), R.style.AppThemeAlertDialogStyle);

                View dialogLayout = LayoutInflater.from(this.getActivity()).inflate(R.layout.alert_dialog, null);
                final EditText editText = (EditText) dialogLayout.findViewById(R.id.alter_dialog_edit);

                builder.setView(dialogLayout);
                builder.setMessage(R.string.message_rename_file);
                builder.setCancelable(true);

                builder.setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String input = editText.getText().toString();
                                if (!input.isEmpty()) {
                                    presenter.renameFile(fileTabRecyclerAdapter.getId(), input);
                                    Snackbar.make(getView(), getString(R.string.message_file_renamed), Snackbar.LENGTH_LONG).show();
                                }
                            }
                        });
                builder.setNegativeButton(R.string.back,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            }
        }
        return super.onContextItemSelected(item);
    }

    // Verändert
    private Drawable getIcon(int pos) {
        Drawable drawable;
        switch (pos) {
            case 1:
                drawable = ContextCompat.getDrawable(this.getActivity(), R.drawable.ic_sort_send_white_24dp);
                break;
            case 2:
                drawable = ContextCompat.getDrawable(this.getActivity(), R.drawable.ic_sort_received_white_24dp);
                break;
            default:
                drawable = ContextCompat.getDrawable(this.getActivity(), R.drawable.ic_sort_white_24dp);
                break;
        }
        return drawable;
    }

    // Erzeugt RecylcerView
    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setAdapter(fileTabRecyclerAdapter);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new Divider(this.getActivity().getApplicationContext()));

        fileTabRecyclerAdapter.setItemListener(new ItemClickListener() {
            @Override
            public void onItemClick(View view, long id) {
                Intent intent = IntentGenerator.getIntentByFileId(id, getContext());
                startActivity(Intent.createChooser(intent, getString(R.string.chooser_file)));
            }
        });
    }

}
