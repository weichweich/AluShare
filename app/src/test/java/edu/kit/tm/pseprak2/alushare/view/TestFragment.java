package edu.kit.tm.pseprak2.alushare.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.view.adapter.ChatTabRecyclerAdapter;
import edu.kit.tm.pseprak2.alushare.view.adapter.ContactTabRecyclerAdapter;
import edu.kit.tm.pseprak2.alushare.view.adapter.FileTabRecyclerAdapter;
import edu.kit.tm.pseprak2.alushare.view.adapter.ItemClickListener;
import edu.kit.tm.pseprak2.alushare.view.adapter.TabAdapter;

/**
 * Created by niklas on 28.08.15.
 */
public class TestFragment extends Fragment {
    public static int TYPE_FILE_TAB = 0;
    public static int TYPE_CHAT_TAB = 1;
    public static int TYPE_CONTACT_TAB = 2;
    private boolean clicked = false;
    private static String TYPE = "TYP";
    private TabAdapter adapter = null;
    private LinearLayoutManager mLayoutManager = null;
    private RecyclerView recyclerView;
    private long idClicked = -1;
    public static TestFragment createInstance(int type) {
        TestFragment fragment = new TestFragment();
        Bundle b = new Bundle();
        b.putInt(TYPE, type);
        fragment.setArguments(b);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int tab = getArguments().getInt(TYPE);

        switch (tab) {
            case 0:
                adapter = new FileTabRecyclerAdapter(this);
                break;
            case 1:
                adapter = new ChatTabRecyclerAdapter(this);
                break;
            case 2:
                adapter = new ContactTabRecyclerAdapter(this);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(
                R.layout.fragment_file_tab, container, false);
        mLayoutManager = new LinearLayoutManager(this.getActivity());

        recyclerView.measure(0, 0);
        recyclerView.layout(0, 0, 100, 10000);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new Divider(this.getActivity().getApplicationContext()));

        adapter.setItemListener(new ItemClickListener() {
            @Override
            public void onItemClick(View view, long id) {
                clicked = true;
                idClicked = id;
            }
        });

        return recyclerView;
    }

    public TabAdapter getAdapter() {
        return this.adapter;
    }

    public RecyclerView getRecyclerView() {
        return this.recyclerView;
    }

    public LinearLayoutManager getLayoutManager() {
        return this.mLayoutManager;
    }

    public boolean wasClicked() {
        return clicked;
    }

    public long getClickID() {
        return idClicked;
    }
}
