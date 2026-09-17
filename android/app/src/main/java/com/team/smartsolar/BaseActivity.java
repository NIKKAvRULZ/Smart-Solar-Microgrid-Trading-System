package com.team.smartsolar;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BaseActivity extends AppCompatActivity {

    /**
     * Call this method from any activity's onCreate to activate the bottom navigation bar.
     * @param currentMenuItemId The menu item ID corresponding to the current activity (e.g., R.id.nav_dashboard)
     */
    protected void setupBottomNavigation(int currentMenuItemId) {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        if (bottomNav != null) {
            bottomNav.setSelectedItemId(currentMenuItemId);

            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                // If user clicks the tab they are already on, do nothing
                if (itemId == currentMenuItemId) {
                    return true;
                }

                Intent intent = null;
                if (itemId == R.id.nav_dashboard) {
                    intent = new Intent(this, DashboardActivity.class);
                } else if (itemId == R.id.nav_booking) {
                    intent = new Intent(this, MyBookingsActivity.class);
                } else if (itemId == R.id.nav_profile) {
                    intent = new Intent(this, ProfileActivity.class);
                }

                if (intent != null) {
                    // Brings an existing instance to the front instead of re-creating duplicates
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    overridePendingTransition(0, 0); // Disable transition animation
                    return true;
                }
                return false;
            });
        }
    }
}