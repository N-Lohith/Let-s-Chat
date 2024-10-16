package com.example.c.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;


import com.example.c.adaptor.UsersAdaptor;
import com.example.c.databinding.ActivityUsersBinding;
import com.example.c.listeners.UserListeners;
import com.example.c.models.User;
import com.example.c.utilites.Constants;
import com.example.c.utilites.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class UsersActivity extends BaseActivity implements UserListeners {
    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager=new PreferenceManager(getApplicationContext());
        getUsers();
        setListners();



    }
    private void setListners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());

    }
    private void getUsers(){
        loading(true);
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId=preferenceManager.getString(Constants.KEY_USER);
                    if(task.isSuccessful() && task.getResult()!=null){
                        List<User>users=new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                            if(currentUserId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            User user=new User();
                            user.name=queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email=queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image=queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token=queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id=queryDocumentSnapshot.getId();
                            //We are collecting the values from the database and copying them to the local variables of the user class
                            users.add(user);
                            // here we added the values to the arraylist

                        }
                        if(users.size()>0 ){
                            UsersAdaptor usersAdaptor=new UsersAdaptor(users,this);
                            binding.usersRecyclerView.setAdapter(usersAdaptor);
                            binding.usersRecyclerView.setVisibility(View.VISIBLE);

                        }
                        else{
                            showErrorMessage();
                        }
                        }else{
                        showErrorMessage();
                    }


                });
    }
    private void showErrorMessage(){
        binding.textErrorMessage.setText(String.format("%s","No Available Users "));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }
    private void loading (Boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else
        {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent=new Intent(getApplicationContext(),chatActivity.class);
        intent.putExtra(Constants.KEY_UUSERR,user);
        startActivity(intent);
        finish();
    }
}