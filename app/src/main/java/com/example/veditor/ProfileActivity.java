package com.example.veditor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    Button btn_logout;
    FirebaseAuth firebaseAuth;
   // ImageView ivimage;
   // TextView tvname;
    GoogleSignInClient googleSignInClient;

    FloatingActionButton addvideobtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setTitle("Videos");

        btn_logout = findViewById(R.id.btn_logout);
       // ivimage = findViewById(R.id.iv_image);
       // tvname = findViewById(R.id.tv_name);
        addvideobtn = findViewById(R.id.addVideo);

        addvideobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, AddVideoActivity.class));

            }
        });



        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
     //       if (firebaseUser != null){
     //         Glide.with(Profile.this).load(firebaseUser.getPhotoUrl())
     //               .into(ivimage);
        //      tvname.setText(firebaseUser.getDisplayName());
      //  }

        googleSignInClient = GoogleSignIn.getClient(ProfileActivity.this, GoogleSignInOptions.DEFAULT_SIGN_IN);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            firebaseAuth.signOut();
                            Toast.makeText(getApplicationContext(),"Logout Successfully",Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
            }
        });
    }
}