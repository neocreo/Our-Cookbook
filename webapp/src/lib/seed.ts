// Sample seed data, taken from Gwen and Daniel's cookbook ("Now That's Good
// Eatin'", _sources/Now thats good eatin_v3.json). Used to populate the app
// on first run so the recipe list is not empty. Runs once when the database
// has no recipes.

import type { Cookbook } from '../types/cookbook'
import type { Recipe } from '../types/recipe'
import { createCookbook } from '../types/cookbook'
import { createRecipe } from '../types/recipe'
import { createIngredient } from '../types/ingredient'

interface SeedIngredient {
  name: string
  amount?: string | null
  unit?: string | null
}

interface SeedRecipe {
  title: string
  description: string | null
  category: string
  servings: number | null
  prepTime: number | null
  cookTime: number | null
  tags: string[]
  ingredients: SeedIngredient[]
  instructions: string[]
  source: string | null
  isFavorite?: boolean
}

const COOKBOOK = {
  name: 'Example Cookbook',
  description: "Recipes from Gwen and Daniel's kitchen — Now That's Good Eatin'",
}

const RECIPES: SeedRecipe[] = [
  {
    title: "Gwen's Huevos Luchadores",
    description:
      'We take breakfast very seriously and this is our absolute favorite breakfast. We eat it constantly, at least once every weekend.',
    category: 'Breakfast',
    servings: 2,
    prepTime: 10,
    cookTime: 15,
    tags: [],
    ingredients: [
      { name: 'chorizo sausage (can be left out for vegetarian), crumbled', amount: '2', unit: 'links' },
      { name: 'red onion, chopped', amount: '1/2' },
      { name: 'cloves fresh garlic, minced', amount: '2' },
      { name: 'fresh jalapeno pepper, finely chopped', amount: '1' },
      { name: 'finely chopped fresh cilantro', amount: '2', unit: 'tbsp' },
      { name: 'tomato, chopped', amount: '1' },
      { name: 'lime, juiced', amount: '1' },
      { name: 'black beans, drained', amount: '1', unit: 'can' },
      { name: 'cumin', amount: '2', unit: 'tsp' },
      { name: 'molasses', amount: '1', unit: 'tbsp' },
      { name: 'Salt and pepper, to taste' },
      { name: 'eggs', amount: '4' },
    ],
    instructions: [
      "Combine crumbled chorizo, chopped onion, minced garlic, finely chopped jalapeno, and finely chopped cilantro and cook on medium high heat until sausage is cooked and onions are soft. Squeeze in the lime and add the chopped tomato. Let cook for a few minutes or until the tomatoes have broken down and released their juices. Then add the beans, cumin, molasses, and salt and pepper to taste. Let this cook together until it's bubbly and fragrant and some of the juices have boiled off. Make four small wells in the sauce and crack in the eggs. Cover and let simmer on medium heat until the eggs are set, approximately five minutes.",
      'Serve with tortillas, pickled jalapenos, and a scoop of sour cream. We like to use nice and thick Turkish yogurt instead of sour cream.',
      'Feeds two very hungry people or four normal people.',
    ],
    source: "Now That's Good Eatin' — Gwen and Daniel's Kitchen",
    isFavorite: true,
  },
  {
    title: 'Cuban Pork Sandwich (Cubanos) from Chef Movie',
    description:
      "This is the cubanos sandwich recipe from the 2014 movie Chef. The food in the recipe was created by real life rock star chef Roy Choi. This is the actual recipe from the movie! It was published on thestar.com. This is supposed to be made with a panini/sandwich press but I do not have one so I've provided instructions for how to make this in a skillet. AND these are great for freezing!",
    category: 'Mains',
    servings: 2,
    prepTime: 10,
    cookTime: 15,
    tags: [],
    ingredients: [
      { name: 'slices baked leg ham', amount: '2', unit: 'thin' },
      { name: 'Mojo Marinated Pork slices', amount: '4', unit: 'large' },
      { name: 'white baguettes, 6 inch/15 cm in length, cut in half', amount: '2', unit: 'pieces' },
      { name: 'Melted butter, for brushing' },
      { name: 'American mustard' },
      { name: 'slices Swiss cheese', amount: '2', unit: 'thin' },
      { name: '3 dill pickles, thinly sliced', amount: '2', unit: 'or' },
    ],
    instructions: [
      'Heat skillet over medium heat. Add ham and pork slices, and cook each side until slightly browned then remove to a plate.',
      'Butter cut sides of baguettes then place in the skillet, cut side down, for 2 minutes until lightly browned. Remove onto work surface.',
      'Layer the bottom of the baguettes with pork, ham, then cheese and pickles. Cut, break or fold the ham, pork and cheese so they fit. Spread the cut side of the bun tops with mustard then place on the sandwich.',
      'Butter the bottom AND top of the outside of the baguettes.',
      'Heat skillet over medium high heat. Place the baguettes in the skillet, top with a sheet of baking paper then weigh it down with a heavy skillet or pot (use cans if necessary for extra weight so the sandwich compressed). Cook for 3 minutes on each side, until dark golden brown and crispy, and the cheese is melted.',
      'Let sandwiches stand 1 minute before cutting in half. Serve IMMEDIATELY.',
    ],
    source: "Now That's Good Eatin' — Gwen and Daniel's Kitchen",
  },
  {
    title: 'Fermented Hot Sauce',
    description:
      'First, ferment the serrano peppers. Roughly chop the peppers, then pack them into a ball jar, leaving at least 1 inch of head space. The peppers may rise a bit when fermenting.',
    category: 'Sauce',
    servings: 2,
    prepTime: 10,
    cookTime: 15,
    tags: [],
    ingredients: [
      { name: 'red serrano peppers, or use any chili peppers, stems removed', amount: '1', unit: 'lb' },
      { name: 'unchlorinated water', amount: '1', unit: 'quart' },
      { name: 'salt', amount: '3', unit: 'tbsp' },
      {
        name: 'white wine vinegar, to your preference, use a good quality vinegar for better quality hot sauce',
        amount: '½-1',
        unit: 'cup',
      },
    ],
    instructions: [
      'Next, mix 1 quart unchlorinated water with 3 tablespoons sea salt. Pour just enough brine over the peppers to cover them, pressing them down a bit as you go. It is important to keep the peppers covered with brine to avoid spoilage. Check this daily.',
      'Screw on the lid and set the jar away from direct sunlight to ferment for at least 1 week. Ideal temperatures are between 55-75 degrees F. The most active fermentation period is between 1-2 weeks, so be sure to monitor it during this time. Burp the jars often by unscrewing the lid a bit to let out some of the accumulating gases. Or, use an airlock or membrane for easier fermenting.',
      'After 1-2 weeks, the fermenting activity will diminish and the brine will turn cloudy and taste acidic.',
      'Drain the peppers, but reserve the brine.',
      'Add the fermented peppers to a food processor or blender, along with 1/2 cup of the brine and 1/2 cup vinegar. You can add more or less of each as desired to your preference. More brine will have more salty flavor, more vinegar will be more acidic.',
      'Process until smooth.',
      'Add the fermented hot sauce to a pot and bring to a quick boil. Reduce heat and simmer for 15 minutes. This will stop the fermentation process.',
    ],
    source: "Now That's Good Eatin' — Gwen and Daniel's Kitchen",
  },
  {
    title: "Gwen's Spicy Love Bomb Chili",
    description: 'With which she captured the heart and stomach of a certain young Swede named Daniel.',
    category: 'Mains',
    servings: 6,
    prepTime: 10,
    cookTime: 60,
    tags: [],
    ingredients: [
      { name: 'boneless chuck roast, cut into small cubes, or you can use ground beef', amount: '1', unit: 'lb' },
      { name: 'onion, chopped', amount: '1' },
      { name: 'cloves of fresh garlic, finely chopped', amount: '3-4' },
      { name: 'fresh jalapeno peppers, finely chopped', amount: '2' },
      { name: "habanero pepper, seeded (if you're weak) and finely chopped", amount: '1' },
      { name: 'Alternatively, you can use a small can of chipotle peppers in adobo sauce' },
      { name: 'fresh cilantro, chopped', amount: '1/2', unit: 'bunch' },
      { name: 'beans of your choice, with liquid. I usually use black beans.', amount: '2', unit: 'cans' },
      { name: 'crushed tomatoes', amount: '1', unit: 'can' },
      { name: 'whole cherry tomatoes, including juice.', amount: '1', unit: 'can' },
      { name: 'lime, juiced', amount: '1' },
      { name: 'molasses', amount: '2', unit: 'tbsp' },
      { name: 'chili powder', amount: '1', unit: 'tbsp' },
      { name: 'cumin', amount: '1', unit: 'tbsp' },
      { name: 'Salt and pepper, to taste' },
      { name: 'dark chocolate (I know, I know....trust me)', amount: '3', unit: 'oz' },
    ],
    instructions: [
      "Heat about a tablespoon of oil in a large dutch oven or pot and cook the meat with the onions, garlic, and peppers, until the meat is browned and the onions are soft. (TIP: When using fresh peppers or chilis it's important to wear rubber gloves when handling them, and to add them early in the cooking process, so they have a chance to release their flavors and some of their heat. If you're not that into spicy food then I recommend using only the jalapenos and leaving out the habanero and chipotle peppers.) Add the cans of beans and tomatoes, chopped cilantro, lime juice, molasses, chili powder, cumin, salt, and pepper. Let it come to a boil and then add the chocolate and stir until melted. It may smell a lot like chocolate but it doesn't taste like it. The chocolate adds a very subtle flavor to it that's very hard to define. If you've ever had Chicken Mole Poblano with a really good mole sauce, you know what I'm talking about. Let it simmer on medium heat for about an hour or until the meat is tender. Serve with a dollop of sour cream or yogurt, shredded cheddar cheese, and chopped cilantro. Or just eat it plain.",
      'Feeds about six people.',
    ],
    source: "Now That's Good Eatin' — Gwen and Daniel's Kitchen",
  },
  {
    title: "She's my cherry pie",
    description:
      'TIP: put sugar, flour and cinnamon in a large zip-loc bag. Add the cherries and toss to coat. This coats them more even than doing it in a bowl and the filling is firmer.',
    category: 'Dessert',
    servings: 6,
    prepTime: 10,
    cookTime: 40,
    tags: [],
    ingredients: [
      { name: 'cherries, pitted', amount: '4', unit: 'cups' },
      { name: 'sugar (1 1/4 cups for sour cherries)', amount: '1', unit: 'cup' },
      { name: 'flour', amount: '1/4', unit: 'cup' },
      { name: 'cinnamon', amount: '1/2', unit: 'tsp' },
      { name: 'pastry for a double-crust 9-inch pie' },
    ],
    instructions: [
      'Mix ingredients for filling.',
      'Place in pastry-lined pie pan.',
      'Put top crust on. Cut vent slits in top crust, and flute edges.',
      'Brush with milk and sprinkle with sugar.',
      'Bake at 425°F for 10 minutes, reduce heat to 375°F and bake for 25-30 minutes more.',
    ],
    source: "Now That's Good Eatin' — Gwen and Daniel's Kitchen",
  },
]

export function buildSeed(deviceId: string): { cookbooks: Cookbook[]; recipes: Recipe[] } {
  const recipes: Recipe[] = RECIPES.map((s) =>
    createRecipe(
      {
        title: s.title,
        description: s.description,
        category: s.category,
        servingSize: s.servings,
        prepTime: s.prepTime,
        cookTime: s.cookTime,
        source: s.source,
        notes: null,
        tags: s.tags,
        ingredients: s.ingredients.map((ing, i) =>
          createIngredient(ing.name, ing.amount ?? null, ing.unit ?? null, null, i),
        ),
        instructions: s.instructions,
        isFavorite: s.isFavorite ?? false,
      },
      deviceId,
    ),
  )

  const cookbook: Cookbook = createCookbook(
    COOKBOOK.name,
    deviceId,
    COOKBOOK.description,
    recipes.map((r) => r.id),
  )

  return { cookbooks: [cookbook], recipes }
}
