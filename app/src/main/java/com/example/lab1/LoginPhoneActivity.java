package com.example.lab1;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginPhoneActivity extends AppCompatActivity {
    private EditText edtPhoneNumber, edtOTP;
    private Button btnSendOTP, btnVerifyOTP;
    private FirebaseAuth mAuth;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_phone);

        edtPhoneNumber = findViewById(R.id.et_phone_number);
        edtOTP = findViewById(R.id.et_otp);
        btnSendOTP = findViewById(R.id.btn_send_otp);
        btnVerifyOTP = findViewById(R.id.btn_verify_otp);

        mAuth = FirebaseAuth.getInstance();

        // Gửi OTP khi nhấn nút Send Code
        btnSendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = edtPhoneNumber.getText().toString().trim();

                // Kiểm tra nếu số điện thoại hợp lệ
                if (!phoneNumber.isEmpty()) {
                    // Thêm mã quốc gia nếu thiếu (mặc định +84 cho Việt Nam)
                    if (!phoneNumber.startsWith("+")) {
                        phoneNumber = "+84" + phoneNumber.substring(1); // Loại bỏ số 0 đầu và thêm +84
                    }

                    sendVerificationCode(phoneNumber);
                } else {
                    Toast.makeText(LoginPhoneActivity.this, "Vui lòng nhập SĐT", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Xác thực OTP khi nhấn nút Verify Code
        btnVerifyOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = edtOTP.getText().toString().trim();
                if (!code.isEmpty()) {
                    verifyCode(code);
                } else {
                    Toast.makeText(LoginPhoneActivity.this, "Vui lòng nhập mã OTP", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)              // Số điện thoại để gửi OTP
                .setTimeout(60L, TimeUnit.SECONDS)       // Thời gian hết hạn của OTP
                .setActivity(this)                       // Activity hiện tại
                .setCallbacks(mCallbacks)                // Callback khi OTP được gửi hoặc thất bại
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);    // Gửi OTP
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    // Tự động điền mã OTP nếu thành công
                    String code = credential.getSmsCode();
                    if (code != null) {
                        edtOTP.setText(code);  // Điền mã OTP vào ô
                        verifyCode(code);      // Tự động xác thực mã
                    }
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    // Khi việc gửi OTP thất bại
                    Toast.makeText(LoginPhoneActivity.this, "Lỗi xác thực: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    super.onCodeSent(s, token);
                    verificationId = s; // Lưu mã xác thực OTP

                    // Hiển thị phần nhập OTP sau khi mã đã được gửi
                    edtOTP.setVisibility(View.VISIBLE);
                    btnVerifyOTP.setVisibility(View.VISIBLE);
                    Toast.makeText(LoginPhoneActivity.this, "Mã OTP đã được gửi", Toast.LENGTH_SHORT).show();
                }
            };

    private void verifyCode(String code) {
        // Tạo đối tượng PhoneAuthCredential từ mã OTP
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        // Đăng nhập Firebase bằng OTP
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(LoginPhoneActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LoginPhoneActivity.this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }
}