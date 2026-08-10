package com.ourcookbook.ui.screens.scan

import com.ourcookbook.domain.model.Ingredient
import com.ourcookbook.domain.model.Recipe
import java.util.regex.Pattern

/**
 * OCR Text Parser for Recipe Extraction
 * Task 2.1.05: OCR Scan Screen Implementation
 * 
 * Parses OCR-extracted text into structured recipe data
 */
class OcrTextParser {

    /**
     * Parses OCR text into a structured recipe
     * 
     * @param text The raw OCR-extracted text
     * @param deviceId The device ID for the recipe
     * @return Parsed Recipe object
     */
    fun parseRecipeFromText(text: String, deviceId: String = ""): Recipe {
        val lines = text.split("\n")
        
        var title = ""
        var description: String? = null
        var category = "Mains"
        val ingredients = mutableListOf<Ingredient>()
        val instructions = mutableListOf<String>()
        var servingSize: Int? = null
        var prepTime: Int? = null
        var cookTime: Int? = null
        var source: String? = null
        val tags = mutableListOf<String>()
        
        var currentSection = ""
        var inIngredients = false
        var inInstructions = false
        var inNotes = false
        
        // First pass: Extract metadata and identify sections
        for (line in lines) {
            val trimmedLine = line.trim()
            
            // Skip empty lines
            if (trimmedLine.isBlank()) continue
            
            // Extract title (first non-empty line that's not a section header)
            if (title.isBlank() && !isSectionHeader(trimmedLine)) {
                title = trimmedLine
                continue
            }
            
            // Check for section headers
            when {
                trimmedLine.equals("Ingredients:", ignoreCase = true) ||
                trimmedLine.equals("Ingredients", ignoreCase = true) ||
                trimmedLine.equals("Ingredient List:", ignoreCase = true) -> {
                    currentSection = "ingredients"
                    inIngredients = true
                    inInstructions = false
                    inNotes = false
                    continue
                }
                trimmedLine.equals("Instructions:", ignoreCase = true) ||
                trimmedLine.equals("Directions:", ignoreCase = true) ||
                trimmedLine.equals("Method:", ignoreCase = true) ||
                trimmedLine.equals("Steps:", ignoreCase = true) ||
                trimmedLine.equals("Instructions", ignoreCase = true) -> {
                    currentSection = "instructions"
                    inIngredients = false
                    inInstructions = true
                    inNotes = false
                    continue
                }
                trimmedLine.equals("Notes:", ignoreCase = true) ||
                trimmedLine.equals("Note:", ignoreCase = true) -> {
                    currentSection = "notes"
                    inIngredients = false
                    inInstructions = false
                    inNotes = true
                    continue
                }
            }
            
            // Extract metadata from lines
            when {
                trimmedLine.startsWith("Category:", ignoreCase = true) -> {
                    category = extractValue(trimmedLine, "Category:")
                    continue
                }
                trimmedLine.startsWith("Serves:", ignoreCase = true) ||
                trimmedLine.startsWith("Serving Size:", ignoreCase = true) ||
                trimmedLine.startsWith("Makes:", ignoreCase = true) -> {
                    val value = extractValue(trimmedLine, "Serves:", "Serving Size:", "Makes:")
                    servingSize = extractNumber(value)
                    continue
                }
                trimmedLine.startsWith("Prep Time:", ignoreCase = true) -> {
                    val value = extractValue(trimmedLine, "Prep Time:")
                    prepTime = extractTime(value)
                    continue
                }
                trimmedLine.startsWith("Cook Time:", ignoreCase = true) ||
                trimmedLine.startsWith("Bake Time:", ignoreCase = true) -> {
                    val value = extractValue(trimmedLine, "Cook Time:", "Bake Time:")
                    cookTime = extractTime(value)
                    continue
                }
                trimmedLine.startsWith("Source:", ignoreCase = true) -> {
                    source = extractValue(trimmedLine, "Source:")
                    continue
                }
                trimmedLine.startsWith("Tags:", ignoreCase = true) -> {
                    val value = extractValue(trimmedLine, "Tags:")
                    tags.addAll(value.split(",").map { it.trim() })
                    continue
                }
            }
            
            // Process section content
            when (currentSection) {
                "ingredients" -> {
                    if (trimmedLine.isNotBlank() && !isSectionHeader(trimmedLine)) {
                        val ingredient = parseIngredient(trimmedLine)
                        if (ingredient != null) {
                            ingredients.add(ingredient)
                        }
                    }
                }
                "instructions" -> {
                    if (trimmedLine.isNotBlank() && !isSectionHeader(trimmedLine)) {
                        val instruction = parseInstruction(trimmedLine)
                        if (instruction.isNotBlank()) {
                            instructions.add(instruction)
                        }
                    }
                }
                "notes" -> {
                    if (description == null) {
                        description = trimmedLine
                    } else {
                        description += "\n" + trimmedLine
                    }
                }
            }
        }
        
        // If we didn't find a title, use the first few words of the text
        if (title.isBlank()) {
            title = extractTitleFromText(text)
        }
        
        // If no category was found, try to infer from title
        if (category == "Mains") {
            category = inferCategoryFromTitle(title)
        }
        
        // If no ingredients were found, try to extract from the entire text
        if (ingredients.isEmpty()) {
            ingredients.addAll(extractIngredientsFromText(text))
        }
        
        // If no instructions were found, try to extract from the entire text
        if (instructions.isEmpty()) {
            instructions.addAll(extractInstructionsFromText(text))
        }
        
        // Create ingredient objects with proper ordering
        val ingredientObjects = ingredients.mapIndexed { index, ingredient ->
            Ingredient.create(
                name = ingredient.name,
                amount = ingredient.amount,
                unit = ingredient.unit,
                notes = ingredient.notes,
                order = index
            )
        }
        
        return Recipe.create(
            title = title.ifBlank { "Untitled Recipe" },
            category = category,
            description = description?.ifBlank { null },
            ingredients = ingredientObjects,
            instructions = instructions.ifEmpty { listOf("Add instructions") },
            servingSize = servingSize,
            prepTime = prepTime,
            cookTime = cookTime,
            source = source?.ifBlank { null },
            tags = tags.ifEmpty { emptyList() },
            deviceId = deviceId
        )
    }
    
