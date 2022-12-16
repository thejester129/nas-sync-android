package com.example.android_nas_sync.models

data class SyncingException(override val message:String): Exception(message)
