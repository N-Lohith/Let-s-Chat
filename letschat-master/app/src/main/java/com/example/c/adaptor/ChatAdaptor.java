package com.example.c.adaptor;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.c.databinding.ItemContainerRecivedMessageBinding;
import com.example.c.databinding.ItemContainerSentMessageBinding;
import com.example.c.models.ChatMessage;

import java.util.List;

 public class ChatAdaptor extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
private final List<ChatMessage>chatMessages;
    private  Bitmap receiverProfileImage;
    private final String senderId;
    public static final int VIEW_TYPE_SENT=1;
    public static final int VIEW_TYPE_RECIVED=2;
    public void setrecyclerProfileImage(Bitmap bitmap){
        receiverProfileImage=bitmap;
    }

    public ChatAdaptor(List<ChatMessage> chatMessages, Bitmap reciverProfileImage, String senderId) {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = reciverProfileImage;
        this.senderId = senderId;

    }

     @NonNull
     @Override
     public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==VIEW_TYPE_SENT){
            return new sentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false)
            );
        }
        else{
            return new receivedMessageViewHolder(ItemContainerRecivedMessageBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }



     }

     @Override
     public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
         if (getItemViewType(position) == VIEW_TYPE_SENT) {
             ((sentMessageViewHolder)holder).setData(chatMessages.get(position));


         }
         else{
             ((receivedMessageViewHolder)holder).setData(chatMessages.get(position),receiverProfileImage);
         }
     }
     @Override
     public int getItemCount() {
         return chatMessages.size();
     }

     @Override
     public int getItemViewType(int position) {
         if(chatMessages.get(position).senderId.equals(senderId)){
             return VIEW_TYPE_SENT;
         }
         else {
             return VIEW_TYPE_RECIVED;
         }
     }

     static class sentMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerSentMessageBinding binding;
        sentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding){
            super (itemContainerSentMessageBinding.getRoot());
            binding=itemContainerSentMessageBinding;
        }
        void setData(ChatMessage chatMessage){
            binding.textDateTime.setText(chatMessage.dateTime);
            binding.textMessage.setText(chatMessage.message);
        }
    }
    static class receivedMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerRecivedMessageBinding binding;
        receivedMessageViewHolder(ItemContainerRecivedMessageBinding itemContainerRecivedMessageBinding){
            super(itemContainerRecivedMessageBinding.getRoot());
            binding=itemContainerRecivedMessageBinding;
        }
        void setData( ChatMessage chatMessage,Bitmap receiverProfileImage){
            binding.textDateTime.setText(chatMessage.dateTime);
            binding.textMessage.setText(chatMessage.message);
            if(receiverProfileImage!=null){
                binding.imageProfile.setImageBitmap(receiverProfileImage);
            }

        }

    }
}
