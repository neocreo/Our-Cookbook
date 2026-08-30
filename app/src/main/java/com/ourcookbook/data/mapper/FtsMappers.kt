package com.ourcookbook.data.mapper

import com.ourcookbook.data.db.entity.RecipeFtsEntity
import com.ourcookbook.domain.model.Ingredient
import com.ourcookbook.domain.model.Recipe

/**
 * Maps the FTS search result entity back to the Recipe domain model.
 *
 * The FTS table stores ingredients and instructions as plain text, so they are
 * split by line into lists. Fields not present in the FTS index fall back to
 * the domain defaults. This is a best-effort reconstruction for search results;
 * callers that need the full recipe should load it from the recipes table.
 */
fun RecipeFtsEntity.toDomain(): Recipe {
    return Recipe(
        id = id,
        title = title,
        description = description,
        category = category,
        ingredients = ingredients
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { Ingredient(name = it) },
        instructions = instructions
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() },
        deviceId = deviceId
    )
}
