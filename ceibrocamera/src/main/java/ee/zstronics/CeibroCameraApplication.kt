package ee.zstronics

import android.app.Application

class CeibroCameraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        context = this

    }

    companion object {
        var context: CeibroCameraApplication? = null
            private set
    }
}