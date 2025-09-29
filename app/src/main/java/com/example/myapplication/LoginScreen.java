package com.example.myapplication; // Đảm bảo tên package này khớp với package của bạn

import android.os.Bundle;
import android.view.View;
import android.widget.Button; // Sử dụng android.widget.Button cho MaterialButton nếu không ép kiểu
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Import Toolbar
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton; // Nên sử dụng MaterialButton trực tiếp
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginScreen extends AppCompatActivity {

    private static final String HARDCODED_USERNAME = "admin";
    private static final String HARDCODED_PASSWORD = "123456";
    private static final String CUSTOMER_USERNAME = "customer";
    private static final String CUSTOMER_PASSWORD = "123456";

    private TextInputLayout emailOrPhoneInputLayout;
    private TextInputEditText emailOrPhoneEditText;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText passwordEditText;
    private MaterialButton loginButton; // Đổi thành MaterialButton
    private MaterialButton signUpButton; // Đổi thành MaterialButton
    private MaterialButton createAccountButton;
    private TextView forgotPasswordTextView;
    private TextView backToLoginText;
    private ImageButton facebookLoginButton;
    private ImageButton googleLoginButton;
    private ImageButton appleLoginButton;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login); // QUAN TRỌNG: Thay đổi thành tên tệp XML của bạn

        

        // Khởi tạo Views
        emailOrPhoneInputLayout = findViewById(R.id.emailOrPhoneInputLayout);
        emailOrPhoneEditText = findViewById(R.id.emailOrPhoneEditText);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signUpButton);
        createAccountButton = findViewById(R.id.createAccountButton);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        backToLoginText = findViewById(R.id.backToLoginText);
        facebookLoginButton = findViewById(R.id.facebookLoginButton);
        googleLoginButton = findViewById(R.id.googleLoginButton);
        appleLoginButton = findViewById(R.id.appleLoginButton);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            // Với ConstraintLayout và AppBarLayout, AppBarLayout đã xử lý insets.
            // ScrollView được ràng buộc bên dưới AppBarLayout, nên không cần điều chỉnh
            // thêm.
            return insets;
        });

        // Đặt OnClick Listeners
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailOrPhone = emailOrPhoneEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (emailOrPhone.isEmpty()) {
                    emailOrPhoneInputLayout.setError("Vui lòng nhập email hoặc số điện thoại");
                    return;
                } else {
                    emailOrPhoneInputLayout.setError(null); // Xóa lỗi
                }

                if (password.isEmpty()) {
                    passwordInputLayout.setError("Vui lòng nhập mật khẩu");
                    return;
                } else {
                    passwordInputLayout.setError(null); // Xóa lỗi
                }

                // Kiểm tra tài khoản cứng (admin hoặc customer)
                boolean isAdmin = HARDCODED_USERNAME.equals(emailOrPhone) && HARDCODED_PASSWORD.equals(password);
                boolean isCustomer = CUSTOMER_USERNAME.equals(emailOrPhone) && CUSTOMER_PASSWORD.equals(password);

                if (isAdmin || isCustomer) {
                    Class<?> destination = isAdmin ? AdminDashboardActivity.class : HomeScreen.class;
                    Intent intent = new Intent(LoginScreen.this, destination);
                    intent.putExtra("username", emailOrPhone);
                    startActivity(intent);
                } else {
                    Toast.makeText(LoginScreen.this, "Sai tên đăng nhập hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                }
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hiển thị form đăng ký
                View loginForm = findViewById(R.id.loginForm);
                View registerForm = findViewById(R.id.registerForm);
                loginForm.setVisibility(View.GONE);
                registerForm.setVisibility(View.VISIBLE);
            }
        });

        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // --- Điều hướng đến Màn hình Quên mật khẩu ---
                Toast.makeText(LoginScreen.this, "Chuyển đến màn hình Quên mật khẩu", Toast.LENGTH_SHORT).show();
            }
        });

        facebookLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // --- Xử lý Đăng nhập Facebook ---
                Toast.makeText(LoginScreen.this, "Đăng nhập Facebook", Toast.LENGTH_SHORT).show();
            }
        });

        googleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // --- Xử lý Đăng nhập Google ---
                Toast.makeText(LoginScreen.this, "Đăng nhập Google", Toast.LENGTH_SHORT).show();
            }
        });

        appleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // --- Xử lý Đăng nhập Apple ---
                Toast.makeText(LoginScreen.this, "Đăng nhập Apple", Toast.LENGTH_SHORT).show();
            }
        });

        // Nút quay lại đăng nhập trong form đăng ký
        if (backToLoginText != null) {
            backToLoginText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View loginForm = findViewById(R.id.loginForm);
                    View registerForm = findViewById(R.id.registerForm);
                    registerForm.setVisibility(View.GONE);
                    loginForm.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Xóa trường nhập khi quay lại màn hình đăng nhập
        if (emailOrPhoneEditText != null) {
            emailOrPhoneEditText.setText("");
        }
        if (passwordEditText != null) {
            passwordEditText.setText("");
        }
    }

    // Xử lý sự kiện nhấn nút back trên Toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Hoặc finish(); nếu bạn muốn kết thúc activity này
        return true;
    }
}
