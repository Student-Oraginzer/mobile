package ru.ulsu.moais.studnetorganizer.fragments;

import static ru.ulsu.moais.studnetorganizer.utils.Utils.MenuToJSONArray;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import ru.ulsu.moais.studnetorganizer.R;
import ru.ulsu.moais.studnetorganizer.utils.Utils;

public class Home extends Fragment {

    SharedPreferences authData;

    FirebaseUser user;

    TextView userName;
    TextView userMain;
    TextView weatherTemp;

    ImageView homeBottomSheetArrow;
    ImageView weatherImage;

    LinearLayout homeBottomSheet;
    LinearLayout weatherFull;

    CardView weatherPreview;

    BottomSheetBehavior<View> homeBottomSheetBehavior;

    RecyclerView homeRecycler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initialize components
        authData = requireContext().getSharedPreferences("authData", Context.MODE_PRIVATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //Initialize fragment view
        View homeView = inflater.inflate(R.layout.fragments_home, container, false);

        //Initialize components with fragment view
        user = FirebaseAuth.getInstance().getCurrentUser();
        userName = homeView.findViewById(R.id.home_userInfo_name);
        userMain = homeView.findViewById(R.id.home_userInfo_main);
        homeBottomSheet = homeView.findViewById(R.id.home_bottom_sheet);
        weatherPreview = homeView.findViewById(R.id.home_bottom_weather_preview);
        weatherFull = homeView.findViewById(R.id.home_bottom_weather_full);
        homeBottomSheetBehavior = BottomSheetBehavior.from(homeBottomSheet);
        homeBottomSheetArrow = homeView.findViewById(R.id.home_bottom_button_up);
        weatherImage = homeView.findViewById(R.id.home_bottom_weather_image);
        weatherTemp = homeView.findViewById(R.id.home_bottom_weather_temp);
        homeRecycler = homeView.findViewById(R.id.home_recycler);

        try {
            setWeather();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        try {
            setMainInfo();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            Utils.setCommonRecycler(homeRecycler, MenuToJSONArray(R.menu.home, requireContext()),
                    new GridLayoutManager(requireContext(), 2), getParentFragmentManager(),
                    R.layout.recycler_grid);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return homeView;
    }

    private void setWeather() throws JSONException, IOException {
        //Some z-index hack
        weatherPreview.setZ(999);

        weatherPreview.setOnClickListener(view -> {
            if (homeBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                homeBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                homeBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            homeBottomSheetArrow.animate()
                    .rotationBy(180).setDuration(300).start();
        });

        JSONObject main = new JSONObject(requireArguments().getString("main"))
                .getJSONObject("weather")
                .getJSONArray("list")
                .getJSONObject(0)
                .getJSONObject("main");
        JSONObject weather = new JSONObject(requireArguments().getString("main"))
                .getJSONObject("weather")
                .getJSONArray("list")
                .getJSONObject(0)
                .getJSONArray("weather")
                .getJSONObject(0);
        weatherTemp.setText(String.format("%s°C, %s",
                main.getString("temp").split("\\.")[0],
                weather.getString("description")));
        weatherImage.setImageDrawable(
                Drawable.createFromStream(
                        requireContext().getAssets().open(
                                "owmIcons/" + weather.get("icon") + "@2x.png"),
                        null));

        LinearLayout weatherForecastNext = new LinearLayout(requireContext());
        weatherFull.addView(weatherForecastNext);

        for (int i = 1; i < 5; i++) {
            LinearLayout wf = new LinearLayout(requireContext());
            wf.setOrientation(LinearLayout.VERTICAL);
            ImageView wfImage = new ImageView(requireContext());
            TextView wfInfo = new TextView(requireContext());
            wfInfo.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            JSONObject wfJSON = new JSONObject(requireArguments().getString("main"))
                    .getJSONObject("weather")
                    .getJSONArray("list")
                    .getJSONObject(i);
            LinearLayout.LayoutParams wfImageParams = new LinearLayout.LayoutParams(245,
                    245);
            wfImageParams.gravity = Gravity.CENTER;
            wf.addView(wfImage, wfImageParams);
            wf.addView(wfInfo);
            wfImage.setImageDrawable(
                    Drawable.createFromStream(
                            requireContext().getAssets().open(
                                    "owmIcons/"
                                            + wfJSON.getJSONArray("weather")
                                                .getJSONObject(0).get("icon")
                                            + "@2x.png"),
                            null));
            wfInfo.setText(String.format("%s °C\n%s",
                    wfJSON.getJSONObject("main").getString("temp"),
                    wfJSON.getJSONArray("weather").getJSONObject(0)
                            .getString("description")));

            LinearLayout.LayoutParams wfParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            wfParams.gravity = Gravity.CENTER;
            weatherForecastNext.addView(wf);
        }
    }

    private void setMainInfo() throws JSONException {
        userName.setText(String.format("%s %s", getString(R.string.hello),
                Objects.requireNonNull(Objects.requireNonNull(user).getDisplayName())
                        .split(" ")[0]));

        if (!authData.contains("smartULSU_code")) {
            userMain.setText(getString(R.string.accounts_pair));
        } else {
            JSONObject data = new JSONObject(authData.getString("smartULSU_all", "[]"));
            userMain.setText(String.format(getString(R.string.your_group_format),
                    data.getJSONArray("gradebooks")
                            .getJSONObject(0).getString("groupname")));
            // FIXME: 03.10.2021 ADD WEEK
        }
    }
}
