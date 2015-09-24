package edu.kit.tm.pseprak2.alushare.view.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.model.ASFile;
import edu.kit.tm.pseprak2.alushare.view.adapter.viewholder.ChatRecyclerItemViewHolder;

/**
 * A class to load thumbnails of files into imageViews / ChatRecyclerItemViewHolder.
 *
 * Created by arthur anselm on 31.08.15.
 */
public class ImageManager {

    private static final float BUBBLE_SIZE_RATIO = (float) 2/3;
    private static final float SMALL_THUMBNAIL_FACTOR = (float) 1/5;

    private static Context context;
    private Picasso picasso;
    private int small_thumbnail_size;
    private int icon_size;

    /**
     * The constructor of this class create a picasso object to have a picasso instance that
     * uses the VideoRequestHandler. Calculates the width and height of display and uses the
     * BUBBLE_WIDTH_RATIO and SMALL_THUMBNAIL_FACTOR to set standard sizes for thumbnails.
     * @param context the app context
     */
    public ImageManager(Context context) {
        ImageManager.context = context;
        picasso = new Picasso.Builder(context.getApplicationContext()).
                addRequestHandler(new VideoRequestHandler()).build();
    }

    /**
     * Returns a callback for a picasso operation. The callback hides the progressbar of the
     * ViewHolder after loading the thumbnail with picasso into the imageView of the holder.
     * If an error occurred while loading a file thumbnail into the imageView another placeHolder will
     * be load into the imageView. The placeholder indicated the error.
     * @param holder the holder that is involved in this operation
     * @return the new callback
     */
    private Callback getCallBack(final ChatRecyclerItemViewHolder holder){
        return new Callback() {
            @Override
            public void onSuccess() {
                holder.setProgressbarVisible(View.GONE);
            }
            @Override
            public void onError() {
                holder.setProgressbarVisible(View.GONE);
                Picasso.with(context).load(R.drawable.ic_clear_black_24dp).resize(small_thumbnail_size,
                        small_thumbnail_size).centerCrop().into(holder.getItemImageFile());

            }
        };
    }

    /**
     * Returns a callback for a picasso operation. The callback hides the progressbar of the
     * ViewHolder after loading the thumbnail with picasso into the imageView of the holder.
     * If an error occurred while loading a file thumbnail into the imageView another placeHolder will
     * be load into the imageView.
     * @param holder the holder that is involved in this operation
     * @return the new callback
     */
    private Callback getVideoAudioCallBack(final ChatRecyclerItemViewHolder holder){
        return new Callback() {
            @Override
            public void onSuccess() {
                holder.setProgressbarVisible(View.GONE);
            }
            @Override
            public void onError() {
                holder.setProgressbarVisible(View.GONE);
                Picasso.with(context).load(R.drawable.ic_mic_none_black_24dp).resize(small_thumbnail_size,
                        small_thumbnail_size).centerCrop().into(holder.getItemImageFile());
            }
        };

    }

    /**
     * Returns a callback for a picasso operation.
     * If an error occurred while loading a file thumbnail into the imageView another placeHolder will
     * be load into the imageView.
     * @param imageView the imageView
     * @return the new callback
     */
    private Callback getVideoAudioCallBack(final ImageView imageView){
        return new Callback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError() {
                Picasso.with(context).load(R.drawable.ic_mic_none_black_24dp).resize(icon_size,
                        icon_size).centerCrop().into(imageView);
            }
        };

    }

    /**
     * A method to load a thumbnail of the given ASFile into the given imageView.
     * The thumbnail will be generated depending on the mimeType of the ASFile.
     * @param imageView the imageView
     * @param asFile    the ASFile object and source of the thumbnail
     */
    public void setImageFile(ImageView imageView, ASFile asFile){
        if(asFile != null){
            Uri uri = Uri.fromFile(asFile);
            icon_size = 48;
            String type = ImageManager.getMimeType(asFile.getPath());
            if(type == null){
                type = "";
            } else {
                type = type.split("/")[0];
            }
            switch (type) {
                case "image":
                    Picasso.with(context).load(uri).resize(icon_size,
                            icon_size).centerCrop().into(imageView);
                    break;
                case "video":
                    picasso.load(VideoRequestHandler.video_Scheme + ":" + uri.getPath()).resize(icon_size,
                            icon_size).centerCrop().into(imageView, getVideoAudioCallBack(imageView));
                    break;
                case "audio":
                    imageView.setImageResource(R.drawable.ic_mic_none_black_24dp);
                    break;
                case "":
                    imageView.setImageResource(R.drawable.ic_clear_black_24dp);
                    break;
                default:
                    imageView.setImageResource(R.drawable.ic_insert_drive_file_black_24dp);
                    break;
            }
        }
    }

    /**
     * A method to load a thumbnail of the given ASFile into the file imageView of the given
     * chatRecyclerItemViewHolder. The thumbnail will be generated depending on the mimeType of the
     * ASFile.
     * @param holder the ChatRecyclerItemViewHolder
     * @param asFile    the ASFile object and source of the thumbnail
     */
    public void setImageViewWithCallback(ChatRecyclerItemViewHolder holder, ASFile asFile){
        if(asFile != null){
            Uri uri = Uri.fromFile(asFile);
            int thumbnail_size = getMaxContentSize();
            small_thumbnail_size = (int)((float) thumbnail_size * SMALL_THUMBNAIL_FACTOR);
            String type = getMimeType(asFile.getPath());
            type = type == null ? "" : type.split("/")[0];
            switch (type) {
                case "image":
                    Picasso.with(context).load(uri).resize(thumbnail_size,
                            thumbnail_size).centerCrop().into(holder.getItemImageFile(), getCallBack(holder));
                    break;
                case "video":
                    picasso.load(VideoRequestHandler.video_Scheme + ":" + uri.getPath()).resize(thumbnail_size,
                            thumbnail_size).centerCrop().into(holder.getItemImageFile(), getVideoAudioCallBack(holder));
                    break;
                case "audio":
                    Picasso.with(context).load(R.drawable.ic_mic_none_black_24dp).resize(small_thumbnail_size,
                            small_thumbnail_size).centerCrop().into(holder.getItemImageFile(), getCallBack(holder));
                    break;
                case "":
                    Picasso.with(context).load(R.drawable.ic_clear_black_24dp).resize(small_thumbnail_size,
                            small_thumbnail_size).centerCrop().into(holder.getItemImageFile(), getCallBack(holder));
                    break;
                default:
                    Picasso.with(context).load(R.drawable.ic_insert_drive_file_black_24dp).resize(small_thumbnail_size,
                            small_thumbnail_size).centerCrop().into(holder.getItemImageFile(), getCallBack(holder));
                    holder.setItemTextMessage(asFile.getASName());
                    break;
            }
        }
    }

    /**
     * Return the mimeType of the given filepath
     * @param url the filepath
     * @return the mimetype of the url
     */
    public static String getMimeType(String url) {
        String type;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension == null || extension.equals("")) {
            int dot = url.lastIndexOf(".");
            extension = url.substring(dot + 1);
        }
        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        return type;
    }

    /**
     * Returns the max size of message TextViews or ImageViews of speech bubbles.
     * @return the max size
     */
    public static int getMaxContentSize(){
        float displayWidth = (float) context.getResources().getDisplayMetrics().widthPixels;
        float displayHeight = (float) context.getResources().getDisplayMetrics().heightPixels;
        return (int) Math.min(BUBBLE_SIZE_RATIO * displayWidth, BUBBLE_SIZE_RATIO * displayHeight);
    }
}
