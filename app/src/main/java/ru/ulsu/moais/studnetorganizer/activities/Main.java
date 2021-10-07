package ru.ulsu.moais.studnetorganizer.activities;

import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Objects;

import ru.ulsu.moais.studnetorganizer.R;
import ru.ulsu.moais.studnetorganizer.fragments.Home;

public class Main extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activities_main);
        // If user signed with phone asking they name
        if (getIntent().hasExtra("phoneSignUp")) setUserName();
        setView();
    }

    // Adding BackStack for properly switching fragments
    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    // Setting UserName method
    private void setUserName() {
        // Getting Firebase user instance
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Adding name field
        EditText name = new EditText(this);
        name.setText(Objects.requireNonNull(user).getDisplayName());

        // Creating alert
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.what_is_your_name)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.next), (dialogInterface, i) -> {
                    // Pushing new name into Firebase
                    Objects.requireNonNull(user).updateProfile(
                            new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name.getText().toString()).build());
                    // Recreating activity
                    recreate();
                });

        // Setting name field
        builder.setView(name);
        // Showing alert
        builder.create().show();
    }

    // Setting main view method
    private void setView() {
        Fragment home = new Home();
        home.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_frame, home)
                .disallowAddToBackStack()
                .commit();
    }
}