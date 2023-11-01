package com.sjbt.sdk.sample.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.NavHostController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sjbt.sdk.sample.BuildConfig
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseActivity
import com.sjbt.sdk.sample.utils.findNavControllerInNavHost

class MainActivity : BaseActivity() {

    private lateinit var navController: NavHostController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navController = findNavControllerInNavHost(R.id.nav_host)
        appBarConfiguration = AppBarConfiguration.Builder(
            R.id.deviceFragment, R.id.combineFragment
        ).build()
        bottomNavigationView = findViewById(R.id.bottom_nav_view)
        bottomNavigationView.setupWithNavController(navController)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val id = destination.id
            val shouldVisible = id == R.id.deviceFragment  || id == R.id.combineFragment
            bottomNavigationView.isVisible = shouldVisible
        }
        Toast.makeText(this, "v" + BuildConfig.VERSION_NAME, Toast.LENGTH_LONG).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }
}