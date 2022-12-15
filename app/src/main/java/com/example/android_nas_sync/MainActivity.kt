package com.example.android_nas_sync

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import com.example.android_nas_sync.R
import com.example.android_nas_sync.common.FileScanner
import com.example.android_nas_sync.common.TimeUtils
import com.example.android_nas_sync.databinding.ActivityMainBinding
import com.example.android_nas_sync.viewmodels.MappingsViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MappingsViewModel by viewModels()
    private var FOLDER_PICK_ACTIVITY_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode == FOLDER_PICK_ACTIVITY_CODE
            && resultData?.data != null) {
            resultData.data?.also { uri ->
                val mapping = viewModel.currentlyEditedMapping.value
                mapping?.sourceFolder = uri.toString()
                viewModel.currentlyEditedMapping.value = mapping
            }
        }
        super.onActivityResult(requestCode, resultCode, resultData)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_refresh -> handleRefresh()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun handleRefresh():Boolean{
        val mappings = viewModel.mappings.value
        mappings?.forEach { mapping ->
            val result = FileScanner.refreshMapping(mapping)
            if(result.success){
                mapping.lastSynced = TimeUtils.unixTimestampNow()
                viewModel.updateMapping(mapping)
            }
            else{
                // TODO show error against item
                Toast.makeText(this, "Error: ${result.errorMessage}"
                        , Toast.LENGTH_LONG).show()
            }
        }
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}