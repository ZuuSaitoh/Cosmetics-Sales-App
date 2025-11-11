package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.auth.AuthManager;
import com.example.myapplication.model.Payment;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.PaymentService;
import com.example.myapplication.network.dto.NotificationDTO;
import com.example.myapplication.notification.NotificationCreator;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VNPayActivity extends AppCompatActivity {

    private WebView webView;
    private String tmnCode = "B4EZDSFD";
    private String secretKey = "EHFO9MXOQYSJ2QV73STA5SY55QP123LU";
    private int orderId;
    private double amount;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vnpay);
        
        authManager = new AuthManager(this);

        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);

        Intent intent = getIntent();
        orderId = intent.getIntExtra("orderId", 0);
        amount = intent.getDoubleExtra("amount", 0.0);

        String paymentUrl = createPaymentUrl();
        webView.loadUrl(paymentUrl);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("app://vnpay/payment/callback")) {
                    handleCallback(url);
                    return true;
                }       
                return false;
            }
        });
    }

    private String createPaymentUrl() {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_OrderInfo = "Thanh toan don hang " + orderId;
        String orderType = "other";
        String vnp_TxnRef = String.valueOf(System.currentTimeMillis());
        String vnp_IpAddr = "127.0.0.1";
        String vnp_TmnCode = tmnCode;
        long amountValue = (long) (amount * 100);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amountValue));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", "app://vnpay/payment/callback");
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Date dt = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        String vnp_CreateDate = formatter.format(dt);
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    //Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = hmacSHA512(secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        return "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?" + queryUrl;
    }

    private void handleCallback(String url) {
        Uri uri = Uri.parse(url);
        String status = uri.getQueryParameter("vnp_ResponseCode");

        savePayment(orderId, amount, status);
    }

    private void savePayment(int orderId, double amount, String paymentStatus) {
        PaymentService paymentService = ApiClient.getRetrofit(this).create(PaymentService.class);
        Payment payment = new Payment(orderId, amount, paymentStatus);
        Call<Void> call = paymentService.createPayment(payment);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    if ("00".equals(paymentStatus)) {
                        // Tạo notification thanh toán thành công
                        createPaymentSuccessNotification(orderId, amount);
                        
                        Intent intent = new Intent(VNPayActivity.this, ActivityOrderSuccess.class);
                        startActivity(intent);
                    } else {
                        goToFailedActivity();
                    }
                } else {
                    goToFailedActivity();
                }
                finish();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                goToFailedActivity();
                finish();
            }
        });
    }

    private void goToFailedActivity() {
        Intent intent = new Intent(VNPayActivity.this, OrderFailedActivity.class);
        intent.putExtra("orderId", orderId);
        intent.putExtra("amount", amount);
        startActivity(intent);
    }

    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();

        } catch (Exception ex) {
            return "";
        }
    }
    
    /**
     * Tạo notification khi thanh toán VNPay thành công
     * Notification sẽ hiển thị ở tab "Của bạn" trong trang Notifications
     */
    private void createPaymentSuccessNotification(int orderId, double amount) {
        Long userId = authManager != null ? authManager.getUserId() : null;
        if (userId == null) {
            android.util.Log.w("VNPay", "Cannot create notification - userId is null");
            return;
        }
        
        String orderIdStr = String.valueOf(orderId);
        
        android.util.Log.d("VNPay", "Creating payment success notification - OrderID: " + orderIdStr + ", Amount: " + amount);
        
        // Tạo notification qua API
        NotificationCreator creator = new NotificationCreator(this);
        creator.notifyPaymentSuccess(userId, orderIdStr, amount, new NotificationCreator.OnNotificationCreatedListener() {
            @Override
            public void onSuccess(NotificationDTO notification) {
                android.util.Log.d("VNPay", "✅ Payment notification created successfully - ID: " + notification.getNotificationId());
                // Không cần show gì cho user - notification sẽ hiển thị trong NotificationsFragment
            }
            
            @Override
            public void onError(String error) {
                // Silent fail - không ảnh hưởng UX chính
                android.util.Log.e("VNPay", "Failed to create payment notification: " + error);
            }
        });
    }
}
