package com.imam.catatnada;

import android.app.Application;
import com.imam.catatnada.utils.ThemeManager;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Terapkan tema yang tersimpan saat aplikasi pertama kali dibuat
        new ThemeManager(this).applyTheme();
    }
}