package com.example.android_nas_sync.common

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.toRawFile
import com.example.android_nas_sync.models.Mapping
import com.example.android_nas_sync.models.ScanResult
import com.example.android_nas_sync.models.SyncingException
import com.hierynomus.smbj.share.DiskShare
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import java.util.stream.Collectors


class DeviceFileReader {
    companion object {
        suspend fun readFilesAtContentUri(contentUri:String, context:Context): List<File> {
            return withContext(Dispatchers.IO){
                val dfile = DocumentFile.fromTreeUri(context, Uri.parse(contentUri))
                val fileList = dfile!!.listFiles()
                val files = fileList.asList().stream()
                    .filter() { file -> file.isFile}
                    .map { file -> convertToRawFile(file, context) }
                    .filter() { file -> file != null }
                    .collect(Collectors.toList())
                    .toList()

                return@withContext files.requireNoNulls()
            }

        }

        private fun convertToRawFile(file: DocumentFile, context: Context): File? {
            val rawFile = file.toRawFile(context)
            if(rawFile != null){
                if(!rawFile.canRead()){
                   throw SyncingException("Missing device read permissions")
                }
                return rawFile
            }
            return null
        }
    }
}