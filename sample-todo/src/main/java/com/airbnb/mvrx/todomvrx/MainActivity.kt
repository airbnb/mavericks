package com.airbnb.mvrx.todomvrx

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.airbnb.mvrx.todomvrx.todoapp.R
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private val drawerLayout by lazy { findViewById<DrawerLayout>(R.id.drawer_layout) }

    private val navController: NavController
        get() = findNavController(R.id.nav_host)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView = findViewById<NavigationView>(R.id.nav_view)
        val toolbar: Toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        navView.setupWithNavController(navController)
        setupActionBarWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isDrawer = destination.isDrawerDestination()
            toolbar.setNavigationIcon(if (isDrawer) R.drawable.ic_menu else R.drawable.ic_back)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            if (navController.currentDestination?.isDrawerDestination() == true) {
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
