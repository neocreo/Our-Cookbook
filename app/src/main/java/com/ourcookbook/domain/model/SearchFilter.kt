package com.ourcookbook.domain.model

/**
 * Domain model for Search Filters
 * Represents all possible search filter options
 */
data class SearchFilter(
    val categories: List<String> = emptyList(),
    val ingredients: List<String> = emptyList(),
    val cookingTime: Int? = null, // in minutes
    val minServings: Int? = null,
    val maxServings: Int? = null,
    val minRating: Float? = null,
    val maxRating: Float? = null,
    val tags: List<String> = emptyList(),
    val showFavoritesOnly: Boolean = false
) {
    val hasActiveFilters: Boolean get() = 
        categories.isNotEmpty() || 
        ingredients.isNotEmpty() || 
        cookingTime != null || 
        minServings != null || 
        maxServings != null || 
        minRating != null || 
        maxRating != null || 
        tags.isNotEmpty() || 
        showFavoritesOnly

    fun toMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        
        if (categories.isNotEmpty()) {
            map["categories"] = categories.joinToString(",")
        }
        if (ingredients.isNotEmpty()) {
            map["ingredients"] = ingredients.joinToString(",")
        }
        cookingTime?.let { map["cookingTime"] = it.toString() }
        minServings?.let { map["minServings"] = it.toString() }
        maxServings?.let { map["maxServings"] = it.toString() }
        minRating?.let { map["minRating"] = it.toString() }
        maxRating?.let { map["maxRating"] = it.toString() }
        if (tags.isNotEmpty()) {
            map["tags"] = tags.joinToString(",")
        }
        if (showFavoritesOnly) {
            map["showFavoritesOnly"] = "true"
        }
        
        return map
    }

    companion object {
        fun fromMap(map: Map<String, String>): SearchFilter {
            return SearchFilter(
                categories = map["categories"]?.split(",") ?: emptyList(),
                ingredients = map["ingredients"]?.split(",") ?: emptyList(),
                cookingTime = map["cookingTime"]?.toIntOrNull(),
                minServings = map["minServings"]?.toIntOrNull(),
                maxServings = map["maxServings"]?.toIntOrNull(),
                minRating = map["minRating"]?.toFloatOrNull(),
                maxRating = map["maxRating"]?.toFloatOrNull(),
                tags = map["tags"]?.split(",") ?: emptyList(),
                showFavoritesOnly = map["showFavoritesOnly"]?.toBoolean() ?: false
            )
        }
        
        val EMPTY = SearchFilter()
    }
}