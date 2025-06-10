package com.imam.catatnada.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.google.android.material.card.MaterialCardView;
import com.imam.catatnada.R;
import com.imam.catatnada.utils.ThemeManager;

public class SettingsFragment extends Fragment {

    private ThemeManager themeManager;
    private ImageView checkSystem, checkLight, checkDark;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        themeManager = new ThemeManager(requireContext());

        // Inisialisasi Views
        MaterialCardView cardSystem = view.findViewById(R.id.card_system);
        MaterialCardView cardLight = view.findViewById(R.id.card_light);
        MaterialCardView cardDark = view.findViewById(R.id.card_dark);
        checkSystem = view.findViewById(R.id.check_system);
        checkLight = view.findViewById(R.id.check_light);
        checkDark = view.findViewById(R.id.check_dark);

        // Update UI sesuai tema yang tersimpan
        updateCheckmark();

        // Setup Click Listeners
        cardSystem.setOnClickListener(v -> selectTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM));
        cardLight.setOnClickListener(v -> selectTheme(AppCompatDelegate.MODE_NIGHT_NO));
        cardDark.setOnClickListener(v -> selectTheme(AppCompatDelegate.MODE_NIGHT_YES));
    }

    private void selectTheme(int themeMode) {
        themeManager.saveTheme(themeMode);
        AppCompatDelegate.setDefaultNightMode(themeMode);
        updateCheckmark();
    }

    private void updateCheckmark() {
        int currentTheme = themeManager.getTheme();
        checkSystem.setVisibility(currentTheme == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM ? View.VISIBLE : View.GONE);
        checkLight.setVisibility(currentTheme == AppCompatDelegate.MODE_NIGHT_NO ? View.VISIBLE : View.GONE);
        checkDark.setVisibility(currentTheme == AppCompatDelegate.MODE_NIGHT_YES ? View.VISIBLE : View.GONE);
    }
}