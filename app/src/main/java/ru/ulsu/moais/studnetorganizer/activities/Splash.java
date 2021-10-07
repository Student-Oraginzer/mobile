package ru.ulsu.moais.studnetorganizer.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import ru.ulsu.moais.studnetorganizer.R;
import ru.ulsu.moais.studnetorganizer.utils.Utils;

public class Splash extends AppCompatActivity {

    String mVerificationId;
    PhoneAuthProvider.ForceResendingToken mResendToken;
    AlertDialog progressBar;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setting main theme (Because we have splash theme)
        setTheme(R.style.Theme_StudentOrganizer);
        super.onCreate(savedInstanceState);

        //Initialize FA before setting view because it avoid some bugs
        auth = FirebaseAuth.getInstance();
        auth.useAppLanguage();
        if (getIntent().hasExtra("logout")) {
            auth.signOut();
        }

        //Setting view
        setContentView(R.layout.activities_splash);

        //Loading ProgressBar
        progressBar = Utils.loadingBar(this);

        //Initialize components
        ImageView imageView = findViewById(R.id.splash_image);
        TextView textView = findViewById(R.id.splash_title);
        CardView googleButton = findViewById(R.id.splash_google_button);
        CardView phoneButton = findViewById(R.id.splash_phone_button);
        AnimatedVectorDrawable drawable = (AnimatedVectorDrawable)
                AppCompatResources.getDrawable(this, R.drawable.avd_heart_fill);

        //Starting animations
        textView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.text_fade_in));
        imageView.setImageDrawable(drawable);
        Objects.requireNonNull(drawable).start();

        //Setting heart animation callback
        drawable.registerAnimationCallback(new Animatable2.AnimationCallback() {
            @SuppressLint("ClickableViewAccessibility")
            public void onAnimationEnd(Drawable drawable) {
                if (auth.getCurrentUser() != null) {
                    //If user logged in starting Main activity
                    Utils.LoadHomeData(Splash.this);
                } else {
                    //Else showing login buttons
                    googleButton.setVisibility(View.VISIBLE);
                    phoneButton.setVisibility(View.VISIBLE);

                    //Starting animations of login buttons
                    googleButton.startAnimation(
                            AnimationUtils.loadAnimation(Splash.this, R.anim.text_fade_in));
                    phoneButton.startAnimation(
                            AnimationUtils.loadAnimation(Splash.this, R.anim.text_fade_in));

                    //Adding Touch and Click listeners for animations and auth procedures
                    googleButton.setOnClickListener(Splash.this::googleAuth);
                    googleButton.setOnTouchListener(Utils::touchAnimation);
                    phoneButton.setOnClickListener(Splash.this::phoneAuth);
                    phoneButton.setOnTouchListener(Utils::touchAnimation);

                }
            }
        });
    }

    //Proceed Google auth with code from Google Firebase
    private void googleAuth(View view) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(Splash.this, gso);
        // Ебал я в рот этот deprecated
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), 0);
    }

    //Proceed Phone auth with code from Google Firebase
    private void phoneAuth(View view) {
        EditText phone = new EditText(Splash.this);
        phone.setPadding(32, 32, 32, 32);
        phone.setText("+7");

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(Splash.this)
                .setTitle(R.string.phone_auth)
                .setPositiveButton(getString(R.string.sign_in), (dialogInterface, i) -> {

                    if (!phone.getText().toString().equals("+7")
                            && phone.getText().toString().startsWith("+7")) {

                        progressBar.show();

                        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                                .setPhoneNumber(phone.getText().toString())
                                .setTimeout(60L, TimeUnit.SECONDS)
                                .setActivity(Splash.this)
                                .setCallbacks(new PhoneAuthProvider
                                        .OnVerificationStateChangedCallbacks() {

                                    @Override
                                    public void onVerificationCompleted(@NonNull PhoneAuthCredential
                                                                                credential) {
                                        // Phone Sign In was successful, authenticate with Firebase
                                        signInWithPhoneAuthCredential(credential, progressBar);
                                    }

                                    @Override
                                    public void onVerificationFailed(@NonNull FirebaseException e) {
                                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                            Toast.makeText(Splash.this, getString(R.string.phone_wrong),
                                                    Toast.LENGTH_SHORT).show();
                                            progressBar.cancel();

                                        } else if (e instanceof FirebaseTooManyRequestsException) {
                                            Toast.makeText(Splash.this,
                                                    getString(R.string.sms_quota),
                                                    Toast.LENGTH_SHORT).show();
                                            progressBar.cancel();
                                        }
                                    }

                                    @Override
                                    public void onCodeSent(@NonNull String verificationId,
                                                           @NonNull PhoneAuthProvider
                                                                   .ForceResendingToken token) {
                                        mVerificationId = verificationId;
                                        mResendToken = token;
                                    }
                                })
                                .build();
                        PhoneAuthProvider.verifyPhoneNumber(options);
                    } else {
                        Toast.makeText(Splash.this, getString(R.string.phone_error),
                                Toast.LENGTH_SHORT).show();
                    }
                });
        builder.setView(phone);
        builder.create().show();
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential, AlertDialog dialog) {
        auth.signInWithCredential(credential).addOnCompleteListener(Splash.this, task -> {
            if (task.isSuccessful()) {
                Utils.LoadHomeData(Splash.this);
            } else {
                if (task.getException() instanceof
                        FirebaseAuthInvalidCredentialsException) {
                    dialog.cancel();
                    Toast.makeText(Splash.this, task.getException().toString(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            // Google Sign In was successful, authenticate with Firebase
            GoogleSignInAccount account = task.getResult(ApiException.class);
            firebaseAuthWithGoogle(account.getIdToken(), progressBar);
        } catch (ApiException e) {
            progressBar.cancel();
        }
    }

    private void firebaseAuthWithGoogle(String idToken, AlertDialog dialog) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential).addOnCompleteListener(Splash.this, task -> {
            if (task.isSuccessful()) {
                Utils.LoadHomeData(Splash.this);
            } else {
                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                    dialog.cancel();
                    Toast.makeText(Splash.this, task.getException().toString(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}