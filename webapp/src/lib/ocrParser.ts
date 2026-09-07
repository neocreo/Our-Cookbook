// OCR text parser — ported from OcrTextParser.kt. Parses raw OCR-extracted
// text into structured recipe data. Platform-independent: same parser for
// web (tesseract.js) and mobile (ML Kit, later).

import type { RecipeInput } from '../types/recipe'
import type { Ingredient } from '../types/ingredient'
import { createIngredient } from '../types/ingredient'

interface ParsedIngredient {
  name: string
  amount: string | null
  unit: string | null
  notes: string | null
}

const SECTION_HEADERS = [
  'ingredients', 'instructions', 'directions', 'method', 'steps',
  'notes', 'description', 'preparation', 'cooking', 'baking',
]

function isSectionHeader(line: string): boolean {
  const lower = line.toLowerCase().replace(/:$/, '')
  return SECTION_HEADERS.includes(lower)
}

function extractValue(line: string, ...prefixes: string[]): string {
  for (const prefix of prefixes) {
    if (line.toLowerCase().startsWith(prefix.toLowerCase())) {
      return line.substring(prefix.length).trim().replace(/^:/, '').trim()
    }
  }
  return line
}

function extractNumber(text: string): number | null {
  const match = /\d+/.exec(text)
  return match ? parseInt(match[0], 10) : null
}

function extractTime(text: string): number | null {
  const lower = text.toLowerCase()
  const hourMatch = /(\d+)\s*hours?/.exec(lower)
  const minuteMatch = /(\d+)\s*minutes?/.exec(lower)

  if (hourMatch && minuteMatch) {
    return parseInt(hourMatch[1], 10) * 60 + parseInt(minuteMatch[1], 10)
  }
  if (hourMatch) return parseInt(hourMatch[1], 10) * 60
  if (minuteMatch) return parseInt(minuteMatch[1], 10)

  // "Xh Ym" format
  const hmMatch = /(\d+)h\s*(\d+)m/.exec(lower)
  if (hmMatch) return parseInt(hmMatch[1], 10) * 60 + parseInt(hmMatch[2], 10)

  return extractNumber(text)
}

function extractNotes(text: string): string | null {
  const match = /\s*\((.*)\)/.exec(text)
  return match ? match[1].trim() : null
}

function parseIngredient(line: string): ParsedIngredient | null {
  let cleanLine = line.trim()
  if (!cleanLine) return null

  // Remove bullet points
  cleanLine = cleanLine.replace(/^[-•·]\s*/, '')
  // Remove numbering (1., 2), etc)
  cleanLine = cleanLine.replace(/^\d+\.\s*/, '').trim()
  if (!cleanLine) return null

  // Try to extract amount and unit from the start
  const amountUnitMatch = /^(\d+\s*\d*\/\d+|\d+\.\d+|\d+)\s*([a-z]+)?/i.exec(cleanLine)
  if (amountUnitMatch) {
    const amount = amountUnitMatch[1]
    const unit = amountUnitMatch[2]?.trim() || null
    const remaining = cleanLine.substring(amountUnitMatch[0].length).trim()
    const notes = extractNotes(remaining)
    const name = remaining.replace(/\s*\(.*\)/, '').trim() || remaining

    return { name: name || remaining, amount, unit, notes }
  }

  return { name: cleanLine, amount: null, unit: null, notes: null }
}

function parseInstruction(line: string): string {
  let cleanLine = line.trim()
  cleanLine = cleanLine.replace(/^[-•·]\s*/, '')
  cleanLine = cleanLine.replace(/^\d+\.\s*/, '').trim()
  return cleanLine
}

function inferCategoryFromTitle(title: string): string {
  const t = title.toLowerCase()
  if (/breakfast|pancake|waffle|omelet|oatmeal|granola/.test(t)) return 'Breakfast'
  if (/cookie|cake|pie|brownie|dessert|sweet|chocolate|ice cream/.test(t)) return 'Dessert'
  if (/salad|soup|side|garnish/.test(t)) return 'Side'
  if (/sauce|dressing|marinade|gravy/.test(t)) return 'Sauce'
  if (/snack|appetizer|dip|spread/.test(t)) return 'Appetizer'
  return 'Mains'
}

const INGREDIENT_PATTERNS = [
  /^\s*[-•·]\s*/,
  /^\d+\s+[a-z]+/i,
  /^\d+\s*\d*\/\d+\s+[a-z]+/i,
]

const ACTION_VERBS = [
  'mix', 'stir', 'bake', 'cook', 'fry', 'boil', 'simmer', 'chop', 'dice',
  'slice', 'preheat', 'whisk', 'combine', 'add', 'pour', 'heat', 'remove',
  'season', 'serve', 'drain', 'blend', 'knead', 'roll', 'grease', 'sprinkle',
]

