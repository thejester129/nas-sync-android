package com.example.android_nas_sync.io

import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2CreateOptions
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.io.FileByteChunkProvider
import com.hierynomus.smbj.share.DiskShare
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.HashSet

class SmbFileWriter(private val diskShare: DiskShare) {
    suspend fun fileExistsInShare(folder:String,  name:String):Boolean{
        return withContext(Dispatchers.IO){
            return@withContext diskShare.fileExists("$folder/$name")
        }
    }

    @Throws(IOException::class)
    suspend fun writeFileToShare(file: File, destinationPath:String) {
        return withContext(Dispatchers.IO){
            val fileChunkProvider = FileByteChunkProvider(file)

            val fileAttributes: MutableSet<FileAttributes> = HashSet()
            fileAttributes.add(FileAttributes.FILE_ATTRIBUTE_NORMAL)
            val createOptions: MutableSet<SMB2CreateOptions> = HashSet()
            createOptions.add(SMB2CreateOptions.FILE_RANDOM_ACCESS)
            val accessMask: MutableSet<AccessMask> = HashSet()
            accessMask.add(AccessMask.GENERIC_ALL)

            val f = diskShare.openFile(
                destinationPath + "/" + file.name,
                accessMask, fileAttributes, SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_CREATE, createOptions
            )
            println("Writing file: " + file.name)
            f.write(fileChunkProvider)
            f.close()
            fileChunkProvider.close()
        }

    }
}