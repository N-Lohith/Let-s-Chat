package com.example.c.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.c.databinding.ActivitySignUpBinding;
import com.example.c.utilites.Constants;
import com.example.c.utilites.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;
    private String encodedImage ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager=new PreferenceManager(getApplicationContext());

        setlistners();
    }
    private  void setlistners(){
        binding.textsignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),SignInActivity.class));
            }
        });
        binding.ButtonSignUp.setOnClickListener(v -> {
            if(isValidDetials()){
                signUp();
            }
        });
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }
    private void showToast(String messsge){
        Toast.makeText(this, messsge, Toast.LENGTH_SHORT).show();
    }
    private  void signUp(){
        loading(true);
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        HashMap<String,Object> user=new HashMap<>();
        user.put(Constants.KEY_NAME,binding.inputName.getText().toString());
        user.put(Constants.KEY_EMAIL,binding.inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD,binding.inputPassword.getText().toString());
        user.put(Constants.KEY_IMAGE,encodedImage);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                    preferenceManager.putString(Constants.KEY_USER,documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME,binding.inputName.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE,encodedImage);
                    Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);


                })
                .addOnFailureListener(exception ->{
                    loading(false);
                    showToast(exception.getMessage());

                });


    }
    private  String encodeImage(Bitmap bitmap){
        int previewWidth=150;
        int previewHeight=bitmap.getHeight()*previewWidth/bitmap.getWidth();
        Bitmap previewbitmap=Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        previewbitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[]bytes=byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);


    }
    private final ActivityResultLauncher<Intent>pickImage=registerForActivityResult( new ActivityResultContracts.StartActivityForResult(),
            result -> {
        if(result.getResultCode()==RESULT_OK){
            if(result.getData()!=null){
                Uri imageuri=result.getData().getData();
                try{
                    InputStream inputStreamReader=getContentResolver().openInputStream(imageuri);
                    Bitmap bitmap= BitmapFactory.decodeStream(inputStreamReader);
                    binding.imageProfile.setImageBitmap(bitmap);
                    binding.textaddimage.setVisibility(View.GONE);
                    encodedImage=encodeImage(bitmap);

                }catch ( Exception e){
                    e.printStackTrace();

                }
            }
        }
            });
    private boolean isValidDetials() {
        if (encodedImage == null) {
            showToast("Select Profile Picture");
            return false;
        } else if (binding.inputName.getText().toString().trim().isEmpty()) {
            showToast("Enter Name");
            return false;
        } else if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter Email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Enter Only EmailAddress");
            return false;

        }

        else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter Password");
            return false;
        } else if (binding.inputconforimPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter Conforim Password");
            return false;
        } else if (!binding.inputPassword.getText().toString().equals(binding.inputconforimPassword.getText().toString())) {
            showToast("Password and Conforim Password must be same");
            return false;
        } else {
            return true;
        }

    }
    private void loading (Boolean isloading){
        if(isloading){
            binding.ButtonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else{
            binding.ButtonSignUp.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }



}