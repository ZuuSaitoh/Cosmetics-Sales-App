package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.model.User;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.UserService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends Fragment {
    private TextView tvFullName, tvPhone;
    private LinearLayout layoutLogout, layoutProfileHeader;
    private String userId = null; // sẽ lấy từ SharedPreferences

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_account, container, false);

        tvFullName = v.findViewById(R.id.tvFullName);
        tvPhone = v.findViewById(R.id.tvPhone);
        layoutLogout = v.findViewById(R.id.layoutLogout);
        layoutProfileHeader = v.findViewById(R.id.layoutProfileHeader);

        // ✅ Lấy userID từ SharedPreferences (được lưu khi login)
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userId = prefs.getString("userID", null);
        String username = prefs.getString("username", "Người dùng");

        // Hiển thị tạm tên user nếu chưa gọi API
        tvFullName.setText(username);

        // ✅ Gọi API để load thêm thông tin user (email, phone,...)
        if (userId != null && !userId.isEmpty()) {
            try {
                int userIdInt = Integer.parseInt(userId);
                loadUserInfo(userIdInt);
            } catch (NumberFormatException e) {
                Log.e("AccountFragment", "userID không phải là số: " + userId);
                Toast.makeText(requireContext(), "Lỗi định dạng ID người dùng!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
        }

        // Khi click vào header → chuyển sang trang chỉnh sửa hồ sơ
        layoutProfileHeader.setOnClickListener(vv -> {
            Intent i = new Intent(requireContext(), CustomerProfileActivity.class);
            startActivity(i);
        });

        // Đăng xuất
        layoutLogout.setOnClickListener(vv -> {
            prefs.edit().clear().apply();
            Toast.makeText(requireContext(), "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(requireContext(), LoginScreen.class));
            requireActivity().finish();
        });

        return v;
    }

    private void loadUserInfo(int userId) {
        UserService api = ApiClient.getRetrofit(requireContext()).create(UserService.class);
        api.getUserById(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> res) {
                if (res.isSuccessful() && res.body() != null) {
                    User user = res.body();
                    tvFullName.setText(user.getUsername());
                    tvPhone.setText(user.getPhoneNumber());
                } else {
                    Log.e("AccountFragment", "Không thể load thông tin user!");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("AccountFragment", "Lỗi API: " + t.getMessage());
            }
        });
    }
}
