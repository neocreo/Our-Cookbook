package com.ourcookbook.di

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CookbookApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize SQLCipher
        net.sqlcipher.database.SQLiteDatabase.loadLibs(this)
    }
}