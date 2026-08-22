package com.ourcookbook.domain.usecase.recipe

import com.ourcookbook.data.service.MarkdownExporter
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.repository.RecipeRepository
import java.io.File
import javax.inject.Inject

/**
 * Use case for importing recipes from Markdown files
 * Task 2.2.08: File Import Implementation
 *
 * Parses Markdown files in Cookbook folder format and converts to Recipe objects
 */
class ImportRecipeFromMarkdown @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val markdownExporter: MarkdownExporter
) {

    /**
     * Result of Markdown import operation
     */
    sealed class ImportResult {
        data class Success(
            val importedCount: Int,
            val skippedCount: Int,
            val conflictCount: Int,
            val recipes: List<Recipe>
        ) : ImportResult()
        
        data class Failure(
            val errorMessage: String,
            val exception: Exception? = null
        ) : ImportResult()
    }

    /**
     * Import settings
     */
    data class ImportSettings(
        val overwriteExisting: Boolean = false,
        val skipDuplicates: Boolean = true
    )

    /**
     * Parse a Markdown file and extract recipes
     *
     * @param markdownContent The Markdown content to parse
     * @return List of parsed Recipe objects
     */
    fun parseMarkdown(markdownContent: String): List<Recipe> {
        val recipes = mutableListOf<Recipe>()
        
        // Split content by recipe separators
        val recipeSections = markdownContent.split("---")
        
        recipeSections.forEach { section ->
            val recipe = parseRecipeFromMarkdown(section)
            if (recipe != null) {
                recipes.add(recipe)
            }
        }
        
        return recipes
    }

    /**
     * Parse a single recipe from Markdown content
     *
     * @param markdownContent The Markdown content for a single recipe
     * @return Parsed Recipe object, or null if parsing fails
     */
    private fun parseRecipeFromMarkdown(markdownContent: String): Recipe? {
        val lines = markdownContent.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
        
        if (lines.isEmpty()) {
            return null
        }
        
        var title: String? = null
        var description: String? = null
        var category: String = "Mains"
        var servings: Int? = null
        var prepTime: Int? = null
        var cookTime: Int? = null
        var totalTime: Int? = null
        var source: String? = null
        val tags = mutableListOf<String>()
        val ingredients = mutableListOf<com.ourcookbook.domain.model.Ingredient>()
        val instructions = mutableListOf<String>()
        
        var currentSection: String? = null
        
        lines.forEachIndexed { index, line ->
            when {
                // Title (starts with ## )
                line.startsWith("##") -> {
                    title = line.removePrefix("##").trim()
                    currentSection = null
                }
                
                // Metadata line (contains | or starts with **)
                line.startsWith("**") && line.endsWith("**") -> {
                    val metadata = line.removeSurrounding("**")
                    
                    // Try to parse metadata
                    if (metadata.contains("Serves:")) {
                        servings = extractNumber(metadata)
                    }
                    if (metadata.contains("Prep Time:")) {
                        prepTime = extractTime(metadata)
                    }
                    if (metadata.contains("Cook Time:")) {
                        cookTime = extractTime(metadata)
                    }
                    if (metadata.contains("Total Time:")) {
                        totalTime = extractTime(metadata)
                    }
                    
                    // Check for category
                    if (metadata.startsWith("Category:")) {
                        category = metadata.removePrefix("Category:").trim()
                    }
                    
                    // Check for source
                    if (metadata.startsWith("Source:")) {
                        source = metadata.removePrefix("Source:").trim()
                    }
                    
                    // Check for tags
                    if (metadata.startsWith("Tags:")) {
                        tags.addAll(
                            metadata.removePrefix("Tags:")
                                .split(",")
                                .map { it.trim() }
                        )
                    }
                    
                    currentSection = null
                }
                
                // Section headers
                line.startsWith("###") -> {
                    currentSection = line.removePrefix("###").trim().lowercase()
                }
                
                // Ingredients table
                line.startsWith("|") && currentSection == "ingredients" -> {
                    val columns = line.split("|")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                    
                    if (columns.size >= 3) {
                        // Format: | Amount | Ingredient | Notes |
                        val amount = columns.getOrNull(0) ?: ""
                        val ingredientName = columns.getOrNull(1) ?: ""
                        val notes = columns.getOrNull(2) ?: ""
                        
                        // Parse amount and unit
                        val (parsedAmount, parsedUnit) = parseAmount(amount)
                        
                        ingredients.add(
                            com.ourcookbook.domain.model.Ingredient.create(
                                name = ingredientName,
                                amount = parsedAmount,
                                unit = parsedUnit,
                                notes = if (notes.isBlank()) null else notes,
                                order = ingredients.size
                            )
                        )
                    }
                }
                
                // Instructions
                currentSection == "instructions" -> {
                    instructions.add(line)
                }
                
                // Description (first non-header, non-metadata line)
                title != null && description == null && 
                    !line.startsWith("#") && 
                    !line.startsWith("|") &&
                    currentSection == null -> {
                    description = line
                }
            }
        }
        
        if (title == null) {
            return null
        }
        
        return Recipe.create(
            title = title,
            category = category,
            description = description,
            ingredients = ingredients,
            instructions = if (instructions.isEmpty()) listOf("Add instructions") else instructions,
            servingSize = servings,
            prepTime = prepTime,
            cookTime = cookTime,
            totalTime = totalTime,
            source = source,
            tags = tags,
            deviceId = ""
        )
    }

    /**
     * Extract number from text
     */
    private fun extractNumber(text: String): Int? {
        return text.find { it.isDigit() }?.let { firstDigit ->
            val numberText = text.substring(firstDigit.position).takeWhile { it.isDigit() }
            numberText.toIntOrNull()
        }
    }

    /**
     * Extract time from text (in minutes)
     */
    private fun extractTime(text: String): Int? {
        val timeRegex = Regex("(\d+)\s*(min|mins|minutes|hour|hours|h)")
        val match = timeRegex.find(text)
        
        return match?.let {
            val number = it.groupValues[1].toIntOrNull() ?: return@let null
            val unit = it.groupValues[2].lowercase()
            
            when {
                unit.contains("hour") || unit == "h" -> number * 60
                else -> number
            }
        }
    }

    /**
     * Parse amount into quantity and unit
     */
    private fun parseAmount(amountText: String): Pair<String?, String?> {
        if (amountText.isBlank()) {
            return Pair(null, null)
        }
        
        // Split by space and find the last non-numeric part
        val parts = amountText.split(" ")
        val numbers = parts.filter { it.any { c -> c.isDigit() || c == '.' || c == '/' } }
        val units = parts.filter { it.none { c -> c.isDigit() || c == '.' || c == '/' } }
        
        val amount = if (numbers.isNotEmpty()) numbers.joinToString(" ") else null
        val unit = if (units.isNotEmpty()) units.joinToString(" ") else null
        
        return Pair(amount, unit)
    }

    /**
     * Import recipes from a Markdown file
     *
     * @param file The Markdown file to import from
     * @param settings Import settings (optional)
     * @return ImportResult with import information
     */
    suspend operator fun invoke(
        file: File,
        settings: ImportSettings = ImportSettings()
    ): ImportResult {
        return try {
            val markdownContent = file.readText()
            val recipes = parseMarkdown(markdownContent)
            
            if (recipes.isEmpty()) {
                return ImportResult.Failure(
                    errorMessage = "No recipes found in the Markdown file"
                )
            }
            
            // Check for conflicts
            val existingRecipes = recipeRepository.getAllRecipesOnce()
            val existingIds = existingRecipes.map { it.id }.toSet()
            val existingTitles = existingRecipes.map { it.title.lowercase() }.toSet()
            
            var importedCount = 0
            var skippedCount = 0
            var conflictCount = 0
            val importedRecipes = mutableListOf<Recipe>()
            
            recipes.forEach { recipe ->
                val isDuplicateId = recipe.id in existingIds
                val isDuplicateTitle = recipe.title.lowercase() in existingTitles
                
                if (isDuplicateId) {
                    if (settings.overwriteExisting) {
                        recipeRepository.updateRecipe(recipe)
                        importedCount++
                        importedRecipes.add(recipe)
                    } else if (settings.skipDuplicates) {
                        skippedCount++
                    } else {
                        // Generate new ID for the duplicate
                        val newRecipe = recipe.copy(id = java.util.UUID.randomUUID().toString())
                        recipeRepository.createRecipe(newRecipe)
                        importedCount++
                        importedRecipes.add(newRecipe)
                    }
                    conflictCount++
                } else if (isDuplicateTitle && !settings.overwriteExisting && !settings.skipDuplicates) {
                    // Title duplicate but different ID - generate new ID
                    val newRecipe = recipe.copy(id = java.util.UUID.randomUUID().toString())
                    recipeRepository.createRecipe(newRecipe)
                    importedCount++
                    importedRecipes.add(newRecipe)
                    conflictCount++
                } else {
                    // No conflict - create new recipe
                    recipeRepository.createRecipe(recipe)
                    importedCount++
                    importedRecipes.add(recipe)
                }
            }
            
            ImportResult.Success(
                importedCount = importedCount,
                skippedCount = skippedCount,
                conflictCount = conflictCount,
                recipes = importedRecipes
            )
            
        } catch (e: Exception) {
            ImportResult.Failure(
                errorMessage = "Failed to import recipes from Markdown: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Import recipes from multiple Markdown files
     *
     * @param files List of Markdown files to import from
     * @param settings Import settings (optional)
     * @return ImportResult with aggregated import information
     */
    suspend fun importFromFiles(
        files: List<File>,
        settings: ImportSettings = ImportSettings()
    ): ImportResult {
        var totalImported = 0
        var totalSkipped = 0
        var totalConflicts = 0
        val allRecipes = mutableListOf<Recipe>()
        
        files.forEach { file ->
            val result = this(file, settings)
            
            when (result) {
                is ImportResult.Success -> {
                    totalImported += result.importedCount
                    totalSkipped += result.skippedCount
                    totalConflicts += result.conflictCount
                    allRecipes.addAll(result.recipes)
                }
                is ImportResult.Failure -> {
                    // Continue with other files
                }
            }
        }
        
        return ImportResult.Success(
            importedCount = totalImported,
            skippedCount = totalSkipped,
            conflictCount = totalConflicts,
            recipes = allRecipes
        )
    }

    /**
     * Preview import without actually importing
     *
     * @param file The Markdown file to preview
     * @return List of recipes that would be imported
     */
    suspend fun previewImport(file: File): List<Recipe> {
        return try {
            val markdownContent = file.readText()
            parseMarkdown(markdownContent)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Check if the file is a valid Markdown recipe file
     *
     * @param file The file to check
     * @return true if valid, false otherwise
     */
    suspend fun isValidImportFile(file: File): Boolean {
        return try {
            val content = file.readText()
            // Check if it looks like a recipe file
            content.contains("##") || content.contains("### Ingredients")
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get recipe count from a Markdown file without importing
     *
     * @param file The Markdown file to check
     * @return Number of recipes in the file, or -1 if invalid
     */
    suspend fun getRecipeCount(file: File): Int {
        return try {
            val markdownContent = file.readText()
            parseMarkdown(markdownContent).size
        } catch (e: Exception) {
            -1
        }
    }
}
