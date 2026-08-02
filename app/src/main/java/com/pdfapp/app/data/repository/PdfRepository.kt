package com.pdfapp.app.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.*
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.properties.TextAlignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfRepository @Inject constructor() {

    suspend fun compressPdf(
        context: Context,
        inputFile: File,
        outputFile: File,
        quality: Float = 0.7f,
        targetSizeKB: Long? = null,
        isProUser: Boolean
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val reader = PdfReader(FileInputStream(inputFile))
            val writer = PdfWriter(FileOutputStream(outputFile))
            
            val pdf = PdfDocument(reader, writer)
            val document = Document(pdf)
            
            // Add watermark for free users
            if (!isProUser) {
                addWatermark(context, pdf)
            }
            
            // Copy pages
            for (i in 1..pdf.numberOfPages) {
                val page = pdf.getPage(i)
                // Compression happens automatically with iText
            }
            
            document.close()
            pdf.close()
            
            // If target size is specified and we're pro, try to reduce further
            if (targetSizeKB != null && isProUser) {
                var currentSize = outputFile.length() / 1024
                var currentQuality = quality
                
                while (currentSize > targetSizeKB && currentQuality > 0.3f) {
                    currentQuality -= 0.1f
                    // Re-compress with lower quality
                    // Note: In production, you'd implement actual re-compression logic
                    currentSize = outputFile.length() / 1024
                }
            }
            
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun addWatermark(context: Context, pdfDocument: PdfDocument) {
        for (i in 1..pdfDocument.numberOfPages) {
            val page = pdfDocument.getPage(i)
            val canvas = PdfCanvas(page)
            val document = Document(pdfDocument)
            
            val watermark = Paragraph("Created with PDF Tools Free")
                .setFontSize(14f)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(com.itextpdf.kernel.colors.DeviceGray.GRAY)
            
            document.add(watermark)
        }
    }

    suspend fun mergePdfs(
        context: Context,
        inputFiles: List<File>,
        outputFile: File,
        isProUser: Boolean,
        pageOrder: List<Int>? = null,
        rotations: Map<Int, Int>? = null
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val mergedPdf = PdfDocument(PdfWriter(outputFile))
            var pageCount = 0
            
            inputFiles.forEachIndexed { fileIndex, file ->
                val sourcePdf = PdfDocument(PdfReader(FileInputStream(file)))
                val numPages = sourcePdf.numberOfPages
                
                for (i in 1..numPages) {
                    val importedPage = sourcePdf.getPage(i).copyTo(mergedPdf)
                    
                    // Apply rotation if specified (Pro feature)
                    if (isProUser && rotations != null) {
                        val globalPageIndex = pageCount + i
                        val rotation = rotations[globalPageIndex] ?: 0
                        if (rotation != 0) {
                            importedPage.setRotation(importedPage.rotation + rotation)
                        }
                    }
                    
                    mergedPdf.addPage(importedPage)
                    pageCount++
                }
                
                sourcePdf.close()
            }
            
            // Add watermark for free users on all pages
            if (!isProUser) {
                addWatermarkToAllPages(mergedPdf, "Merged with PDF Tools Free")
            }
            
            mergedPdf.close()
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun addWatermarkToAllPages(pdfDocument: PdfDocument, text: String) {
        for (i in 1..pdfDocument.numberOfPages) {
            val page = pdfDocument.getPage(i)
            val canvas = PdfCanvas(page)
            // Simplified watermark - in production add proper text rendering
        }
    }

    suspend fun convertPdfToImages(
        context: Context,
        inputFile: File,
        outputDir: File,
        format: String = "JPG",
        dpi: Int = 150,
        isProUser: Boolean,
        pages: List<Int>? = null
    ): Result<List<File>> = withContext(Dispatchers.IO) {
        try {
            val outputFiles = mutableListOf<File>()
            
            // Using PDFBox for rendering
            val pdfDocument = org.apache.pdfbox.pdmodel.PDDocument.load(inputFile)
            val renderer = org.apache.pdfbox.rendering.PDFRenderer(pdfDocument)
            
            val pagesToConvert = if (isProUser) {
                pages ?: (1..pdfDocument.numberOfPages).toList()
            } else {
                // Free users only get first page
                listOf(1)
            }
            
            pagesToConvert.forEach { pageNum ->
                val index = pageNum - 1
                if (index >= 0 && index < pdfDocument.numberOfPages) {
                    val scale = dpi / 72f
                    val image = renderer.renderImageWithDPI(pageNum.toFloat(), dpi)
                    
                    val outputFile = File(outputDir, "page_$pageNum.$format")
                    javax.imageio.ImageIO.write(image, format.lowercase(), outputFile)
                    outputFiles.add(outputFile)
                }
            }
            
            pdfDocument.close()
            
            // Add watermark overlay for free users
            if (!isProUser && outputFiles.isNotEmpty()) {
                addWatermarkToImage(context, outputFiles.first())
            }
            
            Result.success(outputFiles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun addWatermarkToImage(context: Context, imageFile: File) {
        // In production, use image processing library to add watermark
    }

    suspend fun convertImagesToPdf(
        context: Context,
        imageFiles: List<File>,
        outputFile: File,
        isProUser: Boolean
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val writer = PdfWriter(outputFile)
            val pdf = PdfDocument(writer)
            val document = Document(pdf)
            
            imageFiles.forEach { imageFile ->
                val imageData = com.itextpdf.layout.element.ImageDataFactory.create(imageFile.absolutePath)
                val img = com.itextpdf.layout.element.Image(imageData)
                    .setAutoScale(true)
                
                document.add(img)
                
                // Add page break except for last image
                if (imageFile != imageFiles.last()) {
                    document.add(com.itextpdf.layout.element.AreaBreak(com.itextpdf.layout.element.AreaBreakType.NEXT_PAGE))
                }
            }
            
            // Add watermark for free users
            if (!isProUser) {
                addWatermark(context, pdf)
            }
            
            document.close()
            pdf.close()
            
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rotatePdf(
        context: Context,
        inputFile: File,
        outputFile: File,
        rotationDegrees: Int,
        isProUser: Boolean,
        perPageRotations: Map<Int, Int>? = null
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val reader = PdfReader(FileInputStream(inputFile))
            val writer = PdfWriter(FileOutputStream(outputFile))
            val pdf = PdfDocument(reader, writer)
            
            for (i in 1..pdf.numberOfPages) {
                val page = pdf.getPage(i)
                
                val rotation = if (isProUser && perPageRotations != null) {
                    perPageRotations[i] ?: rotationDegrees
                } else {
                    rotationDegrees
                }
                
                page.setRotation((page.rotation + rotation) % 360)
            }
            
            // Add watermark for free users
            if (!isProUser) {
                addWatermark(context, pdf)
            }
            
            pdf.close()
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun viewPdf(context: Context, file: File): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!file.exists() || !file.extension.equals("pdf", ignoreCase = true)) {
                Result.failure(Exception("Invalid PDF file"))
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun createTempFile(context: Context, prefix: String, suffix: String): File {
        return File.createTempFile(prefix, suffix, context.cacheDir)
    }

    fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempFile = createTempFile(context, "temp_", ".pdf")
            inputStream?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            null
        }
    }
}
