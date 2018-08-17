package com.airbnb.mvrx.todomvrx

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.airbnb.mvrx.BaseMvRxActivity
import com.airbnb.mvrx.todomvrx.todoapp.R


class MainActivity : BaseMvRxActivity() {

    private val drawerLayout by lazy { findViewById<DrawerLayout>(R.id.drawer_layout) }

    private val navController: NavController
        get() = findNavController(R.id.nav_host)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: NavigationView = findViewById(R.id.nav_view)
        val toolbar: Toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        navView.setupWithNavController(navController)
        setupActionBarWithNavController(navController)

        navController.addOnNavigatedListener { _, destination ->
            val isDrawer = destination.isDrawerDestination()
            toolbar.setNavigationIcon(if (isDrawer) R.drawable.ic_menu else R.drawable.ic_back)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?) = when(item?.itemId ?: 0) {
        android.R.id.home -> {
            if (navController.currentDestination.isDrawerDestination()) {
                drawerLayout.openDrawer(GravityCompat.START)
            } else {
                navController.navigateUp()
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun NavDestination.isDrawerDestination() = id == R.id.tasksFragment || id == R.id.statisticsFragment
}