package com.example.kanjeng.liveevent;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    public DB_Controller controller;
    public EditText tnick,temail,tpassword;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tnick = (EditText) findViewById(R.id.editNick);
        temail = (EditText)findViewById(R.id.editEmail);
        tpassword = (EditText)findViewById(R.id.editPassword);

        if(isServicesOK()){
            Log.d(TAG, "isServicesOK : mulai");
            controller = new DB_Controller(this,"",null,1);
            mAuth = FirebaseAuth.getInstance();
            mAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    Log.d(TAG, "onAuthStateChanged : mulai");
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        Log.d(TAG, "onAuthStateChanged : masih signin");
                        // User is signed in
                        int jumlah_user=cekJmlUser();
                        if (jumlah_user==1){
                            Log.d(TAG, "onAuthStateChanged : masih sign in,jumlah user 1");
                            Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                            Toast.makeText(MainActivity.this,"Already Sign in",Toast.LENGTH_SHORT).show();
                            //tampilkan maps
                            Intent intent = new Intent(MainActivity.this,MapActivity.class);
                            startActivity(intent);
                        }
                        else {
                            Log.d(TAG, "onAuthStateChanged : berhasil signin,jumlah user <> 1");
                            Toast.makeText(MainActivity.this,"Already Sign out",Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                            controller.truncateData();
                            //dialog register
                        }
                    } else {
                        Log.d(TAG, "onAuthStateChanged : belum signin");
                        // User is signed out
                        int jumlah_user=cekJmlUser();
                        if (jumlah_user==1){
                            Log.d(TAG, "onAuthStateChanged : belum signin,jumlah user 1,coba pakai email:"+controller.getEmail()
                                    +" pass:"+controller.getPassword());
                            //sign in
//                            mAuth.signInWithEmailAndPassword(controller.getEmail(), controller.getPassword());
                            mAuth.signInWithEmailAndPassword(controller.getEmail(), controller.getPassword())
                                    .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                                            // If sign in fails, display a message to the user. If sign in succeeds
                                            // the auth state listener will be notified and logic to handle the
                                            // signed in user can be handled in the listener.
                                            if (!task.isSuccessful()) {
                                                controller.truncateData();
                                                Log.w(TAG, "signInWithEmail:failed", task.getException());
                                                Toast.makeText(MainActivity.this, R.string.auth_failed,
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                            else {
                                                Toast.makeText(MainActivity.this,"Manual Sign in successfully",Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(MainActivity.this,MapActivity.class);
                                                startActivity(intent);
                                                Toast.makeText(MainActivity.this, "Successfull Register&Signin",
                                                        Toast.LENGTH_SHORT).show();
                                            }

                                            // ...
                                        }
                                    });
                        }
                        else {
                            Log.d(TAG, "onAuthStateChanged:signed_out");
                            Toast.makeText(MainActivity.this,"Reset data",Toast.LENGTH_SHORT).show();
//                            mAuth.signOut();
                            controller.truncateData();
                            //dialog register
                        }
                    }
                    // ...
                }
            };
            Log.d(TAG, "selesai proses signin");
//            init();
        }


    }
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void registerUser(View view){
        Log.d(TAG, "registerUser : begin");
        final String nick = tnick.getText().toString().trim();
        final String email = temail.getText().toString().trim();
        final String password = tpassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please provide an email",Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this,"Input the password",Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(nick)){
            Toast.makeText(this,"Input nick",Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "createUserWithEmailAndPassword :"+ task.isSuccessful());
                int jumlah_user=cekJmlUser();
                if (jumlah_user==0) {
                    controller.insert_email(email, password, nick);
                }
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "signInWithEmail:failed", task.getException());
                                    Toast.makeText(MainActivity.this, R.string.auth_failed,
                                            Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Intent intent = new Intent(MainActivity.this,MapActivity.class);
                                    startActivity(intent);
                                    Toast.makeText(MainActivity.this, "Successfull Register&Signin",
                                            Toast.LENGTH_SHORT).show();
                                }

                                // ...
                            }
                        });
            }
        });
    }

    public int cekJmlUser(){
        Cursor cursor = controller.getReadableDatabase().rawQuery("SELECT count(*) from USER",null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }
    private void init(){
        Button btnMap = (Button)findViewById(R.id.btnMap);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,MapActivity.class);
                startActivity(intent);
            }
        });
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
        if (available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map request
            Log.d(TAG,"isServicesOK: Google Play Services is working");
            return true;
        }
        else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            Log.d(TAG,"isServicesOK : an error occur but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this,available,ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else {
            Toast.makeText(this,"You can't make map request",Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}
