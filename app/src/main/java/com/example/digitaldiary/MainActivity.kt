package com.example.digitaldiary

import com.example.digitaldiary.database.AppDatabase
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.room.Room
import com.example.digitaldiary.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(
            applicationContext, AppDatabase::class.java, "diary"
        ).build()


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        viewModel = ViewModelProvider(this)[ViewModel::class.java]
        viewModel.errorMessage.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }


        val navController = findNavController(R.id.nav_host_fragment_content_main)


        setUpNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        viewModel.onBackClicked()
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun setUpNavController(navController: NavController) {
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.NoteFragment -> {
                    binding.fab.setOnClickListener { view ->
                        Toast.makeText(
                            view.context, "Attach functionality not implemented", Toast.LENGTH_SHORT
                        ).show()
                    }
                    binding.fab.setImageDrawable(
                        ContextCompat.getDrawable(
                            this, R.drawable.ic_attach
                        )
                    )
                }

                R.id.MainFragment -> {
                    binding.fab.setOnClickListener { viewModel.editNote() }
                    binding.fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_add))
                }
            }
        }
    }

    companion object {
        lateinit var db: AppDatabase
    }
}