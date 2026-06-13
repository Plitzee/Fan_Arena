package com.example

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import com.google.firebase.FirebaseApp

class FanArenaApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }
}
