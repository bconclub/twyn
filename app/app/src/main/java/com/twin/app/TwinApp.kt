package com.twin.app

import android.app.Application
import com.twin.app.data.Prefs
import com.twin.app.data.TwinApi

class TwinApp : Application() {
    lateinit var prefs: Prefs
        private set
    lateinit var api: TwinApi
        private set

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
        // Pre-warm the HTTP client at process start so first chat call is fast.
        api = TwinApi(prefs)
    }

    companion object {
        lateinit var instance: TwinApp
            private set
    }

    init {
        instance = this
    }
}
