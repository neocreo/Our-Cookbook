package com.ourcookbook.data.service

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.ourcookbook.domain.model.Recipe
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * PDF Exporter for Recipe Export
 * Task 2.2.06: PDF Export Implementation
 *
 * Exports recipes to PDF format with professional layout
 * Supports single recipe, multiple recipes, or entire cookbooks
 */
class PdfExporter {

    companion object {
        private const val PAGE_WIDTH = 595  // A4 width in points (72 dpi)
        private const val PAGE_HEIGHT = 842 // A4 height in points
        private const val MARGIN = 72f       // 1 inch margin
        private const val LINE_SPACING = 15f
        private const val TITLE_SIZE = 24f
        private const val HEADING_SIZE = 18f
        private const val BODY_SIZE = 12f
        private const val SMALL_SIZE = 10f
    }

    /**
     * Export settings for PDF generation
     */
    data class PdfExportSettings(
        val pageSize: PageSize = PageSize.A4,
        val includeImages: Boolean = true,
        val includeMetadata: Boolean = true,
        val includeInstructions: Boolean = true,
        val fontSize: Float = BODY_SIZE,
        val showPageNumbers: Boolean = true,
        val recipesPerPage: Int = 1
    )

    /**
     * Page size options
     */
    enum class PageSize(val width: Int, val height: Int) {
        A4(595, 842),
        LETTER(612, 792),
        A5(420, 595)
    }

    /**
     * Result of PDF export
     */
    data class PdfExportResult(
        val file: File,
        val pageCount: Int,
        val recipeCount: Int,
        val success: Boolean,
        val errorMessage: String? = null
    )

