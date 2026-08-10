package com.ourcookbook.domain.model

import java.util.UUID

/**
 * Domain model for Ingredient
 * Represents a single ingredient in a recipe
 * 
 * Contains all information about an ingredient including amount, unit, name,
 * and additional notes. Supports both metric and imperial measurement systems.
 */
data class Ingredient(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val amount: String? = null,
    val unit: String? = null,
    val notes: String? = null,
    val order: Int = 0
) {
    fun isValid(): Boolean {
        return name.isNotBlank()
    }
    
    // Display string for the ingredient
    val displayString: String get() {
        return buildString {
            if (!amount.isNullOrBlank()) {
                append(amount)
                if (!unit.isNullOrBlank()) {
                    append(" ").append(unit)
                }
                append(" ")
            }
            append(name)
            if (!notes.isNullOrBlank()) {
                append(" (").append(notes).append(")")
            }
        }.trim()
    }
    
    // Simple display without notes
    val simpleDisplay: String get() {
        return buildString {
            if (!amount.isNullOrBlank()) {
                append(amount)
                if (!unit.isNullOrBlank()) {
                    append(" ").append(unit)
                }
                append(" ")
            }
            append(name)
        }.trim()
    }
    
    companion object {
        fun create(
            name: String,
            amount: String? = null,
            unit: String? = null,
            notes: String? = null,
            order: Int = 0
        ): Ingredient {
            return Ingredient(
                name = name,
                amount = amount,
                unit = unit,
                notes = notes,
                order = order
            )
        }
    }
}