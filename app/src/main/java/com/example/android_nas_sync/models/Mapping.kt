package com.example.android_nas_sync.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Mapping(
    @ColumnInfo(name = "source_folder")var sourceFolder: String = "",
    @ColumnInfo(name = "server_ip")var serverIp: String? = null,
    @ColumnInfo(name = "destination_share")var destinationShare: String?,
    @ColumnInfo(name = "destination_path")var destinationPath: String = "",
    @ColumnInfo(name = "last_synced")var lastSynced:Double? = null,
    @ColumnInfo(name = "one_way")var oneWay:Boolean = true,
    @ColumnInfo(name = "share_type") var shareType: ShareType = ShareType.SMB,
    @PrimaryKey(autoGenerate = true) var id: Int? = null,
){
    constructor(mapping: Mapping): this(
        mapping.sourceFolder,
        mapping.serverIp,
        mapping.destinationShare,
        mapping.destinationPath,
        mapping.lastSynced,
        mapping.oneWay,
        mapping.shareType,
        mapping.id
    )
}
