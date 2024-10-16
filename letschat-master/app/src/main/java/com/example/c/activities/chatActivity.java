package com.example.c.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.SavedStateHandle;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import com.example.c.R;
import com.example.c.adaptor.ChatAdaptor;
import com.example.c.databinding.ActivityChatBinding;
import com.example.c.databinding.ActivityUsersBinding;
import com.example.c.models.ChatMessage;
import com.example.c.models.User;
import com.example.c.utilites.Constants;
import com.example.c.utilites.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class chatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private User reciverUser;
    private List<ChatMessage>chatMessages;
    private ChatAdaptor chatAdaptor;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversionId=null;
    private Boolean isReceiverAvailable=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadreciverdetails();
        setListners();
        init();
        listenMessages();
    }
    private void init(){
        preferenceManager=new PreferenceManager(getApplicationContext());
        chatMessages=new ArrayList<>();
        chatAdaptor=new ChatAdaptor(
                chatMessages,getBitmapFromEncodedImage(reciverUser.image)
                ,preferenceManager.getString(Constants.KEY_USER)

        );
        binding.chatRecyclerView.setAdapter(chatAdaptor);
        database=FirebaseFirestore.getInstance();
    }



    private void sendMessage(){
        HashMap<String,Object>message=new HashMap<>();
        message.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER));
        message.put(Constants.KEY_RECEIVER_ID,reciverUser.id);
        message.put(Constants.KEY_MESSAGE,binding.imputMessage.getText().toString());
        message.put(Constants.KEY_TIME_STAMP,new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if(conversionId!=null){
            updateConversion(binding.imputMessage.getText().toString());
        }
        else{
            HashMap<String,Object>conversion=new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER));
            conversion.put(Constants.KEY_SENDER_NAME,preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE,preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID,reciverUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME,reciverUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE,reciverUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE,binding.imputMessage.getText().toString());
            conversion.put(Constants.KEY_TIME_STAMP,new Date());
            addConversion(conversion);
        }
        binding.imputMessage.setText(null);
        // This null is used ````````1to clear the text after sending the message in the input text

    }
    private void listenAvailabilityOfReceiver(){
        database.collection(Constants.KEY_COLLECTION_USERS).document(
                reciverUser.id
        ).addSnapshotListener(chatActivity.this,(value, error) -> {
            if(error !=null){
                return;
            }
            if(value != null){
                if(value.getLong(Constants.KEY_AVAILABILITY)!=null){
                    int availability= Objects.requireNonNull(
                            value.getLong(Constants.KEY_AVAILABILITY)
                    ).intValue();
                    isReceiverAvailable=availability==1;
                }
            }
            if(isReceiverAvailable){
                binding.textAvailability.setVisibility(View.VISIBLE);
            }
            else{
                binding.textAvailability.setVisibility(View.GONE);
            }

        });

    }
   private void listenMessages(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER))
                .whereEqualTo(Constants.KEY_RECEIVER_ID,reciverUser.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,reciverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_USER))
                .addSnapshotListener(eventListener);
   }
    private final EventListener<QuerySnapshot>eventListener=(value,error)->{
        if(error!=null){
            return;
        }
        if (value!=null){
            int count=chatMessages.size();

            for (DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType()== DocumentChange.Type.ADDED){
                    ChatMessage chatMessage=new ChatMessage();
                    chatMessage.senderId=documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId=documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message=documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime=getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIME_STAMP));
                    chatMessage.dateObject=documentChange.getDocument().getDate(Constants.KEY_TIME_STAMP);
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages,(obj1,obj2)-> obj1.dateObject.compareTo(obj2.dateObject ));
            if(count==0){
                chatAdaptor.notifyDataSetChanged();
            }
            else{
               chatAdaptor.notifyItemRangeInserted(chatMessages.size(),chatMessages.size());
               binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size()-1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);

        }
        binding.progressbar.setVisibility(View.GONE);
        if(conversionId==null){
            checkForConversion();
        }
    };
    private Bitmap getBitmapFromEncodedImage(String encodedImage){
        byte[]bytes= Base64.decode(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
    private void loadreciverdetails(){
        reciverUser=(User) getIntent().getSerializableExtra(Constants.KEY_UUSERR);
       binding.textName.setText(reciverUser.name);

    }
    private void setListners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }
    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("MMMM dd,yyyy-hh:mm a", Locale.getDefault()).format(date);
    }
    private void addConversion(HashMap<String,Object>conversion){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId= documentReference.getId());
    }
    private void updateConversion(String message){
        DocumentReference documentReference=
                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversionId);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE,message,Constants.KEY_TIME_STAMP,new Date()
        );
    }

    private void checkForConversion(){
        if(chatMessages.size()!=0){
            checkForConversionRemotely(
                    preferenceManager.getString(Constants.KEY_USER),
                    reciverUser.id
            );
            checkForConversionRemotely(
                    reciverUser.id,
                    preferenceManager.getString(Constants.KEY_USER)
            );
        }
    }
    private void checkForConversionRemotely(String senderId,String receiverId){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID,senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);
    }
    private final OnCompleteListener<QuerySnapshot>conversionOnCompleteListener=task -> {
        if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size()>0){
            DocumentSnapshot documentSnapshot=task.getResult().getDocuments().get(0);
            conversionId=documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }
}