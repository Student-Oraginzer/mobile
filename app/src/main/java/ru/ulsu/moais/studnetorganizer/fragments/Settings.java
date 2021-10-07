package ru.ulsu.moais.studnetorganizer.fragments;

import static ru.ulsu.moais.studnetorganizer.utils.Utils.MenuToJSONArray;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;

import ru.ulsu.moais.studnetorganizer.R;
import ru.ulsu.moais.studnetorganizer.adapters.CommonRecycler;
import ru.ulsu.moais.studnetorganizer.utils.Utils;

public class Settings extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //Initialize fragment view
        View settingsView = inflater.inflate(R.layout.fragments_setings, container, false);

        try {
            Utils.setCommonRecycler(settingsView.findViewById(R.id.settings_recycler),
                    MenuToJSONArray(R.menu.settings, requireContext()),
                    new LinearLayoutManager(requireContext()), getParentFragmentManager(),
                    R.layout.recycler_linear);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return settingsView;
    }
}
