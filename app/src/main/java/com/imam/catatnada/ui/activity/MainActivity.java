package com.imam.catatnada.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.imam.catatnada.R;
import com.imam.catatnada.api.ApiService;
import com.imam.catatnada.api.LastFmModels;
import com.imam.catatnada.api.RetrofitClient;
import com.imam.catatnada.ui.fragment.SettingsFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false); // Disable back button
        getSupportActionBar().setDisplayShowHomeEnabled(false); // Disable home button

        // Set BottomNavigationView
        BottomNavigationView bottomNavView = findViewById(R.id.bottom_nav);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        
        // Setup bottom navigation with NavController
        NavigationUI.setupWithNavController(bottomNavView, navController);

        // Update toolbar title based on current destination
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination != null) {
                String label = destination.getLabel().toString();
                int destinationId = destination.getId();
                
                // Tambahkan emoji sesuai dengan destination menggunakan if-else
                if (destinationId == R.id.trendingFragment) {
                    label = label + " 🔥"; // Api untuk trending
                } else if (destinationId == R.id.searchFragment) {
                    label = label + " 🔍"; // Kaca pembesar untuk search
                } else if (destinationId == R.id.playlistsFragment) {
                    label = label + " 📋" ; // Clipboard untuk playlists
                } else if (destinationId == R.id.playlistDetailFragment && arguments != null) {
                    String playlistName = arguments.getString("playlistName", "Playlist");
                    label = label + "🎵" ; // Note musik untuk playlist detail
                }
                
                toolbar.setTitle(label);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            // Navigate to settings using NavController
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment);
            if (navHostFragment != null) {
                NavController navController = navHostFragment.getNavController();
                navController.navigate(R.id.settingsFragment);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}