#!/bin/bash

# Task 1.8 Validation Script
# Validates the UI Components Implementation

echo "🚀 Starting Task 1.8 Validation..."
echo "========================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Initialize counters
TOTAL_CHECKS=0
PASSED_CHECKS=0
FAILED_CHECKS=0

# Function to check file existence
check_file() {
    local file_path="$1"
    local description="$2"
    
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    
    if [ -f "$file_path" ]; then
        echo -e "${GREEN}✅${NC} $description"
        echo "   Found: $file_path"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
    else
        echo -e "${RED}❌${NC} $description"
        echo "   Missing: $file_path"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
    fi
    echo ""
}

# Function to check directory existence
check_directory() {
    local dir_path="$1"
    local description="$2"
    
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    
    if [ -d "$dir_path" ]; then
        echo -e "${GREEN}✅${NC} $description"
        echo "   Found: $dir_path"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
    else
        echo -e "${RED}❌${NC} $description"
        echo "   Missing: $dir_path"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
    fi
    echo ""
}

# Function to check file content
check_file_content() {
    local file_path="$1"
    local pattern="$2"
    local description="$3"
    
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    
    if [ -f "$file_path" ] && grep -q "$pattern" "$file_path"; then
        echo -e "${GREEN}✅${NC} $description"
        echo "   Pattern found in: $file_path"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
    else
        echo -e "${RED}❌${NC} $description"
        echo "   Pattern not found in: $file_path"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
    fi
    echo ""
}

echo "📁 Checking Component Files..."
echo "----------------------------------------"

# Check component files
check_file "app/src/main/java/com/ourcookbook/ui/components/Buttons.kt" "Buttons component file"
check_file "app/src/main/java/com/ourcookbook/ui/components/Cards.kt" "Cards component file"
check_file "app/src/main/java/com/ourcookbook/ui/components/InputFields.kt" "InputFields component file"
check_file "app/src/main/java/com/ourcookbook/ui/components/Dialogs.kt" "Dialogs component file"
check_file "app/src/main/java/com/ourcookbook/ui/components/Lists.kt" "Lists component file"
check_file "app/src/main/java/com/ourcookbook/ui/components/Navigation.kt" "Navigation component file"
check_file "app/src/main/java/com/ourcookbook/ui/components/Chips.kt" "Chips component file"
check_file "app/src/main/java/com/ourcookbook/ui/components/Theme.kt" "Theme component file"
check_file "app/src/main/java/com/ourcookbook/ui/components/Typography.kt" "Typography component file"
check_file "app/src/main/java/com/ourcookbook/ui/components/ComponentIndex.kt" "ComponentIndex file"
check_file "app/src/main/java/com/ourcookbook/ui/components/AllComponentsPreview.kt" "AllComponentsPreview file"

echo "🧪 Checking Test Files..."
echo "----------------------------------------"

# Check test files
check_file "app/src/test/java/com/ourcookbook/ui/components/ComponentTests.kt" "Component tests file"

echo "📋 Checking Documentation Files..."
echo "----------------------------------------"

# Check documentation files
check_file "TASK_1.8_EVIDENCE_QA.md" "EvidenceQA validation report"
check_file "TASK_1.8_IMPLEMENTATION_SUMMARY.md" "Implementation summary"

echo "🔍 Checking Component Content..."
echo "----------------------------------------"

# Check key component implementations
check_file_content "app/src/main/java/com/ourcookbook/ui/components/Buttons.kt" "CookbookPrimaryButton" "Primary button component"
check_file_content "app/src/main/java/com/ourcookbook/ui/components/Buttons.kt" "CookbookSecondaryButton" "Secondary button component"
check_file_content "app/src/main/java/com/ourcookbook/ui/components/Buttons.kt" "CookbookIconButton" "Icon button component"
check_file_content "app/src/main/java/com/ourcookbook/ui/components/Buttons.kt" "CookbookFloatingActionButton" "FAB component"

