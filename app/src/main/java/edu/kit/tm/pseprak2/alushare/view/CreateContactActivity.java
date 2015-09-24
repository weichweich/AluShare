package edu.kit.tm.pseprak2.alushare.view;


import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;

import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.presenter.CreateContactPresenter;
import edu.kit.tm.pseprak2.alushare.presenter.IntentIntegrator;
import edu.kit.tm.pseprak2.alushare.presenter.IntentResult;

public class CreateContactActivity extends AppCompatActivity {
    private static CreateContactPresenter presenter;
    private String networkAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFinishOnTouchOutside(false);
        setContentView(R.layout.activity_create_contact);
        presenter = new CreateContactPresenter();
        presenter.onTakeView(this);
    }

    public void buildDialog() {
        String[] items = {getString(R.string.to_existing_contact), getString(R.string.new_contact)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

    public void add(View view) {
        String address = ((EditText) findViewById(R.id.editTorID)).getText().toString();
        if (address.equals("")) {
            Snackbar.make(this.getCurrentFocus(), getString(R.string.message_no_networkadress), Snackbar.LENGTH_SHORT).show();
        } else {
            if (address.equals(HelperFactory.getContacHelper(this).getSelf().getNetworkingId())) {
                Snackbar.make(this.getCurrentFocus(), getString(R.string.message_own_networkaddress), Snackbar.LENGTH_SHORT).show();
            } else {
                this.networkAddress = address;
                this.buildDialog();
            }
        }

    }

    public void cancel(View view){
        this.finish();
    }

    public void connectToContact() {
        Intent intentChoose = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intentChoose, 2);
    }

    public void createContact() {
        Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
        intent.putExtra("finishActivityOnSaveCompleted",true);
        startActivityForResult(intent, 2);
    }


    public void scanQrCode(View view) {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();

    }


    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        switch (requestCode) {
            case (0x0000c0de):

                IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
                if (scanResult != null) {
                    String content = scanResult.getContents();
                    if (content != null) {
                        ((EditText) findViewById(R.id.editTorID)).setText(content);
                    }
                }
                break;


            case (2):
                if (resultCode == Activity.RESULT_OK) {

                    Uri contactData = intent.getData();
                    ContactManagement contactManagement = new ContactManagement(this, contactData, this.networkAddress);
                    if (contactManagement.contact()) {
                        this.finish();

                    } else {
                        Snackbar.make(this.getCurrentFocus(), getString(R.string.message_contact_in_database), Snackbar.LENGTH_SHORT).show();
                    }


                    break;
                }
            default:
                break;
        }
    }
}
