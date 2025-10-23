package com.example.myapplication; // Đảm bảo tên package này khớp với package của bạn

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.AuthService;
import com.example.myapplication.network.dto.RegisterRequest;
import com.example.myapplication.network.dto.RegisterResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivitySignUp extends AppCompatActivity {

    // --- VIEWS ---
    private TextInputLayout fullNameInputLayout, emailInputLayout, passwordInputLayout;
    private TextInputEditText fullNameEditText, emailEditText, passwordEditText;
    private MaterialButton registerButton;
    private TextView loginTextView;
    private ImageButton backButton;
    // Social login buttons (optional functionality)
    private View facebookLoginButton, googleLoginButton, appleLoginButton;


    // --- SERVICES ---
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // Đặt layout cho Activity này
        setContentView(R.layout.activity_signup00000);

        // Khởi tạo service để gọi API
        authService = ApiClient.getRetrofit(this).create(AuthService.class);

        // Ánh xạ các view từ file XML
        initializeViews();

        // Xử lý hiển thị tràn viền (edge-to-edge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Thiết lập các sự kiện click
        setupClickListeners();
    }

    /**
     * Ánh xạ các biến view với các component trong file XML.
     */
    private void initializeViews() {
        // Ánh xạ các ID từ 'activity_signup00000.xml'
        fullNameInputLayout = findViewById(R.id.fullNameInputLayout);
        fullNameEditText = findViewById(R.id.fullNameEditText);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        emailEditText = findViewById(R.id.emailEditText);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        passwordEditText = findViewById(R.id.passwordEditText);
        registerButton = findViewById(R.id.registerButton);
        loginTextView = findViewById(R.id.loginText);
        backButton = findViewById(R.id.backButton);

        // Ánh xạ các nút social login
        facebookLoginButton = findViewById(R.id.facebookLoginButton);
        googleLoginButton = findViewById(R.id.googleLoginButton);
        appleLoginButton = findViewById(R.id.appleLoginButton);
    }

    /**
     * Thiết lập các sự kiện OnClickListener cho các view tương tác.
     */
    private void setupClickListeners() {
        // Sự kiện click cho nút "Register"
        registerButton.setOnClickListener(v -> handleRegistration());

        // Sự kiện click cho nút "Back"
        backButton.setOnClickListener(v -> onBackPressed()); // Quay lại màn hình trước đó

        // Sự kiện click cho chữ "Login"
        loginTextView.setOnClickListener(v -> finish()); // Đóng màn hình hiện tại và quay lại LoginScreen

        // Sự kiện click cho các nút social login
        if (facebookLoginButton != null) {
            facebookLoginButton.setOnClickListener(v -> {
                Toast.makeText(this, "Tính năng đăng ký Facebook đang được phát triển", Toast.LENGTH_SHORT).show();
            });
        }

        if (googleLoginButton != null) {
            googleLoginButton.setOnClickListener(v -> {
                Toast.makeText(this, "Tính năng đăng ký Google đang được phát triển", Toast.LENGTH_SHORT).show();
            });
        }

        if (appleLoginButton != null) {
            appleLoginButton.setOnClickListener(v -> {
                Toast.makeText(this, "Tính năng đăng ký Apple đang được phát triển", Toast.LENGTH_SHORT).show();
            });
        }
    }

    /**
     * Xử lý logic khi người dùng nhấn nút đăng ký.
     */
    private void handleRegistration() {
        String fullName = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Kiểm tra tính hợp lệ của dữ liệu đầu vào
        if (!validateInput(fullName, email, password)) {
            return; // Dừng lại nếu dữ liệu không hợp lệ
        }

        // Vô hiệu hóa nút để tránh click nhiều lần
        registerButton.setEnabled(false);

        // Tạo yêu cầu đăng ký
        // LƯU Ý: Lớp RegisterRequest của bạn cần có constructor (username, password, confirmPassword, email)
        // Vì layout không có trường "confirm password", ta sẽ truyền password 2 lần.
        RegisterRequest request = new RegisterRequest(fullName, password, password, email);

        // Gọi API đăng ký
        Call<RegisterResponse> call = authService.register(request);
        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                registerButton.setEnabled(true); // Kích hoạt lại nút
                if (response.isSuccessful()) {
                    Toast.makeText(ActivitySignUp.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    // Đóng màn hình đăng ký và quay lại màn hình đăng nhập
                    finish();
                } else {
                    // Xử lý lỗi từ server, ví dụ: email đã tồn tại
                    Toast.makeText(ActivitySignUp.this, "Đăng ký thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                registerButton.setEnabled(true); // Kích hoạt lại nút
                Toast.makeText(ActivitySignUp.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Kiểm tra các trường nhập liệu và hiển thị lỗi nếu cần.
     * @return true nếu tất cả các trường hợp lệ, ngược lại là false.
     */
    private boolean validateInput(String fullName, String email, String password) {
        // Xóa các lỗi cũ
        fullNameInputLayout.setError(null);
        emailInputLayout.setError(null);
        passwordInputLayout.setError(null);

        if (fullName.isEmpty()) {
            fullNameInputLayout.setError("Vui lòng nhập họ và tên");
            return false;
        }

        if (email.isEmpty()) {
            emailInputLayout.setError("Vui lòng nhập email");
            return false;
        }

        if (password.isEmpty()) {
            passwordInputLayout.setError("Vui lòng nhập mật khẩu");
            return false;
        }

        // Bạn có thể thêm các kiểm tra phức tạp hơn ở đây (ví dụ: độ dài mật khẩu, định dạng email)

        return true;
    }
}