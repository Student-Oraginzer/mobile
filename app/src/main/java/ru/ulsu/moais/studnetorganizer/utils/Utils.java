package ru.ulsu.moais.studnetorganizer.utils;

import static com.android.volley.Request.Method.GET;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ru.ulsu.moais.studnetorganizer.R;
import ru.ulsu.moais.studnetorganizer.activities.Main;
import ru.ulsu.moais.studnetorganizer.adapters.CommonRecycler;
import ru.ulsu.moais.studnetorganizer.fragments.Home;

public class Utils {

    // Creating AlertDialog with ProgressBar
    public static AlertDialog loadingBar(Context context) {
        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setPadding(32, 32, 32, 32);
        MaterialAlertDialogBuilder progress = new MaterialAlertDialogBuilder(context)
                .setCancelable(false);
        progress.setView(progressBar);
        AlertDialog dialog = progress.create();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = 300;
        dialog.getWindow().setAttributes(lp);
        return dialog;
    }

    //This method parsing android menu xml files into JSON array
    @SuppressLint("NonConstantResourceId")
    public static JSONArray MenuToJSONArray(int menu_main, Context context) throws JSONException {

        //Getting auth data for replace actions in menu
        SharedPreferences authData =
                context.getSharedPreferences(
                        "authData", Context.MODE_PRIVATE);

        // Using some RestrictedApi for inflating menu
        @SuppressLint("RestrictedApi")
        Menu menu = new MenuBuilder(context);
        new MenuInflater(context).inflate(menu_main, menu);

        // Creating new JSON Array which will contain menu entries
        JSONArray temp = new JSONArray();

        for (int i = 0; i < menu.size(); i++) {

            // Checking for some entries
            switch (menu.getItem(i).getItemId()) {
                case R.id.smartULSUAuth:
                    if (authData.contains("smartULSU_code")) {
                        temp.put(new JSONObject()
                                .put("id", R.id.smartULSUDeAuth)
                                .put("title", context.getString(R.string.smartULSU_logout))
                                .putOpt("icon", menu.getItem(i).getIcon()));
                    } else {
                        temp.put(new JSONObject()
                                .put("id", menu.getItem(i).getItemId())
                                .put("title", menu.getItem(i).getTitle())
                                .putOpt("icon", menu.getItem(i).getIcon()));
                    }
                    break;
                default:
                    temp.put(new JSONObject()
                            .put("id", menu.getItem(i).getItemId())
                            .put("title", menu.getItem(i).getTitle())
                            .putOpt("icon", menu.getItem(i).getIcon()));
            }
        }
        return temp;
    }

    // Auth in SmartULSU Api system with some magic
    // Я ебал все тут описывать
    public static void SmartULSUAuth(Context context, FragmentManager fm) {

        //Creating alert view
        LinearLayout alertLinear = new LinearLayout(context);
        alertLinear.setOrientation(LinearLayout.VERTICAL);

        // Adding login field
        EditText login = new EditText(context);
        login.setHint(R.string.login);
        alertLinear.addView(login);

        // Adding password field
        EditText password = new EditText(context);
        password.setHint(R.string.password);
        alertLinear.addView(password);

        // Building alert
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.login_and_password)
                .setPositiveButton(context.getString(R.string.next), (dialogInterface, i) -> {

                    // Creating progress bar and alert for it and setting width
                    AlertDialog progressBar = loadingBar(context);

                    // Requesting ULSU's portal for auth and getting student id
                    Volley.newRequestQueue(context).add(new JsonObjectRequest(
                            GET,
                            "https://portal.ulsu.ru/CDO/authorized_user_id.php?login="
                                    + login.getText()
                                    + "&password="
                                    + password.getText(),
                            null,
                            response -> {
                                try {
                                    // Magic with student id
                                    if (response.getInt("id") == 0) {
                                        progressBar.dismiss();
                                        Toast.makeText(context,
                                                context.getString(
                                                        R.string.incorrect_logpass),
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Saving id and other info in SharedPrefs
                                        // TODO: Save data into Firebase database
                                        SharedPreferences authData =
                                                context.getSharedPreferences(
                                                        "authData", Context.MODE_PRIVATE);
                                        authData.edit().putString("smartULSU_code",
                                                String.valueOf(response.get("code")))
                                                .apply();
                                        authData.edit().putString("smartULSU_all",
                                                response.toString())
                                                .apply();
                                        // Dismissing progress bar alert
                                        progressBar.dismiss();
                                        // Load Home fragment
                                        fm.beginTransaction()
                                                .setCustomAnimations(R.anim.in_alpha,
                                                        R.anim.out_alpha)
                                                .replace(R.id.main_frame, new Home())
                                                .addToBackStack("Home")
                                                .commit();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            },
                            Throwable::printStackTrace
                    ));
                });

        // Show auth alert
        builder.setView(alertLinear).create().show();
    }

    // DeAuth user in SmartULSU
    public static void SmartULSUDeAuth(Context context, FragmentManager fm) {
        // Creating alert view
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.are_you_sure)
                .setPositiveButton(R.string.next, (dialogInterface, i) -> {

                    // Deleting user data
                    // TODO: Delete data ifrom Firebase database
                    SharedPreferences authData =
                            context.getSharedPreferences(
                                    "authData", Context.MODE_PRIVATE);
                    authData.edit().remove("smartULSU_code").apply();
                    authData.edit().remove("smartULSU_all").apply();
                    // Load Home fragment
                    fm.beginTransaction()
                            .setCustomAnimations(R.anim.in_alpha, R.anim.out_alpha)
                            .replace(R.id.main_frame, new Home())
                            .addToBackStack("Home")
                            .commit();
                });
        // Show DeAuth alert
        builder.create().show();
    }

    // Loading Main data in Splash (Slow but not ugly) before showing Home fragment
    public static void LoadHomeData(Activity context) {
        Volley.newRequestQueue(context).add(new JsonObjectRequest(
                GET,
                "https://api.zadli.me/so/main/",
                null,
                response -> {
                    context.startActivity(new Intent(context, Main.class)
                            .putExtra("main", response.toString()));
                    context.overridePendingTransition(R.anim.in_alpha, R.anim.out_alpha);
                    context.finish();
                },
                Throwable::printStackTrace
        ));
    }

    // Touch animation for different buttons and views
    public static boolean touchAnimation(View view, MotionEvent motionEvent) {
        ObjectAnimator animationX;
        ObjectAnimator animationY;
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            animationX = ObjectAnimator
                    .ofFloat(view, "ScaleX", 0.9f);
            animationY = ObjectAnimator
                    .ofFloat(view, "ScaleY", 0.9f);
        } else {
            animationX = ObjectAnimator
                    .ofFloat(view, "ScaleX", 1f);
            animationY = ObjectAnimator
                    .ofFloat(view, "ScaleY", 1f);
        }
        animationX.setDuration(150);
        animationY.setDuration(150);
        animationX.start();
        animationY.start();
        return false;
    }

    // Method for setup Common RecyclerView
    public static void setCommonRecycler(RecyclerView recycler, JSONArray menu,
                                         Object layoutManager, FragmentManager fm, int layout) {
        recycler.setLayoutManager((RecyclerView.LayoutManager) layoutManager);
        recycler.setAdapter(new CommonRecycler(layout, menu, fm));
        recycler.setNestedScrollingEnabled(false);
    }
}
