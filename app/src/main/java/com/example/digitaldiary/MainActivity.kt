package com.example.digitaldiary

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.room.Room
import com.example.digitaldiary.database.AppDatabase
import com.example.digitaldiary.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices


class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: ViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            // Permission granted
        } else {
            Toast.makeText(
                this,
                applicationContext.resources.getString(R.string.permission_denied),
                Toast.LENGTH_LONG
            ).show()
        }
    }

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

        requestLocationPermissions()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fillDatabaseWithSampleNotes()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun setUpNavController(navController: NavController) {
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.NoteFragment -> {
                    binding.fab.setOnClickListener { view ->
                        AlertDialog.Builder(this).setTitle(getString(R.string.choose_an_option))
                            .setItems(
                                arrayOf(
                                    getString(R.string.record_audio),
                                    getString(R.string.make_a_photo)
                                )
                            ) { _, which ->
                                when (which) {
                                    0 -> viewModel.navigateToAudio()
                                    1 -> viewModel.navigateToPaint()
                                }
                            }.show()
                    }
                    binding.fab.setImageDrawable(
                        ContextCompat.getDrawable(
                            this, R.drawable.ic_attach
                        )
                    )
                }

                R.id.MainFragment -> {
                    binding.fab.setOnClickListener { viewModel.navigateCreateNote() }
                    binding.fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_add))
                }
            }
        }
    }

    private fun requestLocationPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission granted
            }

            else -> {
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    fun getLocation(callback: (String) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            callback(getString(R.string.unknown_city))
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val geocoder = Geocoder(this)
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val cityName = addresses[0].locality ?: getString(R.string.unknown_city)
                    callback(cityName)
                } else callback(getString(R.string.unknown_city))
            } else callback(getString(R.string.unknown_city))
        }
    }

    fun hideFab() {
        binding.fab.hide()
    }

    fun showFab() {
        binding.fab.show()
    }

    companion object {
        lateinit var db: AppDatabase
    }

    override fun onDestroy() {
        super.onDestroy()
        db.close()
    }

    private fun fillDatabaseWithSampleNotes() {
        for (i in 1..10) {
            val note = viewModel.generateSampleNote("Sample Note $i")
            viewModel.insertSampleNote(note)
        }
    }


}