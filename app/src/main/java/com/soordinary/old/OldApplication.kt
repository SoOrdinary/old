package com.soordinary.old

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class OldApplication: Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var _context: Context
        // 只允许取，不可修改
        val context get() = _context
    }

    override fun onCreate() {
        super.onCreate()
        _context = applicationContext
    }
}