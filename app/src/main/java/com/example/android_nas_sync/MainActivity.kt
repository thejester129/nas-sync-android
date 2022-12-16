package com.example.android_nas_sync

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.android_nas_sync.common.FileScanner
import com.example.android_nas_sync.common.TimeUtils
import com.example.android_nas_sync.databinding.ActivityMainBinding
import com.example.android_nas_sync.models.Mapping
import com.example.android_nas_sync.viewmodels.MappingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


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
            this.lifecycleScope.launch(Dispatchers.IO){
                refreshMapping(mapping)
            }
        }
        return true
    }

    private suspend fun refreshMapping(mapping: Mapping){
        val result = FileScanner.refreshMapping(mapping, this)
        if(result.success){
            mapping.lastSynced = TimeUtils.unixTimestampNowSecs()
            mapping.error = null
            viewModel.updateMapping(mapping)
        }
        else{
            mapping.error = result.errorMessage
            viewModel.updateMapping(mapping)
            Toast.makeText(this, "Error syncing", Toast.LENGTH_LONG).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}