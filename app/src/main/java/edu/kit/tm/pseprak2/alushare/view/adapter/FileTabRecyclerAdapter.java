package edu.kit.tm.pseprak2.alushare.view.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.model.ASFile;
import edu.kit.tm.pseprak2.alushare.view.adapter.viewholder.FileTabRecyclerItemViewHolder;

/**
 * RecyclerAdapter for FileTab
 */
public class FileTabRecyclerAdapter extends TabAdapter<ASFile> {
    private Fragment context;
    private long id;
    private ImageManager imageManager;

    public FileTabRecyclerAdapter(Fragment context) {
        this.context = context;
        this.id = -1;
        this.imageManager = new ImageManager(context.getActivity());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context tContext = parent.getContext();
        View view = LayoutInflater.from(tContext).inflate(R.layout.recycler_item_file_tab, parent, false);
        return FileTabRecyclerItemViewHolder.newInstance(view, context.getActivity().getMenuInflater());
    }

    /**
     * Adds data to the ViewHolder. Sets the item path, the sender and the contact name
     *
     * @param viewHolder List item.
     * @param position   Position of the list item.
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        FileTabRecyclerItemViewHolder holder = (FileTabRecyclerItemViewHolder) viewHolder;

        ASFile file = getList().get(position);
        String itemText = file.getASName();
        String path = file.getPath();
        final long fileId = file.getId();
        holder.setItemPath(itemText);
        holder.setItemSender(path);

        //holder.setItemImage(R.drawable.ic_insert_drive_file_black_24dp);
        imageManager.setImageFile(holder.getItemImageFile(), file);

        holder.setItemListener(getItemListener());
        holder.setItemID(file.getId());
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                id = fileId;
                return false;
            }
        });
    }

    /**
     * Gibt ID des gedrückten Elements zurück
     *
     * @return ID
     */
    public long getId() {
        return this.id;
    }
}