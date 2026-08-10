#!/bin/bash

# Task 2.1.03 Validation Script
# Recipe Create/Edit Screen Implementation Validation

echo "=========================================="
echo "Task 2.1.03 Validation Script"
echo "Recipe Create/Edit Screen Implementation"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Initialize counters
PASSED=0
FAILED=0
TOTAL=0

# Function to check file existence
check_file() {
    local file="$1"
    local description="$2"
    TOTAL=$((TOTAL + 1))
    
    if [ -f "$file" ]; then
        echo -e "${GREEN}✅ PASS${NC}: $description"
        echo "   Location: $file"
        PASSED=$((PASSED + 1))
    else
        echo -e "${RED}❌ FAIL${NC}: $description"
        echo "   Expected: $file"
        FAILED=$((FAILED + 1))
    fi
    echo ""
}

# Function to check file content
check_content() {
    local file="$1"
    local pattern="$2"
    local description="$3"
    TOTAL=$((TOTAL + 1))
    
    if [ -f "$file" ] && grep -q "$pattern" "$file"; then
        echo -e "${GREEN}✅ PASS${NC}: $description"
        echo "   File: $file"
        echo "   Pattern: $pattern"
        PASSED=$((PASSED + 1))
    else
        echo -e "${RED}❌ FAIL${NC}: $description"
        echo "   File: $file"
        echo "   Pattern: $pattern"
        FAILED=$((FAILED + 1))
    fi
    echo ""
}

# Function to check line count
check_line_count() {
    local file="$1"
    local min_lines="$2"
    local description="$3"
    TOTAL=$((TOTAL + 1))
    
    if [ -f "$file" ]; then
        local lines=$(wc -l < "$file")
        if [ "$lines" -ge "$min_lines" ]; then
            echo -e "${GREEN}✅ PASS${NC}: $description"
            echo "   File: $file"
            echo "   Lines: $lines (minimum: $min_lines)"
            PASSED=$((PASSED + 1))
        else
            echo -e "${RED}❌ FAIL${NC}: $description"
            echo "   File: $file"
            echo "   Lines: $lines (minimum: $min_lines)"
            FAILED=$((FAILED + 1))
        fi
    else
        echo -e "${RED}❌ FAIL${NC}: $description"
        echo "   File not found: $file"
        FAILED=$((FAILED + 1))
    fi
    echo ""
}

echo "📁 Checking Required Files..."
echo "------------------------------------------"

# Check main implementation file
check_file "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeEditScreen.kt" "RecipeEditScreen implementation"

# Check documentation files
check_file "TASK_2.1.03_EVIDENCE_QA.md" "EvidenceQA documentation"
check_file "TASK_2.1.03_IMPLEMENTATION_SUMMARY.md" "Implementation summary"
check_file "TASK_2.1.03_FINAL_SUMMARY.md" "Final summary"

echo ""
echo "🔍 Checking File Content..."
echo "------------------------------------------"

# Check RecipeEditScreen.kt content
check_content "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeEditScreen.kt" "fun RecipeEditScreen" "RecipeEditScreen function"
check_content "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeEditScreen.kt" "fun RecipeEditContent" "RecipeEditContent function"
check_content "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeEditScreen.kt" "fun IngredientsSection" "IngredientsSection function"
check_content "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeEditScreen.kt" "fun InstructionsSection" "InstructionsSection function"
check_content "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeEditScreen.kt" "fun RecipeImageSection" "RecipeImageSection function"
check_content "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeEditScreen.kt" "fun CategorySelectionSection" "CategorySelectionSection function"
check_content "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeEditScreen.kt" "fun TagsSection" "TagsSection function"
check_content "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeEditScreen.kt" "fun IngredientDialog" "IngredientDialog function"
check_content "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeEditScreen.kt" "fun ValidationSummary" "ValidationSummary function"
check_content "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeEditScreen.kt" "rememberLauncherForActivityResult" "Activity result launcher"
check_content "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeEditScreen.kt" "ActivityResultContracts.PickVisualMedia" "Image picker contract"
check_content "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeEditScreen.kt" "ActivityResultContracts.TakePicturePreview" "Camera contract"

