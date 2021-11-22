package iothoth.edlugora.com

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.dynamicfeatures.createGraph
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.fragment
import androidx.navigation.ui.NavigationUI
import iothoth.edlugora.com.ui.ProfileViewFragment


class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        navController.setGraph(R.navigation.nav_main)
        val navGraph = navController.graph
        navGraph.setStartDestination(R.id.profileViewFragment)
        navController.graph = navGraph


    }
}