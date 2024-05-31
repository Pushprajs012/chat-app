package com.talk.walk.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.talk.walk.Activities.MainActivity;
import com.talk.walk.Activities.MatchPeopleActivity;
import com.talk.walk.Adapters.InboxAdapter2;
import com.talk.walk.Models.Chat;
import com.talk.walk.R;
import com.talk.walk.Utils.Constants;
import com.talk.walk.Utils.Controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment2#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment2 extends Fragment implements InboxAdapter2.OnItemClickListener, InboxAdapter2.OnItemChange {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Context mContext;

    private RecyclerView rvInbox;
    private Button bChatMeetNewPeople;
    private TextView tvNoChats;
    private SwipeRefreshLayout srlChat;
    private ProgressBar pbChatLoader;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    public List<Chat> chatList = new ArrayList<>();
    private List<Chat> chatList2 = new ArrayList<>();
    private InboxAdapter2 inboxAdapter2;
    private LinearLayoutManager linearLayoutManager;


    public ChatFragment2() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatFragment2.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatFragment2 newInstance(String param1, String param2) {
        ChatFragment2 fragment = new ChatFragment2();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mContext = getActivity();

        rvInbox = view.findViewById(R.id.rvInbox);
        bChatMeetNewPeople = view.findViewById(R.id.bChatMeetNewPeople);
        tvNoChats = view.findViewById(R.id.tvNoChats);
        srlChat = view.findViewById(R.id.srlChat);
        pbChatLoader = view.findViewById(R.id.pbChatLoader);

        pbChatLoader.setVisibility(View.VISIBLE);
        tvNoChats.setVisibility(View.GONE);

        mDatabase = FirebaseDatabase.getInstance(Constants.Urls.DATABASE_URL).getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();


        inboxAdapter2 = new InboxAdapter2(mContext, chatList, this, this);
        linearLayoutManager = new LinearLayoutManager(getActivity());
//        inboxAdapter2.setHasStableIds(true);
        rvInbox.setHasFixedSize(true);
        rvInbox.setAdapter(inboxAdapter2);
        rvInbox.setLayoutManager(linearLayoutManager);

        getInbox();

        srlChat.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                refreshFragment();
//                srlChat.setRefreshing(true);
//                chatList.clear();
//                rvInbox.getRecycledViewPool().clear();
//                getInbox();
                MainActivity.Companion.refreshActivity(Controller.mainActivity);
            }
        });

        bChatMeetNewPeople.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent meetNewPeopleIntent = new Intent(mContext, MatchPeopleActivity.class);
                startActivity(meetNewPeopleIntent);
            }
        });
        AdView adView = new AdView(mContext);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(Controller.Companion.getADMOB_BANNER_ID());
        MobileAds.initialize(mContext);
        AdView mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }

    private void getInbox() {
        chatList.clear();
        rvInbox.setVisibility(View.VISIBLE);
        mDatabase.child("chats").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    tvNoChats.setVisibility(View.GONE);
                    chatList.clear();
                    pbChatLoader.setVisibility(View.GONE);
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        tvNoChats.setVisibility(View.GONE);
                        String key = ds.getKey().toString();
                        mDatabase.child("chats").child(currentUser.getUid()).child(key).limitToLast(1).addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                if (snapshot.exists()) {
                                    srlChat.setRefreshing(false);
                                    tvNoChats.setVisibility(View.GONE);
                                    Chat chat = snapshot.getValue(Chat.class);
                                    assert chat != null;
                                    chatList.add(chat);
                                    chat.setUser_key(key);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        chatList.sort((e1, e2) -> new Long(e2.getTimestamp()).compareTo(new Long(e1.getTimestamp())));
                                    }
                                }
                                inboxAdapter2.setChatList(removeTheDuplicates(chatList));

                            }

                            @Override
                            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                            }

                            @Override
                            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                                Chat chat = snapshot.getValue(Chat.class);
                                chatList.remove(chat);
                                inboxAdapter2.notifyDataSetChanged();
                            }

                            @Override
                            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                srlChat.setRefreshing(false);
                                tvNoChats.setVisibility(View.VISIBLE);
                                pbChatLoader.setVisibility(View.GONE);
                            }
                        });

//                        mDatabase.child("recent_message").child(currentUser.getUid()).child(key).addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                if (snapshot.exists()) {
//                                    rvInbox.getRecycledViewPool().clear();
//                                    srlChat.setRefreshing(false);
//                                    tvNoChats.setVisibility(View.GONE);
//                                    Chat chat = snapshot.getValue(Chat.class);
//                                    assert chat != null;
//                                    chatList.add(chat);
//                                    chat.setUser_key(key);
//                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                                        chatList.sort((e1, e2) -> new Long(e2.getTimestamp()).compareTo(new Long(e1.getTimestamp())));
//                                    }
//                                    inboxAdapter2.notifyDataSetChanged();
//                                }
//
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError error) {
//                                srlChat.setRefreshing(false);
//                                tvNoChats.setVisibility(View.VISIBLE);
//                            }
//                        });
                    }

                } else {
                    srlChat.setRefreshing(false);
                    tvNoChats.setVisibility(View.VISIBLE);
                    pbChatLoader.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                srlChat.setRefreshing(false);
                tvNoChats.setVisibility(View.VISIBLE);
            }
        });


