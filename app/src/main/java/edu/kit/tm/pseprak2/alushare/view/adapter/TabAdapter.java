package edu.kit.tm.pseprak2.alushare.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;


/**
 * Abstract class for the fragemnts
 * Created by niklas on 28.06.15.
 */
public abstract class TabAdapter<Item> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Item> mList;
    private ItemClickListener listener;

    /**
     * Initializes Adapter
     */
    public TabAdapter() {
        mList = null;
        listener = null;
    }

    /**
     * Creates Viewholder
     *
     * @param parent Parent View
     * @return Viewholder
     */
    public abstract RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType);

    /**
     * Binds Viewlholder to postion.
     *
     * @param viewHolder List Item
     * @param position   Position in list.
     */
    public abstract void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position);

    /**
     * Returns list item count.
     *
     * @return item count
     */
    @Override
    public int getItemCount() {
        if (mList == null) {
            return 0;
        }
        return mList.size();
    }

    /**
     * Removes Item at given position and notifys ViewHolder
     *
     * @param position Position
     * @return removed item
     */
    public Item removeItem(int position) {
        if (mList == null) {
            throw new NullPointerException();
        }
        Item item = mList.remove(position);
        notifyItemRemoved(position);
        return item;
    }

    /**
     * Updates DataSet, notifys ViewHolder when Data changed.
     *
     * @param list new list.
     */
    public void updateDataSet(List<Item> list) {
        if (this.mList != null) {
            this.mList.clear();
        }
        this.mList = list;
        notifyDataSetChanged();
    }

    /**
     * Adds item at given position.
     *
     * @param position New position.
     * @param item     Item to add.
     */
    public void addItem(int position, Item item) {
        if (mList == null) {
            throw new NullPointerException();
        }
        mList.add(position, item);
    }

    /**
     * Moves Item
     *
     * @param from Original position.
     * @param to   New position.
     */
    public void moveItem(int from, int to) {
        if (mList == null) {
            throw new NullPointerException();
        }
        Item chat = mList.remove(from);
        mList.add(to, chat);
        notifyItemMoved(from, to);
    }

    /**
     * Animates List when text was entered in the searchview.
     *
     * @param itemList New list.
     */
    public void animateTo(List<Item> itemList) {
        if (mList == null) {
            throw new NullPointerException();
        }
        applyAndAnimateRemovals(itemList);
        applyAndAnimateAdditions(itemList);
        applyAndAnimateMovedItems(itemList);
    }

    /**
     * Returns ItemClickListener
     *
     * @return ItemClickListener
     */
    protected ItemClickListener getItemListener() {
        return this.listener;
    }

    /**
     * Listener for click events on list items.
     *
     * @param listener Click Listener
     */
    public void setItemListener(ItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * Returns list of current items..
     *
     * @return Current items.
     */
    public List<Item> getList() {
        if (mList == null) {
            new NullPointerException();
        }
        return this.mList;
    }

    /**
     * Removes items not used matching current search
     *
     * @param newModels List with searched items.
     */
    private void applyAndAnimateRemovals(List<Item> newModels) {
        for (int i = mList.size() - 1; i >= 0; i--) {
            final Item model = mList.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    /**
     * Adds items matching current search
     *
     * @param newModels List with items matching search
     */
    private void applyAndAnimateAdditions(List<Item> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final Item model = newModels.get(i);
            if (!mList.contains(model)) {
                addItem(i, model);
            }
        }
    }

    /**
     * Moves items matching current search to their correct position.
     *
     * @param newModels List with items matching search
     */
    private void applyAndAnimateMovedItems(List<Item> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final Item model = newModels.get(toPosition);
            final int fromPosition = mList.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

}
