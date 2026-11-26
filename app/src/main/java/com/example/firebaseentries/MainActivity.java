package com.example.firebaseentries;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnSubmit, btnView;
    private ListView listView;

    private ArrayAdapter<String> adapter;
    private final ArrayList<String> rows = new ArrayList<>();

    private DatabaseReference entriesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // keep the layout out of the status bar / navigation bar
        final float d = getResources().getDisplayMetrics().density;
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    (int) (24 * d) + bars.left,
                    (int) (16 * d) + bars.top,
                    (int) (24 * d) + bars.right,
                    bars.bottom);
            return insets;
        });

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnView = findViewById(R.id.btnView);
        listView = findViewById(R.id.listView);

        adapter = new ArrayAdapter<>(this, R.layout.list_item, rows);
        listView.setAdapter(adapter);

        entriesRef = FirebaseDatabase.getInstance().getReference("entries");

        btnSubmit.setOnClickListener(v -> saveEntry());
        btnView.setOnClickListener(v -> loadEntries());
    }

    private void saveEntry() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> record = new HashMap<>();
        record.put("username", username);
        record.put("password", password);

        entriesRef.push().setValue(record)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Saved successfully", Toast.LENGTH_SHORT).show();
                    etUsername.setText("");
                    etPassword.setText("");
                    etUsername.requestFocus();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void loadEntries() {
        entriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                rows.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    String u = child.child("username").getValue(String.class);
                    String p = child.child("password").getValue(String.class);
                    if (u != null && p != null) {
                        rows.add(u + " - " + p);
                    }
                }

                adapter.notifyDataSetChanged();

                if (rows.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No records found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this,
                        "Failed to load: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}