    /**
     * Export a single recipe to PDF
     *
     * @param recipe The recipe to export
     * @param outputStream The output stream to write to
     * @param settings Export settings
     * @return PdfExportResult with export information
     */
    suspend fun exportRecipe(
        recipe: Recipe,
        outputStream: OutputStream,
        settings: PdfExportSettings = PdfExportSettings()
    ): PdfExportResult {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(
                settings.pageSize.width,
                settings.pageSize.height,
                1
            ).create()
            
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            // Draw recipe content
            drawRecipe(canvas, recipe, settings)
            
            pdfDocument.finishPage(page)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            
            PdfExportResult(
                file = File(""),
                pageCount = 1,
                recipeCount = 1,
                success = true
            )
        } catch (e: Exception) {
            PdfExportResult(
                file = File(""),
                pageCount = 0,
                recipeCount = 0,
                success = false,
                errorMessage = e.message
            )
        }
    }

    /**
     * Export multiple recipes to a single PDF file
     *
     * @param recipes List of recipes to export
     * @param outputFile The output file
     * @param settings Export settings
     * @return PdfExportResult with export information
     */
    suspend fun exportRecipes(
        recipes: List<Recipe>,
        outputFile: File,
        settings: PdfExportSettings = PdfExportSettings()
    ): PdfExportResult {
        return try {
            FileOutputStream(outputFile).use { outputStream ->
                val pdfDocument = PdfDocument()
                var pageCount = 0
                
                recipes.forEachIndexed { index, recipe ->
                    // Start new page for each recipe (or group by settings.recipesPerPage)
                    val pageInfo = PdfDocument.PageInfo.Builder(
                        settings.pageSize.width,
                        settings.pageSize.height,
                        index + 1
                    ).create()
                    
                    val page = pdfDocument.startPage(pageInfo)
                    val canvas = page.canvas
                    
                    // Draw recipe content
                    drawRecipe(canvas, recipe, settings, index + 1, recipes.size)
                    
                    pdfDocument.finishPage(page)
                    pageCount++
                }
                
                pdfDocument.writeTo(outputStream)
                pdfDocument.close()
                
                PdfExportResult(
                    file = outputFile,
                    pageCount = pageCount,
                    recipeCount = recipes.size,
                    success = true
                )
            }
        } catch (e: Exception) {
            PdfExportResult(
                file = outputFile,
                pageCount = 0,
                recipeCount = 0,
                success = false,
                errorMessage = e.message
            )
        }
    }

    /**
     * Export recipes to a PDF file at the specified path
     *
     * @param recipes List of recipes to export
     * @param filePath Full path to the output PDF file
     * @param settings Export settings
     * @return PdfExportResult with export information
     */
    suspend fun exportToFile(
        recipes: List<Recipe>,
        filePath: String,
        settings: PdfExportSettings = PdfExportSettings()
    ): PdfExportResult {
        val outputFile = File(filePath)
        
        // Create parent directories if they don't exist
        outputFile.parentFile?.mkdirs()
        
        return exportRecipes(recipes, outputFile, settings)
    }

    /**
     * Draw a single recipe on the canvas
     *
     * @param canvas The canvas to draw on
     * @param recipe The recipe to draw
     * @param settings Export settings
     * @param pageNumber Current page number
     * @param totalPages Total number of pages
     */
    private fun drawRecipe(
        canvas: Canvas,
        recipe: Recipe,
        settings: PdfExportSettings,
        pageNumber: Int = 1,
        totalPages: Int = 1
    ) {
        val paint = Paint()
        val titlePaint = Paint()
        val headingPaint = Paint()
        val bodyPaint = Paint()
        val smallPaint = Paint()
        
        // Set up paints
        titlePaint.apply {
            textSize = TITLE_SIZE * settings.fontSize / BODY_SIZE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.BLACK
            isAntiAlias = true
        }
        
        headingPaint.apply {
            textSize = HEADING_SIZE * settings.fontSize / BODY_SIZE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.BLACK
            isAntiAlias = true
        }
        
        bodyPaint.apply {
            textSize = settings.fontSize
            color = Color.BLACK
            isAntiAlias = true
        }
        
        smallPaint.apply {
            textSize = SMALL_SIZE * settings.fontSize / BODY_SIZE
            color = Color.GRAY
            isAntiAlias = true
        }
        
        // Draw content
        var y = MARGIN
        
        // Draw header with page number
        if (settings.showPageNumbers && totalPages > 1) {
            canvas.drawText(
                "Page $pageNumber of $totalPages",
                MARGIN,
                y + smallPaint.textSize,
                smallPaint
            )
            y += LINE_SPACING * 2
        }
        
        // Draw title
        canvas.drawText(
            recipe.title,
            MARGIN,
            y + titlePaint.textSize,
            titlePaint
        )
        y += titlePaint.textSize + LINE_SPACING * 2
        
        // Draw category if available
        if (settings.includeMetadata && recipe.category.isNotBlank()) {
            canvas.drawText(
                "Category: ${recipe.category}",
                MARGIN,
                y + bodyPaint.textSize,
                bodyPaint
            )
            y += bodyPaint.textSize + LINE_SPACING
        }
        
        // Draw metadata (servings, times)
        if (settings.includeMetadata) {
            val metadataParts = mutableListOf<String>()
            recipe.servingSize?.let { metadataParts.add("Serves: $it") }
            recipe.prepTime?.let { metadataParts.add("Prep: ${it}min") }
            recipe.cookTime?.let { metadataParts.add("Cook: ${it}min") }
            recipe.totalTime?.let { metadataParts.add("Total: ${it}min") }
            
            if (metadataParts.isNotEmpty()) {
                canvas.drawText(
                    metadataParts.joinToString(" | "),
                    MARGIN,
                    y + bodyPaint.textSize,
                    bodyPaint
                )
                y += bodyPaint.textSize + LINE_SPACING * 2
            }
        }
        
        // Draw description if available
        if (recipe.description?.isNotBlank() == true) {
            canvas.drawText(
                "Description:",
                MARGIN,
                y + headingPaint.textSize,
                headingPaint
            )
            y += headingPaint.textSize + LINE_SPACING
            
            drawWrappedText(
                canvas,
                recipe.description ?: "",
                MARGIN,
                y,
                settings.pageSize.width - MARGIN * 2,
                bodyPaint,
                LINE_SPACING
            )
            y += bodyPaint.textSize + LINE_SPACING * 2
        }
        
        // Draw ingredients section
        if (recipe.ingredients.isNotEmpty()) {
            canvas.drawText(
                "Ingredients:",
                MARGIN,
                y + headingPaint.textSize,
                headingPaint
            )
            y += headingPaint.textSize + LINE_SPACING
            
            recipe.ingredients.forEach { ingredient ->
                val ingredientText = buildIngredientText(ingredient)
                drawWrappedText(
                    canvas,
                    ingredientText,
                    MARGIN,
                    y,
                    settings.pageSize.width - MARGIN * 2,
                    bodyPaint,
                    LINE_SPACING
                )
                y += bodyPaint.textSize + LINE_SPACING
            }
            y += LINE_SPACING
        }
        
        // Draw instructions section
        if (settings.includeInstructions && recipe.instructions.isNotEmpty()) {
            canvas.drawText(
                "Instructions:",
                MARGIN,
                y + headingPaint.textSize,
                headingPaint
            )
            y += headingPaint.textSize + LINE_SPACING
            
            recipe.instructions.forEachIndexed { index, instruction ->
                val stepText = "${index + 1}. $instruction"
                drawWrappedText(
                    canvas,
                    stepText,
                    MARGIN,
                    y,
                    settings.pageSize.width - MARGIN * 2,
                    bodyPaint,
                    LINE_SPACING
                )
                y += bodyPaint.textSize + LINE_SPACING
            }
        }
        
        // Draw source if available
        if (recipe.source?.isNotBlank() == true) {
            y += LINE_SPACING
            canvas.drawText(
                "Source: ${recipe.source}",
                MARGIN,
                y + bodyPaint.textSize,
                bodyPaint
            )
        }
        
        // Draw tags if available
        if (recipe.tags.isNotEmpty()) {
            y += bodyPaint.textSize + LINE_SPACING
            canvas.drawText(
                "Tags: ${recipe.tags.joinToString(", ")}",
                MARGIN,
                y + bodyPaint.textSize,
                bodyPaint
            )
        }
    }

    /**
     * Draw wrapped text that spans multiple lines
     *
     * @param canvas The canvas to draw on
     * @param text The text to draw
     * @param x X position
     * @param y Y position
     * @param maxWidth Maximum width for text
     * @param paint The paint to use
     * @param lineSpacing Spacing between lines
     */
    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        paint: Paint,
        lineSpacing: Float
    ): Float {
        val words = text.split(" ")
        var currentLine = ""
        var currentY = y
        
        words.forEach { word ->
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val testWidth = paint.measureText(testLine)
            
            if (testWidth <= maxWidth) {
                currentLine = testLine
            } else {
                // Draw current line
                canvas.drawText(currentLine, x, currentY + paint.textSize, paint)
                currentY += paint.textSize + lineSpacing
                currentLine = word
            }
        }
        
        // Draw the last line
        if (currentLine.isNotBlank()) {
            canvas.drawText(currentLine, x, currentY + paint.textSize, paint)
            currentY += paint.textSize + lineSpacing
        }
        
        return currentY
    }

    /**
     * Build ingredient text with amount, unit, and name
     */
    private fun buildIngredientText(ingredient: com.ourcookbook.domain.model.Ingredient): String {
        val parts = mutableListOf<String>()
        
        ingredient.amount?.let { parts.add(it) }
        ingredient.unit?.let { parts.add(it) }
        parts.add(ingredient.name)
        ingredient.notes?.let { parts.add("($it)") }
        
        return parts.joinToString(" ")
    }

    /**
     * Check if PDF export is supported on this device
     */
    fun isSupported(): Boolean {
        return true // PDF export is supported on all Android devices
    }

    /**
     * Get default export directory
     */
    fun getDefaultExportDirectory(): File {
        return Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )
    }

    /**
     * Generate a default file name for PDF export
     */
    fun generateFileName(recipeOrCookbookName: String): String {
        val safeName = recipeOrCookbookName
            .replace("[^a-zA-Z0-9]+".toRegex(), "_")
            .replace("__+".toRegex(), "_")
            .trim('_')
        
        val timestamp = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        
        return "${safeName}_$timestamp.pdf"
    }
}
