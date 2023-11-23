package com.example.imageuploadwithdatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button uploadBtn;
    private Uri imageUri;
    private ImageView img;
    private TextInputEditText name, phone;

    private StorageReference storageReference;

    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    // method for initialization of widgets and variables
    private void init() {

        uploadBtn = findViewById(R.id.uploadBtn);
        uploadBtn.setOnClickListener(this);

        img = findViewById(R.id.img);
        img.setOnClickListener(this);

        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);

        reference = FirebaseDatabase.getInstance().getReference("person");
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img:
                choosePicture();
                break;
            case R.id.uploadBtn:

                User user = new User();
                user.setName(name.getText().toString().trim());
                user.setPhone(phone.getText().toString().trim());

                if (isValid(user)) {
                    uploadImage(user);
                }
                break;
        }
    }

    //method for choosing image from gallery
    private void choosePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            img.setImageURI(imageUri);

        }
    }

    //method for upload image to the storage
    private void uploadImage(User user) {
        StorageReference strRef = storageReference.child("image/" + UUID.randomUUID().toString());
        strRef
                .putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(MainActivity.this, "image uploaded", Toast.LENGTH_SHORT).show();
                        strRef
                                .getDownloadUrl()
                                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        user.setImageUrl(task.getResult().toString());
                                        addToDb(user);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // method for sending data into database
    private void addToDb(User user) {
        reference
                .child(user.getName())
                .setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "data uploaded successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // validation method for validation of data
    private boolean isValid(User user) {

        boolean valid = true;

        if (imageUri == null) {
            Toast.makeText(MainActivity.this, "plzz upload image", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        if (user.getName().length() < 3) {
            name.setError("enter valid name");
            valid = false;
        }
        if (user.getPhone().length() < 11 || user.getPhone().length() > 11) {
            phone.setError("enter valid phone no");
            valid = false;
        }

        return valid;
    }
}