    /**
     * Extracts the title from text when no explicit title is found
     */
    private fun extractTitleFromText(text: String): String {
        val lines = text.split("\n")
        val firstLine = lines.firstOrNull { it.trim().isNotBlank() } ?: return "Untitled Recipe"
        
        // If the first line looks like a title (short, capitalized, etc.)
        if (firstLine.length < 100 && firstLine.any { it.isUpperCase() }) {
            return firstLine.trim()
        }
        
        // Otherwise, take the first 50 characters
        return firstLine.trim().take(50)
    }
    
    /**
     * Infers category from recipe title
     */
    private fun inferCategoryFromTitle(title: String): String {
        val lowerTitle = title.lowercase()
        
        return when {
            lowerTitle.contains("breakfast") || lowerTitle.contains("pancake") || 
                   lowerTitle.contains("waffle") || lowerTitle.contains("omelet") ||
                   lowerTitle.contains("oatmeal") || lowerTitle.contains("granola") -> "Breakfasts"
            lowerTitle.contains("cookie") || lowerTitle.contains("cake") || 
                   lowerTitle.contains("pie") || lowerTitle.contains("brownie") ||
                   lowerTitle.contains("dessert") || lowerTitle.contains("sweet") ||
                   lowerTitle.contains("chocolate") || lowerTitle.contains("ice cream") -> "Desserts & Snacks"
            lowerTitle.contains("salad") || lowerTitle.contains("soup") || 
                   lowerTitle.contains("side") || lowerTitle.contains("garnish") -> "Sides"
            lowerTitle.contains("sauce") || lowerTitle.contains("dressing") || 
                   lowerTitle.contains("marinade") || lowerTitle.contains("gravy") -> "Sauces and Spices"
            lowerTitle.contains("snack") || lowerTitle.contains("appetizer") || 
                   lowerTitle.contains("dip") || lowerTitle.contains("spread") -> "Desserts & Snacks"
            else -> "Mains"
        }
    }
    
    /**
     * Checks if a line is a section header
     */
    private fun isSectionHeader(line: String): Boolean {
        val headers = listOf(
            "Ingredients", "Instructions", "Directions", "Method", "Steps",
            "Notes", "Description", "Preparation", "Cooking", "Baking"
        )
        
        return headers.any { header ->
            line.equals(header, ignoreCase = true) ||
            line.equals("$header:", ignoreCase = true)
        }
    }
    
