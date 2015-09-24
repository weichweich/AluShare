package edu.kit.tm.pseprak2.alushare.view.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.model.ASFile;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.Data;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.view.adapter.viewholder.ChatRecyclerItemViewHolder;

/**
 * A class to fill the ViewHolder of the RecyclerView with content und align it.
 * The ChatRecyclerAdapter gets a reference to a data list which will be modified during
 * lifecycle. The view holder in position i of the RecyclerView obtains its information
 * from the data object in mDataList in position i.
 *
 * Created by arthuranselm on 27.06.15.
 */
public class ChatRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<Data> mDataList;
    private ChatItemClickListener itemClickListener;
    private Activity context;
    private ImageManager imageManager;
    //private Calendar cToday;
    //private Calendar cVar;

    /**
     * The constructor of the ChatRecyclerAdapter gets the context of the application as a parameter
     * to use the Helper from model and have access to internal storage.
     * Creates an ImageManager to load Image into ImageViews.
     * @param context   the context fo the app
     */
    public ChatRecyclerAdapter(Activity context){
        this.context = context;
        imageManager = new ImageManager(context);
        //cToday = Calendar.getInstance();
        //cVar = Calendar.getInstance();
    }

    /**
     * The method that will be called to create a new ViewHolder when a data object was added to
     * dataList and the notification to update the RecyclerView was invoked.
     * @param parent    the parent of the RecyclerView
     * @param viewType  the viewType of RecyclerView
     * @return  a new ChatRecyclerItemViewHolder
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item_chat, parent, false);
        return ChatRecyclerItemViewHolder.newInstance(view, ImageManager.getMaxContentSize());
    }

    /**
     * The method that will be called to initializes or overwrite a ViewHolder. The view holder in
     * position i of the RecyclerView obtains its information from the data object in mDataList in
     * position i.Sets the alignment, background and content of the ChatRecyclerItemViewHolder.
     * Sets the status of received data objects to "read" if the data object/ message wasn't read.
     * @param viewHolder    the viewHolder that will be initialized or overwritten
     * @param position  the position of the viewHolder in the recyclerView and the data
     *                  object related to this viewHolder in the dataList.
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ChatRecyclerItemViewHolder holder = (ChatRecyclerItemViewHolder) viewHolder;
        Data data = mDataList.get(position);
        holder.setDataID(data.getId());
        holder.setItemTextDate(data.getTimestampString(context));
        boolean itsMyMessage = !data.received();
        holder.setChatBubbleAlignment(itsMyMessage);
        holder.setItemSendingFailed(data.needsResend());
        if(itsMyMessage){
            holder.setItemTextSender(null);
        } else {
            holder.setItemTextSender(data.getSender().getName(context));
            setDataRead(data);
        }
        holder.setItemTextMessage(data.getText());
        setImageFile(holder, data.getFile());
        holder.setItemListener(itemClickListener);
    }

    /**
     * Returns the size of the dataList of this ChatRecyclerAdapter
     * @return the size of the dataList of this ChatRecyclerAdapter
     */
    @Override
    public int getItemCount() {
        return (mDataList == null) ? 0 : mDataList.size();
    }

    /**
     * Initializes the dataList of this ChatRecyclerAdapter with the delivered dataList reference
     * @param dataList the dataList
     */
    public void setDataList(List<Data> dataList){
        mDataList = dataList;
    }

    /**
     * Loads the right image depending on asFile into the imageView of the delivered ViewHolder.
     * @param holder    the holder whose imageView must be set
     * @param asFile    the ASFile object of the data object related to the delivered ViewHolder.
     */
    private void setImageFile(ChatRecyclerItemViewHolder holder, ASFile asFile){
        if(asFile == null){
            holder.setItemImageFile(null);
            holder.setProgressbarVisible(View.GONE);
            holder.setFileID(-1);
        } else {
            holder.setProgressbarVisible(View.VISIBLE);
            imageManager.setImageViewWithCallback(holder, asFile);
            holder.setFileID(asFile.getId());
        }
    }

    /**
     * Sets the given data object to the status read if not already read. Updates the database.
     * @param data the data object that needs to have the status read
     */
    private void setDataRead(final Data data){
        Contact self = HelperFactory.getContacHelper(this.context).getSelf();
        if (!data.getState(self).wasRead()) {
            data.getState(self).setWasRead();
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    HelperFactory.getDataHelper(context).update(data);
                }
            });
        }
    }

    /**
     * Sets the Listener for click events on list items.
     *
     * @param listener Click Listener
     */
    public void setItemListener(ChatItemClickListener listener) {
        this.itemClickListener = listener;
    }

    /**
     * Sets the time of the message/ data-object.
     * Displays also the date if the message wasn't sent the same day.
     * @param timeStamp
     * @return
     */
    /*public String getTime(Timestamp timeStamp){
        cVar.setTime(new Date(timeStamp.getTime()));
        cToday.setTime(new Date(System.currentTimeMillis()));
        boolean sameDay = cToday.get(Calendar.YEAR) == cVar.get(Calendar.YEAR) &&
                cToday.get(Calendar.DAY_OF_YEAR) == cVar.get(Calendar.DAY_OF_YEAR);
        if(sameDay){
            return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(timeStamp);
        } else {
            return new SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(timeStamp);
        }
    }*/
}
