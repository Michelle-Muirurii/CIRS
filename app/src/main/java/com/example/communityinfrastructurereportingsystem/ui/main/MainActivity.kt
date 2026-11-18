package com.example.communityinfrastructurereportingsystem.ui.main


import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.communityinfrastructurereportingsystem.R
import com.example.communityinfrastructurereportingsystem.data.local.SessionManager
import com.example.communityinfrastructurereportingsystem.data.model.UserRole
import com.example.communityinfrastructurereportingsystem.databinding.ActivityMainBinding
import com.example.communityinfrastructurereportingsystem.databinding.NavHeaderBinding
import com.example.communityinfrastructurereportingsystem.ui.auth.LoginActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Safety net: nobody gets in without logging in
        val session = SessionManager(this)
        if (!session.isLoggedIn()) {
            goToLogin()
            return
        }

        // 1. Use our Toolbar as the App Bar
        setSupportActionBar(binding.toolbar)

        // 2. Get the NavController from the NavHost
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // 3. Staff and residents start on different screens, so the graph is set here
        val isStaff = session.role == UserRole.STAFF
        val graph = navController.navInflater.inflate(R.navigation.nav_graph)
        graph.setStartDestination(
            if (isStaff) R.id.staffDashboardFragment else R.id.dashboardFragment
        )
        navController.graph = graph

        // 4. ...and see different drawer menus
        binding.navView.inflateMenu(
            if (isStaff) R.menu.drawer_menu_staff else R.menu.drawer_menu_resident
        )

        // 5. Top-level screens show the ☰ icon, all others show ←
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.dashboardFragment,
                R.id.submitReportFragment,
                R.id.myReportsFragment,
                R.id.staffDashboardFragment
            ),
            binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        // 6. Show who is logged in. The header already exists, so we BIND to it
        val headerBinding = NavHeaderBinding.bind(binding.navView.getHeaderView(0))
        headerBinding.headerNameTextView.text = session.userName
        headerBinding.headerEmailTextView.text = session.userEmail
    }

    // ☰ opens the drawer; ← goes back
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    // Turn main_menu.xml into the ⋮ menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_logout) {
            SessionManager(this).clear()   // forget who was logged in
            goToLogin()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // NEW_TASK + CLEAR_TASK closes every open screen, so Back cannot return to the app
    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}