package com.talk.walk.Adapters;

import static androidx.core.content.ContextCompat.getDrawable;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

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
import com.talk.walk.Activities.ChatActivity;
import com.talk.walk.Models.Chat;
import com.talk.walk.R;
import com.talk.walk.Utils.Constants;
import com.talk.walk.Utils.Controller;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class InboxAdapter2 extends RecyclerView.Adapter<InboxAdapter2.ViewHolder> {

    private static final String TAG = InboxAdapter2.class.getSimpleName();
    private Context mContext;
    private List<Chat> chatList;
    private OnItemClickListener onItemClickListener;
    private OnItemChange onItemChange;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    public InboxAdapter2(Context mContext, List<Chat> chatList, OnItemClickListener onItemClickListener, OnItemChange onItemChange) {
        this.mContext = mContext;
        this.chatList = chatList;
        this.onItemClickListener = onItemClickListener;
        this.onItemChange = onItemChange;
    }

    public void setChatList(List<Chat> chatList) {
        this.chatList = chatList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InboxAdapter2.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_inbox_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InboxAdapter2.ViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        holder.setIsRecyclable(false);


        holder.tvInboxMessage.setText(chat.getMessage());

        mDatabase = FirebaseDatabase.getInstance(Constants.Urls.DATABASE_URL).getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();


        holder.cvInbox.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.white));

        mDatabase.child("chats").child(chat.getSender_user_id()).child(chat.getReceiver_user_id()).limitToFirst(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    if (snapshot.child("sender_user_id").exists() && snapshot.child("receiver_user_id").exists()) {
                        String const_sender_user_id = snapshot.child("sender_user_id").getValue().toString();
                        String const_receiver_user_id = snapshot.child("receiver_user_id").getValue().toString();
                        if (snapshot.child("is_paid").exists()) {
                            boolean isPaid = (boolean) snapshot.child("is_paid").getValue();
                            Controller.Companion.setPaid(isPaid);
                            if (isPaid) {
                                holder.ivChatPaidCoin.setVisibility(View.VISIBLE);
                            } else {
                                holder.ivChatPaidCoin.setVisibility(View.GONE);
                            }
                        } else {
                            holder.ivChatPaidCoin.setVisibility(View.VISIBLE);
                        }
                    }

                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (Controller.Companion.isDarkTheme(Controller.mainActivity)) {
            holder.tvInboxMessage.setTextColor(Color.GRAY);
            holder.tvInboxName.setTextColor(Color.WHITE);
        } else {
            holder.tvInboxMessage.setTextColor(Color.GRAY);
            holder.tvInboxName.setTextColor(Color.BLACK);
        }
        if (chat.getMedia_type().isEmpty()) {
            if (chat.getMessage().isEmpty()) {
                holder.tvInboxMessage.setVisibility(View.GONE);
            } else {
                holder.tvInboxMessage.setText(chat.getMessage());
            }
        } else if (chat.getMedia_type().equals(Constants.Values.GALLERY)  || chat.getMedia_type().equals(Constants.Values.CAMERA) && chat.getSender_user_id().equals(currentUser.getUid())) {
            holder.tvInboxMessage.setText("You have sent an image");
        } else if (chat.getMedia_type().equals(Constants.Values.GALLERY) || chat.getMedia_type().equals(Constants.Values.CAMERA) && chat.getReceiver_user_id().equals(currentUser.getUid())) {
            holder.tvInboxMessage.setText("You have received an image");
        } else if (chat.getMedia_type().equals(Constants.Values.VOICE) && chat.getSender_user_id().equals(currentUser.getUid())) {
            holder.tvInboxMessage.setText("You have sent an voice message");
        } else if (chat.getMedia_type().equals(Constants.Values.VOICE) && chat.getReceiver_user_id().equals(currentUser.getUid())) {
            holder.tvInboxMessage.setText("You have received an voice message");
        } else if (chat.getMedia_type().equals(Constants.Values.VIDEO) && chat.getSender_user_id().equals(currentUser.getUid())) {
            holder.tvInboxMessage.setText("You have sent a video");
        } else if (chat.getMedia_type().equals(Constants.Values.VIDEO) && chat.getReceiver_user_id().equals(currentUser.getUid())) {
            holder.tvInboxMessage.setText("You have received a video");
        }

        if (chat.is_read()) {
            holder.ivInboxIsReadIndicator.setVisibility(View.GONE);
        } else {
            holder.ivInboxIsReadIndicator.setVisibility(View.INVISIBLE);
        }

//        val databaseRef: DatabaseReference = if (chat.receiver_user_id == currentUser.uid) {
//            databseReference.child("users").child(chat.sender_user_id)
//        } else {
//            databseReference.child("users").child(chat.receiver_user_id)
//
//        }


        mDatabase.child("users").child(chat.getUser_key()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.child("username").exists()) {
                        String username = snapshot.child("username").getValue().toString();
                        holder.tvInboxName.setText(username);
                        if (snapshot.child("profile_image").exists()) {
                            String profile_image = snapshot.child("profile_image").getValue().toString();
                            try {
                                //                            Glide.with(mContext).load(profile_image).error(R.drawable.user).placeholder(R.drawable.user).into(holder.civInboxProfilePic);
                                Picasso.get().load(profile_image).error(R.drawable.user).placeholder(R.drawable.user).into(holder.civInboxProfilePic);
                            } catch (Exception e) {
                                Log.e(TAG, "onDataChange: ", e);
                            }
                        } else {
                            holder.civInboxProfilePic.setImageDrawable(getDrawable(mContext, R.drawable.user));
                            holder.cvProfileBG.setCardBackgroundColor(Controller.Companion.getRandomColor());
                            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                            layoutParams.leftMargin = 16;
                            layoutParams.rightMargin = 16;
                            layoutParams.topMargin = 16;
                            layoutParams.bottomMargin = 16;
                            holder.civInboxProfilePic.setLayoutParams(layoutParams);
                            holder.civInboxProfilePic.setColorFilter(Color.WHITE);
                        }
                    } else {
                        holder.tvInboxName.setText(mContext.getResources().getText(R.string.account_deleted));
                        holder.cvInbox.setVisibility(View.GONE);
                    }
                } else {
                    holder.tvInboxMessage.setText(mContext.getResources().getString(R.string.account_deleted));
                    holder.civInboxProfilePic.setImageDrawable(getDrawable(mContext, R.drawable.user));
                    holder.cvProfileBG.setCardBackgroundColor(Controller.Companion.getRandomColor());
                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                    layoutParams.leftMargin = 16;
                    layoutParams.rightMargin = 16;
                    layoutParams.topMargin = 16;
                    layoutParams.bottomMargin = 16;
                    holder.civInboxProfilePic.setLayoutParams(layoutParams);
                    holder.civInboxProfilePic.setColorFilter(Color.WHITE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.cvInbox.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onItemClickListener.onItemClick(holder, chat, chatList, position);
                return false;
            }
        });

        holder.cvInbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chatIntent = new Intent(mContext, ChatActivity.class);
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Controller.mainActivity,
                        Pair.create(holder.cvProfileBG, "chatProfileIcon"),
                        Pair.create(holder.tvInboxName, "chatProfileName"),
                        Pair.create(holder.ivChatPaidCoin, "chatProfilePaid"));
                if (currentUser.getUid().equals(chat.getReceiver_user_id())) {
                    chatIntent.putExtra("met_user_id", chat.getSender_user_id());
                } else {
                    chatIntent.putExtra("met_user_id", chat.getReceiver_user_id());
                }
                mContext.startActivity(chatIntent);
            }
        });
    }

    public void clear() {
        int size = chatList.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                chatList.remove(0);
            }

            notifyItemRangeRemoved(0, size);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvInboxName, tvInboxMessage;
        private CircleImageView civInboxProfilePic;
        private CardView cvInbox, cvProfileBG;
        private ImageView ivInboxIsReadIndicator, ivChatPaidCoin;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvInboxName = itemView.findViewById(R.id.tvInboxName);
            tvInboxMessage = itemView.findViewById(R.id.tvInboxMessage);
            civInboxProfilePic = itemView.findViewById(R.id.civInboxProfilePic);
            cvInbox = itemView.findViewById(R.id.cvInbox);
            cvProfileBG = itemView.findViewById(R.id.cvProfileBG);
            ivInboxIsReadIndicator = itemView.findViewById(R.id.ivInboxIsReadIndicator);
            ivChatPaidCoin = itemView.findViewById(R.id.ivChatPaidCoin);
        }
    }

    public interface OnItemChange {
        void notifyAdapter(int position);
    }

    public interface OnItemClickListener {
        void onItemClick(ViewHolder holder, Chat chat, List<Chat> chatList, int position);
    }
}
