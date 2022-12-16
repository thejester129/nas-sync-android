package com.example.android_nas_sync.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.android_nas_sync.common.DeviceFileReader
import com.example.android_nas_sync.common.ShareConnector
import com.example.android_nas_sync.common.SmbFileWriter
import com.example.android_nas_sync.common.TimeUtils
import com.example.android_nas_sync.db.MappingDatabase
import com.example.android_nas_sync.models.Mapping
import com.example.android_nas_sync.models.SyncingException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MappingsViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application
    private val db = Room.databaseBuilder(
        application.applicationContext,
        MappingDatabase::class.java, "mappings"
    ).build()

    private val mappingDao = db.mappingDao()

    val mappings: LiveData<List<Mapping>> = mappingDao.getAll()

    var currentlyEditedMapping:MutableLiveData<Mapping> = MutableLiveData()
    var canDeleteCurrentlyEdited = false

    fun addMapping(mapping: Mapping){
        viewModelScope.launch {
            mappingDao.insert(mapping)
        }
    }
    fun updateMapping(mapping: Mapping) {
        viewModelScope.launch {
            mappingDao.update(mapping)
        }
    }

    fun updateCurrentEdited() {
        viewModelScope.launch {
            val mapping = currentlyEditedMapping.value
            if (mapping != null) {
                mappingDao.insert(mapping)
            }
        }
    }

    fun deleteCurrentlyEdited(){
        viewModelScope.launch {
            val mapping = currentlyEditedMapping.value
            if(mapping != null){
                mappingDao.delete(mapping)
            }
        }
    }

    fun syncAllMappings(){
        viewModelScope.launch(Dispatchers.IO) {
            mappings.value?.forEach { mapping ->
                run{
                    try{
                        syncMapping(mapping)
                    }
                    catch (e:SyncingException){
                        mapping.error = e.message
                    }
                    catch (_:Exception){
                        mapping.error = "Failed to sync"
                    }
                } }
        }
    }

    private fun syncMapping(mapping: Mapping){
        val ipAddress = mapping.serverIp!!
        val shareName = mapping.destinationShare!!
        val sharePath = mapping.destinationPath
        val username = mapping.username
        val password = mapping.password
        val contentUri = mapping.sourceFolder!!

        val share = ShareConnector.connectToSmbShare(ipAddress, shareName, username, password)
        val fileWriter = SmbFileWriter(share)

        val phoneFiles = DeviceFileReader.readFilesAtContentUri(contentUri, context)

        val writeExceptions = mutableListOf<Exception>()//TODO do something with these?
        var filesAdded = 0
        phoneFiles.forEach { file -> run{
            if(!fileWriter.fileExistsInShare(file.path)){
                try{
                    fileWriter.writeFileToShare(file,sharePath )
                    filesAdded++
                }
                catch (e:Exception){
                    writeExceptions.add(e)
                }
            }
        } }
        // TODO if deleted then synced again counts the same item twice
        // props need to keep a list of items synced.. yuk
        mapping.filesSynced = mapping.filesSynced + filesAdded
        mapping.lastSynced = TimeUtils.unixTimestampNowSecs()
        updateMapping(mapping)
    }
}
