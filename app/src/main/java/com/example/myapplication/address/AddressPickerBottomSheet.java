    package com.example.myapplication.address;

    import android.content.Context;
    import android.os.Bundle;
    import android.text.TextUtils;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.EditText;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.annotation.Nullable;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;

    import com.example.myapplication.R;
    import com.example.myapplication.network.dto.UserAddressRequest;
    import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
    import com.google.android.material.button.MaterialButton;
    import com.google.android.material.floatingactionbutton.FloatingActionButton;

    import java.util.ArrayList;
    import java.util.List;

    public class AddressPickerBottomSheet extends BottomSheetDialogFragment {

        public interface OnAddressPickedListener {
            void onAddressPicked(AddressEntry entry);
        }

    private RecyclerView recycler;
    private MaterialButton btnUse;
    private FloatingActionButton fabAdd;
    private android.view.View layoutEmptyState;
    private AddressSelectAdapter adapter;
    private final List<AddressEntry> addresses = new ArrayList<>();
    private int tempSelectedIndex = -1;
    private OnAddressPickedListener listener;
    private com.example.myapplication.network.UserAddressService addressService;
    private com.example.myapplication.auth.AuthManager authManager;

        @Override
        public void onAttach(@NonNull Context context) {
            super.onAttach(context);
            if (getParentFragment() instanceof OnAddressPickedListener) {
                listener = (OnAddressPickedListener) getParentFragment();
            } else if (getActivity() instanceof OnAddressPickedListener) {
                listener = (OnAddressPickedListener) getActivity();
            }
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.bottomsheet_address_picker, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
        recycler = view.findViewById(R.id.recycler_addresses);
        btnUse = view.findViewById(R.id.btn_use_address);
        fabAdd = view.findViewById(R.id.fab_add_address);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);

            recycler.setLayoutManager(new LinearLayoutManager(getContext()));

            // Init services
            addressService = com.example.myapplication.network.ApiClient.getRetrofit(requireContext()).create(com.example.myapplication.network.UserAddressService.class);
            authManager = new com.example.myapplication.auth.AuthManager(requireContext());

            // Load from API first; fallback to local storage
            loadAddressesFromApi();
            adapter = new AddressSelectAdapter(addresses, tempSelectedIndex, new AddressSelectAdapter.Listener() {
                @Override
                public void onSelect(int position) {
                    tempSelectedIndex = position;
                    adapter.setSelectedIndex(position);
                    updateUseButtonState();
                }

                @Override
                public void onSetDefault(int position) {
                    // Backend hiện chỉ lưu Address, không có cờ mặc định → xử lý local
                    for (int i = 0; i < addresses.size(); i++) addresses.get(i).isDefault = (i == position);
                    adapter.notifyDataSetChanged();
                    AddressStorage.save(requireContext(), addresses);
                }

                @Override
                public void onEdit(int position) {
                    showEditDialog(addresses.get(position), position);
                }

                @Override
                public void onDelete(int position) {
                    AddressEntry a = addresses.get(position);
                    if (a.id != null) {
                        addressService.delete(a.id).enqueue(new retrofit2.Callback<Void>() {
                            @Override public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                                loadAddressesFromApi();
                            }
                            @Override public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                                Toast.makeText(getContext(), "Không thể xóa địa chỉ", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    }
                    addresses.remove(position);
                    if (tempSelectedIndex == position) tempSelectedIndex = -1;
                    adapter.notifyDataSetChanged();
                    AddressStorage.save(requireContext(), addresses);
                    updateUseButtonState();
                    updateEmptyState();
                }
            });
            recycler.setAdapter(adapter);

            updateUseButtonState();
            btnUse.setOnClickListener(v -> {
                if (tempSelectedIndex >= 0 && tempSelectedIndex < addresses.size()) {
                    if (listener != null) listener.onAddressPicked(addresses.get(tempSelectedIndex));
                    dismiss();
                }
            });

            fabAdd.setOnClickListener(v -> showAddDialog());
        }

        private void loadAddressesFromApi() {
            Long userId = authManager.getUserId();
            if (userId == null) {
                // fallback local
                addresses.clear();
                addresses.addAll(AddressStorage.load(requireContext()));
                int defaultIndex = AddressStorage.findDefaultIndex(addresses);
                if (defaultIndex >= 0) tempSelectedIndex = defaultIndex;
                setupOrRefreshAdapter();
                return;
            }
            addressService.getAddresses(userId).enqueue(new retrofit2.Callback<com.example.myapplication.network.dto.ApiResponse<List<UserAddressRequest>>>() {
                @Override
                public void onResponse(
                        retrofit2.Call<com.example.myapplication.network.dto.ApiResponse<List<UserAddressRequest>>> call,
                        retrofit2.Response<com.example.myapplication.network.dto.ApiResponse<List<UserAddressRequest>>> response) {
                    addresses.clear();
                    if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                        // Load local để merge name/phone nếu API không có
                        List<AddressEntry> localAddresses = AddressStorage.load(requireContext());
                        
                        for (UserAddressRequest dto : response.body().getResult()) {
                            // Ưu tiên lấy name và phone từ local storage trước (để giữ lại dữ liệu đã có)
                            String name = null;
                            String phone = null;
                            boolean isDefault = false;
                            
                            // Tìm trong local storage theo ID để lấy name/phone và isDefault
                            if (dto.userAddressId != null) {
                                for (AddressEntry local : localAddresses) {
                                    if (local.id != null && local.id.equals(dto.userAddressId)) {
                                        name = local.name;
                                        phone = local.phone;
                                        isDefault = local.isDefault;
                                        break;
                                    }
                                }
                            }
                            
                            // Nếu local không có, mới lấy từ API
                            if ((name == null || name.trim().isEmpty()) && dto.recipientName != null && !dto.recipientName.trim().isEmpty()) {
                                name = dto.recipientName;
                            }
                            if ((phone == null || phone.trim().isEmpty()) && dto.phone != null && !dto.phone.trim().isEmpty()) {
                                phone = dto.phone;
                            }
                            
                            // Nếu vẫn không có, dùng giá trị mặc định
                            if (name == null || name.trim().isEmpty()) {
                                name = null; // Sẽ hiển thị "Người nhận" trong UI
                            }
                            
                            addresses.add(new AddressEntry(
                                    dto.userAddressId,
                                    name,
                                    phone,
                                    dto.address != null ? dto.address : "",
                                    isDefault
                            ));
                        }
                        
                        // Lưu lại để giữ name/phone cho lần sau
                        AddressStorage.save(requireContext(), addresses);
                    } else {
                        addresses.addAll(AddressStorage.load(requireContext()));
                    }

                    int defaultIndex = AddressStorage.findDefaultIndex(addresses);
                    if (defaultIndex >= 0) tempSelectedIndex = defaultIndex;
                    setupOrRefreshAdapter();
                }

                @Override
                public void onFailure(
                        retrofit2.Call<com.example.myapplication.network.dto.ApiResponse<List<UserAddressRequest>>> call,
                        Throwable t) {
                    addresses.clear();
                    addresses.addAll(AddressStorage.load(requireContext()));
                    int defaultIndex = AddressStorage.findDefaultIndex(addresses);
                    if (defaultIndex >= 0) tempSelectedIndex = defaultIndex;
                    setupOrRefreshAdapter();
                }
            });

        }

        // Removed old helpers for composing address parts; backend returns a single address field now.

    private void setupOrRefreshAdapter() {
        if (adapter == null) return; // created earlier, just refresh data binding states
        adapter.notifyDataSetChanged();
        updateUseButtonState();
        updateEmptyState();
    }
    
    private void updateEmptyState() {
        if (layoutEmptyState == null) return;
        if (addresses.isEmpty()) {
            layoutEmptyState.setVisibility(android.view.View.VISIBLE);
            recycler.setVisibility(android.view.View.GONE);
        } else {
            layoutEmptyState.setVisibility(android.view.View.GONE);
            recycler.setVisibility(android.view.View.VISIBLE);
        }
    }

        private void showAddDialog() {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_address_full, null);
            EditText edtName = dialogView.findViewById(R.id.edt_name);
            EditText edtPhone = dialogView.findViewById(R.id.edt_phone);
            EditText edtDetail = dialogView.findViewById(R.id.edt_detail);
            EditText edtWard = dialogView.findViewById(R.id.edt_ward);
            EditText edtDistrict = dialogView.findViewById(R.id.edt_district);
            EditText edtProvince = dialogView.findViewById(R.id.edt_province);
            android.widget.CheckBox cbDefault = dialogView.findViewById(R.id.cb_default);

            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Thêm địa chỉ")
                    .setView(dialogView)
                    .setPositiveButton("Lưu", (dialog, which) -> {
                        String name = edtName.getText().toString().trim();
                        String phone = edtPhone.getText().toString().trim();
                        String detail = edtDetail.getText().toString().trim();
                        String ward = edtWard.getText().toString().trim();
                        String district = edtDistrict.getText().toString().trim();
                        String province = edtProvince.getText().toString().trim();
                        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(detail)
                                || TextUtils.isEmpty(ward) || TextUtils.isEmpty(district) || TextUtils.isEmpty(province)) {
                            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String addr = detail + ", " + ward + ", " + district + ", " + province;
                        boolean makeDefault = cbDefault.isChecked() || addresses.isEmpty();
                        // Call API create
                        Long userId = authManager.getUserId();
                        UserAddressRequest req = new UserAddressRequest();
                        req.userId = userId;
                        req.recipientName = name;
                        req.phone = phone;
                        req.address = addr;
                        addressService.create(req).enqueue(new retrofit2.Callback<com.example.myapplication.network.dto.ApiResponse<UserAddressRequest>>() {
                            @Override
                            public void onResponse(
                                    retrofit2.Call<com.example.myapplication.network.dto.ApiResponse<UserAddressRequest>> call,
                                    retrofit2.Response<com.example.myapplication.network.dto.ApiResponse<UserAddressRequest>> response) {
                                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                                    UserAddressRequest created = response.body().getResult();
                                    // Lấy name và phone từ response (nếu có), nếu không thì dùng giá trị đã nhập
                                    String savedName = created.recipientName != null && !created.recipientName.trim().isEmpty() 
                                            ? created.recipientName : name;
                                    String savedPhone = created.phone != null && !created.phone.trim().isEmpty() 
                                            ? created.phone : phone;
                                    AddressEntry entry = new AddressEntry(created.userAddressId, savedName, savedPhone, addr, makeDefault);
                                    addresses.add(0, entry);
                                    tempSelectedIndex = 0;
                                    if (makeDefault) {
                                        for (int i = 1; i < addresses.size(); i++) addresses.get(i).isDefault = false;
                                    }
                                    adapter.notifyDataSetChanged();
                                    AddressStorage.save(requireContext(), addresses);
                                    updateUseButtonState();
                                    updateEmptyState();
                                    Toast.makeText(getContext(), "Đã lưu địa chỉ!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Không thể lưu địa chỉ", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(
                                    retrofit2.Call<com.example.myapplication.network.dto.ApiResponse<UserAddressRequest>> call,
                                    Throwable t) {
                                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .show();
        }

        private void showEditDialog(AddressEntry entry, int position) {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_address_full, null);
            EditText edtName = dialogView.findViewById(R.id.edt_name);
            EditText edtPhone = dialogView.findViewById(R.id.edt_phone);
            EditText edtDetail = dialogView.findViewById(R.id.edt_detail);
            EditText edtWard = dialogView.findViewById(R.id.edt_ward);
            EditText edtDistrict = dialogView.findViewById(R.id.edt_district);
            EditText edtProvince = dialogView.findViewById(R.id.edt_province);
            android.widget.CheckBox cbDefault = dialogView.findViewById(R.id.cb_default);

            // Pre-populate với dữ liệu hiện có
            edtName.setText(entry.name != null ? entry.name : "");
            edtPhone.setText(entry.phone != null ? entry.phone : "");
            
            // Parse địa chỉ để điền vào các trường
            if (entry.address != null && !entry.address.trim().isEmpty()) {
                String[] parts = entry.address.split(",");
                if (parts.length >= 4) {
                    edtDetail.setText(parts[0].trim());
                    edtWard.setText(parts[1].trim());
                    edtDistrict.setText(parts[2].trim());
                    edtProvince.setText(parts[3].trim());
                } else if (parts.length >= 3) {
                    edtDetail.setText(parts[0].trim());
                    edtWard.setText(parts[1].trim());
                    edtDistrict.setText(parts[2].trim());
                } else if (parts.length >= 2) {
                    edtDetail.setText(parts[0].trim());
                    edtWard.setText(parts[1].trim());
                } else {
                    edtDetail.setText(entry.address);
                }
            }
            
            cbDefault.setChecked(entry.isDefault);

            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Chỉnh sửa địa chỉ")
                    .setView(dialogView)
                    .setPositiveButton("Lưu", (dialog, which) -> {
                        String name = edtName.getText().toString().trim();
                        String phone = edtPhone.getText().toString().trim();
                        String detail = edtDetail.getText().toString().trim();
                        String ward = edtWard.getText().toString().trim();
                        String district = edtDistrict.getText().toString().trim();
                        String province = edtProvince.getText().toString().trim();
                        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(detail)
                                || TextUtils.isEmpty(ward) || TextUtils.isEmpty(district) || TextUtils.isEmpty(province)) {
                            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String addr = detail + ", " + ward + ", " + district + ", " + province;
                        boolean makeDefault = cbDefault.isChecked();
                        
                        // Update local entry first
                        entry.name = name;
                        entry.phone = phone;
                        entry.address = addr;
                        if (makeDefault) {
                            for (int i = 0; i < addresses.size(); i++) {
                                addresses.get(i).isDefault = (i == position);
                            }
                        } else {
                            entry.isDefault = false;
                        }
                        
                        // Call API update nếu có ID
                        if (entry.id != null) {
                            Long userId = authManager.getUserId();
                            UserAddressRequest req = new UserAddressRequest();
                            req.userAddressId = entry.id;
                            req.userId = userId;
                            req.recipientName = name;
                            req.phone = phone;
                            req.address = addr;
                            addressService.update(req).enqueue(new retrofit2.Callback<com.example.myapplication.network.dto.ApiResponse<UserAddressRequest>>() {
                                @Override
                                public void onResponse(
                                        retrofit2.Call<com.example.myapplication.network.dto.ApiResponse<UserAddressRequest>> call,
                                        retrofit2.Response<com.example.myapplication.network.dto.ApiResponse<UserAddressRequest>> response) {
                                    if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                                        UserAddressRequest updated = response.body().getResult();
                                        // Cập nhật với dữ liệu từ server nếu có
                                        if (updated.recipientName != null && !updated.recipientName.trim().isEmpty()) {
                                            entry.name = updated.recipientName;
                                        }
                                        if (updated.phone != null && !updated.phone.trim().isEmpty()) {
                                            entry.phone = updated.phone;
                                        }
                                        if (updated.address != null && !updated.address.trim().isEmpty()) {
                                            entry.address = updated.address;
                                        }
                                        adapter.notifyDataSetChanged();
                                        AddressStorage.save(requireContext(), addresses);
                                        Toast.makeText(getContext(), "Đã cập nhật địa chỉ!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Vẫn lưu local nếu API fail
                                        adapter.notifyDataSetChanged();
                                        AddressStorage.save(requireContext(), addresses);
                                        Toast.makeText(getContext(), "Đã cập nhật địa chỉ (local)", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(
                                        retrofit2.Call<com.example.myapplication.network.dto.ApiResponse<UserAddressRequest>> call,
                                        Throwable t) {
                                    // Vẫn lưu local nếu API fail
                                    adapter.notifyDataSetChanged();
                                    AddressStorage.save(requireContext(), addresses);
                                    Toast.makeText(getContext(), "Đã cập nhật địa chỉ (local)", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            // Chỉ có local, không có ID từ server
                            adapter.notifyDataSetChanged();
                            AddressStorage.save(requireContext(), addresses);
                            Toast.makeText(getContext(), "Đã cập nhật địa chỉ!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .show();
        }

        private void updateUseButtonState() {
            btnUse.setEnabled(tempSelectedIndex >= 0 && tempSelectedIndex < addresses.size());
        }
    }