    /**
     * Extracts value from a line with a prefix
     */
    private fun extractValue(line: String, vararg prefixes: String): String {
        for (prefix in prefixes) {
            if (line.startsWith(prefix, ignoreCase = true)) {
                return line.substring(prefix.length).trim().trim(':')
            }
        }
        return line
    }
    
    /**
     * Extracts a number from a string
     */
    private fun extractNumber(text: String): Int? {
        val match = Regex("(\d+)").find(text)
        return match?.groupValues?.get(1)?.toIntOrNull()
    }
    
    /**
     * Extracts time in minutes from a string
     * Handles formats like "15 minutes", "1 hour", "1h 30m", etc.
     */
    private fun extractTime(text: String): Int? {
        val lowerText = text.lowercase()
        
        // Handle "X hours Y minutes" format
        val hourMatch = Regex("(\d+)\s*hours?").find(lowerText)
        val minuteMatch = Regex("(\d+)\s*minutes?").find(lowerText)
        
        if (hourMatch != null && minuteMatch != null) {
            val hours = hourMatch.groupValues[1].toIntOrNull() ?: 0
            val minutes = minuteMatch.groupValues[1].toIntOrNull() ?: 0
            return hours * 60 + minutes
        }
        
        // Handle "X hours" format
        if (hourMatch != null) {
            val hours = hourMatch.groupValues[1].toIntOrNull() ?: 0
            return hours * 60
        }
        
        // Handle "X minutes" or "X min" format
        if (minuteMatch != null) {
            return minuteMatch.groupValues[1].toIntOrNull()
        }
        
        // Handle "Xh Ym" format
        val timePattern = Pattern.compile("(\d+)h\s*(\d+)m")
        val matcher = timePattern.matcher(lowerText)
        if (matcher.find()) {
            val hours = matcher.group(1).toIntOrNull() ?: 0
            val minutes = matcher.group(2).toIntOrNull() ?: 0
            return hours * 60 + minutes
        }
        
        // Handle simple number (assume minutes)
        return extractNumber(text)
    }
    
    /**
     * Parses an ingredient line into an Ingredient object
     */
    private fun parseIngredient(line: String): ParsedIngredient? {
        val trimmedLine = line.trim()
        
        // Skip lines that don't look like ingredients
        if (trimmedLine.isBlank() || trimmedLine.startsWith("-") && trimmedLine.length < 2) {
            return null
        }
        
        // Remove bullet points and numbering
        var cleanLine = trimmedLine
        if (cleanLine.startsWith("-") || cleanLine.startsWith("•") || cleanLine.startsWith("·")) {
            cleanLine = cleanLine.substring(1).trim()
        }
        
        // Remove numbering (1., 2), etc.)
        cleanLine = cleanLine.replaceFirst(Regex("^\d+[.)\s]"), "").trim()
        
        // Skip if line is now empty
        if (cleanLine.isBlank()) return null
        
        // Try to extract amount and unit
        val amountUnitPattern = Regex("^(\d+\s*\d*\/\d+|\d+\.\d+|\d+)\s*([a-z]+)?", RegexOption.IGNORE_CASE)
        val match = amountUnitPattern.find(cleanLine)
        
        if (match != null) {
            val amount = match.groupValues[1]
            val unit = match.groupValues.getOrNull(2)?.trim()
            val remaining = cleanLine.substring(match.range.last + 1).trim()
            
            // Extract notes if present (in parentheses)
            val notes = extractNotes(remaining)
            val name = remaining.replace(Regex("\s*\(.*\)"), "").trim()
            
            return ParsedIngredient(
                name = name.ifBlank { remaining },
                amount = amount,
                unit = unit?.ifBlank { null },
                notes = notes
            )
        }
        
        // If no amount/unit found, treat the whole line as ingredient name
        return ParsedIngredient(
            name = cleanLine,
            amount = null,
            unit = null,
            notes = null
        )
    }
    
    /**
     * Extracts notes from ingredient text (text in parentheses)
     */
    private fun extractNotes(text: String): String? {
        val match = Regex("\s*\((.*)\)").find(text)
        return match?.groupValues?.get(1)?.trim()
    }
    
    /**
     * Parses an instruction line
     */
    private fun parseInstruction(line: String): String {
        var cleanLine = line.trim()
        
        // Remove bullet points and numbering
        if (cleanLine.startsWith("-") || cleanLine.startsWith("•") || cleanLine.startsWith("·")) {
            cleanLine = cleanLine.substring(1).trim()
        }
        
        // Remove numbering (1., 2), etc.)
        cleanLine = cleanLine.replaceFirst(Regex("^\d+[.)\s]"), "").trim()
        
        return cleanLine.ifBlank { "" }
    }
    
