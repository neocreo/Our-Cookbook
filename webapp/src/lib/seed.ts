// Sample seed data, adapted from _sources/cookbook-data.js, mapped to the
// domain models. Used to populate the app on first run so the recipe list is
// not empty. Runs once when the database has no recipes.

import type { Cookbook } from '../types/cookbook'
import type { Recipe } from '../types/recipe'
import { createCookbook } from '../types/cookbook'
import { createRecipe } from '../types/recipe'
import { createIngredient } from '../types/ingredient'

interface SeedRecipe {
  cookbookId: string
  title: string
  category: string
  servings: number
  time: string
  tags: string[]
  ingredients: string[]
  steps: string[]
  source: string
  notes: string
}

function parseMinutes(time: string): number | null {
  const hr = /(\d+)\s*hr/.exec(time)
  const min = /(\d+)\s*min/.exec(time)
  let total = 0
  let found = false
  if (hr) {
    total += Number(hr[1]) * 60
    found = true
  }
  if (min) {
    total += Number(min[1])
    found = true
  }
  return found ? total : null
}

const COOKBOOKS = [
  { id: 'cb-my', name: 'My Cookbook', subtitle: 'Recipes gathered over the years' },
  { id: 'cb-beths', name: "Beth's Favourites", subtitle: "Recipes from Beth's kitchen" },
  { id: 'cb-family', name: 'Family Cookbook', subtitle: 'Passed down through generations' },
]

