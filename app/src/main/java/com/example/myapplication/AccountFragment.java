package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Color;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.myapplication.R;
import androidx.fragment.app.Fragment;

public class AccountFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_account, container, false);

            LinearLayout layoutLogout = view.findViewById(R.id.layoutLogout);
            TextView tvFullName = view.findViewById(R.id.tvFullName);
            TextView tvPhone = view.findViewById(R.id.tvPhone);

            // Fetch user profile
            try {
                com.example.myapplication.auth.AuthManager authManager = new com.example.myapplication.auth.AuthManager(requireContext());
                String userId = authManager.getUserId();
                if (userId != null && !userId.isEmpty()) {
                    com.example.myapplication.network.UserService userService = com.example.myapplication.network.ApiClient.getRetrofit(requireContext()).create(com.example.myapplication.network.UserService.class);
                    userService.getUserById(userId).enqueue(new retrofit2.Callback<com.example.myapplication.network.dto.User>() {
                        @Override
                        public void onResponse(retrofit2.Call<com.example.myapplication.network.dto.User> call, retrofit2.Response<com.example.myapplication.network.dto.User> response) {
                            if (!isAdded()) return;
                            if (response.isSuccessful() && response.body() != null) {
                                com.example.myapplication.network.dto.User u = response.body();
                                if (tvFullName != null && u.getFullName() != null) tvFullName.setText(u.getFullName());
                                if (tvPhone != null && u.getPhone() != null) tvPhone.setText(u.getPhone());
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<com.example.myapplication.network.dto.User> call, Throwable t) {
                            // ignore for now
                        }
                    });
                }
            } catch (Exception ignored) {}
            if (layoutLogout != null) {
                layoutLogout.setOnClickListener(v -> {
                    android.content.Context ctx = getContext();
                    if (ctx != null) {
                        Toast.makeText(ctx, "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();
                    }
                    // TODO: thêm code chuyển về trang đăng nhập nếu có
                });
            }

            return view;
        } catch (Exception e) {
            Log.e("AccountFragment", "Failed to inflate fragment_account", e);
            // Fallback simple view to avoid crash and make the issue visible
            android.content.Context ctx = inflater.getContext();
            LinearLayout fallback = new LinearLayout(ctx);
            fallback.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            fallback.setBackgroundColor(Color.WHITE);
            return fallback;
        }
    }
}
