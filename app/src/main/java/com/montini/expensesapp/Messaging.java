package com.montini.expensesapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Messaging extends AppCompatActivity {

    public static boolean isChatWindowOpen;

    String id, email, name, projName, newChatsId;
    boolean chatOn;

    TextView heading;
    EditText msgTxt;
    ListView chatList;
    List<HelperClass3items> msgs;
    ArrayAdapter<HelperClass3items> adapter;

    FirebaseAuth auth;
    FirebaseUser user;

    DatabaseReference chatRef;
    ValueEventListener chatListner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        Intent i= getIntent();////.... incase you have 2 intents coming here... if(i.hasExtra("a particualr VariableName"))
        id = i.getStringExtra("id");// id of 2nd person
        projName = i.getStringExtra("proj");
        email = i.getStringExtra("email");// email of 2nd person

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();


        chatList = findViewById(R.id.listViewMsgs);
        heading = findViewById(R.id.textView2);
        msgTxt = findViewById(R.id.editText2);

        heading.setText(email);
        msgs = new ArrayList<HelperClass3items>();

        //keep chat ref ready
        chatRef = FirebaseDatabase.getInstance().getReference("ExpenseApp/Projects/" + projName + "/Chats");


        DatabaseReference chatExistRef = FirebaseDatabase.getInstance().getReference("ExpenseApp/Projects/" + projName + "/Team/" + user.getUid() + "/Chats/" + id);
        chatExistRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                     if(dataSnapshot.exists()){
                         //load chat msgs
                         newChatsId = dataSnapshot.child("mainItem").getValue(String.class);
                         loadChat();
                     }
                     else{
                         //start a new chat
                         startNewChat();
                     }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

    }

    private void loadChat() {
        chatListner = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                msgs.clear();
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    Log.e("===x===", child.getKey() +": " + child.getValue());
                    HelperClass3items obj = child.getValue(HelperClass3items.class);
                    msgs.add(obj);
                }
                Log.e("xxxxxxxxxx", msgs.toString());
                adapter = new DisplayItemsAdapter(Messaging.this, msgs);
                chatList.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        chatRef.child(newChatsId).addValueEventListener(chatListner);
    }


    public void startNewChat(){
        Log.e("======", "new chat begins");
        //first make a newChat node and get its ID

            HelperClass3items obj = new HelperClass3items(user.getEmail(), email,"any xtra info");
            msgs.add(obj);
            newChatsId = chatRef.push().getKey();
            chatRef.child(newChatsId).child("0000").setValue(obj);
            loadChat();

            //save Ref of NewChat to each usersId under the Teams Node
            DatabaseReference MemberRef1 = FirebaseDatabase.getInstance().getReference("ExpenseApp/Projects/" + projName + "/Team/" + user.getUid() + "/Chats/" + id);
            HelperClass3items obj4Member1 = new HelperClass3items(newChatsId,email,null);
            MemberRef1.setValue(obj4Member1);
            DatabaseReference MemberRef2 = FirebaseDatabase.getInstance().getReference("ExpenseApp/Projects/" + projName + "/Team/" + id + "/Chats/" + user.getUid());
            HelperClass3items obj4Member2 = new HelperClass3items(newChatsId,user.getEmail(),null);
            MemberRef2.setValue(obj4Member2);

    }




    public void send(View v) {
        String str = msgTxt.getText().toString();
        HelperClass3items obj = new HelperClass3items(user.getEmail(),str,email);

        String newMsgId = chatRef.push().getKey();
        chatRef.child(newChatsId).child(newMsgId).setValue(obj);
    }


    @Override
    protected void onResume() {
        super.onResume();
        isChatWindowOpen=true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isChatWindowOpen=false;
    }

}
