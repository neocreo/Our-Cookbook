#!/bin/bash

# Task 2.1.01 Verification Script
# This script verifies that the Recipe List Screen implementation meets all requirements

echo "🔍 Starting Task 2.1.01 Verification..."
echo "=========================================="

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Counters
PASSED=0
FAILED=0
TOTAL=0

# Function to check if a file exists and has content
check_file() {
    local file="$1"
    local description="$2"
    TOTAL=$((TOTAL + 1))
    
    if [ -f "$file" ] && [ -s "$file" ]; then
        echo -e "${GREEN}✅${NC} $description"
        PASSED=$((PASSED + 1))
    else
        echo -e "${RED}❌${NC} $description - File not found or empty"
        FAILED=$((FAILED + 1))
    fi
}

# Function to check if a pattern exists in a file
check_pattern() {
    local file="$1"
    local pattern="$2"
    local description="$3"
    TOTAL=$((TOTAL + 1))
    
    if grep -q "$pattern" "$file" 2>/dev/null; then
        echo -e "${GREEN}✅${NC} $description"
        PASSED=$((PASSED + 1))
    else
        echo -e "${RED}❌${NC} $description - Pattern not found"
        FAILED=$((FAILED + 1))
    fi
}

# Function to check if a class/function exists
check_class() {
    local file="$1"
    local class="$2"
    local description="$3"
    TOTAL=$((TOTAL + 1))
    
    if grep -q "class $class" "$file" 2>/dev/null || grep -q "fun $class" "$file" 2>/dev/null; then
        echo -e "${GREEN}✅${NC} $description"
        PASSED=$((PASSED + 1))
    else
        echo -e "${RED}❌${NC} $description - Class/function not found"
        FAILED=$((FAILED + 1))
    fi
}

echo ""
echo "📁 Checking Required Files..."
echo "----------------------------"

# Check main implementation files
check_file "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeListScreen.kt" "RecipeListScreen.kt exists"
check_file "app/src/main/java/com/ourcookbook/ui/viewmodel/RecipeListViewModel.kt" "RecipeListViewModel.kt exists"
check_file "app/src/test/java/com/ourcookbook/ui/screens/recipe/RecipeListScreenTest.kt" "RecipeListScreenTest.kt exists"
check_file "TASK_2.1.01_IMPLEMENTATION_SUMMARY.md" "Implementation summary exists"

echo ""
echo "🔍 Checking RecipeListScreen.kt Features..."
echo "-------------------------------------------"

# Check search functionality
check_pattern "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeListScreen.kt" "CookbookSearchField" "Search functionality implemented"
check_pattern "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeListScreen.kt" "searchQuery" "Search state management"
check_pattern "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeListScreen.kt" "onSearch" "Search event handling"

# Check filtering functionality
check_pattern "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeListScreen.kt" "CookbookFilterChip" "Filter chips implemented"
check_pattern "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeListScreen.kt" "showFavoritesOnly" "Favorites filter implemented"
check_pattern "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeListScreen.kt" "FilterBottomSheetContent" "Filter bottom sheet implemented"

# Check sorting functionality
check_pattern "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeListScreen.kt" "SortOption" "Sort options defined"
check_pattern "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeListScreen.kt" "DropdownMenu" "Sort dropdown menu implemented"
check_pattern "app/src/main/java/com/ourcookbook/ui/viewmodel/RecipeListViewModel.kt" "SortOption" "Sort option enum in ViewModel"

# Check pagination functionality
check_pattern "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeListScreen.kt" "LazyColumn" "Lazy loading implemented"
check_pattern "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeListScreen.kt" "LoadMore" "Load more functionality"
check_pattern "app/src/main/java/com/ourcookbook/ui/viewmodel/RecipeListViewModel.kt" "currentPage" "Pagination state in ViewModel"
check_pattern "app/src/main/java/com/ourcookbook/ui/viewmodel/RecipeListViewModel.kt" "pageSize" "Page size configuration"

