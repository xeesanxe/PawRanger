package com.example.pawranger

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.example.pawranger.utils.SessionManager
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val sessionManager = SessionManager(this)
        if (sessionManager.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNav = findViewById<View>(R.id.bottom_navigation)
        val bottomNavContainer = findViewById<View>(R.id.cv_bottom_nav)
        
        if (bottomNav is NavigationBarView) {
            bottomNav.setOnItemSelectedListener { item ->
                val navOptions = NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setRestoreState(true)
                    .setPopUpTo(
                        navController.graph.startDestinationId,
                        inclusive = false,
                        saveState = true
                    )
                    .setEnterAnim(R.anim.fade_in)
                    .setExitAnim(R.anim.fade_out)
                    .setPopEnterAnim(R.anim.fade_in)
                    .setPopExitAnim(R.anim.fade_out)
                    .build()
                
                navController.navigate(item.itemId, null, navOptions)
                true
            }
            // Sync current destination with bottom nav
            navController.addOnDestinationChangedListener { _, destination, _ ->
                bottomNav.menu.findItem(destination.id)?.let {
                    it.isChecked = true
                }
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.splashFragment, R.id.loginFragment, R.id.registerFragment, R.id.navigation_profile -> {
                    bottomNavContainer?.visibility = View.GONE
                }
                else -> {
                    bottomNavContainer?.visibility = View.VISIBLE
                }
            }
        }
    }
}
