package ru.ulsu.moais.studnetorganizer.adapters;

import static ru.ulsu.moais.studnetorganizer.utils.Utils.SmartULSUAuth;
import static ru.ulsu.moais.studnetorganizer.utils.Utils.SmartULSUDeAuth;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import ru.ulsu.moais.studnetorganizer.R;
import ru.ulsu.moais.studnetorganizer.activities.Main;
import ru.ulsu.moais.studnetorganizer.activities.Splash;
import ru.ulsu.moais.studnetorganizer.fragments.Attestation;
import ru.ulsu.moais.studnetorganizer.fragments.Settings;
import ru.ulsu.moais.studnetorganizer.utils.Utils;

public class CommonRecycler extends RecyclerView.Adapter<CommonRecycler.ViewHolder> {

    int layout_id;
    JSONArray actions;
    FragmentManager fm;
    public CommonRecycler(int layout_id, JSONArray actions, FragmentManager fm) {
        this.layout_id = layout_id;
        this.actions = actions;
        this.fm = fm;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(layout_id, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            holder.title.setText(actions.getJSONObject(position).getString("title"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            holder.icon.setImageDrawable((Drawable) actions.getJSONObject(position).get("icon"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return actions.length();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        ImageView icon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.common_recycler_title);
            icon = itemView.findViewById(R.id.common_recycler_icon);

            itemView.setOnClickListener(this::onClick);
            itemView.setOnTouchListener(Utils::touchAnimation);
        }

        @SuppressLint("NonConstantResourceId")
        private void onClick(View view) {
            try {
                switch (actions.getJSONObject(getAdapterPosition()).getInt("id")) {
                    case R.id.timetable:
                        /* fm.beginTransaction()
                                .replace(R.id.main_frame, new Timetable())
                                .addToBackStack("Timetable")
                                .commit(); */
                        break;
                    case R.id.settings:
                        fm.beginTransaction()
                                .replace(R.id.main_frame, new Settings())
                                .addToBackStack("Settings")
                                .commit();
                        break;
                    case R.id.attestation:
                        fm.beginTransaction()
                                .replace(R.id.main_frame, new Attestation())
                                .addToBackStack("Attestation")
                                .commit();
                        break;
                    case R.id.smartULSUAuth:
                        SmartULSUAuth(view.getContext(),fm);
                        break;
                    case R.id.smartULSUDeAuth:
                        SmartULSUDeAuth(view.getContext(),fm);
                        break;
                    case R.id.logOut:
                        view.getContext().startActivity(
                                new Intent(view.getContext(), Splash.class)
                                        .putExtra("logout",true));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
