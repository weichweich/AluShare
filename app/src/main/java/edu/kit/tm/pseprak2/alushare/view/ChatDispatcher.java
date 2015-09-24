package edu.kit.tm.pseprak2.alushare.view;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import edu.kit.tm.pseprak2.alushare.R;


/**
 * A class that starts Intents for data input of the chatActivity.
 *
 * Created by Arthur Anselm on 16.08.15.
 */
public class ChatDispatcher {

    private Activity chatActivity;
    private PackageManager packageManager;
    //private ContentResolver contentResolver;

    private Uri fileUri;
    private boolean hasCamera;
    private boolean dispatchLocked[];

    public static final int REQUEST_SELECT_PHOTO_VIDEO = 1;
    public static final int REQUEST_SELECT_FILE = 2;
    public static final int REQUEST_IMAGE_CAPTURE = 3;
    public static final int REQUEST_VIDEO_CAPTURE = 4;
    private static final int NUMBER_OF_DISPATCHES = 5;

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /**
     * The constructor of the ChatDispatcher. Initializes its attributes.
     * @param chatActivity the chatActivity of this chatDispatcher
     */
    public ChatDispatcher(Activity chatActivity){
        this.chatActivity = chatActivity;
        //this.contentResolver = chatActivity.getContentResolver();
        this.packageManager = chatActivity.getPackageManager();
        hasCamera = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA);
        this.dispatchLocked = new boolean[NUMBER_OF_DISPATCHES];
        for(int j = 0; j < NUMBER_OF_DISPATCHES; j++){
            dispatchLocked[j] = false;
        }
    }

    /**
     * If the dispatch with the delivered requestcode is locked returns true else false.
     * @param requestCode the reqeustcode of an intent
     * @return  true if the intent is locked else false
     */
    private boolean dispatchIsLocked(int requestCode){
        if(!dispatchLocked[requestCode]){
            dispatchLocked[requestCode] = true;
            return false;
        }
        return true;
    }

    /**
     * Invokes the method selectIntent with the specific parameters for this intent.
     */
    public void selectPicVidIntent(){
        selectIntent(REQUEST_SELECT_PHOTO_VIDEO, "image/*,video/*", chatActivity.getString(R.string.select_photo_video));
    }

    /**
     * Invokes the method selectIntent with the specific parameters for this intent.
     */
    public void selectFileIntent() {
        selectIntent(REQUEST_SELECT_FILE, "*/*", chatActivity.getString(R.string.select_file));
    }

    /**
     * Start a selectIntent with the given parameters and the chatActivity object.
     */
    private void selectIntent(final int REQUEST_DATA, String mimeType, String userText){
        if(!dispatchIsLocked(REQUEST_DATA)) {
            Intent selectIntent = new Intent(Intent.ACTION_GET_CONTENT);
            selectIntent.setType(mimeType);
            if (selectIntent.resolveActivity(packageManager) != null) {
                chatActivity.startActivityForResult(Intent.createChooser(selectIntent, userText)
                        , REQUEST_DATA);
            }
        }
        dispatchLocked[REQUEST_DATA] = false;
    }

    /**
     * Start a take picture intent with the chatActivity object.
     */
    public void takePictureIntent(){
        if(hasCamera && !dispatchIsLocked(REQUEST_IMAGE_CAPTURE)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
                chatActivity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
        dispatchLocked[REQUEST_IMAGE_CAPTURE] = false;
    }

    /**
     * Start a take video intent with the chatActivity object.
     */
    public void takeVideoIntent(){
        if(hasCamera && !dispatchIsLocked(REQUEST_VIDEO_CAPTURE)) {
            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            if (takeVideoIntent.resolveActivity(packageManager) != null) {
                fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);  // create a file to save the video
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);  // set the image file name
                takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1); // set the video image quality to high
                chatActivity.startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
            }
        }
        dispatchLocked[REQUEST_VIDEO_CAPTURE] = false;
    }

    /**
     * Adds the file stored in the fileUri to the Gallery
     */
    /*public void addFileToGallery() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        String mimeType = ImageManager.getMimeType(fileUri.getPath());
        values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
        values.put(MediaStore.MediaColumns.DATA, fileUri.getPath());
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }*/

    /**
     * Create a file Uri for saving an image or video
     */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "AluShare");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("AluShare", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    /**
     * Return the fileUri.
     * @return the fileUri
     */
    public Uri getFileUri(){
        return fileUri;
    }
}
