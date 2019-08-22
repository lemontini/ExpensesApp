package com.montini.expensesapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Main2Activity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    Button btnProj, btnUser;
    EditText projName, userEmail, cashAmt, expenseAmt, expenseListItem;
    Boolean isAdmin = false;
    Spinner expensetype;
    DatabaseReference expenseListRef;
    ValueEventListener expense_listener;
    List<String> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        btnProj = findViewById(R.id.button);
        btnUser = findViewById(R.id.button3);
        projName = findViewById(R.id.editText);
        userEmail = findViewById(R.id.editText3);
        cashAmt = findViewById(R.id.editText4);
        expenseAmt = findViewById(R.id.editText5);
        expenseListItem = findViewById(R.id.editTextexpList);
        expensetype = findViewById(R.id.spinner);
        list = new ArrayList<String>();
        expensetype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e("LIST", "Selected: " + list.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void AddProject(View v) {
        final String project = projName.getText().toString();
        final DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("ExpenseApp/Projects/" + project + "/Team");
        dbref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    dbref.child(user.getUid()).child("email").setValue(user.getEmail());
                    dbref.child(user.getUid()).child("exp").setValue(0);
                    dbref.child(user.getUid()).child("inc").setValue(0);
                    dbref.child(user.getUid()).child("admin").setValue(true);

                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("ExpenseApp/Users/" + user.getUid() + "/MyProjects/" + project);
                    userRef.child("ref").setValue("ExpenseApp/Projects/" + project);

                    DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference("ExpenseApp/Projects/" + project + "/Status");
                    statusRef.child("IncomeTotal").setValue(0);
                    statusRef.child("ExpenseTotal").setValue(0);
                    statusRef.child("Profit").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void AddUser(View v) {
        if (!isAdmin) {
            Toast.makeText(Main2Activity.this, "Not Admins", Toast.LENGTH_SHORT).show();
            return;
        }

        final String project = projName.getText().toString();
        final String addEmail = userEmail.getText().toString();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("ExpenseApp/Users");
        final Query myQuery = userRef.orderByChild("email").equalTo(addEmail);

        final ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e("XXXXXXXX", dataSnapshot.toString());
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Log.e("UserAdd", ds.getKey() + "|" + ds.child("email").getValue());

                        String forKey = ds.getKey();

                        DatabaseReference add2TeamRef = FirebaseDatabase.getInstance().getReference("ExpenseApp/Projects/" + project + "/Team");
                        add2TeamRef.child(forKey).child("email").setValue(addEmail);
                        add2TeamRef.child(forKey).child("exp").setValue(0);
                        add2TeamRef.child(forKey).child("inc").setValue(0);
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("ExpenseApp/Users/" + forKey + "/OtherProjects/" + project);
                        userRef.child("ref").setValue("ExpenseApp/Projects/" + project);
                        break; // exit for loop,since we dont need anything else
                    }
                } else {
                    Toast.makeText(Main2Activity.this, "NO User", Toast.LENGTH_SHORT).show();
                }
                myQuery.removeEventListener(this);//and the listener is removed too!
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        myQuery.addValueEventListener(listener);//add listener
    }

    //tripple effect
    // 1...make entry in Incomes
    // 2...make entry in personal node in TEAM
    // 3...update status
    public void putCash(View v) {
        String project = projName.getText().toString();
        final int amt = Integer.valueOf(cashAmt.getText().toString());
        final DatabaseReference putCashRef = FirebaseDatabase.getInstance().getReference("ExpenseApp/Projects/" + project + "/Incomes");
        String id = putCashRef.push().getKey();
        putCashRef.child(id).child("email").setValue(user.getEmail());
        putCashRef.child(id).child("amount").setValue(amt);

        final DatabaseReference putTeamRef = FirebaseDatabase.getInstance().getReference("ExpenseApp/Projects/" + project + "/Team/" + user.getUid());
        putTeamRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer inc = dataSnapshot.child("inc").getValue(Integer.class);
                inc = inc + amt;

                putTeamRef.child("inc").setValue(inc);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        final DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference("ExpenseApp/Projects/" + project + "/Status");
        statusRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer inc = dataSnapshot.child("IncomeTotal").getValue(Integer.class);
                inc = inc + amt;
                statusRef.child("IncomeTotal").setValue(inc);
                Integer p = dataSnapshot.child("Profit").getValue(Integer.class);
                p = p + amt;
                statusRef.child("Profit").setValue(p);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //tripple effect
    public void putExpense(View v) {
        String project = projName.getText().toString();
        final int amt = Integer.valueOf(expenseAmt.getText().toString());
        String exp_type = expensetype.getSelectedItem().toString();
        Log.e("ExpenseTYPE", exp_type);
        DatabaseReference expenseRef = FirebaseDatabase.getInstance().getReference("ExpenseApp/Projects/" + project + "/Expenses");
        String id = expenseRef.push().getKey();
        expenseRef.child(id).child("email").setValue(user.getEmail());
        expenseRef.child(id).child("amount").setValue(amt);
        expenseRef.child(id).child("type").setValue(exp_type);
        final DatabaseReference putTeamRef = FirebaseDatabase.getInstance().getReference("ExpenseApp/Projects/" + project + "/Team/" + user.getUid());
        putTeamRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer exp = dataSnapshot.child("exp").getValue(Integer.class);
                exp = exp + amt;
                putTeamRef.child("exp").setValue(exp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        final DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference("ExpenseApp/Projects/" + project + "/Status");
        statusRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer exp = dataSnapshot.child("ExpenseTotal").getValue(Integer.class);
                exp = exp + amt;
                statusRef.child("ExpenseTotal").setValue(exp);
                Integer p = dataSnapshot.child("Profit").getValue(Integer.class);
                p = p - amt;
                statusRef.child("Profit").setValue(p);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void AddItemToList(View v) {
        if (!isAdmin) {
            Toast.makeText(Main2Activity.this, "Not Admins", Toast.LENGTH_SHORT).show();
            return;
        }
        String project = projName.getText().toString();
        String item = expenseListItem.getText().toString();
        DatabaseReference expenseListItemsRef = FirebaseDatabase.getInstance().getReference("ExpenseApp/Projects/" + project + "/ExpenseList");
        expenseListItemsRef.child(item).setValue(item);
    }

    public void SetProjectName(View v) {
        final String project = projName.getText().toString();
        //=====================check if user is in the TEAM,,,,,check if Admin..
        Log.e("-------", "0000000000");
        DatabaseReference aminCheckRef = FirebaseDatabase.getInstance().getReference("ExpenseApp/Projects/" + project + "/Team/" + user.getUid());
        aminCheckRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    getItemsForSpinner(project);
                    if (dataSnapshot.child("admin").exists()) {
                        isAdmin = dataSnapshot.child("admin").getValue(Boolean.class);//true
                    }
                    Log.e("-------", "YES in Team... and Admin: " + isAdmin);
                } else {
                    Toast.makeText(Main2Activity.this, "Not AUTHORIZED", Toast.LENGTH_SHORT).show();
                    Log.e("-------", "No");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void getItemsForSpinner(String project) {
        //-------------------------get Expenses List listener ready
        expenseListRef = FirebaseDatabase.getInstance().getReference("ExpenseApp/Projects/" + project + "/ExpenseList");
        expense_listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list.clear();
                list.add("Expense Type");
                if (dataSnapshot.exists()) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        String projName = child.getKey();
                        list.add(projName);
                        Log.e("onStart()", "Child: " + projName);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(Main2Activity.this, android.R.layout.simple_list_item_1, android.R.id.text1, list);
                expensetype.setAdapter(adapter);
                Log.e("-------", "list list list");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        expenseListRef.addValueEventListener(expense_listener);
    }

    public void Chatting(View v) {
        String proj = projName.getText().toString();
        Intent i = new Intent(Main2Activity.this, Team.class);
        i.putExtra("proj", proj);
        startActivity(i);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (expenseListRef != null) {
            expenseListRef.removeEventListener(expense_listener);
        }
    }
}