    /**
     * Extracts ingredients from text when no explicit ingredients section is found
     */
    private fun extractIngredientsFromText(text: String): List<ParsedIngredient> {
        val lines = text.split("\n")
        val ingredients = mutableListOf<ParsedIngredient>()
        
        // Look for lines that look like ingredients (start with -, •, or have common ingredient patterns)
        val ingredientPatterns = listOf(
            Regex("^\s*[-•·]\s*"),
            Regex("^\d+\s+[a-z]+", RegexOption.IGNORE_CASE),
            Regex("^\d+\s*\d*\/\d+\s+[a-z]+", RegexOption.IGNORE_CASE)
        )
        
        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isBlank()) continue
            
            // Check if line matches any ingredient pattern
            if (ingredientPatterns.any { it.containsMatchIn(trimmedLine) }) {
                val ingredient = parseIngredient(trimmedLine)
                if (ingredient != null) {
                    ingredients.add(ingredient)
                }
            }
        }
        
        return ingredients
    }
    
    /**
     * Extracts instructions from text when no explicit instructions section is found
     */
    private fun extractInstructionsFromText(text: String): List<String> {
        val lines = text.split("\n")
        val instructions = mutableListOf<String>()
        
        // Look for lines that look like instructions (start with numbers, or contain action verbs)
        val actionVerbs = listOf("preheat", "combine", "mix", "add", "stir", "bake", "cook", "heat", "pour", "whisk", "fold", "beat", "chop", "slice", "dice", "mince", "peel", "grate")
        
        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isBlank()) continue
            
            // Check if line starts with a number (step number)
            if (trimmedLine.matches(Regex("^\d+[.)\s].*"))) {
                val instruction = parseInstruction(trimmedLine)
                if (instruction.isNotBlank()) {
                    instructions.add(instruction)
                }
                continue
            }
            
            // Check if line contains action verbs
            if (actionVerbs.any { verb -> trimmedLine.contains(verb, ignoreCase = true) }) {
                val instruction = parseInstruction(trimmedLine)
                if (instruction.isNotBlank()) {
                    instructions.add(instruction)
                }
            }
        }
        
        return instructions.ifEmpty { listOf("Add instructions") }
    }
    
    /**
     * Data class for parsed ingredient (temporary structure)
     */
    private data class ParsedIngredient(
        val name: String,
        val amount: String? = null,
        val unit: String? = null,
        val notes: String? = null
    )
    
    /**
     * Validates and cleans up OCR text before parsing
     */
    fun preprocessOcrText(text: String): String {
        return text
            .replace("\r\n", "\n")  // Normalize line endings
            .replace("\r", "\n")
            .replace(Regex("\n{3,}"), "\n\n")  // Reduce multiple newlines
            .replace(Regex("\s{2,}"), " ")  // Reduce multiple spaces
            .trim()
    }
    
    /**
     * Calculates confidence score for the parsed recipe
     */
    fun calculateConfidence(text: String, recipe: Recipe): Float {
        var score = 0f
        
        // Title confidence
        if (recipe.title.isNotBlank() && recipe.title.length > 3) {
            score += 0.2f
        }
        
        // Ingredients confidence
        if (recipe.ingredients.isNotEmpty()) {
            score += 0.3f
            // Bonus for having amounts/units
            val ingredientsWithAmounts = recipe.ingredients.count { it.amount != null }
            if (ingredientsWithAmounts > 0) {
                score += 0.1f * (ingredientsWithAmounts.toFloat() / recipe.ingredients.size)
            }
        }
        
        // Instructions confidence
        if (recipe.instructions.isNotEmpty()) {
            score += 0.3f
            // Bonus for having numbered steps
            val numberedSteps = recipe.instructions.count { it.matches(Regex("^\d+[.)\s].*")) }
            if (numberedSteps > 0) {
                score += 0.1f * (numberedSteps.toFloat() / recipe.instructions.size)
            }
        }
        
        // Metadata confidence
        if (recipe.servingSize != null) score += 0.05f
        if (recipe.prepTime != null) score += 0.05f
        if (recipe.cookTime != null) score += 0.05f
        
        return score.coerceAtMost(1.0f)
    }
}
