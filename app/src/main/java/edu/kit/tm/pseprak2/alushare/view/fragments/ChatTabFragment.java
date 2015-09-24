package edu.kit.tm.pseprak2.alushare.view.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
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
import edu.kit.tm.pseprak2.alushare.presenter.ChatTabPresenter;
import edu.kit.tm.pseprak2.alushare.view.ChatActivity;
import edu.kit.tm.pseprak2.alushare.view.ChooseContactActivity;
import edu.kit.tm.pseprak2.alushare.view.Divider;
import edu.kit.tm.pseprak2.alushare.view.adapter.ChatTabRecyclerAdapter;
import edu.kit.tm.pseprak2.alushare.view.adapter.ItemClickListener;

/**
 * ChatTab fragment shows list of chats.
 */
public class ChatTabFragment extends Fragment implements SearchView.OnQueryTextListener {
    private static ChatTabPresenter presenter;
    private static String TAG = "ChatTabFragment";
    private ChatTabRecyclerAdapter chatTabRecyclerAdapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager mLayoutManager;


    /**
     * Creates an instance of ChatTabFragment
     *
     * @return instance of ChatTabFragment
     */
    public static ChatTabFragment createInstance() {
        return new ChatTabFragment();
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
     * Creates the view and initializes the presenter.
     *
     * @param inflater           Inflater
     * @param container          Container
     * @param savedInstanceState Saved Instance
     * @return Returns the new view.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(
                R.layout.fragment_chat_tab, container, false);

        chatTabRecyclerAdapter = new ChatTabRecyclerAdapter(this);
        presenter = new ChatTabPresenter(this, chatTabRecyclerAdapter);
        mLayoutManager = new LinearLayoutManager(this.getActivity());

        setupRecyclerView(recyclerView);

        registerForContextMenu(recyclerView);
        return recyclerView;
    }

    /**
     * Creates the options menu and the searchaction.
     *
     * @param menu     Menu layout.
     * @param inflater MenuInflater.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_tab_chat, menu);
        try {
            final MenuItem item = menu.findItem(R.id.action_search);
            final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
            searchView.setOnQueryTextListener(this);
        } catch (NullPointerException e) {
            Log.e("ChatTabFragment", e.toString());
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            Intent groupChat = new Intent(getActivity(), ChooseContactActivity.class);
            groupChat.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(groupChat);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        chatTabRecyclerAdapter.animateTo(presenter.getChatList(newText));
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
        Log.d(TAG, item.toString() + " " + chatTabRecyclerAdapter.getId());
        String id = chatTabRecyclerAdapter.getId();
        if (item.getGroupId() == R.id.context_tab_chat_group) {
            if (item.getItemId() == R.id.context_tab_chat_rename) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity(), R.style.AppThemeAlertDialogStyle);

                View dialogLayout = LayoutInflater.from(this.getActivity()).inflate(R.layout.alert_dialog, null);
                final EditText editText = (EditText) dialogLayout.findViewById(R.id.alter_dialog_edit);

                builder.setView(dialogLayout);
                builder.setMessage(R.string.groupmessage);
                builder.setCancelable(true);

                builder.setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String input = editText.getText().toString();
                                if (!input.isEmpty()) {
                                    presenter.renameChat(chatTabRecyclerAdapter.getId(), input);
                                    Snackbar.make(getView(), getString(R.string.message_chat_renamed), Snackbar.LENGTH_LONG).show();
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


            } else if (item.getItemId() == R.id.context_tab_chat_delete) {
                Log.d(TAG, id);
                presenter.removeChat(id);
                Snackbar.make(getView(), getString(R.string.message_chat_deleted), Snackbar.LENGTH_LONG).show();
            }
        }
        return false;
    }

    //Initialisiert RecyclerView
    private void setupRecyclerView(RecyclerView recyclerView) {
        chatTabRecyclerAdapter.updateDataSet(presenter.getChatList());

        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(chatTabRecyclerAdapter);
        recyclerView.addItemDecoration(new Divider(this.getActivity().getApplicationContext()));

        chatTabRecyclerAdapter.setItemListener(new ItemClickListener() {
            @Override
            public void onItemClick(View view, long id) {
                Intent chat = new Intent(getActivity(), ChatActivity.class);
                chat.putExtra(getString(R.string.CHAT_ID), chatTabRecyclerAdapter.getChatIdentByPos(id));
                getActivity().startActivity(chat);
            }
        });
    }
}
