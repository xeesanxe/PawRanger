package com.example.pawranger

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Hanya berikan padding atas untuk status bar
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNavContainer = findViewById<View>(R.id.cv_bottom_nav)
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Berikan padding bawah pada NavHost agar konten tidak tertutup bottom nav melayang
        findViewById<View>(R.id.nav_host_fragment).setPadding(0, 0, 0, 100)
        
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
            // Sync current destination with bottom nav
            navController.addOnDestinationChangedListener { _, destination, _ ->
                bottomNavView.menu.findItem(destination.id)?.let {
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
