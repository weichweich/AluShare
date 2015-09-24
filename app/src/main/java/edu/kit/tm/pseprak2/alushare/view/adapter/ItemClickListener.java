package edu.kit.tm.pseprak2.alushare.view.adapter;

import android.view.MenuItem;
import android.view.View;

/**
 * Interface for ClickListener
 * @author Niklas Sänger
 */
public interface ItemClickListener {
    /**
     * For normal clicks.
     *
     * @param view View containing listener.
     * @param id   ID of the object.
     */
    void onItemClick(View view, long id);
}
