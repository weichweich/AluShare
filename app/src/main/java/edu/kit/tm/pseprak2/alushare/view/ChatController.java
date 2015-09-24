package edu.kit.tm.pseprak2.alushare.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.model.ASFile;
import github.ankushsachdeva.emojicon.EmojiconEditText;
import github.ankushsachdeva.emojicon.EmojiconGridView;
import github.ankushsachdeva.emojicon.EmojiconsPopup;
import github.ankushsachdeva.emojicon.emoji.Emojicon;

/**
 * A class to initializes the the controls of buttons and the editTextView  of a ChatActivity.
 * Created by arthur anselm on 24.08.15.
 */
public class ChatController {

    private ChatActivity chatActivity;
    private Context context;
    private AudioRecorder recorder;
    private InputMethodManager imm;
    private SharedPreferences preferences;
    private Vibrator v;

    private boolean emojiconsReady;
    private boolean editTextHasContent;
    private AtomicBoolean audio;

    private Button buttonSend;
    private EmojiconEditText mEmojiEditText;
    private EmojiconsPopup popupEmojiWindow;
    private ImageButton mButtonSmiley;
    private ImageButton mButtonCamera;
    private ImageButton mButtonMicro;
    private ImageButton mButtonPlay;
    private ImageButton mButtonStop;
    private ImageButton mButtonClear;
    private ImageButton mButtonDone;


    private static final int STANDARD_TIME = 250;
    private static final long RECORD_DELAY = 250l;


    public ChatController(ChatActivity chatActivity){
        this.chatActivity = chatActivity;
        this.context = chatActivity.getApplicationContext();
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        this.recorder = new AudioRecorder(this);
        this.imm = (InputMethodManager) chatActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        this.popupEmojiWindow = new EmojiconsPopup(chatActivity.getWindow().getDecorView().getRootView(), chatActivity);
        popupEmojiWindow.setSizeForSoftKeyboard();
        audio = new AtomicBoolean(false);

        initControls();
        initEmojiPopupWindow();
    }

