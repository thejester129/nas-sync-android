package com.example.android_nas_sync.models

data class SyncResult(val filedAdded:Int,
                      val filesFailedToAdd:Int,
                      val errorMessage:String? = null)
