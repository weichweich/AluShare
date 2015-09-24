package edu.kit.tm.pseprak2.alushare.presenter;

import java.util.List;

import edu.kit.tm.pseprak2.alushare.model.Chat;
import edu.kit.tm.pseprak2.alushare.model.Data;
import edu.kit.tm.pseprak2.alushare.model.DataState;
import edu.kit.tm.pseprak2.alushare.model.helper.AluObserver;
import edu.kit.tm.pseprak2.alushare.model.helper.ChatHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.ContactHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.view.adapter.ChatTabRecyclerAdapter;
import edu.kit.tm.pseprak2.alushare.view.fragments.ChatTabFragment;

/**
 * @author Niklas Sänger
 *         Presenter for the ChatTab fragment.
 */
public class ChatTabPresenter implements AluObserver<Chat> {
    private static final String TAG = "ChatTabPresenter";
    private final ChatHelper chathelper;
    private ChatTabFragment view;
    private List<Chat> chatList;
    private ChatTabRecyclerAdapter adapter;
    private ContactHelper contactHelper;
    // Field can't be local because the observable holds just weak references. If the field was
    // local, the object would be destroyed. The class wont receive any updates.
    @SuppressWarnings("FieldCanBeLocal")
    private AluObserver<Data> dataAluObserver;

    /**
     * Constructor. Initializes SQLContext
     */
    public ChatTabPresenter(final ChatTabFragment view, ChatTabRecyclerAdapter adapter) {
        if (view == null || adapter == null) {
            throw new IllegalArgumentException();
        }
        this.view = view;
        this.adapter = adapter;

        this.chathelper = HelperFactory.getChatHelper(view.getActivity().getApplicationContext());
        chathelper.addObserver(this);
        this.adapter = adapter;
        this.chatList = chathelper.getChats();
        this.dataAluObserver = new AluObserver<Data>() {
            @Override
            public void updated(final Data data) {
                if (view != null && view.getActivity() != null) {
                    view.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ChatTabPresenter.this.adapter.updateChat(data);
                        }
                    });
                }
            }

            @Override
            public void inserted(final Data data) {
                if (view != null && view.getActivity() != null) {
                    view.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ChatTabPresenter.this.adapter.updateChat(data.getNetworkChatID(), data);
                        }
                    });
                }
            }

            @Override
            public void removed(Data data) {
            }
        };
        HelperFactory.getDataHelper(view.getActivity()).addObserver(dataAluObserver);

        this.contactHelper = HelperFactory.getContacHelper(view.getActivity().getApplicationContext());
        adapter.setOwnContact(contactHelper.getSelf());
    }

    /**
     * Loads more chat items..
     */
    public List<Chat> getChatList() {
        return chathelper.getChats();
    }

    /**
     * Returns filterd list of chats.
     *
     * @param query Parameter to filter the list.
     * @return filtered list.
     */
    public List<Chat> getChatList(String query) {
        return chathelper.getChatsByTitle(query.toLowerCase(), Integer.MAX_VALUE, 0);
    }

    public void removeChat(String id) {
        final Chat chat = chathelper.getChat(id);
        new Thread(new Runnable() {
            public void run() {
                chathelper.delete(chat);
            }
        }).run();

    }

    /**
     * Benennt einen Chat um
     *
     * @param netId Identifikation des Chats
     * @param name  neuer Name des Chats
     */
    public void renameChat(String netId, String name) {
        Chat chat = chathelper.getChat(netId);
        chat.setTitle(name);
        chathelper.update(chat);
    }

    /**
     * Gibt Identifikation des Chats zurück
     *
     * @param id ID des Chats
     * @return ID des Chats
     */
    public String getNetworkIdentifier(int id) {
        return chathelper.getChats().get(id).getNetworkChatID();
    }

    @Override
    public void updated(Chat data) {
        this.update(data);
    }

    @Override
    public void inserted(Chat data) {
        this.update();
    }

    @Override
    public void removed(Chat data) {
        this.update();
    }

    //Lädt liste neu
    private void update() {
        if (view != null && view.getActivity() != null) {
            view.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.updateDataSet(getChatList());
                }
            });
        }
    }

    //Lädt nur einzelnes Chat Element neu
    private void update(final Chat chat) {
        if (view != null && view.getActivity() != null) {
            view.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.updateData(chat.getNetworkChatID(), chat);
                }
            });
        }
    }
}
