package com.practise.eatitserver.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.practise.eatitserver.R;
import com.practise.eatitserver.model.User;
import com.practise.eatitserver.utils.Common;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

public class SignInActivity extends AppCompatActivity {

    private EditText emailET, passET;
    private Button singInButton;

    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        initialization();
    }

    private void initialization() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        mUserDR = firebaseDatabase.getReference("User");
        mAuth = FirebaseAuth.getInstance();
        singInButton = findViewById(R.id.signInSignButton);

        emailET = findViewById(R.id.signInEmailEditText);
        passET = findViewById(R.id.signInPassEditText);

        singInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userSignIn();
            }
        });


    }

    private void userSignIn() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Please Wait...");
        dialog.show();

        mAuth.signInWithEmailAndPassword(emailET.getText().toString(), passET.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    mUserDR.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.child(mAuth.getCurrentUser().getUid()).getValue(User.class);
                            dialog.dismiss();
                            if (user.isStaff()){
                                Common.currentUser = user;
                                finish();
                                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                DynamicToast.makeSuccess(getApplicationContext(), "SignIn Successful", Toast.LENGTH_SHORT).show();
                            } else {
                                DynamicToast.makeError(getApplicationContext(), "Your sccount is register as a user,not owner", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {
                    dialog.dismiss();
                    DynamicToast.makeError(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
}
