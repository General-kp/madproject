package com.example.madlabproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowId;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class user_panel extends AppCompatActivity implements selectlistener{
    ArrayList<productmodel> productmodels=new ArrayList<>();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    Button search;
    EditText searchbar;
    ArrayList<productmodel> searchResults;
    CollectionReference collectionRef = firestore.collection("products");
    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();
    CollectionReference collectionRef1 = firestore.collection("users/"+currentUser.getEmail()+"/products");
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

    }
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.aitem) {
            // Handle search action
            Toast.makeText(user_panel.this, "cart clicked", Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(getApplicationContext(), cart.class);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.ditem) {
            Intent intent=new Intent(getApplicationContext(), user_panel.class);
            startActivity(intent);
            finish();
            Toast.makeText(user_panel.this, "search clicked", Toast.LENGTH_SHORT).show();
            // Handle settings action
            return true;
        }else if (id == R.id.logout) {
            Toast.makeText(user_panel.this, "logout clicked", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();
            Intent intent=new Intent(getApplicationContext(), choose.class);
            startActivity(intent);
            finish();
            // Handle settings action
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#22C7A9"));
        assert actionBar != null;
        actionBar.setTitle("Foodie Finds");
        actionBar.setBackgroundDrawable(colorDrawable);
        setContentView(R.layout.activity_user_panel);
        search=findViewById(R.id.search);
        searchbar=findViewById(R.id.searchbar);
        RecyclerView recyclerview=findViewById(R.id.recyclerView3);
        setupproductmodels();
//        recyclerview.bringToFront();
        pc_adapter adapter=new pc_adapter(this,productmodels,"",productmodels,this);
        recyclerview.postDelayed(new Runnable() {
            @Override
            public void run() {

                recyclerview.setAdapter(adapter);

            }
        },1000);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        collectionRef1.document("product0")
                .set(new HashMap<>())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Empty document created successfully
                        //Toast.makeText(user_panel.this, "Empty document created", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to create empty document
                        //Toast.makeText(user_panel.this, "Failed to create empty document", Toast.LENGTH_SHORT).show();
                    }
                });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchText = searchbar.getText().toString().trim();
                if (!searchText.isEmpty()) {

                        searchResults = new ArrayList<>();
                        for (productmodel item : productmodels) {
                            if (item.getTitle().toLowerCase().equals(searchText.toLowerCase())) {
                                searchResults.add(item);
                            }
                        }
                    // Update the adapter with search results
                    adapter.setItems(searchResults);
                    adapter.notifyDataSetChanged();
                } else {
                    // Clear the search results and show all items
//                    adapter.setItems(productmodels);
//                    adapter.notifyDataSetChanged();
                    searchbar.setText("");
                }
            }
        });
        searchbar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    adapter.setItems(productmodels);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {

                    RecyclerView recyclerView = findViewById(R.id.recyclerView3);
                    setupproductmodels();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchbar.getWindowToken(), 0);
                    if (recyclerView.getLayoutManager() != null && recyclerView.getLayoutManager().getChildCount() > productmodels.size()) {
                        adapter.setItems(productmodels);
                    }
                    adapter.clean(searchResults);
                    adapter.retrieveItems(productmodels);
                    adapter.notifyDataSetChanged();
                    if (recyclerView.getLayoutManager() != null && recyclerView.getLayoutManager().getChildCount() > 0) {
                        searchbar.clearFocus();
                    }
                }
            }
        });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    private void setupproductmodels(){
        String []title = new String[100];
        String []desc = new String[100];
        String []cost = new String[100];
        String []count = new String[100];
        String []imagename = new String[100];
        collectionRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();

                            if (querySnapshot != null) {
                                List<DocumentSnapshot> documents = querySnapshot.getDocuments();

                                for (DocumentSnapshot document : documents) {
                                    int i=0;
                                    // Access the field values for each document
                                    title[i] = document.getString("title");
                                    desc[i] = document.getString("description");
                                    cost[i] = document.getString("cost");
                                    count[i] = document.getString("count");
                                    imagename[i]=title[i]+cost[i];
                                    productmodels.add(new productmodel(title[i],desc[i],cost[i],count[i],imagename[i]));
                                    i++;
                                }
                            }
                        }
                    }
                });

    }

    @Override
    public void onItemClicked(productmodel productmodel1) {
        Toast.makeText(user_panel.this, productmodel1.getTitle(), Toast.LENGTH_SHORT).show();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = firestore.collection("users/"+ currentUser.getEmail().toString()+"/products");
        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();

                    if (querySnapshot != null) {
                        int documentcount = (querySnapshot.size() + 2);
                        DocumentReference docref = firestore.collection("users").document(currentUser.getEmail().toString()).collection("products").document();
                        Map<String, Object> data = new HashMap<>();
                        data.put("title", productmodel1.getTitle());
                        data.put("description", productmodel1.getDescription());
                        data.put("cost", productmodel1.getCost());
                        data.put("count", productmodel1.getCount());
                        data.put("imagename",productmodel1.getimagename());
                        docref.set(data)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(user_panel.this, "Product added",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(user_panel.this, "Product addition failed",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
                }
            });
        }
}