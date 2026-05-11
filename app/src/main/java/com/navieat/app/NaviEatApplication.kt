package com.navieat.app

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NaviEatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialise PdfBox-Android once for the whole process. It's cheap.
        PDFBoxResourceLoader.init(applicationContext)
    }
}
