package com.example.android_nas_sync.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Mapping(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "share_type") val shareType: ShareType = ShareType.SMB,
    @ColumnInfo(name = "source_folder")val sourceFolder: String? = null,
    @ColumnInfo(name = "server_ip")val serverIp: String? = null,
    @ColumnInfo(name = "destination_share")val destinationShare: String?,
    @ColumnInfo(name = "destination_path")val destinationPath: String = "",
    @ColumnInfo(name = "last_synced")val lastSynced:Double? = null,
    @ColumnInfo(name = "one_way")val oneWay:Boolean = true,
)
