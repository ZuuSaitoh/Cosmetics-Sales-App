package com.example.myapplication;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.ProductAdapter;
import com.example.myapplication.model.Category;
import com.example.myapplication.model.Product;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.CategoryService;
import com.example.myapplication.network.ProductService;
import com.example.myapplication.network.dto.ApiResponse;
import com.example.myapplication.network.dto.AddProductRequest;
// removed file-pick/multipart imports

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import android.app.ProgressDialog;
import android.net.Uri;
import android.content.Intent;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class ProductListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;
    private ProductAdapter productAdapter;
    private GridLayoutManager layoutManager;
    private boolean isGrid = true;
    private List<Product> allProducts = new ArrayList<>();

    // Sort UI
    private TextView btnSortFeatured;
    private TextView btnSortBestSelling;
    private TextView btnSortNewest;
    private LinearLayout btnSortPrice;
    private TextView textPriceLabel;
    private ImageView imgPriceSort;
    private ImageButton btnLayoutToggle;

    // Sort state
    private String currentSort = "Nổi bật";
    private boolean isPriceAscending = true;

    private static final int PICK_IMAGE_REQUEST = 101;
    private Uri imageUri;
    private StorageReference storageReference;
    private com.google.android.material.textfield.TextInputEditText etImageGlobal;
    private androidx.cardview.widget.CardView cardMainImageGlobal;


    public ProductListFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_product_list, container, false);
        storageReference = FirebaseStorage.getInstance().getReference("product_images");

        recyclerView = root.findViewById(R.id.rv_products_admin);
        progressBar = root.findViewById(R.id.progress_bar);
        emptyText = root.findViewById(R.id.tv_empty);
        com.google.android.material.appbar.MaterialToolbar toolbar = root.findViewById(R.id.toolbar_products);
        View fabAdd = root.findViewById(R.id.fab_add);
        View btnCreate = root.findViewById(R.id.btn_create_product);
        final com.google.android.material.textfield.TextInputEditText etSearch = root.findViewById(R.id.et_search);

        // Sort views
        btnSortFeatured = root.findViewById(R.id.btnSortFeatured);
        btnSortBestSelling = root.findViewById(R.id.btnSortBestSelling);
        btnSortNewest = root.findViewById(R.id.btnSortNewest);
        btnSortPrice = root.findViewById(R.id.btnSortPrice);
        textPriceLabel = root.findViewById(R.id.textPriceLabel);
        imgPriceSort = root.findViewById(R.id.imgPriceSort);
        btnLayoutToggle = root.findViewById(R.id.btnLayoutToggle);

        layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        productAdapter = new ProductAdapter(requireContext(), new ArrayList<>(), true);
        recyclerView.setAdapter(productAdapter);

        productAdapter.setOnProductActionListener(new ProductAdapter.OnProductActionListener() {
            @Override
            public void onEdit(Product product) { openEditProductDialog(product); }

            @Override
            public void onDelete(Product product) {
                confirmAndDelete(product);
            }
        });

        // No image picker (URL input instead)

        // Toolbar actions
        toolbar.inflateMenu(R.menu.menu_product_list);
        toolbar.setOnMenuItemClickListener(this::onToolbarItemClick);

        // Add product actions
        View.OnClickListener addAction = v -> openAddProductDialog();
        fabAdd.setOnClickListener(addAction);
        btnCreate.setOnClickListener(addAction);

        // removed old top toggle button; use icon in sort toolbar instead

        // Sort listeners
        if (btnSortFeatured != null) btnSortFeatured.setOnClickListener(v -> { currentSort = "Nổi bật"; sortProducts("Nổi bật"); updateSortButtons(); });
        if (btnSortBestSelling != null) btnSortBestSelling.setOnClickListener(v -> { currentSort = "Bán chạy"; sortProducts("Bán chạy"); updateSortButtons(); });
        if (btnSortNewest != null) btnSortNewest.setOnClickListener(v -> { currentSort = "Mới nhất"; sortProducts("Mới nhất"); updateSortButtons(); });
        if (btnSortPrice != null) btnSortPrice.setOnClickListener(v -> { currentSort = "Giá"; isPriceAscending = !isPriceAscending; sortProducts("Giá"); updateSortButtons(); });
        if (btnLayoutToggle != null) btnLayoutToggle.setOnClickListener(v -> { isGrid = !isGrid; layoutManager.setSpanCount(isGrid ? 2 : 1); recyclerView.setLayoutManager(layoutManager); recyclerView.getAdapter().notifyDataSetChanged(); });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (productAdapter != null) {
                    productAdapter.getFilter().filter(s.toString());
                }
            }
        });

        loadProductsFromAPI();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Attach click to open admin review
        if (productAdapter != null) {
            productAdapter.setOnProductClickListener(this::openReviewDialog);
        }
    }

    private boolean onToolbarItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_toggle_layout) {
            toggleLayout(item);
            return true;
        } else if (id == R.id.action_add) {
            Toast.makeText(getContext(), "Thêm sản phẩm", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private void openReviewDialog(Product product) {
        if (getContext() == null || product == null) return;
        // Fetch latest by id
        Long id = product.getProductID();
        if (id != null) {
            ProductService svc = ApiClient.getRetrofit(getContext()).create(ProductService.class);
            svc.getProductById(id).enqueue(new Callback<ApiResponse<Product>>() {
                @Override
                public void onResponse(Call<ApiResponse<Product>> call, Response<ApiResponse<Product>> response) {
                    Product p = (response.isSuccessful() && response.body() != null && response.body().getResult() != null)
                            ? response.body().getResult() : product;
                    showReviewDialog(p);
                }

                @Override
                public void onFailure(Call<ApiResponse<Product>> call, Throwable t) {
                    showReviewDialog(product);
                }
            });
        } else {
            showReviewDialog(product);
        }
    }

    private void showReviewDialog(Product p) {
        if (getContext() == null || p == null) return;
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_review_product, null, false);
        android.widget.ImageView img = view.findViewById(R.id.img_product);
        android.widget.TextView tvName = view.findViewById(R.id.tv_name);
        android.widget.TextView tvPrice = view.findViewById(R.id.tv_price);
        android.widget.TextView tvBrand = view.findViewById(R.id.tv_brand);
        android.widget.TextView tvQty = view.findViewById(R.id.tv_quantity);
        android.widget.TextView tvBrief = view.findViewById(R.id.tv_brief);
        android.widget.TextView tvFull = view.findViewById(R.id.tv_full);

        tvName.setText(p.getName());
        tvPrice.setText(String.format("%,.0f VND", p.getPrice()));
        tvBrand.setText("Thương hiệu: " + (p.getBrand() == null ? "" : p.getBrand()));
        tvQty.setText("Số lượng: " + (p.getInstockQuantity() == null ? 0 : p.getInstockQuantity()));
        tvBrief.setText(p.getDescription());
        tvFull.setText(p.getFullDescription());

        String image = p.getImageURL();
        if (image != null && !image.isEmpty()) {
            com.bumptech.glide.Glide.with(this).load(image).placeholder(R.drawable.img_no_product).error(R.drawable.img_no_product).into(img);
        } else {
            img.setImageResource(R.drawable.img_no_product);
        }

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xem sản phẩm")
                .setView(view)
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void toggleLayout(MenuItem item) {
        isGrid = !isGrid;
        layoutManager.setSpanCount(isGrid ? 2 : 1);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.getAdapter().notifyDataSetChanged();
        if (item != null) {
            item.setTitle(isGrid ? "Chuyển List" : "Chuyển Grid");
        }
    }

    private void showLoading(boolean loading) {
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private void showEmpty(boolean isEmpty) {
        if (emptyText != null) emptyText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (recyclerView != null) recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void loadProductsFromAPI() {
        if (getContext() == null) return;

        showLoading(true);
        showEmpty(false);

        ProductService productService = ApiClient.getRetrofit(getContext()).create(ProductService.class);
        productService.fetchAllProducts().enqueue(new Callback<ApiResponse<List<Product>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Product>>> call, Response<ApiResponse<List<Product>>> response) {
                if (getContext() == null) return;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Product>> api = response.body();
                    List<Product> products = api.getResult();
                    if (products != null && !products.isEmpty()) {
                        allProducts.clear();
                        allProducts.addAll(products);
                        // initial sort like other screens
                        sortProducts(currentSort);
                        updateSortButtons();
                        showEmpty(false);
                    } else {
                        showEmpty(true);
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi tải sản phẩm: " + response.code(), Toast.LENGTH_SHORT).show();
                    showEmpty(true);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Product>>> call, Throwable t) {
                if (getContext() == null) return;
                showLoading(false);
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmpty(true);
            }
        });
    }

    private void sortProducts(String sortType) {
        if (allProducts == null || allProducts.isEmpty()) return;
        List<Product> sorted = new ArrayList<>(allProducts);
        switch (sortType) {
            case "Nổi bật":
                java.util.Collections.shuffle(sorted);
                break;
            case "Bán chạy":
                java.util.Collections.sort(sorted, (p1, p2) -> {
                    int q1 = p1.getInstockQuantity() == null ? 0 : p1.getInstockQuantity();
                    int q2 = p2.getInstockQuantity() == null ? 0 : p2.getInstockQuantity();
                    return Integer.compare(q2, q1);
                });
                break;
            case "Mới nhất":
                java.util.Collections.sort(sorted, (p1, p2) -> {
                    Long id1 = p1.getProductID() == null ? 0L : p1.getProductID();
                    Long id2 = p2.getProductID() == null ? 0L : p2.getProductID();
                    return Long.compare(id2, id1);
                });
                break;
            case "Giá":
                if (isPriceAscending) {
                    java.util.Collections.sort(sorted, (p1, p2) -> Double.compare(p1.getPrice(), p2.getPrice()));
                } else {
                    java.util.Collections.sort(sorted, (p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
                }
                break;
        }
        productAdapter.updateProducts(sorted);
    }

    private void updateSortButtons() {
        if (btnSortFeatured == null) return;
        btnSortFeatured.setTextColor(0xFF666666);
        btnSortBestSelling.setTextColor(0xFF666666);
        btnSortNewest.setTextColor(0xFF666666);
        textPriceLabel.setTextColor(0xFF666666);
        if (imgPriceSort != null) imgPriceSort.setImageResource(R.drawable.ic_price_sort_default);

        switch (currentSort) {
            case "Nổi bật":
                btnSortFeatured.setTextColor(0xFF4CAF50);
                break;
            case "Bán chạy":
                btnSortBestSelling.setTextColor(0xFF4CAF50);
                break;
            case "Mới nhất":
                btnSortNewest.setTextColor(0xFF4CAF50);
                break;
            case "Giá":
                textPriceLabel.setTextColor(0xFF4CAF50);
                if (imgPriceSort != null) imgPriceSort.setImageResource(isPriceAscending ? R.drawable.ic_price_sort_asc : R.drawable.ic_price_sort_desc);
                break;
        }
    }

    private void openAddProductDialog() {
        if (getContext() == null) return;
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_product, null, false);
        final com.google.android.material.textfield.TextInputEditText etName = view.findViewById(R.id.et_name);
        final com.google.android.material.textfield.TextInputEditText etPrice = view.findViewById(R.id.et_price);
        final com.google.android.material.textfield.TextInputEditText etQty = view.findViewById(R.id.et_quantity);
        final com.google.android.material.textfield.TextInputEditText etBrand = view.findViewById(R.id.et_brand);
        final AutoCompleteTextView spCategory = view.findViewById(R.id.sp_category);
        final com.google.android.material.textfield.TextInputLayout tilCategory = view.findViewById(R.id.til_category);
        if (tilCategory != null) {
            tilCategory.setEndIconMode(com.google.android.material.textfield.TextInputLayout.END_ICON_DROPDOWN_MENU);
        }
        spCategory.setThreshold(0);
        spCategory.setInputType(0); // disable soft keyboard
        spCategory.setOnClickListener(v2 -> spCategory.showDropDown());
        spCategory.setOnFocusChangeListener((v2, hasFocus) -> { if (hasFocus) spCategory.showDropDown(); });
        final com.google.android.material.textfield.TextInputEditText etBrief = view.findViewById(R.id.et_brief);
        final com.google.android.material.textfield.TextInputEditText etFull = view.findViewById(R.id.et_full);
        final com.google.android.material.textfield.TextInputEditText etImage = view.findViewById(R.id.et_image);
        final TextView btnReset = view.findViewById(R.id.btn_reset);
        final TextView tvTitle = view.findViewById(R.id.tv_title);
        final com.google.android.material.button.MaterialButton btnSave = view.findViewById(R.id.btn_save_changes);
        final androidx.cardview.widget.CardView cardMainImage = view.findViewById(R.id.card_main_image);

        tvTitle.setText("THÊM SẢN PHẨM MỚI");
        btnReset.setText("ĐẶT LẠI");
        etImageGlobal = etImage;
        cardMainImageGlobal = cardMainImage;

// Khi bấm vào ảnh chính -> mở thư viện
        View firstChildAdd = cardMainImage.getChildAt(0);
        View.OnClickListener openPicker = v1 -> openImagePicker();
        cardMainImage.setOnClickListener(openPicker);
        if (firstChildAdd instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) firstChildAdd;
            group.setOnClickListener(openPicker);
            if (group.getChildCount() > 0) {
                View inner = group.getChildAt(0);
                inner.setOnClickListener(openPicker);
            }
        }

        // Enable image pick/upload like Add dialog (already set above)

        // Load categories for dropdown
        final List<Category>[] categoriesArray = new List[]{new ArrayList<>()};
        final List<String> categoryNames = new ArrayList<>();
        CategoryService categoryService = ApiClient.getRetrofit(getContext()).create(CategoryService.class);
        categoryService.fetchAllCategories().enqueue(new Callback<ApiResponse<List<Category>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Category>>> call, Response<ApiResponse<List<Category>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Category>> apiResponse = response.body();
                    if (apiResponse.getCode() == 9999 && apiResponse.getResult() != null) {
                        categoriesArray[0].clear();
                        categoriesArray[0].addAll(apiResponse.getResult());
                        categoryNames.clear();
                        for (Category cat : categoriesArray[0]) {
                            categoryNames.add(cat.getName());
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                                android.R.layout.simple_dropdown_item_1line, categoryNames);
                        spCategory.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Category>>> call, Throwable t) {
                // Fallback - use empty adapter
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
                spCategory.setAdapter(adapter);
            }
        });

        // Reset button
        btnReset.setOnClickListener(v -> {
            etName.setText("");
            etPrice.setText("");
            etQty.setText("");
            etBrand.setText("");
            spCategory.setText("");
            etBrief.setText("");
            etFull.setText("");
            etImage.setText("");
        });

        final android.app.Dialog dialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("")
                .setView(view)
                .setCancelable(true)
                .create();

        // Save Changes button
        btnSave.setOnClickListener(v -> {
            // Get selected category ID
            String selectedCategoryName = spCategory.getText().toString();
            Long categoryID = null;
            for (Category cat : categoriesArray[0]) {
                if (cat.getName().equals(selectedCategoryName)) {
                    categoryID = cat.getCategoryID();
                    break;
                }
            }

            if (categoryID == null) {
                Toast.makeText(getContext(), "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
                return;
            }

            AddProductRequest req = new AddProductRequest();
            req.productName = String.valueOf(etName.getText());
            req.price = safeDouble(etPrice.getText());
            req.instockQuantity = (int) safeDouble(etQty.getText());
            req.imageURL = String.valueOf(etImage.getText());
            req.brand = String.valueOf(etBrand.getText());
            req.categoryID = categoryID;
            req.briefDescription = String.valueOf(etBrief.getText());
            req.fullDescription = String.valueOf(etFull.getText());
            addProduct(req);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void openEditProductDialog(Product product) {
        if (getContext() == null || product == null) return;
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_product, null, false);
        final com.google.android.material.textfield.TextInputEditText etName = view.findViewById(R.id.et_name);
        final com.google.android.material.textfield.TextInputEditText etPrice = view.findViewById(R.id.et_price);
        final com.google.android.material.textfield.TextInputEditText etQty = view.findViewById(R.id.et_quantity);
        final com.google.android.material.textfield.TextInputEditText etBrand = view.findViewById(R.id.et_brand);
        final AutoCompleteTextView spCategory = view.findViewById(R.id.sp_category);
        final com.google.android.material.textfield.TextInputLayout tilCategory = view.findViewById(R.id.til_category);
        if (tilCategory != null) {
            tilCategory.setEndIconMode(com.google.android.material.textfield.TextInputLayout.END_ICON_DROPDOWN_MENU);
        }
        spCategory.setThreshold(0);
        spCategory.setInputType(0);
        spCategory.setOnClickListener(v2 -> spCategory.showDropDown());
        spCategory.setOnFocusChangeListener((v2, hasFocus) -> { if (hasFocus) spCategory.showDropDown(); });
        final com.google.android.material.textfield.TextInputEditText etBrief = view.findViewById(R.id.et_brief);
        final com.google.android.material.textfield.TextInputEditText etFull = view.findViewById(R.id.et_full);
        final com.google.android.material.textfield.TextInputEditText etImage = view.findViewById(R.id.et_image);
        final TextView btnReset = view.findViewById(R.id.btn_reset);
        final TextView tvTitle = view.findViewById(R.id.tv_title);
        final com.google.android.material.button.MaterialButton btnSave = view.findViewById(R.id.btn_save_changes);
        final androidx.cardview.widget.CardView cardMainImage = view.findViewById(R.id.card_main_image);

        tvTitle.setText("CHỈNH SỬA SẢN PHẨM");
        btnReset.setText("ĐẶT LẠI");

        // Enable image pick/upload similar to Add dialog
        etImageGlobal = etImage;
        cardMainImageGlobal = cardMainImage;
        if (cardMainImage != null) {
            View firstChildEdit = cardMainImage.getChildAt(0);
            View.OnClickListener openPicker2 = v -> openImagePicker();
            cardMainImage.setOnClickListener(openPicker2);
            if (firstChildEdit instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) firstChildEdit;
                group.setOnClickListener(openPicker2);
                if (group.getChildCount() > 0) {
                    View inner = group.getChildAt(0);
                    inner.setOnClickListener(openPicker2);
                }
            }
        }

        // Load categories for dropdown
        final List<Category>[] categoriesArray = new List[]{new ArrayList<>()};
        final List<String> categoryNames = new ArrayList<>();
        CategoryService categoryService = ApiClient.getRetrofit(getContext()).create(CategoryService.class);
        categoryService.fetchAllCategories().enqueue(new Callback<ApiResponse<List<Category>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Category>>> call, Response<ApiResponse<List<Category>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Category>> apiResponse = response.body();
                    if (apiResponse.getCode() == 9999 && apiResponse.getResult() != null) {
                        categoriesArray[0].clear();
                        categoriesArray[0].addAll(apiResponse.getResult());
                        categoryNames.clear();
                        for (Category cat : categoriesArray[0]) {
                            categoryNames.add(cat.getName());
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                                android.R.layout.simple_dropdown_item_1line, categoryNames);
                        spCategory.setAdapter(adapter);

                        // Prefill category name
                        if (product.getCategoryID() != null) {
                            try {
                                Long categoryId = product.getCategoryID().getCategoryID();
                                for (Category cat : categoriesArray[0]) {
                                    if (cat.getCategoryID().equals(categoryId)) {
                                        spCategory.setText(cat.getName(), false);
                                        break;
                                    }
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Category>>> call, Throwable t) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
                spCategory.setAdapter(adapter);
            }
        });

        // Prefill
        etName.setText(product.getName());
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setGroupingUsed(false);
        etPrice.setText(nf.format(product.getPrice()));
        etQty.setText(String.valueOf(product.getInstockQuantity() == null ? 0 : product.getInstockQuantity()));
        etBrand.setText(product.getBrand());
        etBrief.setText(product.getDescription());
        etFull.setText(product.getFullDescription());
        etImage.setText(product.getImageURL());
        try {
            String imgUrl = product.getImageURL();
            if (imgUrl != null && !imgUrl.trim().isEmpty()) {
                setPreviewImage(Uri.parse(imgUrl));
            }
        } catch (Exception ignored) {}

        // Reset button
        btnReset.setOnClickListener(v -> {
            etName.setText(product.getName());
            etPrice.setText(nf.format(product.getPrice()));
            etQty.setText(String.valueOf(product.getInstockQuantity() == null ? 0 : product.getInstockQuantity()));
            etBrand.setText(product.getBrand());
            if (product.getCategoryID() != null) {
                try {
                    Long categoryId = product.getCategoryID().getCategoryID();
                    for (Category cat : categoriesArray[0]) {
                        if (cat.getCategoryID().equals(categoryId)) {
                            spCategory.setText(cat.getName(), false);
                            break;
                        }
                    }
                } catch (Exception ignored) {}
            }
            etBrief.setText(product.getDescription());
            etFull.setText(product.getFullDescription());
            etImage.setText(product.getImageURL());
        });

        final android.app.Dialog dialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("")
                .setView(view)
                .setCancelable(true)
                .create();

        // Save Changes button
        btnSave.setOnClickListener(v -> {
            if (product.getProductID() == null) return;

            // Get selected category ID
            String selectedCategoryName = spCategory.getText().toString();
            Long categoryID = null;
            for (Category cat : categoriesArray[0]) {
                if (cat.getName().equals(selectedCategoryName)) {
                    categoryID = cat.getCategoryID();
                    break;
                }
            }

            if (categoryID == null) {
                Toast.makeText(getContext(), "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
                return;
            }

            final Long productId = product.getProductID();
            final int newQty = (int) safeDouble(etQty.getText());

            com.example.myapplication.network.dto.UpdateProductInfoRequest req = new com.example.myapplication.network.dto.UpdateProductInfoRequest();
            req.productName = String.valueOf(etName.getText());
            req.price = safeDouble(etPrice.getText());
            req.imageURL = String.valueOf(etImage.getText());
            req.brand = String.valueOf(etBrand.getText());
            req.categoryID = categoryID;
            req.briefDescription = String.valueOf(etBrief.getText());
            req.fullDescription = String.valueOf(etFull.getText());
            updateProduct(productId, req, product, newQty);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateProduct(Long productId, com.example.myapplication.network.dto.UpdateProductInfoRequest info, Product originalProduct, int newQty) {
        if (getContext() == null || productId == null) return;
        showLoading(true);
        ProductService svc = ApiClient.getRetrofit(getContext()).create(ProductService.class);
        svc.updateProductInfo(productId, info).enqueue(new Callback<ApiResponse<Product>>() {
            @Override
            public void onResponse(Call<ApiResponse<Product>> call, Response<ApiResponse<Product>> response) {
                if (getContext() == null) return;
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    Product updated = response.body().getResult();
                    productAdapter.replaceProduct(updated);
                    // quantity update if changed
                    Integer currentQty = originalProduct.getInstockQuantity();
                    if (currentQty == null) currentQty = 0;
                    if (currentQty != newQty) {
                        com.example.myapplication.network.dto.UpdateQuantityRequest q = new com.example.myapplication.network.dto.UpdateQuantityRequest();
                        q.instockQuantity = newQty;
                        svc.updateProductQuantity(productId, q).enqueue(new Callback<ApiResponse<Product>>() {
                            @Override
                            public void onResponse(Call<ApiResponse<Product>> call, Response<ApiResponse<Product>> resp2) {
                                showLoading(false);
                                if (resp2.isSuccessful() && resp2.body() != null && resp2.body().getResult() != null) {
                                    productAdapter.replaceProduct(resp2.body().getResult());
                                    Toast.makeText(getContext(), "Đã cập nhật sản phẩm", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<ApiResponse<Product>> call, Throwable t) {
                                showLoading(false);
                                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        showLoading(false);
                        Toast.makeText(getContext(), "Đã cập nhật sản phẩm", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showLoading(false);
                    Toast.makeText(getContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Product>> call, Throwable t) {
                if (getContext() == null) return;
                showLoading(false);
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private double safeDouble(CharSequence cs) {
        try { return Double.parseDouble(cs == null ? "0" : cs.toString().trim()); } catch (Exception e) { return 0; }
    }

    private void addProduct(AddProductRequest req) {
        if (getContext() == null) return;
        showLoading(true);
        ProductService svc = ApiClient.getRetrofit(getContext()).create(ProductService.class);
        svc.addProduct(req).enqueue(new Callback<ApiResponse<Product>>() {
            @Override
            public void onResponse(Call<ApiResponse<Product>> call, Response<ApiResponse<Product>> response) {
                if (getContext() == null) return;
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    Toast.makeText(getContext(), "Đã tạo sản phẩm", Toast.LENGTH_SHORT).show();
                    loadProductsFromAPI();
                } else {
                    Toast.makeText(getContext(), "Tạo sản phẩm thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Product>> call, Throwable t) {
                if (getContext() == null) return;
                showLoading(false);
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmAndDelete(Product product) {
        if (getContext() == null || product == null || product.getProductID() == null) return;
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn có chắc muốn xóa '" + product.getName() + "'?")
                .setPositiveButton("Xóa", (d, w) -> deleteProduct(product))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteProduct(Product product) {
        if (getContext() == null) return;
        showLoading(true);
        ProductService svc = ApiClient.getRetrofit(getContext()).create(ProductService.class);
        svc.deleteProduct(product.getProductID()).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                if (getContext() == null) return;
                showLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
                    // remove locally for instant feedback
                    productAdapter.removeProductById(product.getProductID());
                    showEmpty(productAdapter.getItemCount() == 0);
                } else {
                    Toast.makeText(getContext(), "Xóa thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                if (getContext() == null) return;
                showLoading(false);
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Removed multipart helpers; using URL input for image

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == android.app.Activity.RESULT_OK
                && data != null && data.getData() != null) {

            imageUri = data.getData();
            // Hiển thị preview ngay lập tức bằng URI cục bộ trước khi upload
            setPreviewImage(imageUri);
            uploadImageToFirebase(imageUri);
        }
    }

    private void openImagePicker() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(Intent.createChooser(intent, "Chọn ảnh sản phẩm"), PICK_IMAGE_REQUEST);
        } catch (Exception e) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(Intent.createChooser(intent, "Chọn ảnh sản phẩm"), PICK_IMAGE_REQUEST);
        }
    }

    private void uploadImageToFirebase(Uri uri) {
        if (uri == null) return;

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Đang tải ảnh...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String fileName = System.currentTimeMillis() + ".jpg";
        StorageReference fileRef = storageReference.child(fileName);

        fileRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot ->
                        fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            progressDialog.dismiss();
                            if (etImageGlobal != null) {
                                etImageGlobal.setText(downloadUri.toString());
                            }

                            // Cập nhật ảnh xem trước trong card
                            // Hiển thị preview với URL tải về (ghi đè preview tạm thời nếu có)
                            setPreviewImage(downloadUri);

                            Toast.makeText(getContext(), "Tải ảnh thành công", Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Lỗi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setPreviewImage(Uri uri) {
        if (cardMainImageGlobal == null || uri == null) return;
        View firstChild = cardMainImageGlobal.getChildAt(0);
        if (firstChild instanceof ViewGroup) {
            ViewGroup container = (ViewGroup) firstChild;
            if (container.getChildCount() > 0) {
                View maybeImageView = container.getChildAt(0);
                if (maybeImageView instanceof ImageView) {
                    ImageView preview = (ImageView) maybeImageView;
                    // Làm cho ảnh lấp đầy thẻ và cắt giữa, bỏ tint icon mặc định
                    ViewGroup.LayoutParams lp = preview.getLayoutParams();
                    lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    preview.setLayoutParams(lp);
                    preview.setAdjustViewBounds(false);
                    preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    preview.clearColorFilter();
                    try { preview.setImageTintList(null); } catch (Throwable ignored) {}
                    // Bỏ padding nếu LinearLayout đặt padding ảnh
                    container.setPadding(0, 0, 0, 0);
                    // Bỏ nền xám để hình hiển thị rõ
                    try { container.setBackgroundColor(android.graphics.Color.TRANSPARENT); } catch (Throwable ignored) {}
                    // Nạp ảnh
                    com.bumptech.glide.Glide.with(preview.getContext())
                            .load(uri)
                            .centerCrop()
                            .into(preview);
                }
            }
        }
    }

}


