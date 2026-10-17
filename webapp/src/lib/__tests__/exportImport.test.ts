import { describe, it, expect } from 'vitest'
import { recipesToMarkdown, parseRecipesJSON } from '../../lib/exportImport'
import { createRecipe } from '../../types/recipe'
import { createIngredient, createIngredientSection } from '../../types/ingredient'

const ing = (name: string, amount: string | null = null, unit: string | null = null, order = 0) =>
  createIngredient(name, amount, unit, null, order)

describe('recipesToMarkdown', () => {
  it('writes named ingredient sections as sub-headings', () => {
    const r = createRecipe({
      title: 'Biscuits and gravy',
      category: 'Breakfast',
      ingredientSections: [
        createIngredientSection('Biscuits', [createIngredient('flour', '2', 'cups', null, 0)]),
        createIngredientSection('Gravy', [createIngredient('milk', '1', 'cup', null, 0)]),
      ],
      instructions: ['Bake the biscuits'],
    })
    const md = recipesToMarkdown([r])
    expect(md).toContain('## Ingredients')
    expect(md).toContain('### Biscuits')
    expect(md).toContain('### Gravy')
    expect(md).toContain('- 2 cups flour')
    expect(md).toContain('- 1 cup milk')
  })

  it('keeps a single unnamed list without a sub-heading', () => {
    const r = createRecipe({
      title: 'Soup',
      category: 'Mains',
      ingredients: [createIngredient('water', '1', 'l', null, 0)],
    })
    const md = recipesToMarkdown([r])
    expect(md).toContain('## Ingredients')
    expect(md).not.toContain('###')
    expect(md).toContain('- 1 l water')
  })
})

describe('parseRecipesJSON', () => {
  it('preserves ingredient sections and mirrors them in the flat list', () => {
    const json = JSON.stringify([{
      title: 'Biscuits and gravy',
      category: 'Breakfast',
      ingredientSections: [
        { id: 's1', name: 'Biscuits', ingredients: [{ id: 'i1', name: 'flour', amount: '2', unit: 'cups', notes: null, order: 0 }] },
        { id: 's2', name: 'Gravy', ingredients: [{ id: 'i2', name: 'milk', amount: '1', unit: 'cup', notes: null, order: 0 }] },
      ],
    }])
    const [r] = parseRecipesJSON(json)
    expect(r.ingredientSections?.map((s) => s.name)).toEqual(['Biscuits', 'Gravy'])
    expect(r.ingredients.map((i) => i.name)).toEqual(['flour', 'milk'])
  })

  it('derives the flat list from sections when it is missing', () => {
    const json = JSON.stringify([{
      title: 'X',
      category: 'C',
      ingredientSections: [
        { id: 's1', name: 'A', ingredients: [{ id: 'i1', name: 'sugar', amount: null, unit: null, notes: null, order: 0 }] },
      ],
    }])
    const [r] = parseRecipesJSON(json)
    expect(r.ingredients.map((i) => i.name)).toEqual(['sugar'])
  })
})

describe('section-header detection at import', () => {
  const recipeJSON = (ingredients: ReturnType<typeof ing>[]) =>
    JSON.stringify([{ title: 'R', category: 'C', ingredients }])

  it('splits label rows into named sections (biscuits and gravy)', () => {
    const [r] = parseRecipesJSON(recipeJSON([
      ing('Biscuits'),
      ing('all-purpose flour', '2', 'cups', 1),
      ing('baking powder', '1', 'tbsp', 2),
      ing('Gravy'),
      ing('breakfast sausages', '2', null, 4),
      ing('Salt, to taste'),
    ]))
    expect(r.ingredientSections?.map((s) => s.name)).toEqual(['Biscuits', 'Gravy'])
    expect(r.ingredients.map((i) => i.name)).toEqual([
      'all-purpose flour', 'baking powder', 'breakfast sausages', 'Salt, to taste',
    ])
  })

  it('drops empty sections created by consecutive labels', () => {
    const [r] = parseRecipesJSON(recipeJSON([
      ing('General'),
      ing('Chorizo'),
      ing('finely ground pork', '2', 'lb', 2),
      ing('Salciccia (Italian sausage)'),
      ing('ground pork', '1', 'lb', 4),
    ]))
    expect(r.ingredientSections?.map((s) => s.name)).toEqual(['Chorizo', 'Salciccia (Italian sausage)'])
  })

  it('keeps leading rows before the first label in an unnamed section', () => {
    const [r] = parseRecipesJSON(recipeJSON([
      ing('ground beef', '1', 'lb', 0),
      ing('For the sauce'),
      ing('crushed tomatoes', '1', 'can', 2),
      ing('For the topping'),
      ing('cheese', '1', 'cup', 4),
    ]))
    expect(r.ingredientSections?.map((s) => s.name)).toEqual(['', 'For the sauce', 'For the topping'])
  })

  it('needs at least two label rows — a lone bare row stays an ingredient', () => {
    const [r] = parseRecipesJSON(recipeJSON([
      ing('Nonstick cooking spray'),
      ing('whole-grain bread', '4', null, 1),
    ]))
    expect(r.ingredientSections).toBeUndefined()
    expect(r.ingredients.map((i) => i.name)).toEqual(['Nonstick cooking spray', 'whole-grain bread'])
  })

  it('keeps "Salt to taste", "OR" and trailing bare rows as ingredients', () => {
    const [r] = parseRecipesJSON(recipeJSON([
      ing('beans', '1', 'can', 0),
      ing('Salt to taste'),
      ing('OR'),
      ing('stalks of celery', '2', null, 3),
    ]))
    expect(r.ingredientSections).toBeUndefined()
    expect(r.ingredients.map((i) => i.name)).toEqual(['beans', 'Salt to taste', 'OR', 'stalks of celery'])
  })

  it('does not turn a run of bare rows into labels when it dead-ends', () => {
    const [r] = parseRecipesJSON(recipeJSON([
      ing('beans', '1', 'can', 0),
      ing('Fresh Basil'),
      ing('Fresh Rosemary'),
      ing('Fresh Thyme'),
      ing('Salt, to taste'),
    ]))
    expect(r.ingredientSections).toBeUndefined()
    expect(r.ingredients).toHaveLength(5)
  })

  it('does not touch recipes that already have sections', () => {
    const json = JSON.stringify([{
      title: 'R',
      category: 'C',
      ingredients: [ing('Biscuits'), ing('flour', '2', 'cups', 1)],
      ingredientSections: [
        { id: 's1', name: 'Dough', ingredients: [ing('flour', '2', 'cups', 0)] },
      ],
    }])
    const [r] = parseRecipesJSON(json)
    expect(r.ingredientSections?.map((s) => s.name)).toEqual(['Dough'])
  })
})