    /**
     * Sets the controls of buttons & etc to the desired behaviour.
     */
    private void initControls() {
        mEmojiEditText = (EmojiconEditText) chatActivity.findViewById(R.id.editText_Message);
        buttonSend = (Button) chatActivity.findViewById(R.id.button_Send);
        mButtonCamera = (ImageButton) chatActivity.findViewById(R.id.imageButtonCamera);
        chatActivity.registerForContextMenu(mButtonCamera);
        mButtonSmiley = (ImageButton) chatActivity.findViewById(R.id.imageButtonSmiley);
        mButtonMicro = (ImageButton) chatActivity.findViewById(R.id.imageButtonMicro);
        mButtonPlay = (ImageButton) chatActivity.findViewById(R.id.imageButtonPlay);
        mButtonStop = (ImageButton) chatActivity.findViewById(R.id.imageButtonStop);
        mButtonClear = (ImageButton) chatActivity.findViewById(R.id.imageButtonClear);
        mButtonDone = (ImageButton) chatActivity.findViewById(R.id.imageButtonDone);
        //mSeekBarAudio = (SeekBar) findViewById(R.id.audio_seekbar);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mEmojiEditText.getText().toString();
                if (TextUtils.isEmpty(message)) {
                    return;
                }
                mEmojiEditText.setText("");
                chatActivity.showMessage(message);
            }
        });

        mButtonSmiley.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!popupEmojiWindow.isShowing()){
                    mEmojiEditText.requestFocus();
                    imm.showSoftInput(mEmojiEditText, InputMethodManager.SHOW_IMPLICIT);
                    if(emojiconsReady) {
                        popupEmojiWindow.showAtBottom();
                    } else {
                        popupEmojiWindow.setSizeForSoftKeyboard();
                        emojiconsReady = true;
                    }
                }
                else {
                    popupEmojiWindow.dismiss();
                }
            }
        });

        mButtonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.showContextMenu();
            }
        });

        mButtonMicro.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN && !recorder.isBusy()) {
                        if(!audio.getAndSet(true)) {
                            startRecording();
                            return true;
                        }
                    } else if (event.getAction() == MotionEvent.ACTION_UP && recorder.isBusy()) {
                        if(audio.get()) {
                            stopRecording();
                            new AudioSetter().execute();
                            return true;
                        }
                }
                return false;
            }
        });

        mButtonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recorder.onPlay(true);
                setPlayButtonVisible(false);
            }
        });

        mButtonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recorder.onPlay(false);
                setPlayButtonVisible(true);
            }
        });

        mButtonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recorder.onPlay(false);
                setAudioButtons(false);
                recorder.setBusy(false);
            }
        });

        mButtonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recorder.isBusy()) {
                    recorder.setBusy(false);
                    recorder.onPlay(false);
                    chatActivity.showMessage(new File(recorder.getFilePath()));
                    setAudioButtons(false);
                }
            }
        });

        mEmojiEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                editTextHasContent = s.length() != 0;
                if (!recorder.isBusy()) {
                    if (editTextHasContent) {
                        setSendButtonVisible(true);
                    } else {
                        setSendButtonVisible(false);
                    }
                }
            }
        });
    }

    /**
     * Sets the EmojiPopupWindows of the ChatActivity.
     */
    private void initEmojiPopupWindow(){
        emojiconsReady = false;
        popupEmojiWindow.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {
                mEmojiEditText.append(emojicon.getEmoji());
            }
        });

        popupEmojiWindow.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {
                KeyEvent event = new KeyEvent(
                        0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                mEmojiEditText.dispatchKeyEvent(event);
            }
        });

        popupEmojiWindow.setOnSoftKeyboardOpenCloseListener(new EmojiconsPopup.OnSoftKeyboardOpenCloseListener() {

            @Override
            public void onKeyboardOpen(int keyBoardHeight) {
                if (!emojiconsReady) {
                    emojiconsReady = true;
                }
            }

            @Override
            public void onKeyboardClose() {
                if (popupEmojiWindow.isShowing()) {
                    popupEmojiWindow.dismiss();
                }
            }
        });
    }

    private void startRecording(){
        Toast.makeText(context,
                context.getString(R.string.audio_record_hint), Toast.LENGTH_LONG).show();
        if (preferences.getBoolean("vibrate_mic_button", true)) {
            v.vibrate(STANDARD_TIME);
            sleep();
        }
        recorder.onRecord(true);
    }

    private void stopRecording(){
        try {
            recorder.onRecord(false);
        } catch (RuntimeException stopException) {
            recorder.setBusy(false);
        }
        if (recorder.isBusy()) {
            setAudioButtons(true);
        }
        if (preferences.getBoolean("vibrate_mic_button", true)) {
            v.vibrate(STANDARD_TIME);
        }
    }

    /**
     * Makes send buttons visible & media buttons invisible if visible is true else
     * send buttons invisible & media buttons visible.
     * @param visible visibility of the send button
     */
    private void setSendButtonVisible(boolean visible) {
        if (visible) {
            buttonSend.setVisibility(View.VISIBLE);
            mButtonCamera.setVisibility(View.GONE);
            mButtonMicro.setVisibility(View.GONE);
        } else {
            buttonSend.setVisibility(View.GONE);
            mButtonCamera.setVisibility(View.VISIBLE);
            mButtonMicro.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Makes audio buttons visible & standard buttons invisible if start is true else
     * audio buttons invisible & standard buttons visible.
     * @param start visibility of the send button
     */
    private void setAudioButtons(boolean start){
        if(start){
            //Standard-Buttons
            mButtonSmiley.setVisibility(View.GONE);
            mButtonCamera.setVisibility(View.GONE);
            mButtonMicro.setVisibility(View.GONE);
            //Audio-Buttons
            mButtonPlay.setVisibility(View.VISIBLE);
            mButtonDone.setVisibility(View.VISIBLE);
            mButtonClear.setVisibility(View.VISIBLE);
        } else {
            //Audio-Buttons
            mButtonPlay.setVisibility(View.GONE);
            mButtonStop.setVisibility(View.GONE);
            mButtonClear.setVisibility(View.GONE);
            mButtonDone.setVisibility(View.GONE);
            //Standard-Buttons
            mButtonSmiley.setVisibility(View.VISIBLE);
            setSendButtonVisible(editTextHasContent);
        }
    }

    /**
     * Makes the playButton visible and stopButton invisible if visible is true else
     * playButton invisible and stopButton visible.
     * @param visible the visibility of the play button
     */
    public void setPlayButtonVisible(boolean visible){
        if(visible){
            mButtonStop.setVisibility(View.GONE);
            mButtonPlay.setVisibility(View.VISIBLE);
        }else {
            mButtonPlay.setVisibility(View.GONE);
            mButtonStop.setVisibility(View.VISIBLE);
        }
    }

    /**
     * hide the keyboard if the keyboard visible.
     */
    /*private void hideKeyboard() {
        // Check if no view has focus:
        View view = chatActivity.getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }*/

    /**
     * Dismisses the EmojiPopupWindow. Enables Dismissing the EmojiPopupWindow out of this class.
     */
    public void dismissEmojiPopup(){
        if (popupEmojiWindow.isShowing()) {
            popupEmojiWindow.dismiss();
        }
    }

    /**
     * Lets the current thread sleep the STANDARD_TIME.
     */
    private void sleep(){
        try {
            Thread.sleep(STANDARD_TIME);
        } catch (InterruptedException i) {
            i.printStackTrace();
        }
    }

    public void setAudio(boolean flag){
        audio.set(flag);
    }

    private class AudioSetter extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(RECORD_DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ChatController.this.setAudio(false);
            return null;
        }
    }
}
