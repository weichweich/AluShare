package edu.kit.tm.pseprak2.alushare.view.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
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

import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.presenter.ContactTabPresenter;
import edu.kit.tm.pseprak2.alushare.presenter.IntentGenerator;
import edu.kit.tm.pseprak2.alushare.view.ChatActivity;
import edu.kit.tm.pseprak2.alushare.view.ContactManagement;
import edu.kit.tm.pseprak2.alushare.view.CreateContactActivity;
import edu.kit.tm.pseprak2.alushare.view.Divider;
import edu.kit.tm.pseprak2.alushare.view.adapter.ContactTabRecyclerAdapter;
import edu.kit.tm.pseprak2.alushare.view.adapter.ItemClickListener;

/**
 * CcontactTab fragment shows list of contacts.
 * TODO: Add Button implementieren
 */
public class ContactTabFragment extends Fragment implements SearchView.OnQueryTextListener {
    private static String TAG = "ContactTabFragment";
    private ContactTabPresenter presenter;
    private RecyclerView recyclerView;
    private ContactTabRecyclerAdapter contactTabRecyclerAdapter;
    private LinearLayoutManager mLayoutManager;
    private long id = -1;

    /**
     * Creates an instance of ContactTabFragment
     *
     * @return instance of ContactTabFragment
     */
    public static ContactTabFragment createInstance() {
        return new ContactTabFragment();
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
                R.layout.fragment_contact_tab, container, false);

        contactTabRecyclerAdapter = new ContactTabRecyclerAdapter(this);
        presenter = new ContactTabPresenter(this, contactTabRecyclerAdapter);

        mLayoutManager = new LinearLayoutManager(this.getActivity());

        setupRecyclerView(recyclerView);
        return recyclerView;
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
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            Intent i = new Intent(getActivity(), CreateContactActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Creates the options menu and the searchaction.
     *
     * @param menu     Menu layout.
     * @param inflater MenuInflater.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_tab_contact, menu);
        try {
            final MenuItem item = menu.findItem(R.id.action_search);
            final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
            searchView.setOnQueryTextListener(this);
        } catch (NullPointerException e) {
            Log.e("ContactTabFragment", e.toString());
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        contactTabRecyclerAdapter.animateTo(presenter.getContactList(newText));
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

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        id = contactTabRecyclerAdapter.getId();
        if (item.getGroupId() == R.id.context_tab_contact_group) {
            Log.d(TAG, "" + item.toString() + " " + contactTabRecyclerAdapter.getId());
            if (item.getItemId() == R.id.context_tab_show_in_contacts) {
                try {
                    Intent intent = IntentGenerator.getIntentByContactId(id, this.getActivity().getApplication().getApplicationContext());
                    startActivityForResult(intent, 0);
                    return true;
                } catch (Exception e) {
                    presenter.updateContactNotFound(id);
                    Snackbar.make(this.getView(), getString(R.string.message_contact_not_found), Snackbar.LENGTH_LONG).show();
                    return true;
                }
            } else if (item.getItemId() == R.id.context_tab_contact_delete) {
                if (presenter.removeContact(id)) {
                    Snackbar.make(this.getView(), getString(R.string.message_contact_deleted), Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(this.getView(), getString(R.string.message_contact_in_chat), Snackbar.LENGTH_LONG).show();
                }
                return true;
            } else if (item.getItemId() == R.id.context_tab_contact_link_to_contact) {
                buildDialog();
                return true;
            }
        }
        return super.onContextItemSelected(item);
    }

    /**
     * ActivityResult
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case 2:
                if (resultCode == ChatActivity.RESULT_OK) {
                    Uri contactData = intent.getData();
                    ContactManagement contactManagement = new ContactManagement(this.getActivity().getApplicationContext(), contactData, id);
                    if(!contactManagement.contact()){
                        Snackbar.make(this.getView(), getString(R.string.message_contact_in_database), Snackbar.LENGTH_LONG).show();
                    }
                }
                break;
            default:
                break;
        }
    }

    //erzeugt RecylcerView
    private void setupRecyclerView(final RecyclerView recyclerView) {
        contactTabRecyclerAdapter.updateDataSet(presenter.getContactList());

        recyclerView.setAdapter(contactTabRecyclerAdapter);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new Divider(this.getActivity().getApplicationContext()));
        contactTabRecyclerAdapter.setItemListener(new ItemClickListener() {
            @Override
            public void onItemClick(View view, long id) {

                String chatID = presenter.getChatID(id);

                if (chatID == null) {
                    chatID = presenter.startChat(id);
                }
                Intent chat = new Intent(getActivity(), ChatActivity.class);
                chat.putExtra(getString(R.string.CHAT_ID), chatID);
                getActivity().startActivity(chat);
            }
        });

    }

    // Erzeugt Dialog für neuen Kontakt
    private void buildDialog() {
        String[] items = {getString(R.string.to_existing_contact), getString(R.string.new_contact)};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppThemeAlertDialogStyle);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int index) {
                switch (index) {
                    case (0): {
                        connectToContact();
                        break;
                    }
                    case (1): {
                        createContact();
                        break;
                    }
                    default:
                        break;
                }
            }
        });
        builder.show();
    }
    // Veknüpft zu Kontakt
    private void connectToContact() {
        Intent intentChoose = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intentChoose, 2);
    }

    // Erstellt neuen Kontakt
    private void createContact() {
        Intent intentCreate = new Intent(Intent.ACTION_INSERT);
        intentCreate.setType(ContactsContract.Contacts.CONTENT_TYPE);
        intentCreate.putExtra("finishActivityOnSaveCompleted", true);
        startActivityForResult(intentCreate, 2);
    }

}
