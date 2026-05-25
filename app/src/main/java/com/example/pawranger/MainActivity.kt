package com.example.pawranger

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.example.pawranger.utils.ViewUtils
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Force Light Mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val mainView = findViewById<View>(R.id.main)
        if (mainView != null) {
            ViewUtils.setupSystemBarsInsets(mainView)
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        
        if (navHostFragment != null) {
            val navController = navHostFragment.navController
            val bottomNavContainer = findViewById<View>(R.id.cv_bottom_nav)
            val bottomNavView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

            val fragmentContainer = findViewById<View>(R.id.nav_host_fragment)
            if (fragmentContainer != null) {
                fragmentContainer.setPadding(0, 0, 0, 100)
            }
            
            if (bottomNavView != null) {
                bottomNavView.setOnItemSelectedListener { item ->
                    val navOptions = NavOptions.Builder()
                        .setLaunchSingleTop(true)
                        .setRestoreState(true)
                        .setPopUpTo(
                            navController.graph.startDestinationId,
                            inclusive = false,
                            saveState = true
                        )
                        .build()
                    
                    navController.navigate(item.itemId, null, navOptions)
                    true
                }
                navController.addOnDestinationChangedListener { _, destination, _ ->
                    val menuItem = bottomNavView.menu.findItem(destination.id)
                    if (menuItem != null) {
                        menuItem.isChecked = true
                    }
                }
            }

            navController.addOnDestinationChangedListener { _, destination, _ ->
                if (bottomNavContainer != null) {
                    when (destination.id) {
                        R.id.splashFragment, R.id.loginFragment, R.id.registerFragment, R.id.navigation_profile -> {
                            bottomNavContainer.visibility = View.GONE
                        }
                        else -> {
                            bottomNavContainer.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }
}