# Check navigation integration
check_pattern "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeListScreen.kt" "Route\." "Navigation routes used"
check_pattern "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeListScreen.kt" "navController" "Navigation controller used"
check_pattern "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeListScreen.kt" "recipeDetail" "Recipe detail navigation"
check_pattern "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeListScreen.kt" "RECIPE_CREATE" "Recipe create navigation"

# Check theme integration
check_pattern "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeListScreen.kt" "MaterialTheme" "Theme applied"
check_pattern "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeListScreen.kt" "CookbookTypography" "Typography used"
check_pattern "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeListScreen.kt" "CookbookSpacing" "Spacing used"

# Check UI components integration
check_pattern "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeListScreen.kt" "RecipeCard" "RecipeCard component used"
check_pattern "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeListScreen.kt" "CompactRecipeCard" "CompactRecipeCard component used"
check_pattern "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeListScreen.kt" "LoadingState" "LoadingState component used"
check_pattern "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeListScreen.kt" "EmptyState" "EmptyState component used"

# Check ViewModel integration
check_pattern "app/src/main/java/com/ourcookbook/ui/viewmodel/RecipeListViewModel.kt" "GetAllRecipes" "GetAllRecipes use case integrated"
check_pattern "app/src/main/java/com/ourcookbook/ui/viewmodel/RecipeListViewModel.kt" "SearchRecipes" "SearchRecipes use case integrated"
check_pattern "app/src/main/java/com/ourcookbook/ui/viewmodel/RecipeListViewModel.kt" "GetRecipesByCategory" "GetRecipesByCategory use case integrated"
check_pattern "app/src/main/java/com/ourcookbook/ui/viewmodel/RecipeListViewModel.kt" "ToggleFavorite" "ToggleFavorite use case integrated"
check_pattern "app/src/main/java/com/ourcookbook/ui/viewmodel/RecipeListViewModel.kt" "DeleteRecipe" "DeleteRecipe use case integrated"

# Check state management
check_pattern "app/src/main/java/com/ourcookbook/ui/viewmodel/RecipeListViewModel.kt" "RecipeListState" "State classes defined"
check_pattern "app/src/main/java/com/ourcookbook/ui/viewmodel/RecipeListViewModel.kt" "RecipeListEvent" "Event classes defined"
check_pattern "app/src/main/java/com/ourcookbook/ui/viewmodel/RecipeListViewModel.kt" "RecipeListAction" "Action classes defined"

# Check error handling
check_pattern "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeListScreen.kt" "ErrorState" "Error state handling"
check_pattern "app/src/main/java/com/ourcookbook/ui/viewmodel/RecipeListViewModel.kt" "Error" "Error state in ViewModel"

# Check accessibility
check_pattern "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeListScreen.kt" "contentDescription" "Content descriptions for accessibility"

# Check responsive design
check_pattern "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeListScreen.kt" "gridView" "Grid view toggle implemented"

# Check testing
check_pattern "app/src/test/java/com/ourcookbook/ui/screens/recipe/RecipeListScreenTest.kt" "RecipeListScreenTest" "Test class exists"
check_pattern "app/src/test/java/com/ourcookbook/ui/screens/recipe/RecipeListScreenTest.kt" "@Test" "Test methods defined"
check_pattern "app/src/test/java/com/ourcookbook/ui/screens/recipe/RecipeListScreenTest.kt" "EvidenceQA" "EvidenceQA validation"

echo ""
echo "📊 Verification Summary"
echo "======================"
echo -e "Total Checks: ${TOTAL}"
echo -e "${GREEN}Passed: ${PASSED}${NC}"
echo -e "${RED}Failed: ${FAILED}${NC}"

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}🎉 All checks passed! Implementation is ready for EvidenceQA validation.${NC}"
    exit 0
else
    echo -e "${RED}⚠️  Some checks failed. Please review the implementation.${NC}"
    exit 1
fi