package com.example.diy_simulator;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    public static int TIME_OUT = 1001;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[a-zA-Z0-9!@.#$%^&*?_~]{4,16}$");
    ProgressDialog dialog;

    private EditText email_join, pwd_join, check_pwd_join, name_join, address_join, phone_number_join, store_name_join;

    private TextView check_show; //비밀번호 일치 확인 텍스트
    private Button sign_up_btn;

    FirebaseAuth firebaseAuth;
    FirebaseUser mFirebaseUser;

    private String email = "";
    private String password = "";
    private String name = "";
    private String address = "";
    private String phone_number = "";
    private String store_name = "";
    long count = 0;
    String whois = ""; //판매자 가입인지 고객 가입인지 확인

    // Write a message to the database
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("구매자");
    private DatabaseReference myRef2 = database.getReference("판매자");

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        email_join = (EditText) findViewById(R.id.emailInput);
        pwd_join = (EditText) findViewById(R.id.passwordInput);
        check_pwd_join = (EditText) findViewById(R.id.passwordCheck);
        name_join = (EditText) findViewById(R.id.nameInput);
        phone_number_join = (EditText) findViewById(R.id.phonenumberInput);
        address_join = (EditText) findViewById(R.id.addressInput);
        store_name_join = (EditText) findViewById(R.id.storenameInput);

        check_show = (TextView) findViewById(R.id.checkText);

        sign_up_btn = (Button) findViewById(R.id.signup_button);

        //sign in 액티비티에서 스트링 받아옴
        final Intent intent = getIntent();
        String who = intent.getStringExtra("who");
        whois = who;
        if(who.equals("customer")) store_name_join.setVisibility(View.INVISIBLE);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.useAppLanguage();

        //비밀번호 일치 여부 확인
        //일치하면 회원가입 버튼 활성화
        check_pwd_join.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String comp1 = pwd_join.getText().toString();
                String comp2 = check_pwd_join.getText().toString();

                if (comp1.equals(comp2)) {
                    check_show.setText("비밀번호가 일치합니다");
                    sign_up_btn.setEnabled(true);
                } else {
                    check_show.setText("비밀번호가 일치하지 않습니다");
                }
            }
        });

        //회원가입 하면 유저를 등록
        sign_up_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = ProgressDialog.show(SignUpActivity.this, "회원가입 진행 중입니다!"
                        , "이메일로 인증 메일이 발송됩니다.", true);
                registerUser();

                email_join.setText(null);
                pwd_join.setText(null);
                check_pwd_join.setText(null);
                name_join.setText(null);
                phone_number_join.setText(null);
                address_join.setText(null);
                store_name_join.setText(null);
            }
        });
    }

    // 이메일 유효성 검사
    private boolean isValidEmail() {
        if (email.isEmpty()) {
            // 이메일 공백
            return false;
        } else return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // 비밀번호 유효성 검사
    private boolean isValidPasswd() {
        if (password.isEmpty()) {
            // 비밀번호 공백
            return false;
        } else return PASSWORD_PATTERN.matcher(password).matches();
    }

    // 회원가입
    //firebase에 이메일 인증 방식 사용
    //등록된 이메일이 아니면 firebase authentication에 등록되고 인증 메일 발송
    private void createUser(final String email, String password) {

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 회원가입 성공
                            mFirebaseUser = firebaseAuth.getCurrentUser();
                            if (mFirebaseUser != null)
                                mFirebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            dialog.dismiss();
                                            Toast.makeText(SignUpActivity.this,
                                                    "인증 메일이 " + mFirebaseUser.getEmail()+ "로 발송되었습니다. 인증 후 이용해주세요.",
                                                    Toast.LENGTH_LONG).show();

                                            //디비에 유저 등록 - 고객
                                            if(whois.equals("customer")) {
                                                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                            count++;
                                                        }
                                                        //구매자 - 유저정보를 디비에 넣는다.
                                                        Customer customer= new Customer(email, name, phone_number, address);

                                                        StringTokenizer st = new StringTokenizer(email, "@");
                                                        if (count >= 9) {
                                                            myRef.child("user0" + (count + 1) + ":" + st.nextToken()).setValue(customer);
                                                        } else
                                                            myRef.child("user00" + (count + 1) + ":" + st.nextToken()).setValue(customer);
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                            //디비에 유저 등록 - 판매자
                                            else {
                                                myRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                            count++;
                                                        }
                                                        //판매자 - 유저정보를 디비에 넣는다.
                                                        Seller seller = new Seller(email, name, phone_number, address, store_name);

                                                        StringTokenizer st = new StringTokenizer(email, "@");
                                                        if (count >= 9) {
                                                            myRef2.child("user0" + (count + 1) + ":" + st.nextToken()).setValue(seller);
                                                        } else
                                                            myRef2.child("user00" + (count + 1) + ":" + st.nextToken()).setValue(seller);
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                            startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                                        } else {                                             //메일 보내기 실패
                                            Toast.makeText(SignUpActivity.this,
                                                    "Failed to send verification email.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                        } else {
                            // 회원가입 실패
                            Toast.makeText(SignUpActivity.this, R.string.failed_signup, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //회원 등록 함수
    public void registerUser() {
        email = email_join.getText().toString();
        password = pwd_join.getText().toString();
        name = name_join.getText().toString();
        phone_number = phone_number_join.getText().toString();
        address = address_join.getText().toString();
        store_name = store_name_join.getText().toString();

        //이메일 입력 칸이 빈칸인 경우 알림
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "이메일을 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        //비밀번호 입력 칸이 빈칸인 경우 알림
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        //유저 등록 함수 실행
        if (isValidEmail() && isValidPasswd()) {
            createUser(email, password);
        }

    }

}
