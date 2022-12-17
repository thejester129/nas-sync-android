package com.example.android_nas_sync.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.android_nas_sync.db.MappingDAO
import com.example.android_nas_sync.db.MappingDatabase
import com.example.android_nas_sync.io.DeviceFileReader
import com.example.android_nas_sync.io.SmbFileWriter
import com.example.android_nas_sync.io.SmbShareConnector
import com.example.android_nas_sync.models.Mapping
import com.example.android_nas_sync.models.SyncResult
import com.example.android_nas_sync.models.SyncingException
import com.example.android_nas_sync.utils.TimeUtils
import com.hierynomus.smbj.share.DiskShare
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MappingsRepository(private val database:MappingDatabase, private val context: Context) {
    private val dao:MappingDAO = database.mappingDao()
    val mappings:LiveData<List<Mapping>> = dao.getAll()

    suspend fun insert(mapping: Mapping) {
        return withContext(Dispatchers.IO){
            dao.insert(mapping)
        }
    }

    suspend fun update(mapping: Mapping) {
        return withContext(Dispatchers.IO){
            dao.update(mapping)
        }
    }

    suspend fun delete(mapping: Mapping) {
        return withContext(Dispatchers.IO){
            dao.delete(mapping)
        }
    }

    suspend fun syncMapping(mapping: Mapping):SyncResult{
        val ipAddress = mapping.serverIp!!
        val shareName = mapping.destinationShare!!
        val sharePath = mapping.destinationPath
        val username = mapping.username
        val password = mapping.password
        val contentUri = mapping.sourceFolder!!

        var filesAdded = 0
        var filesFailedToAdd = 0
        var share: DiskShare? = null
        var smbShareConnector:SmbShareConnector? = null
        var errorMessage:String? = null

        try{
            smbShareConnector = SmbShareConnector()
            share = smbShareConnector.connectToSmbShare(ipAddress, shareName, username, password)
            val fileWriter = SmbFileWriter(share)

            val phoneFiles = DeviceFileReader.readFilesAtContentUri(contentUri, context)

            phoneFiles.forEach { file -> run{
                if(!fileWriter.fileExistsInShare(sharePath, file.name)){
                    try{
                        fileWriter.writeFileToShare(file,sharePath )
                        filesAdded++
                    }
                    catch (e:Exception){
                        filesFailedToAdd++
                    }
                }
            } }
        }
        catch (e:Exception){
            errorMessage = if(e is SyncingException){
                e.message
            } else{
                "Failed to sync mapping"
            }
        }

        updateMappingAfterSync(mapping, filesAdded)

        smbShareConnector?.closeConnection()

        return SyncResult(filesAdded, filesFailedToAdd, errorMessage)
    }

    private suspend fun updateMappingAfterSync(mapping: Mapping, filesAdded:Int){
        // TODO if deleted then synced again counts the same item twice
        // probs need to keep a list of items synced.. yuk
        mapping.filesSynced = mapping.filesSynced + filesAdded
        mapping.lastSynced = TimeUtils.unixTimestampNowSecs()
        update(mapping)
    }

}