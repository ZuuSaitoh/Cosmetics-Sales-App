package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.example.myapplication.R;
import androidx.fragment.app.Fragment;

public class AccountFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        Button btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();
            // TODO: thêm code chuyển về trang đăng nhập nếu có
        });

        return view;
    }
}