echo ""
echo "📊 Checking Line Counts..."
echo "------------------------------------------"

# Check minimum line counts
check_line_count "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeEditScreen.kt" 1500 "RecipeEditScreen minimum lines"
check_line_count "TASK_2.1.03_EVIDENCE_QA.md" 200 "EvidenceQA documentation minimum lines"
check_line_count "TASK_2.1.03_IMPLEMENTATION_SUMMARY.md" 300 "Implementation summary minimum lines"

echo ""
echo "🔗 Checking Integration Points..."
echo "------------------------------------------"

# Check integration with existing components
check_content "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeEditScreen.kt" "CookbookTextField" "Integration with CookbookTextField"
check_content "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeEditScreen.kt" "CookbookPrimaryButton" "Integration with CookbookPrimaryButton"
check_content "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeEditScreen.kt" "CookbookTheme" "Integration with CookbookTheme"
check_content "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeEditScreen.kt" "MaterialTheme" "Integration with MaterialTheme"
check_content "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeEditScreen.kt" "hiltViewModel" "Integration with Hilt"
check_content "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeEditScreen.kt" "RecipeEditViewModel" "Integration with RecipeEditViewModel"

echo ""
echo "📝 Checking Navigation Integration..."
echo "------------------------------------------"

# Check navigation updates
check_content "app/src/main/java/com/ourcookbook/ui/navigation/NavGraph.kt" "RecipeEditScreen(" "Navigation integration with RecipeEditScreen"
check_content "app/src/main/java/com/ourcookbook/ui/navigation/NavGraph.kt" "recipeId = null" "Create mode parameter"
check_content "app/src/main/java/com/ourcookbook/ui/navigation/NavGraph.kt" "recipeId = recipeId" "Edit mode parameter"

echo ""
echo "🎯 Checking Feature Implementation..."
echo "------------------------------------------"

# Check feature implementations
check_content "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeEditScreen.kt" "ValidationSummary" "Form validation implementation"
check_content "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeEditScreen.kt" "IngredientsSection" "Ingredient management implementation"
check_content "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeEditScreen.kt" "InstructionsSection" "Instruction steps implementation"
check_content "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeEditScreen.kt" "RecipeImageSection" "Image capture implementation"
check_content "app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeEditScreen.kt" "CategorySelectionSection" "Category selection implementation"

echo ""
echo "=========================================="
echo "VALIDATION RESULTS"
echo "=========================================="
echo ""
echo "Total Checks: $TOTAL"
echo -e "${GREEN}Passed: $PASSED${NC}"
echo -e "${RED}Failed: $FAILED${NC}"
echo ""

# Calculate percentage
if [ "$TOTAL" -gt 0 ]; then
    PERCENTAGE=$((PASSED * 100 / TOTAL))
    echo "Success Rate: ${PERCENTAGE}%"
else
    echo "Success Rate: 0%"
fi

echo ""

# Final verdict
if [ "$FAILED" -eq 0 ]; then
    echo -e "${GREEN}🎉 VALIDATION PASSED! 🎉${NC}"
    echo "Task 2.1.03 is ready for EvidenceQA validation."
    echo ""
    echo "Next Steps:"
    echo "1. Review the implementation"
    echo "2. Run manual testing"
    echo "3. Submit for EvidenceQA validation"
    echo "4. Proceed to next task"
    exit 0
else
    echo -e "${RED}❌ VALIDATION FAILED${NC}"
    echo "Please fix the failed checks and retry."
    echo ""
    echo "Failed Items:"
    # List failed items (this would require more complex tracking)
    echo "Check the output above for specific failures."
    exit 1
fi