//        inboxAdapter2.notifyDataSetChanged();

//        databseReference.child("recent_message").child(currentUser.uid).addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.exists()) {
//                    tvNoChats.visibility = View.GONE
//                    chatList.clear()
//                    for (ds in snapshot.children.iterator()) {
//                        val key = ds.key.toString()
//                        tvNoChats.visibility = View.GONE
//                        databseReference.child("recent_message").child(currentUser.uid).child(key).addListenerForSingleValueEvent(object : ValueEventListener {
//                            override fun onDataChange(snapshot: DataSnapshot) {
//                                if (snapshot.exists()) {
//                                    val chat = snapshot.getValue(Chat::class.java)
//                                    if (chat != null) {
//                                        chatList.add(chat)
//                                        chatList.sortWith(Comparator { e1, e2 ->
//                                                e2.timestamp.compareTo(e1.timestamp)
//                                        })
//                                        chat.user_key = key
//                                        tvNoChats.visibility = View.GONE
//                                    }
//                                    rvInbox.visibility = View.VISIBLE
//                                    srlChat.isRefreshing = false
//                                    inboxAdapter.notifyDataSetChanged()
//                                }
//                            }
//
//                            override fun onCancelled(error: DatabaseError) {
//                            }
//
//                        })
//
//                    }
//                } else {
//                    srlChat.isRefreshing = false
//                    tvNoChats.visibility = View.VISIBLE
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Log.e(TAG, "onCancelled: ${error.details}")
//
//            }
//
//        })
    }

    @Override
    public void notifyAdapter(int position) {

    }

    @Override
    public void onItemClick(@NonNull InboxAdapter2.ViewHolder holder, @NonNull Chat chat, @NonNull List<Chat> chatList, int position) {
        Dialog dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.dialog_delete_account_layout);
        dialog.getWindow().setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        TextView tvDialogTwoButtonMessage = dialog.findViewById(R.id.tvDialogTwoButtonMessage);
        TextView tvDialogTwoButtonHeader = dialog.findViewById(R.id.tvDialogTwoButtonHeader);
        TextView tvDeleteCancel = dialog.findViewById(R.id.tvDeleteCancel);
        TextView tvDeleteOkay = dialog.findViewById(R.id.tvDeleteOkay);

        tvDialogTwoButtonHeader.setText("Delete Conversation?");
        tvDialogTwoButtonMessage.setText("Are you sure you want to delete this conversation? By deleting you cannot restore it.");


        tvDeleteCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        tvDeleteOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Task<Void> query;
                if (currentUser.getUid().equals(chat.getSender_user_id())) {
                    query = mDatabase.child("chats").child(chat.getSender_user_id()).child(chat.getReceiver_user_id()).setValue(null);
                } else {
                    query = mDatabase.child("chats").child(chat.getReceiver_user_id()).child(chat.getSender_user_id()).setValue(null);
                }
                Task<Void> query2;
                if (currentUser.getUid().equals(chat.getSender_user_id())) {
                    query2 = mDatabase.child("recent_message").child(chat.getSender_user_id()).child(chat.getReceiver_user_id()).setValue(null);
                } else {
                    query2 = mDatabase.child("recent_message").child(chat.getReceiver_user_id()).child(chat.getSender_user_id()).setValue(null);

                }
                query.addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if (chat.getSender_user_id().equals(currentUser.getUid())) {
                                mDatabase.child("already_met").child(chat.getSender_user_id()).child(chat.getReceiver_user_id()).setValue(null);
                            }
                            Toast.makeText(mContext, "Chat deleted", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            chatList.remove(chat);
                            inboxAdapter2.notifyItemRemoved(holder.getAdapterPosition());
                            inboxAdapter2.notifyItemRangeChanged(holder.getAdapterPosition(), chatList.size());
                            inboxAdapter2.notifyDataSetChanged();
//                            getInbox();
//                            MainActivity.Companion.refreshActivity(Controller.mainActivity);
                        }
                    }
                });
            }
        });
    }

    private List<Chat> removeTheDuplicates(List<Chat> myList) {
        for (ListIterator<Chat> iterator = myList.listIterator(); iterator.hasNext(); ) {
            Chat customer = iterator.next();
            if (Collections.frequency(myList, customer) > 1) {
                iterator.remove();
            }
        }
        System.out.println(myList.toString());
        return myList;
    }

    private void refreshFragment() {
        // Reload current fragment
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (Build.VERSION.SDK_INT >= 26) {
            ft.setReorderingAllowed(false);
        }
        ft.detach(this).attach(this).commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        chatList.clear();
        getInbox();
    }

    @Override
    public void onStop() {
        super.onStop();
//        chatList.clear();
//        inboxAdapter2.notifyDataSetChanged();

    }
}