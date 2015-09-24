package edu.kit.tm.pseprak2.alushare.view;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.presenter.ChooseContactPresenter;
import edu.kit.tm.pseprak2.alushare.view.adapter.ChooseContactAdapter;
import edu.kit.tm.pseprak2.alushare.view.adapter.ContactTabRecyclerAdapter;
import edu.kit.tm.pseprak2.alushare.view.adapter.ItemClickListener;

public class ChooseContactActivity extends AppCompatActivity {
    private ChooseContactPresenter presenter;
    private ChooseContactAdapter adapter;
    private RecyclerView recyclerView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_contact);
        this.initToolbar();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChooseContactAdapter(this);
        recyclerView.setAdapter(adapter);
        presenter = new ChooseContactPresenter(this, adapter);
        setupRecyclerView(recyclerView);
    }



    public void add(View view) {
        List chosenContacts = presenter.getChoosenContacts();
        if (chosenContacts.size()>1) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(view.getContext(), R.style.AppThemeAlertDialogStyle);

            View dialogLayout = LayoutInflater.from(this).inflate(R.layout.alert_dialog,null);
            final EditText editText = (EditText) dialogLayout.findViewById(R.id.alter_dialog_edit);

            builder1.setView(dialogLayout);
            builder1.setMessage(R.string.groupmessage);
            builder1.setCancelable(true);

            builder1.setPositiveButton(R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String name = editText.getText().toString();
                            if (name != "")  {
                                String chatID = presenter.startChat(name);
                                Intent chat = new Intent(getApplicationContext(), ChatActivity.class);
                                chat.putExtra(getString(R.string.CHAT_ID), chatID);
                                startActivity(chat);
                            }


                        }
                    });
            builder1.setNegativeButton(R.string.back,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog dialog = builder1.create();
            dialog.show();
        }else{
            Snackbar.make(getCurrentFocus(),getString(R.string.message_not_enough_contacts),Snackbar.LENGTH_LONG).show();
        }

    }

    public void cancel(View view){
        this.finish();
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new Divider(this.getApplicationContext()));


    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Kontakte auswählen");
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
    }
}
