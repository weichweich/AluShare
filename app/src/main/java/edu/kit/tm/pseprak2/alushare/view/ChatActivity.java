package edu.kit.tm.pseprak2.alushare.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.Data;
import edu.kit.tm.pseprak2.alushare.presenter.ChatPresenter;
import edu.kit.tm.pseprak2.alushare.presenter.IntentGenerator;
import edu.kit.tm.pseprak2.alushare.view.adapter.ChatItemClickListener;
import edu.kit.tm.pseprak2.alushare.view.adapter.ChatRecyclerAdapter;

/**
 * Represents the View of a chat between sender and receivers
 * Controlls its RecyclerView with the ChatRecyclerAdapter
 * and makes Changes visible to the user.
 * Delegates result from intents like takePictureIntent (ChatDispather)
 * to the ChatPresenter.
 * Has a ChatController to initialize parts of the view and its behavior on events.
 *
 * Created by Arthur Anselm
 */
public class ChatActivity extends AppCompatActivity {

    private ChatRecyclerAdapter chatRecyclerAdapter;
    private static ChatPresenter chatPresenter;
    private RecyclerView recyclerView;
    private ChatDispatcher dispatcher;
    private ChatController chatController;
    private Toolbar toolbar;
    private boolean active;

    //private static AtomicBoolean delayedUpdate = new AtomicBoolean(false);
    //private static final long UPDATE_DELAY = 100l;


