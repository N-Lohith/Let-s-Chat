package com.example.c.adaptor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.c.databinding.ItemContainUserBinding;
import com.example.c.listeners.UserListeners;
import com.example.c.models.User;

import java.util.List;

public class UsersAdaptor  extends RecyclerView.Adapter<UsersAdaptor.UserViewHolder>{
        private final List<User>users;
        private final UserListeners userListeners;

    public UsersAdaptor(List<User> users,UserListeners userListeners) {
        this.users = users;
        this.userListeners=userListeners;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainUserBinding itemContainUserBinding=ItemContainUserBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);

        return new  UserViewHolder(itemContainUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.UserData(users.get(position));

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder{
        ItemContainUserBinding binding;
        UserViewHolder(ItemContainUserBinding itemContainUserBinding){
            super(itemContainUserBinding.getRoot());
            binding=itemContainUserBinding;
        }
        void UserData(User user){
            binding.textName.setText(user.name);
            binding.textEmail.setText(user.email);
            binding.imageProfile.setImageBitmap(getUserImage(user.image));
            binding.getRoot().setOnClickListener(v -> {userListeners.onUserClicked(user);
            });
        }
    }

    private Bitmap getUserImage(String encodedImage){
        byte[]bytes = Base64.decode(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);



    }
}
