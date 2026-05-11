package com.navieat.app.data.pdf

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extracts plain text from a PDF [Uri]. Uses tom-roush/PdfBox-Android, a port of
 * Apache PDFBox that works without AWT.
 *
 * IMPORTANT: PDFBoxResourceLoader.init(context) MUST be called once before the
 * first usage. We do it lazily here, but it's also fine to do it in the
 * Application onCreate.
 */
@Singleton
class PdfTextExtractor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    @Volatile private var initialized = false

    suspend fun extractText(uri: Uri): String = withContext(Dispatchers.IO) {
        if (!initialized) {
            PDFBoxResourceLoader.init(context.applicationContext)
            initialized = true
        }
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Could not open input stream for $uri" }
            PDDocument.load(input).use { doc ->
                PDFTextStripper().getText(doc)
            }
        }
    }
}
