package com.montini.expensesapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Team extends AppCompatActivity {

    String projName;
    ListView teamList;
    List<HelperClass3items> infoList;
    ArrayAdapter<HelperClass3items> adapter;
    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team);

        Intent i= getIntent();
        projName = i.getStringExtra("proj");
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        infoList = new ArrayList<HelperClass3items>();
        teamList = findViewById(R.id.listTeam);
        getTeamMembers(projName);
    }
    private void getTeamMembers(String projName) {
        DatabaseReference teamRef = FirebaseDatabase.getInstance().getReference("ExpenseApp/Projects/" + projName + "/Team");
        teamRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Log.e("======", ds.toString());
                    String key = ds.getKey();
                    String email = ds.child("email").getValue(String.class);
                    HelperClass3items obj = new HelperClass3items(key,email,null);
                    infoList.add(obj);
                }
                Log.e("xxxxxxxxxx", infoList.toString());
                adapter = new DisplayItemsAdapter(Team.this, infoList);
                teamList.setAdapter(adapter);
                setClickListener();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    public void setClickListener() {
        Log.e("LISTENER", "Setting up List CLICK Listener: ");
        teamList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String uid = infoList.get(position).getMainItem();//this is either the members Uid or the ref of the chat
                String email = infoList.get(position).getNextItem();
                Log.e("selection", "Selected: " + uid);
                Intent i = new Intent(Team.this,Messaging.class);
                i.putExtra("proj",projName);
                i.putExtra("id",uid);
                i.putExtra("email", email);
                startActivity(i);
            }
        });
    }
}