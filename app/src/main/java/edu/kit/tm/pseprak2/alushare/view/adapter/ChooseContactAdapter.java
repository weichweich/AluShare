package edu.kit.tm.pseprak2.alushare.view.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.view.adapter.viewholder.ChooseContactItemViewHolder;


public class ChooseContactAdapter extends RecyclerView.Adapter<ChooseContactItemViewHolder> {
    private final List<Contact> list;
    Context context;

    public ChooseContactAdapter(Activity context) {
        super();
        this.context = context;
        this.list = HelperFactory.getContacHelper(this.context).getContacts();
    }


    @Override
    public ChooseContactItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item_choose_contact_activity, parent, false);
        return ChooseContactItemViewHolder.newInstance(view);
    }


    @Override
    public int getItemCount() {
        return (null != list ? list.size() : 0);
    }

    @Override
    public void onBindViewHolder(final ChooseContactItemViewHolder holder, int position) {


        Contact contact = getList().get(position);
        String itemText = contact.getName(context);
        holder.getCheckBox()
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        Contact element = (Contact) holder.getCheckBox()
                                .getTag();
                        element.setSelected(buttonView.isChecked());

                    }
                });
        holder.getCheckBox().setTag(list.get(position));

        holder.setText(itemText);
        holder.getImage().setImageBitmap((list.get(position).getPicture(context)));
        holder.getCheckBox().setChecked(list.get(position).isSelected());


    }

    public List<Contact> getList() {
        return this.list;
    }
}

