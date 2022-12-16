package com.example.android_nas_sync.common

import android.content.Context
import android.os.Environment
import com.example.android_nas_sync.models.Mapping
import com.example.android_nas_sync.models.ScanResult
import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2CreateOptions
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.*
import java.util.stream.Collectors


class FileScanner {
    //    static String ipAddress = "192.168.0.45";
    //    static String username = "file-sync";
    //    static String password = "file-sync";
    //    static String shareName = "backup";
    //    static String shareFolder = "phone_pixel/camera/";
    //    static String phoneFolder = "src/img";
    fun refreshAllMappings(mappings: List<Mapping?>?) {}

    companion object {
         fun refreshMapping(mapping: Mapping, context:Context): ScanResult {
            val client = SMBClient()

            val connection: Connection = try {
                client.connect(mapping.serverIp)
            } catch (e: Exception) {
                client.close()
                return ScanResult(false, "Unable to find server")
            }

            val session: Session = try {
                val authContext = AuthenticationContext(
                    mapping.username,
                    mapping.password.toCharArray(),
                    mapping.serverIp
                )
                connection.authenticate(authContext)
            } catch (e: Exception) {
                client.close()
                return ScanResult(false, "Unable to authenticate to server")
            }

            val share: DiskShare? = try {
                session.connectShare(mapping.destinationShare) as DiskShare
            } catch (e: Exception) {
                client.close()
                return ScanResult(false, "Unable to connect to share")
            }

            val newFiles = try {
                scanNewFiles(share, mapping.destinationShare, mapping.sourceFolder, context)
            } catch (e: Exception) {
                client.close()
                return ScanResult(false, "Failed to scan phone files")
            }

            try {
                // TODO make non atomic
                for (file in newFiles) {
                    writeToFile(share, file, mapping.destinationShare)
                }
            } catch (e: Exception) {
                client.close()
                return ScanResult(false, "Failed to write new files")
            }

            return ScanResult(true, "")
        }

        private fun scanNewFiles(
            diskShare: DiskShare?,
            shareName: String?,
            sourceFolder: String?,
            context: Context
        ): List<File> {
            if(sourceFolder == null){
                return emptyList()
            }
            val phoneFiles = FileUtils.getFilesFromContentUri(sourceFolder, context)
            val newFiles: MutableList<File> = LinkedList()
            // Find filenames not yet uploaded
            for (file in phoneFiles) {
                if (!diskShare!!.fileExists(shareName + file.name)) {
                    println("Found new file: " + file.name)
                    newFiles.add(file)
                }
            }
            return newFiles
        }

        @Throws(IOException::class)
        private fun writeToFile(diskShare: DiskShare?, file: File, shareFolder: String?) {
            val fileContent = Files.readAllBytes(file.toPath())

            val fileAttributes: MutableSet<FileAttributes> = HashSet()
            fileAttributes.add(FileAttributes.FILE_ATTRIBUTE_NORMAL)
            val createOptions: MutableSet<SMB2CreateOptions> = HashSet()
            createOptions.add(SMB2CreateOptions.FILE_RANDOM_ACCESS)
            val accessMask: MutableSet<AccessMask> = HashSet()
            accessMask.add(AccessMask.GENERIC_ALL)

            val f = diskShare!!.openFile(
                shareFolder + file.name,
                accessMask, fileAttributes, SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_CREATE, createOptions
            )
            println("Writing file: " + file.name)
            f.write(fileContent, 0)
        }
    }
}