check_file_content "app/src/main/java/com/ourcookbook/ui/components/Cards.kt" "RecipeCard" "Recipe card component"
check_file_content "app/src/main/java/com/ourcookbook/ui/components/Cards.kt" "CategoryBadge" "Category badge component"
check_file_content "app/src/main/java/com/ourcookbook/ui/components/Cards.kt" "RatingDisplay" "Rating display component"

check_file_content "app/src/main/java/com/ourcookbook/ui/components/InputFields.kt" "CookbookTextField" "Text field component"
check_file_content "app/src/main/java/com/ourcookbook/ui/components/InputFields.kt" "CookbookNumberField" "Number field component"
check_file_content "app/src/main/java/com/ourcookbook/ui/components/InputFields.kt" "CookbookSearchField" "Search field component"

check_file_content "app/src/main/java/com/ourcookbook/ui/components/Dialogs.kt" "CookbookConfirmationDialog" "Confirmation dialog component"
check_file_content "app/src/main/java/com/ourcookbook/ui/components/Dialogs.kt" "CookbookDeleteDialog" "Delete dialog component"
check_file_content "app/src/main/java/com/ourcookbook/ui/components/Dialogs.kt" "CookbookLoadingDialog" "Loading dialog component"

check_file_content "app/src/main/java/com/ourcookbook/ui/components/Lists.kt" "CookbookLazyColumn" "Lazy column component"
check_file_content "app/src/main/java/com/ourcookbook/ui/components/Lists.kt" "RecipeList" "Recipe list component"
check_file_content "app/src/main/java/com/ourcookbook/ui/components/Lists.kt" "IngredientItem" "Ingredient item component"

check_file_content "app/src/main/java/com/ourcookbook/ui/components/Navigation.kt" "CookbookBottomNavigation" "Bottom navigation component"
check_file_content "app/src/main/java/com/ourcookbook/ui/components/Navigation.kt" "CookbookTopAppBar" "Top app bar component"

check_file_content "app/src/main/java/com/ourcookbook/ui/components/Chips.kt" "CookbookFilterChip" "Filter chip component"
check_file_content "app/src/main/java/com/ourcookbook/ui/components/Chips.kt" "TagChip" "Tag chip component"

check_file_content "app/src/main/java/com/ourcookbook/ui/components/Theme.kt" "CookbookColors" "Color system"
check_file_content "app/src/main/java/com/ourcookbook/ui/components/Theme.kt" "LightColorScheme" "Light theme"
check_file_content "app/src/main/java/com/ourcookbook/ui/components/Theme.kt" "DarkColorScheme" "Dark theme"

check_file_content "app/src/main/java/com/ourcookbook/ui/components/Typography.kt" "CookbookTypography" "Typography system"
check_file_content "app/src/main/java/com/ourcookbook/ui/components/Typography.kt" "CookbookSpacing" "Spacing system"
check_file_content "app/src/main/java/com/ourcookbook/ui/components/Typography.kt" "CookbookElevation" "Elevation system"

echo "📊 Validation Summary"
echo "----------------------------------------"
echo "Total Checks: $TOTAL_CHECKS"
echo -e "${GREEN}Passed: $PASSED_CHECKS${NC}"
echo -e "${RED}Failed: $FAILED_CHECKS${NC}"
echo ""

# Calculate score
if [ $TOTAL_CHECKS -gt 0 ]; then
    SCORE=$((PASSED_CHECKS * 100 / TOTAL_CHECKS))
else
    SCORE=0
fi

echo "🎯 Validation Score: ${SCORE}%"
echo ""

# Final result
if [ $FAILED_CHECKS -eq 0 ]; then
    echo -e "${GREEN}🎉 Task 1.8 Validation: PASSED${NC}"
    echo "All components are implemented and ready for EvidenceQA validation."
    exit 0
else
    echo -e "${RED}❌ Task 1.8 Validation: FAILED${NC}"
    echo "Please fix the failed checks and retry validation."
    exit 1
fi
