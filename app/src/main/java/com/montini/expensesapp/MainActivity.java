package com.montini.expensesapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class MainActivity extends AppCompatActivity {

    TextView status;
    private EditText editTextEmail, editTextPassword, editTextName;
    private Button btnLogin;
    FirebaseAuth auth;
    FirebaseUser FbUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        status = findViewById(R.id.textView);
        editTextEmail =  findViewById(R.id.editTextEmail);
        editTextPassword =  findViewById(R.id.editTextPassword);
        editTextName =  findViewById(R.id.editTextName);
        btnLogin =  findViewById(R.id.btnLogin);
        auth = FirebaseAuth.getInstance();
        FbUser = auth.getCurrentUser();
        if(FbUser!=null){
            Intent i = new Intent(this, Main2Activity.class);
            startActivity(i);
            finish();
        }
        else {
            status.setText("NULL USER");
        }
    }
    public void Login(View v) {
        final String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Log-In Successful.", Toast.LENGTH_SHORT).show();
                            FbUser = auth.getCurrentUser();
                            AddToUsers();
                        }
                        else {
                            Toast.makeText(MainActivity.this, "Sign-In Error", Toast.LENGTH_LONG).show();
                            status.setText(task.getException().getMessage());
                        }
                    }
                });
    }
    private void AddToUsers() {
        final String name = editTextName.getText().toString();
        final DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("ExpenseApp/Users/" + FbUser.getUid());
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String deviceTokenId = instanceIdResult.getToken();
                AppUsers appUsersObj = new AppUsers(name, deviceTokenId, FbUser.getEmail());
                dbref.setValue(appUsersObj);
            }
        });
       /*FirebaseMessaging.getInstance().subscribeToTopic("Incomes");
       FirebaseMessaging.getInstance().subscribeToTopic("Expenses");
       FirebaseMessaging.getInstance().subscribeToTopic("GroupMsg");
       FirebaseMessaging.getInstance().subscribeToTopic("PersonalMsg");*/
    }
}