package com.example.notetakingapp


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.notetakingapp.viewmodel.FilterType
import com.example.notetakingapp.viewmodel.NoteTakingViewModel
import com.google.android.material.navigation.NavigationView

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity(R.layout.activity_main), NavigationView.OnNavigationItemSelectedListener {

    private val viewModel: NoteTakingViewModel by viewModels {
        NoteTakingViewModel.NoteTakingViewModelFactory(
            (application as BaseApplication).database.noteDao()
        )
    }

    lateinit var navController: NavController
    lateinit var appBarConfiguration: AppBarConfiguration
    private var checkedMenuItem: MenuItem? = null
    var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)

        val navView = findViewById<NavigationView>(R.id.navigation_view)
        navView.menu.findItem(R.id.filterAll_menu_item).setOnMenuItemClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            onNavigationItemSelected(it)
        }

        val subMenu = navView.menu.addSubMenu(R.id.filterCategory_menu_group, 0, 0, "Categories")
        viewModel.noteCategories.observe(this) { category ->
            subMenu.clear()
            if (category.isEmpty()){
                subMenu.add(R.id.filterCategory_menu_group, 1, 100,"No categories yet").isEnabled = false
            } else{
                category.forEachIndexed { index, it ->
                    subMenu.add(R.id.filterCategory_menu_group, index, index * 100,  it).setOnMenuItemClickListener { menu ->
                        menu.isChecked = true
                        checkedMenuItem = menu
                        drawerLayout.closeDrawer(GravityCompat.START)
                        onNavigationItemSelected(menu)
                    }
                }
            }
            subMenu.setGroupCheckable(R.id.filterCategory_menu_group, true, true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.filterAll_menu_item){
            checkedMenuItem?.isChecked = false
            viewModel.filterNotes(FilterType.CATEGORY, "")
        } else {
            viewModel.filterNotes(FilterType.CATEGORY, item.toString())
        }
        return true
    }
}