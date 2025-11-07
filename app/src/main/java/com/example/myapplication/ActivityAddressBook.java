package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;
// removed insets handling

import com.example.myapplication.adapter.AddressBookAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.example.myapplication.auth.AuthManager;
import com.example.myapplication.model.User;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.UserService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Temporary Address Book screen without backend API.
 * Stores a simple list of addresses in SharedPreferences as JSON.
 */
public class ActivityAddressBook extends AppCompatActivity {

    private static final String PREFS_NAME = "address_book_prefs";
    private static final String KEY_ADDRESSES = "addresses";

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private AddressBookAdapter adapter;
    private final List<AddressEntry> addresses = new ArrayList<>();
    private AuthManager authManager;
    private UserService userService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Do not enable edge-to-edge here to avoid status bar overlap
        setContentView(R.layout.activity_address_book);
        // Status bar color to match app theme
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.cosmetic_primary_dark));

        initViews();
        setupRecycler();
        loadFromPrefs();
        authManager = new AuthManager(this);
        userService = ApiClient.getRetrofit(this).create(UserService.class);
        if (addresses.isEmpty()) {
            tryPrefillFromUser();
        }
        bindEvents();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_addresses);
        fabAdd = findViewById(R.id.fab_add_address);
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    // no status bar insets adjustments needed

    private void setupRecycler() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AddressBookAdapter(addresses, new AddressBookAdapter.AddressActionListener() {
            @Override
            public void onDelete(int position) {
                addresses.remove(position);
                adapter.notifyItemRemoved(position);
                saveToPrefs();
            }

            @Override
            public void onSetDefault(int position) {
                for (int i = 0; i < addresses.size(); i++) {
                    addresses.get(i).isDefault = (i == position);
                }
                adapter.notifyDataSetChanged();
                saveToPrefs();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void bindEvents() {
        fabAdd.setOnClickListener(v -> showAddDialog());
    }

    private void showAddDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_address, null);
        EditText edtName = dialogView.findViewById(R.id.edt_name);
        EditText edtPhone = dialogView.findViewById(R.id.edt_phone);
        EditText edtAddress = dialogView.findViewById(R.id.edt_address);

        new AlertDialog.Builder(this)
                .setTitle("Thêm địa chỉ")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = edtName.getText().toString().trim();
                    String phone = edtPhone.getText().toString().trim();
                    String addr = edtAddress.getText().toString().trim();
                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(addr)) {
                        Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    AddressEntry entry = new AddressEntry(name, phone, addr, addresses.isEmpty());
                    addresses.add(entry);
                    adapter.notifyItemInserted(addresses.size() - 1);
                    saveToPrefs();
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void loadFromPrefs() {
        addresses.clear();
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = sp.getString(KEY_ADDRESSES, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                AddressEntry e = new AddressEntry(
                        o.optString("name"),
                        o.optString("phone"),
                        o.optString("address"),
                        o.optBoolean("isDefault", false)
                );
                addresses.add(e);
            }
        } catch (JSONException e) {
            // ignore malformed data and reset
        }
        // Provide a friendly empty state example if nothing exists
        if (addresses.isEmpty()) {
            // no default data to keep it clean
        }
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void saveToPrefs() {
        JSONArray arr = new JSONArray();
        for (AddressEntry e : addresses) {
            JSONObject o = new JSONObject();
            try {
                o.put("name", e.name);
                o.put("phone", e.phone);
                o.put("address", e.address);
                o.put("isDefault", e.isDefault);
                arr.put(o);
            } catch (JSONException ex) {
                // ignore
            }
        }
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_ADDRESSES, arr.toString()).apply();
    }

    private void tryPrefillFromUser() {
        Long userId = authManager.refreshUserId();
        if (userId == null) return;
        Call<User> call = userService.getUserById(userId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User u = response.body();
                    if (u.getAddress() != null && !u.getAddress().trim().isEmpty()) {
                        AddressEntry e = new AddressEntry(
                                u.getUsername() != null ? u.getUsername() : "Người nhận",
                                u.getPhoneNumber() != null ? u.getPhoneNumber() : "",
                                u.getAddress(),
                                true
                        );
                        addresses.add(e);
                        adapter.notifyDataSetChanged();
                        saveToPrefs();
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // ignore if offline; user can add manually
            }
        });
    }

    public static class AddressEntry {
        public String name;
        public String phone;
        public String address;
        public boolean isDefault;

        public AddressEntry(String name, String phone, String address, boolean isDefault) {
            this.name = name;
            this.phone = phone;
            this.address = address;
            this.isDefault = isDefault;
        }
    }
}


