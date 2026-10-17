import { describe, it, expect } from 'vitest'
import { parseRecipeFromText, preprocessOcrText, parseIngredientsFromText, parseInstructionsFromText, stripLeadingSectionHeaders } from '../../lib/ocrParser'

describe('preprocessOcrText', () => {
  it('normalizes line endings', () => {
    expect(preprocessOcrText('a\r\nb\rc')).toBe('a\nb\nc')
  })

  it('collapses excessive newlines', () => {
    expect(preprocessOcrText('a\n\n\n\nb')).toBe('a\n\nb')
  })

  it('collapses multiple spaces but preserves newlines', () => {
    expect(preprocessOcrText('a    b\n  c')).toBe('a b\n c')
  })
})

describe('parseRecipeFromText', () => {
  const sample = [
    "Grandma's Apple Pie",
    '',
    'Category: Dessert',
    'Serves: 8',
    'Prep Time: 20 minutes',
    'Cook Time: 45 minutes',
    '',
    'Ingredients:',
    '- 2 cups flour',
    '- 1/2 cup butter',
    '- 6 apples, sliced',
    '- 3/4 cup sugar',
    '- 1 tbsp cinnamon',
    '',
    'Instructions:',
    '1. Preheat oven to 375F',
    '2. Mix flour and butter into a dough',
    '3. Roll out dough and place in pie dish',
    '4. Fill with apples, sugar and cinnamon',
    '5. Bake 45 minutes until golden',
    '',
    'Notes:',
    'Serve with vanilla ice cream.',
  ].join('\n')

  it('extracts the title from the first non-empty line', () => {
    const r = parseRecipeFromText(sample)
    expect(r.title).toBe("Grandma's Apple Pie")
  })

  it('extracts category', () => {
    expect(parseRecipeFromText(sample).category).toBe('Dessert')
  })

  it('extracts serving size, prep and cook time', () => {
    const r = parseRecipeFromText(sample)
    expect(r.servingSize).toBe(8)
    expect(r.prepTime).toBe(20)
    expect(r.cookTime).toBe(45)
  })

  it('parses 5 ingredients with amounts and units', () => {
    const r = parseRecipeFromText(sample)
    expect(r.ingredients).toHaveLength(5)
    expect(r.ingredients![0]).toMatchObject({ amount: '2', unit: 'cups', name: 'flour' })
    expect(r.ingredients![1]).toMatchObject({ amount: '1/2', unit: 'cup', name: 'butter' })
  })

  it('parses 5 instructions with numbering stripped', () => {
    const r = parseRecipeFromText(sample)
    expect(r.instructions).toHaveLength(5)
    expect(r.instructions![0]).toBe('Preheat oven to 375F')
  })

  it('puts notes into description', () => {
    expect(parseRecipeFromText(sample).description).toBe('Serve with vanilla ice cream.')
  })

  it('infers category from title when not specified', () => {
    const r = parseRecipeFromText('Chocolate Cake\n\nInstructions:\n1. Bake it')
    expect(r.category).toBe('Dessert')
  })

  it('handles empty text gracefully', () => {
    const r = parseRecipeFromText('')
    expect(r.title).toBe('Untitled Recipe')
    expect(r.ingredients).toHaveLength(0)
  })
})

describe('region text parsing (marked OCR areas)', () => {
  it('parses one ingredient per line', () => {
    const ings = parseIngredientsFromText('2 cups flour\n1/2 cup butter\nsalt to taste')
    expect(ings).toHaveLength(3)
    expect(ings[0]).toMatchObject({ amount: '2', unit: 'cups', name: 'flour' })
    expect(ings[2]).toMatchObject({ name: 'salt to taste' })
  })

  it('parses one instruction per line', () => {
    expect(parseInstructionsFromText('Mix everything\nBake 30 min')).toEqual(['Mix everything', 'Bake 30 min'])
  })

  it('does not turn the "Ingredients" heading into an ingredient', () => {
    // the marked rectangle included the heading line above the list
    const ings = parseIngredientsFromText(stripLeadingSectionHeaders('Ingredients:\n2 cups flour\n1 egg'))
    expect(ings).toHaveLength(2)
    expect(ings[0]).toMatchObject({ amount: '2', unit: 'cups', name: 'flour' })
    expect(ings.map((i) => i.name)).not.toContain('Ingredients:')
  })

  it('does not turn the "Instructions" heading into a step', () => {
    const steps = parseInstructionsFromText(stripLeadingSectionHeaders('Instructions:\n1. Mix\n2. Bake'))
    expect(steps).toEqual(['Mix', 'Bake'])
  })
})

describe('stripLeadingSectionHeaders', () => {
  it('removes leading blank lines and section headers', () => {
    expect(stripLeadingSectionHeaders('\n\nIngredients:\n\n1 cup flour')).toBe('1 cup flour')
  })

  it('removes several leading headers', () => {
    expect(stripLeadingSectionHeaders('Ingredients:\nInstructions:\nMix')).toBe('Mix')
  })

  it('matches headers case-insensitively and with or without colon', () => {
    expect(stripLeadingSectionHeaders('INGREDIENTS\n1 cup flour')).toBe('1 cup flour')
    expect(stripLeadingSectionHeaders('Directions:\nMix')).toBe('Mix')
  })

  it('leaves content without leading headers untouched', () => {
    expect(stripLeadingSectionHeaders('2 cups flour\n1 egg')).toBe('2 cups flour\n1 egg')
  })

  it('does not strip headers in the middle of the text', () => {
    expect(stripLeadingSectionHeaders('1 cup flour\nNotes:\nchill')).toBe('1 cup flour\nNotes:\nchill')
  })
})
