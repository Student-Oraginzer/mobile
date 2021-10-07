package ru.ulsu.moais.studnetorganizer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

import ru.ulsu.moais.studnetorganizer.R;

public class Attestation extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View attestationView
                = inflater.inflate(R.layout.fragments_attestation, container,false);
        TabLayout attestationTabs = attestationView.findViewById(R.id.attestation_tabs);
        ViewPager2 attestationViewPager = attestationView.findViewById(R.id.attestation_viewpager);
        return attestationView;
    }
}
