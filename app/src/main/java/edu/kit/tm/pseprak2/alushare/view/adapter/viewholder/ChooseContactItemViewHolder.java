package edu.kit.tm.pseprak2.alushare.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import edu.kit.tm.pseprak2.alushare.R;


public class ChooseContactItemViewHolder extends RecyclerView.ViewHolder {
    private ImageView image;
    private TextView text;
    private CheckBox checkBox;


    public ChooseContactItemViewHolder(View itemView) {
        super(itemView);
    }

    public ChooseContactItemViewHolder(final View parent, TextView itemText, ImageView itemImage, CheckBox checkBox) {
        super(parent);
        this.text = itemText;
        this.image = itemImage;
        this.checkBox = checkBox;
    }

    public static ChooseContactItemViewHolder newInstance(View parent) {
        TextView itemTextContactName = (TextView) parent.findViewById(R.id.contact_name);
        ImageView itemImageContact = (ImageView) parent.findViewById(R.id.contact_photo);
        CheckBox checkbox = (CheckBox) parent.findViewById(R.id.check);
        return new ChooseContactItemViewHolder(parent, itemTextContactName, itemImageContact, checkbox);
    }

    public CheckBox getCheckBox() {
        return this.checkBox;
    }

    public void setText(String name) {
        this.text.setText(name);
    }

    public ImageView getImage() {
        return this.image;
    }
}
