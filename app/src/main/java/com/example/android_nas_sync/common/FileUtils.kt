package com.example.android_nas_sync.common

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.toRawFile
import java.io.File
import java.util.stream.Collectors

class FileUtils {
    companion object{
        fun getFilesFromContentUri(uri:String, context: Context):List<File>{
            val dfile = DocumentFile.fromTreeUri(context, Uri.parse(uri))
            val fileList = dfile!!.listFiles()
            val fileNames = fileList.asList().stream()
                .filter() { file -> file.isFile}
                .map { file -> file.toRawFile(context)?.takeIf { it.canRead() }}
                .filter() { file -> file != null }
                .collect(Collectors.toList())
                .toList()

            return fileNames.requireNoNulls()
        }
    }
}