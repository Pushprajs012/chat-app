package com.talk.walk.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.talk.walk.Adapters.ChatMoreAdapter;
import com.talk.walk.Models.ChatMore;
import com.talk.walk.R;
import com.talk.walk.Utils.Constants;
import com.talk.walk.Utils.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatMoreBottomSheetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatMoreBottomSheetFragment extends BottomSheetDialogFragment implements ChatMoreAdapter.OnItemClickListener, ChatMoreAdapter.OnActionListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = ChatMoreBottomSheetFragment.class.getSimpleName();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Context mContext;

    private RecyclerView rvChatMore;
    private ImageButton ibChatMoreClose;
    private ChatMoreAdapter chatMoreAdapter;
    private List<ChatMore> chatMoreList = new ArrayList<>();

    private ChatMoreItemCLickListener chatMoreItemCLickListener;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    public ChatMoreBottomSheetFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatMoreBottomSheetFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatMoreBottomSheetFragment newInstance(String param1, String param2) {
        ChatMoreBottomSheetFragment fragment = new ChatMoreBottomSheetFragment();
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
        return inflater.inflate(R.layout.fragment_chat_more_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mContext = getActivity();
        chatMoreItemCLickListener = (ChatMoreItemCLickListener) mContext;

        mDatabase = FirebaseDatabase.getInstance(Constants.Urls.DATABASE_URL).getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();

        if (currentUser != null) {

            chatMoreList.add(new ChatMore(mContext.getDrawable(R.drawable.ic_baseline_person_add_alt_1_24), mContext.getString(R.string.add_friend), false));
            chatMoreList.add(new ChatMore(mContext.getDrawable(R.drawable.ic_baseline_clear_all_24), mContext.getString(R.string.clear_chat), true));
            chatMoreList.add(new ChatMore(mContext.getDrawable(R.drawable.ic_baseline_block_24), mContext.getString(R.string.block), true));
            chatMoreList.add(new ChatMore(mContext.getDrawable(R.drawable.ic_baseline_report_24), mContext.getString(R.string.report), true));
            chatMoreList.add(new ChatMore(mContext.getDrawable(R.drawable.ic_baseline_logout_24), mContext.getString(R.string.leave), true));

            rvChatMore = view.findViewById(R.id.rvChatMore);
            ibChatMoreClose = view.findViewById(R.id.ibChatMoreClose);
            chatMoreAdapter = new ChatMoreAdapter(mContext, chatMoreList, mParam1, this, this::onUnBlockAction, getDialog());
            rvChatMore.setLayoutManager(new LinearLayoutManager(mContext));
            rvChatMore.setAdapter(chatMoreAdapter);
        }

        ibChatMoreClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

    }

    @Override
    public void onItemClick(@NonNull ChatMore chatMore, @NonNull boolean isAlreadyBlocked) {
        chatMoreItemCLickListener.onItemClick(chatMore);
        if (chatMore.getName().equalsIgnoreCase(mContext.getString(R.string.leave))) {
            mDatabase.child(Constants.Keys.CHAT_DISCONNECTS).child(currentUser.getUid()).child(mParam1).child(Constants.Keys.MET_USER_ID).setValue(mParam1).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        mDatabase.child(Constants.Keys.CHAT_DISCONNECTS).child(mParam1).child(currentUser.getUid()).child(Constants.Keys.MET_USER_ID).setValue(mParam1);
                        Toast.makeText(mContext, "Disconnected", Toast.LENGTH_SHORT).show();
                        chatMoreItemCLickListener.onDisconnected(true);
                        getDialog().dismiss();
                    }
                }
            });
        } else if (chatMore.getName().equalsIgnoreCase(mContext.getString(R.string.report))) {
            showReportDialog();
        } else if (!isAlreadyBlocked && chatMore.getName().equalsIgnoreCase(mContext.getString(R.string.block))) {
            getDialog().dismiss();
            showBlockDialog();
        } else if (chatMore.getName().equalsIgnoreCase(mContext.getString(R.string.clear_chat))) {
            clearChat();
        }
    }

    private void clearChat() {
        mDatabase.child("chats").child(currentUser.getUid()).child(mParam1).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    String chat_id = mDatabase.child("chat").push().getKey();
                    HashMap<String, Object> dataMap = new HashMap<String, Object>();
                    dataMap.put("sender_user_id", currentUser.getUid());
                    dataMap.put("receiver_user_id", mParam1);
                    dataMap.put("chat_id", chat_id);
                    dataMap.put("timestamp", System.currentTimeMillis());
                    if (Controller.Companion.isPaid()) {
                        dataMap.put("is_paid", true);
                    }
                    assert chat_id != null;
                    mDatabase.child("chats").child(currentUser.getUid()).child(mParam1).child(chat_id).setValue(dataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(mContext, "Chat cleared", Toast.LENGTH_SHORT).show();
                                sendChat(mParam1);
                                chatMoreItemCLickListener.onChatCleared(true);
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendChat(String met_user_id) {
        String chat_id = mDatabase.child("chat").push().getKey().toString();
        HashMap<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("sender_user_id", currentUser.getUid());
        dataMap.put("receiver_user_id", met_user_id);
        dataMap.put("chat_id", chat_id);
        dataMap.put("timestamp", System.currentTimeMillis());
        dataMap.put("is_paid", Controller.Companion.isPaid());
        dataMap.put("type", "empty_message");
        mDatabase.child("chats").child(currentUser.getUid()).child(met_user_id).child(chat_id).setValue(dataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
//                    mDatabase.child("chats").child(met_user_id).child(currentUser.getUid()).child(chat_id).setValue(dataMap);
                } else  {
                    Log.e(TAG, "onComplete: " + task.getException());
                }
            }
        });
    }

    private void showBlockDialog() {
        Dialog dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.dialog_block_layout);
        dialog.getWindow().setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        TextView tvDialogBlockCancel = dialog.findViewById(R.id.tvDialogBlockCancel);
        TextView tvDialogBlock = dialog.findViewById(R.id.tvDialogBlock);

        tvDialogBlockCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        tvDialogBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                blockUser(dialog);
            }
        });
    }

    private void blockUser(Dialog dialog) {
        String block_id = mDatabase.child("blocks").push().getKey().toString();
        HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put("user_id", currentUser.getUid());
        dataMap.put("met_user_id", mParam1);
        dataMap.put("timestamp", System.currentTimeMillis());
        mDatabase.child("blocks").child(currentUser.getUid()).child(mParam1).setValue(dataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mDatabase.child("blocks").child(mParam1).child(currentUser.getUid()).setValue(dataMap);
                    dialog.dismiss();
                    Toast.makeText(mContext, "User blocked", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void reportUser(String reason, Dialog dialog) {
        String report_id = mDatabase.child("reports").push().getKey().toString();
        HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put("report_id", report_id);
        dataMap.put("user_id", currentUser.getUid());
        dataMap.put("met_user_id", mParam1);
        dataMap.put("timestamp", System.currentTimeMillis());
        dataMap.put("report_reason", reason);
        mDatabase.child("reports").child(currentUser.getUid()).child(mParam1).setValue(dataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mDatabase.child("reports").child(mParam1).child(currentUser.getUid()).setValue(dataMap);
                    dialog.dismiss();
                    Toast.makeText(mContext, "You report is submitted", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showReportDialog() {
        Dialog dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.dialog_report_layout);
        dialog.getWindow().setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        TextView tvDialogReportCancel = dialog.findViewById(R.id.tvDialogReportCancel);
        TextView tvDialogReport = dialog.findViewById(R.id.tvDialogReport);
        RadioGroup rgDialogReport = dialog.findViewById(R.id.rgDialogReport);

        tvDialogReportCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        tvDialogReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rgDialogReport.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(mContext, "Please select reason", Toast.LENGTH_SHORT).show();
                } else {
                    int selectedId = rgDialogReport.getCheckedRadioButtonId();
                    // find the radiobutton by returned id
                    RadioButton selectedRadioButton = (RadioButton) dialog.findViewById(selectedId);
                    reportUser(selectedRadioButton.getText().toString(), dialog);
                }
            }
        });


    }

    @Override
    public void onUnBlockAction(@NonNull ChatMore chatMore, boolean isUnBlock) {
        if (isUnBlock) {
            chatMoreItemCLickListener.onUnBlocked(true);
        }
    }


    public interface ChatMoreItemCLickListener {
        void onItemClick(ChatMore chatMore);

        void onDisconnected(boolean is_disconnected);

        void onBlocked(boolean isBlocked);

        void onChatCleared(boolean isCleared);

        void onUnBlocked(boolean isUnBlocked);
    }

    @Override
    public int getTheme() {
        return R.style.CustomBottomSheetDialog;
    }
}