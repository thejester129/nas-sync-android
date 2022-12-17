package com.example.android_nas_sync.io

import com.example.android_nas_sync.models.SyncingException
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmbShareConnector() {
        private val client:SMBClient? = null

        suspend fun connectToSmbShare(ipAddress:String,shareName:String, username:String, password:String):DiskShare{
            return withContext(Dispatchers.IO) {
                val client = SMBClient()

                val connection: Connection = try {
                    client.connect(ipAddress)
                } catch (e: Exception) {
                    client.close()
                    throw SyncingException("Unable to find server")
                }

                val session: Session = try {
                    val authContext = AuthenticationContext(
                        username,
                        password.toCharArray(),
                        ipAddress
                    )
                    connection.authenticate(authContext)
                } catch (e: Exception) {
                    client.close()
                    throw SyncingException("Unable to authenticate to server")
                }

                val share: DiskShare = try {
                    session.connectShare(shareName) as DiskShare
                } catch (e: Exception) {
                    client.close()
                    throw SyncingException("Unable to authenticate to share")
                }

                return@withContext share
            }

        }
    fun closeConnection(){
        client?.close()
    }
}