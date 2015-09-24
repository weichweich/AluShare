package edu.kit.tm.pseprak2.alushare.view.adapter.viewholder;

import android.graphics.Bitmap;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.view.adapter.ChatItemClickListener;
import github.ankushsachdeva.emojicon.EmojiconTextView;

/**
 * Represent the ViewHolder of a data-object in a chat. Displays the information of the data-object.
 *
 * Created by arthur anselm on 27.06.15.
 */
public class ChatRecyclerItemViewHolder extends RecyclerView.ViewHolder
        implements View.OnLongClickListener, PopupMenu.OnMenuItemClickListener {

    private ChatItemClickListener itemClickListener;
    private View itemView;
    private PopupMenu popupMenu;
    private long fileID;
    private long dataID;

    private final LinearLayout mItemLayoutOutside;
    private final RelativeLayout mItemLayoutBubble;
    private final LinearLayout mItemLayoutContent;

    private final EmojiconTextView mItemTextMessage;
    private final TextView mItemTextDate;
    private final TextView mItemTextSender;
    private final ImageView mItemImageFile;
    private final ProgressBar mItemProgress;
    private final ImageView mItemSendingFailed;

    public ChatRecyclerItemViewHolder(View parent, EmojiconTextView itemTextMessage, ImageView itemImageFile,
                                      TextView itemTextDate, TextView itemTextSender, ImageView itemSendingFailed,
                                      RelativeLayout itemLayoutMessage, LinearLayout itemLayoutFrame,
                                      LinearLayout itemLayoutContent, ProgressBar itemProgress) {
        super(parent);
        mItemTextMessage = itemTextMessage;
        mItemImageFile = itemImageFile;
        mItemTextDate = itemTextDate;
        mItemTextSender = itemTextSender;
        mItemProgress = itemProgress;
        mItemSendingFailed = itemSendingFailed;
        mItemLayoutBubble = itemLayoutMessage;
        mItemLayoutOutside  = itemLayoutFrame;
        mItemLayoutContent = itemLayoutContent;

        this.itemView = parent;
        this.setListenerClicks();
        itemView.setOnLongClickListener(this);
        createPopUp(R.menu.menu_chat_bubble);
        itemClickListener = null;
        fileID = -1;
        dataID = -1;
    }

    /**
     * Creates a new viewHolder for a message and returns it.
     *
     * @param parent the parent view
     * @return  the new viewHolder
     */
    public static ChatRecyclerItemViewHolder newInstance(View parent, int maxTextViewSize) {
        EmojiconTextView itemTextMessage = (EmojiconTextView) parent.findViewById(R.id.textView_Message);
        itemTextMessage.setMaxWidth(maxTextViewSize);
        ImageView itemImageFile = (ImageView) parent.findViewById(R.id.imageView_File);
        TextView itemTextDate = (TextView) parent.findViewById(R.id.text_Date);
        TextView itemTextSender = (TextView) parent.findViewById(R.id.text_Sender);
        ProgressBar itemProgress = (ProgressBar) parent.findViewById(R.id.progressbar);
        ImageView itemSendingFailed = (ImageView) parent.findViewById(R.id.imageView_SendingFailed);

        RelativeLayout itemLayoutBubble = (RelativeLayout) parent.findViewById(R.id.layoutBubble);
        LinearLayout itemLayoutOutside = (LinearLayout) parent.findViewById(R.id.layoutOutside);
        LinearLayout itemLayoutContent = (LinearLayout) parent.findViewById(R.id.layoutContent);
        return new ChatRecyclerItemViewHolder(parent, itemTextMessage, itemImageFile, itemTextDate,
                itemTextSender, itemSendingFailed, itemLayoutBubble, itemLayoutOutside, itemLayoutContent , itemProgress);
    }

    /**
     * Sets the alignment depending on the message type. There are sent and received messages.
     * Displays the viewHolders as speech bubbles.
     *
     * @param itsMyMessage  true if sent message else false
     */
    public void setChatBubbleAlignment(boolean itsMyMessage){
       // LinearLayout.LayoutParams layoutBubblePar = (LinearLayout.LayoutParams) mItemLayoutBubble.getLayoutParams();
        LinearLayout.LayoutParams layoutBubblePar = (LinearLayout.LayoutParams) mItemLayoutContent.getLayoutParams();
        RelativeLayout.LayoutParams layoutOutsidePar = (RelativeLayout.LayoutParams) mItemLayoutOutside.getLayoutParams();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (itsMyMessage) {
            mItemLayoutBubble.setBackgroundResource(R.drawable.out_message_bg);
            layoutBubblePar.gravity = Gravity.END;
            layoutOutsidePar.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            layoutOutsidePar.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.gravity = Gravity.END;
            mItemTextDate.setLayoutParams(params);
        } else {
            mItemLayoutBubble.setBackgroundResource(R.drawable.in_message_bg);
            layoutBubblePar.gravity = Gravity.START;
            layoutOutsidePar.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            layoutOutsidePar.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.gravity = Gravity.START;
            mItemTextDate.setLayoutParams(params);
        }
    }

    /**
     * Sets the text of the ViewHolder and makes it visible if the text contains content.
     * Otherwise displays the textView.
     * @param text  the text message of the data-object
     */
    public void setItemTextMessage(CharSequence text){
        if(text == null || text == ""){
            mItemTextMessage.setText("");
            mItemTextMessage.setVisibility(View.GONE);
        }
        else {
            mItemTextMessage.setVisibility(View.VISIBLE);
            mItemTextMessage.setText(text);

        }
    }

    /**
     * Sets the sender TextView to the given sender if the sender is valid.
     * @param sender the sender of this message
     */
    public void setItemTextSender(CharSequence sender){
        if(sender == null || sender == "") {
            mItemTextSender.setText(null);
            mItemTextSender.setVisibility(View.GONE);
        }
        else {
            mItemTextSender.setVisibility(View.VISIBLE);
            mItemTextSender.setText(sender);
        }
    }

    /**
     * Sets the date of the viewHolder to the given date string.
     * @param date the date of this message
     */
    public void setItemTextDate(CharSequence date){
        mItemTextDate.setText(date);
    }

    /**
     * Sets the visibility of the itemNotSent imageView.
     * @param notSent true if the message wasn't sent else false
     */
    public void setItemSendingFailed(boolean notSent){
        if(notSent){
            mItemSendingFailed.setVisibility(View.VISIBLE);
        } else {
            mItemSendingFailed.setVisibility(View.GONE);
        }
    }

    /**
     * Sets the the file imageView of the holder to the given bitmap.
     * @param imageBitmap the bitmap of the imageView for the file
     */
    public void setItemImageFile(Bitmap imageBitmap) {
        mItemImageFile.setImageBitmap(imageBitmap);
    }

    /**
     * Return the file imageView of this holder
     * @return the imageView for the file of the message
     */
    public ImageView getItemImageFile(){
        return mItemImageFile;
    }

    /**
     * Sets the visibility of the progressbar
     * @param visibility the given visibility
     */
    public void setProgressbarVisible(int visibility){
            mItemProgress.setVisibility(visibility);
    }

    /**
     * Creates a popup menu for the holder
     */
    private void createPopUp(int menu) {
        try {
            popupMenu = new PopupMenu(itemView.getContext(), itemView);
            popupMenu.inflate(menu);
            popupMenu.setOnMenuItemClickListener(this);
        } catch (Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * Shows the popup menu
     * @param v the view that was long clicked
     * @return  true if successful
     */
    @Override
    public boolean onLongClick(View v) {
        popupMenu.show();
        return true;
    }

    /**
     * Sets the ListenerClicks of this holder. Delegates the different clicks to
     * the ChatItemClickListener of this ViewHolder.
     */
    public void setListenerClicks(){
        mItemLayoutBubble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(v, fileID);
                }
            }
        });

        mItemSendingFailed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.onReSendClick(v, dataID);
                }
            }
        });
    }

    /**
     * Delegates to the onPopUpClick in the ChatItemClickListener if the listener isn't null.
     * @param item  the item that got clicked
     * @return  true if successful
     */
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return itemClickListener != null && itemClickListener.onPopUpClick(item, dataID);
    }

    /**
     * Sets the listener of this ViewHolder
     * @param itemListener the chatItemClickListener
     */
    public void setItemListener(ChatItemClickListener itemListener) {
        this.itemClickListener = itemListener;
    }

    /**
     * Sets the data object id of this ViewHolder to the given id.
     * @param id the data id
     */
    public void setDataID(long id) {
        this.dataID = id;
    }

    /**
     * Sets the data object id of this ViewHolder to the given id.
     * @param id the file id
     */
    public void setFileID(long id) {
        this.fileID = id;
    }
}