const RECIPES: SeedRecipe[] = [
  {
    cookbookId: 'cb-my',
    title: 'Sunday Pot Roast',
    category: 'Dinner',
    servings: 6,
    time: '3 hr 10 min',
    tags: ['Dinner', 'Beef'],
    ingredients: [
      '3 lb beef chuck roast', '2 tbsp flour', '1 onion, sliced', '3 carrots, chopped',
      '3 potatoes, quartered', '2 cups beef broth', '1 tbsp Worcestershire sauce', 'Salt and pepper',
    ],
    steps: [
      'Season roast with salt, pepper and flour on all sides.',
      'Sear roast in a hot dutch oven until browned, about 4 min per side.',
      'Add onion, carrots and potatoes around the roast.',
      'Pour in broth and Worcestershire, cover and simmer low 3 hours.',
      'Rest 10 minutes, then slice against the grain and serve.',
    ],
    source: 'Grandma Ruth, 1974',
    notes: 'Leftovers make an excellent sandwich the next day.',
  },
  {
    cookbookId: 'cb-my',
    title: 'Skillet Cornbread',
    category: 'Bread',
    servings: 8,
    time: '40 min',
    tags: ['Bread', 'Side'],
    ingredients: [
      '1 cup cornmeal', '1 cup flour', '2 tbsp sugar', '1 tbsp baking powder',
      '1 cup buttermilk', '2 eggs', '1/4 cup melted butter',
    ],
    steps: [
      'Preheat oven to 425°F with a cast iron skillet inside.',
      'Whisk dry ingredients together.',
      'Stir in buttermilk, eggs and melted butter until just combined.',
      'Pour into the hot skillet and bake 20-22 minutes until golden.',
    ],
    source: 'Family recipe box',
    notes: 'A cast iron skillet gives the crust its snap.',
  },
  {
    cookbookId: 'cb-my',
    title: 'Lemon Icebox Pie',
    category: 'Dessert',
    servings: 8,
    time: '25 min + chill',
    tags: ['Dessert'],
    ingredients: [
      '1 graham cracker crust', '1 can sweetened condensed milk', '3 egg yolks',
      '1/2 cup lemon juice', '1 tsp lemon zest', 'Whipped cream to serve',
    ],
    steps: [
      'Whisk condensed milk, egg yolks, lemon juice and zest until smooth.',
      'Pour into crust and bake at 350°F for 15 minutes.',
      'Cool completely, then chill at least 3 hours before serving.',
    ],
    source: "Aunt Carol's card box",
    notes: '',
  },
  {
    cookbookId: 'cb-beths',
    title: "Beth's Deviled Eggs",
    category: 'Appetizer',
    servings: 12,
    time: '20 min',
    tags: ['Appetizer'],
    ingredients: [
      '12 hard-boiled eggs', '1/3 cup mayonnaise', '1 tbsp mustard',
      '1 tsp paprika', 'Salt to taste', 'Chives for garnish',
    ],
    steps: [
      'Halve eggs and scoop yolks into a bowl.',
      'Mash yolks with mayonnaise, mustard and salt until smooth.',
      'Pipe filling back into whites and dust with paprika.',
      'Garnish with chives and chill until serving.',
    ],
    source: 'Beth, church potluck 1981',
    notes: 'Double the recipe — they disappear fast.',
  },
  {
    cookbookId: 'cb-beths',
    title: 'Green Bean Casserole',
    category: 'Side',
    servings: 8,
    time: '55 min',
    tags: ['Side', 'Casserole'],
    ingredients: [
      '2 lb green beans, trimmed', '2 cans cream of mushroom soup', '1 cup milk',
      '2 cups fried onions', 'Salt and pepper',
    ],
    steps: [
      'Preheat oven to 350°F.',
      'Blanch green beans 4 minutes, then drain.',
      'Mix beans, soup, milk and half the onions in a baking dish.',
      'Bake 30 minutes, top with remaining onions, bake 10 more.',
    ],
    source: 'Beth',
    notes: '',
  },
  {
    cookbookId: 'cb-beths',
    title: 'Chicken & Dumplings',
    category: 'Dinner',
    servings: 6,
    time: '1 hr 20 min',
    tags: ['Dinner', 'Comfort Food'],
    ingredients: [
      '1 whole chicken', '6 cups chicken broth', '2 carrots, sliced',
      '2 celery stalks, sliced', '2 cups flour', '1 tbsp baking powder', '3/4 cup milk',
    ],
    steps: [
      'Simmer chicken in broth with carrots and celery until tender, about 45 min.',
      'Remove chicken, shred meat and return to the pot.',
      'Mix flour, baking powder, salt and milk into a soft dough.',
      'Drop spoonfuls of dough into the simmering broth, cover and cook 15 minutes.',
    ],
    source: "Beth's mother",
    notes: 'Do not lift the lid while the dumplings steam.',
  },
  {
    cookbookId: 'cb-family',
    title: "Grandma's Meatloaf",
    category: 'Dinner',
    servings: 6,
    time: '1 hr 10 min',
    tags: ['Dinner', 'Beef'],
    ingredients: [
      '2 lb ground beef', '1 cup breadcrumbs', '2 eggs', '1/2 cup ketchup',
      '1 onion, diced', '1 tsp garlic powder', 'Salt and pepper',
    ],
    steps: [
      'Preheat oven to 375°F.',
      'Mix all ingredients except a quarter cup of ketchup.',
      'Shape into a loaf in a baking pan and brush with remaining ketchup.',
      'Bake 55-60 minutes until cooked through.',
    ],
    source: 'Grandma Ruth, 1968',
    notes: 'Rest 10 minutes before slicing so it holds together.',
  },
  {
    cookbookId: 'cb-family',
    title: 'Rhubarb Crisp',
    category: 'Dessert',
    servings: 6,
    time: '50 min',
    tags: ['Dessert'],
    ingredients: [
      '4 cups rhubarb, chopped', '3/4 cup sugar', '1 cup oats', '1/2 cup flour',
      '1/2 cup brown sugar', '1/2 cup butter, softened', '1 tsp cinnamon',
    ],
    steps: [
      'Preheat oven to 375°F. Toss rhubarb with sugar in a baking dish.',
      'Mix oats, flour, brown sugar, butter and cinnamon into a crumble.',
      'Scatter crumble over rhubarb and bake 35-40 minutes until bubbling.',
    ],
    source: 'Family recipe box',
    notes: 'Serve warm with vanilla ice cream.',
  },
]

export function buildSeed(deviceId: string): { cookbooks: Cookbook[]; recipes: Recipe[] } {
  const recipes: Recipe[] = RECIPES.map((s) =>
    createRecipe(
      {
        title: s.title,
        category: s.category,
        servingSize: s.servings,
        cookTime: parseMinutes(s.time),
        source: s.source,
        notes: s.notes || null,
        tags: s.tags,
        ingredients: s.ingredients.map((line, i) => createIngredient(line, null, null, null, i)),
        instructions: s.steps,
        isFavorite: s.title === 'Sunday Pot Roast',
      },
      deviceId,
    ),
  )

  const cookbooks: Cookbook[] = COOKBOOKS.map((cb) => {
    const ids = RECIPES
      .map((s, i) => (s.cookbookId === cb.id ? recipes[i].id : null))
      .filter((id): id is string => id != null)
    return createCookbook(cb.name, deviceId, cb.subtitle, ids)
  })

  return { cookbooks, recipes }
}