function extractIngredientsFromText(text: string): ParsedIngredient[] {
  const ingredients: ParsedIngredient[] = []
  for (const line of text.split('\n')) {
    const trimmed = line.trim()
    if (!trimmed) continue
    if (INGREDIENT_PATTERNS.some((p) => p.test(trimmed))) {
      const ing = parseIngredient(trimmed)
      if (ing) ingredients.push(ing)
    }
  }
  return ingredients
}

function extractInstructionsFromText(text: string): string[] {
  const instructions: string[] = []
  for (const line of text.split('\n')) {
    const trimmed = line.trim()
    if (!trimmed || isSectionHeader(trimmed)) continue
    if (/^\d+\.\s/.test(trimmed) || /^[-•·]\s/.test(trimmed)) {
      const inst = parseInstruction(trimmed)
      if (inst) instructions.push(inst)
      continue
    }
    if (ACTION_VERBS.some((v) => trimmed.toLowerCase().includes(v))) {
      const inst = parseInstruction(trimmed)
      if (inst) instructions.push(inst)
    }
  }
  return instructions.length ? instructions : ['Add instructions']
}

/** Preprocess raw OCR text: normalize line endings, collapse whitespace. */
export function preprocessOcrText(text: string): string {
  return text
    .replace(/\r\n/g, '\n')
    .replace(/\r/g, '\n')
    .replace(/\n{3,}/g, '\n\n')
    .replace(/[ \t]{2,}/g, ' ')
    .trim()
}

/** Parse OCR text into a RecipeInput ready for saving. */
export function parseRecipeFromText(text: string): RecipeInput {
  const clean = preprocessOcrText(text)
  const lines = clean.split('\n')

  let title = ''
  let description: string | null = null
  let category = 'Mains'
  const ingredients: ParsedIngredient[] = []
  const instructions: string[] = []
  let servingSize: number | null = null
  let prepTime: number | null = null
  let cookTime: number | null = null
  let source: string | null = null
  const tags: string[] = []

  let currentSection = ''

  for (const line of lines) {
    const trimmed = line.trim()
    if (!trimmed) continue

    // Title: first non-empty, non-header line
    if (!title && !isSectionHeader(trimmed)) {
      title = trimmed
      continue
    }

    // Section headers
    const lower = trimmed.toLowerCase().replace(/:$/, '')
    if (['ingredients', 'ingredient list'].includes(lower)) {
      currentSection = 'ingredients'
      continue
    }
    if (['instructions', 'directions', 'method', 'steps'].includes(lower)) {
      currentSection = 'instructions'
      continue
    }
    if (['notes', 'note'].includes(lower)) {
      currentSection = 'notes'
      continue
    }

    // Metadata
    if (trimmed.toLowerCase().startsWith('category:')) {
      category = extractValue(trimmed, 'Category:')
      continue
    }
    if (/^(serves|serving size|makes):/i.test(trimmed)) {
      servingSize = extractNumber(extractValue(trimmed, 'Serves:', 'Serving Size:', 'Makes:'))
      continue
    }
    if (trimmed.toLowerCase().startsWith('prep time:')) {
      prepTime = extractTime(extractValue(trimmed, 'Prep Time:'))
      continue
    }
    if (/^(cook time|bake time):/i.test(trimmed)) {
      cookTime = extractTime(extractValue(trimmed, 'Cook Time:', 'Bake Time:'))
      continue
    }
    if (trimmed.toLowerCase().startsWith('source:')) {
      source = extractValue(trimmed, 'Source:')
      continue
    }
    if (trimmed.toLowerCase().startsWith('tags:')) {
      tags.push(...extractValue(trimmed, 'Tags:').split(',').map((t) => t.trim()).filter(Boolean))
      continue
    }

    // Section content
    if (currentSection === 'ingredients' && !isSectionHeader(trimmed)) {
      const ing = parseIngredient(trimmed)
      if (ing) ingredients.push(ing)
    } else if (currentSection === 'instructions' && !isSectionHeader(trimmed)) {
      const inst = parseInstruction(trimmed)
      if (inst) instructions.push(inst)
    } else if (currentSection === 'notes') {
      description = description ? `${description}\n${trimmed}` : trimmed
    }
  }

  if (!title) title = clean.split('\n').find((l) => l.trim())?.trim().slice(0, 50) || 'Untitled Recipe'
  if (category === 'Mains') category = inferCategoryFromTitle(title)
  if (ingredients.length === 0) ingredients.push(...extractIngredientsFromText(clean))
  if (instructions.length === 0) instructions.push(...extractInstructionsFromText(clean))

  const ingredientObjects: Ingredient[] = ingredients.map((ing, i) =>
    createIngredient(ing.name, ing.amount, ing.unit, ing.notes, i),
  )

  return {
    title: title || 'Untitled Recipe',
    category,
    description: description || undefined,
    ingredients: ingredientObjects,
    instructions: instructions.length ? instructions : ['Add instructions'],
    servingSize,
    prepTime,
    cookTime,
    source: source || undefined,
    tags,
  }
}