    /**
     * Creates the view and ChatController, initializes the ChatRecyclerAdapter,
     * ChatPresenter and ChatDispatcher.
     *
     * @param savedInstanceState saved instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Bundle bundle = this.getIntent().getExtras();
        String chatId = bundle.getString(getString(R.string.CHAT_ID));
        chatRecyclerAdapter = new ChatRecyclerAdapter(this);
        //needs a ChatRecyclerAdapter!
        setUpRecyclerView();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //needs a valid RecyclerView and a valid chatId and valid toolbar
        chatPresenter = new ChatPresenter(this, chatId);
        dispatcher = new ChatDispatcher(this);
        //uses chatPresenter
        setUpToolbar();
        chatController = new ChatController(this);
    }

    @Override
    protected void onStart(){
        super.onStart();
        this.active = true;
        updateDataSet();
    }

    @Override
    protected void onResume(){
        super.onResume();
        this.active = true;
        updateDataSet();
    }

    @Override
    protected void onPause(){
        super.onPause();
        this.active = false;

    }

    @Override
    protected void onStop(){
        super.onStop();
        this.active = false;
        chatController.dismissEmojiPopup();
    }

    /**
     * Create the options menu
     *
     * @param menu menu layout
     * @return true if successful
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement
        if(item.getItemId() == R.id.action_add){
            dispatcher.selectFileIntent();
            return true;
        }
        else if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, Preferences.class));
            return true;
        }
        else if (item.getItemId() == android.R.id.home) {
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Create the ContextMenu for the CameraButton Klick.
     * @param menu the context menu
     * @param v the view of the context menu
     * @param menuInfo the menuInfo of the context menu
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle(R.string.select_photo_video);
        menu.add(0, v.getId(), 0, R.string.take_photo);
        menu.add(0, v.getId(), 0, R.string.take_video);
        menu.add(0, v.getId(), 0, R.string.select_photo_video);
    }

    /**
     * Sets the behavior of an item click in the context menu.
     * @param menuItem  the menuItem that was clicked
     * @return  true if item click was valid
     */
    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        if (menuItem.getTitle().equals(getString(R.string.take_photo))) {
            dispatcher.takePictureIntent();
            return true;
        } else if (menuItem.getTitle().equals(getString(R.string.take_video))) {
            dispatcher.takeVideoIntent();
            return true;
        } else if (menuItem.getTitle().equals(getString(R.string.select_photo_video))) {
            dispatcher.selectPicVidIntent();
            return true;
        }
        return false;
    }

    /**
     * Initializes the toolbar of this ChatActivity.
     */
    private void setUpToolbar() {
        setTitle(chatPresenter.getTitle());
        toolbar.setTitleTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.white));
        try {
            getSupportActionBar().setHomeButtonEnabled(true);
        } catch (NullPointerException n){
           n.printStackTrace();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Contact> receivers = chatPresenter.getReceivers();
                if (receivers.size() == 1) {
                    long receiverId = receivers.get(0).getId();
                    Intent intent = IntentGenerator.getIntentByContactId(receiverId,
                            getApplicationContext());
                    try {
                        startActivityForResult(intent, 0);
                    } catch (Exception e) {
                        chatPresenter.updateContactNotFound(receiverId);
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.message_contact_not_found), Toast.LENGTH_LONG).show();
                    }
                }
                /*else {
                    //start activity oder view to show all receivers and link the clicked one
                }*/
            }
        });
    }

    /**
     * Initializes the RecyclerView for the ChatRecyclerAdapter
     * and sets the ItemListener of the RecyclerView.
     * (The ChatRecyclerAdapter gives each ViewHolder a ChatItemClickListener.)
     */
    private void setUpRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.chat_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager lManager = new LinearLayoutManager(this);
        lManager.setOrientation(LinearLayoutManager.VERTICAL);
        lManager.setReverseLayout(true);
        lManager.setSmoothScrollbarEnabled(true);
        recyclerView.setLayoutManager(lManager);
        recyclerView.setAdapter(chatRecyclerAdapter);
        recyclerView.smoothScrollToPosition(0);

        chatRecyclerAdapter.setItemListener(new ChatItemClickListener() {
            @Override
            public void onItemClick(View view, long fileId) {
                if (fileId != -1) {
                    Intent intent = IntentGenerator.getIntentByFileId(fileId, getApplicationContext());
                    if (intent != null) {
                        startActivity(Intent.createChooser(intent,
                                getString(R.string.chooser_file)));
                    }
                }
            }

            @Override
            public void onReSendClick(View view, long dataId) {
                chatPresenter.resendData(dataId);
            }

            @Override
            public boolean onPopUpClick(MenuItem item, long dataid) {

                return false;
            }
        });
    }

    /**
     * Scrolls in the RecyclerView to the first element if in idle state.
     */
    public void scrollDown() {
        if(recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
            recyclerView.smoothScrollToPosition(0);
        }
    }

    /**
     * After starting Intents in the ChatDispatcher this method will be called to
     * manage the result. If the intent was a "Select..Intent" the Uri of the selected Resource
     * will be fetched from the delivered intent "data". If the Intent was a "Take..Intent"
     * the Uri will be fetched from the ChatDispatcher. After that the uri will be delivered
     * to the ChatPresenter. The ChatPresenter loads the resource into the app storage.
     * The database will safe the file and its information to identify it later on.
     * Changes in the database will be recognized by the ChatPresenter.
     * The ChatPresenter will now invoke the update of the view.
     * @param requestCode   The requestCode to identify the intents
     * @param resultCode    The resultCode to check the validity of the result
     * @param data  The intent itself after completion of its purpose
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri uri;
            switch (requestCode) {
                case ChatDispatcher.REQUEST_SELECT_PHOTO_VIDEO:
                    uri = data.getData();
                    break;
                case ChatDispatcher.REQUEST_SELECT_FILE:
                    uri = data.getData();
                    break;
                case ChatDispatcher.REQUEST_IMAGE_CAPTURE:
                    uri = dispatcher.getFileUri();
                    //dispatcher.addFileToGallery();
                    break;
                case ChatDispatcher.REQUEST_VIDEO_CAPTURE:
                    uri = dispatcher.getFileUri();
                    //dispatcher.addFileToGallery();
                    break;
                default:
                    uri = null;
                    break;
            }
            showMessage(uri);
        }
    }

    /**
     * Delivers the paramters to the ChatPresenter to create a new Data object (Message).
     * @param message the message to send
     */
    public void showMessage(String message){
        chatPresenter.addData(message);
    }

    /**
     * Delivers the paramters to the ChatPresenter to create a new Data object (Message).
     * @param file  the file of the resource to send
     */
    public void showMessage(File file){
        chatPresenter.addData(file);
    }

    /**
     * Delivers the paramters to the ChatPresenter to create a new Data object (Message).
     * @param returnedUri  the uri of the resource to send
     */
    public void showMessage(Uri returnedUri){
        if(returnedUri != null) {
            chatPresenter.addData(returnedUri);
        } else {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.message_data_not_sent), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Initializes the DataList in the ChatRecyclerAdapter.
     * Latest data objects need to be the first objects in the data list of the
     * chatRecyclerAdapter.
     * @param dataList the dataList of a chat
     */
    public void showDataList(List<Data> dataList){
        chatRecyclerAdapter.setDataList(dataList);
    }

    /**
     * Updates the complete recyclerView.
     */
    public void updateDataSet(){
        if(this.active) {
            /*if (!delayedUpdate.get()) {
                updateDataSetLater();
            } else {}*/
                chatRecyclerAdapter.notifyDataSetChanged();

        }
    }

    /*private void updateDataSetLater() {
        if (!delayedUpdate.getAndSet(true)) {
            final long lastDataUpDateTime = System.currentTimeMillis();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (System.currentTimeMillis() - lastDataUpDateTime < UPDATE_DELAY) {
                        try {
                            Thread.sleep(100l);
                        } catch (InterruptedException ignored) {
                        }
                    }
                    ChatActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            chatRecyclerAdapter.notifyDataSetChanged();
                        }
                    });
                    delayedUpdate.getAndSet(false);
                }
            }).start();
        }
    }*/

    /**
     * Updates the first viewHolder in the chatRecyclerAdapter and scrolls down.
     */
    public void updateItemInserted(int position){
        if(this.active) {
            chatRecyclerAdapter.notifyItemInserted(position);
            scrollDown();
        }
    }

    /**
     * Updates the viewHolder in the chatRecyclerAdapter in the
     * position of the delivered parameter.
     */
    public void updateItemChanged(int position){
        if(this.active) {
            chatRecyclerAdapter.notifyItemChanged(position);
            //scrollDown();
        }
    }

    /**
     * Removes the viewHolder in the chatRecyclerAdapter in the
     * position of the delivered parameter
     * @param position the position of the remove data object
     */
    public void updateItemRemoved(int position){
        if(this.active) {
            chatRecyclerAdapter.notifyItemRemoved(position);
            //scrollDown();
        }
    }
}
