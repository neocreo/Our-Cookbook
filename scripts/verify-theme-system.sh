#!/bin/bash

# Cookbook Theme System Verification Script
# This script verifies that Task 1.10 (Theme and Styling) implementation is complete

set -e  # Exit on error

echo "🚀 Starting Theme System Verification for Task 1.10"
echo "=================================================="

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
    local file_path="$1"
    local description="$2"
    
    TOTAL=$((TOTAL + 1))
    
    if [ -f "$file_path" ] && [ -s "$file_path" ]; then
        echo -e "${GREEN}✅${NC} $description"
        PASSED=$((PASSED + 1))
        return 0
    else
        echo -e "${RED}❌${NC} $description - File not found or empty: $file_path"
        FAILED=$((FAILED + 1))
        return 1
    fi
}

# Function to check if a directory exists
check_directory() {
    local dir_path="$1"
    local description="$2"
    
    TOTAL=$((TOTAL + 1))
    
    if [ -d "$dir_path" ]; then
        echo -e "${GREEN}✅${NC} $description"
        PASSED=$((PASSED + 1))
        return 0
    else
        echo -e "${RED}❌${NC} $description - Directory not found: $dir_path"
        FAILED=$((FAILED + 1))
        return 1
    fi
}

# Function to check if a string exists in a file
check_content() {
    local file_path="$1"
    local search_string="$2"
    local description="$3"
    
    TOTAL=$((TOTAL + 1))
    
    if [ -f "$file_path" ] && grep -q "$search_string" "$file_path"; then
        echo -e "${GREEN}✅${NC} $description"
        PASSED=$((PASSED + 1))
        return 0
    else
        echo -e "${RED}❌${NC} $description - Content not found in: $file_path"
        FAILED=$((FAILED + 1))
        return 1
    fi
}

# Function to check if a class/interface exists in Kotlin files
check_kotlin_class() {
    local dir_path="$1"
    local class_name="$2"
    local description="$3"
    
    TOTAL=$((TOTAL + 1))
    
    if grep -r "class $class_name" "$dir_path" --include="*.kt" > /dev/null 2>&1 || \
       grep -r "object $class_name" "$dir_path" --include="*.kt" > /dev/null 2>&1 || \
       grep -r "val $class_name" "$dir_path" --include="*.kt" > /dev/null 2>&1; then
        echo -e "${GREEN}✅${NC} $description"
        PASSED=$((PASSED + 1))
        return 0
    else
        echo -e "${RED}❌${NC} $description - Class not found: $class_name"
        FAILED=$((FAILED + 1))
        return 1
    fi
}

echo ""
echo "📁 Checking Theme System Directory Structure..."
echo "----------------------------------------------"

# Check theme directory structure
check_directory "./app/src/main/java/com/ourcookbook/ui/theme" "Theme directory exists"

# Check theme files
check_file "./app/src/main/java/com/ourcookbook/ui/theme/Theme.kt" "Theme.kt exists"
check_file "./app/src/main/java/com/ourcookbook/ui/theme/Typography.kt" "Typography.kt exists"
check_file "./app/src/main/java/com/ourcookbook/ui/theme/Shapes.kt" "Shapes.kt exists"
check_file "./app/src/main/java/com/ourcookbook/ui/theme/Spacing.kt" "Spacing.kt exists"
check_file "./app/src/main/java/com/ourcookbook/ui/theme/Elevation.kt" "Elevation.kt exists"
check_file "./app/src/main/java/com/ourcookbook/ui/theme/ThemeIndex.kt" "ThemeIndex.kt exists"
check_file "./app/src/main/java/com/ourcookbook/ui/theme/ThemePreview.kt" "ThemePreview.kt exists"
check_file "./app/src/main/java/com/ourcookbook/ui/theme/README.md" "README.md exists"

echo ""
echo "🎨 Checking Color System Implementation..."
echo "----------------------------------------"

# Check color system
check_content "./app/src/main/java/com/ourcookbook/ui/theme/Theme.kt" "object CookbookColors" "CookbookColors object exists"
check_content "./app/src/main/java/com/ourcookbook/ui/theme/Theme.kt" "val primary: Color = Color(0xFFE57373)" "Primary color defined"
check_content "./app/src/main/java/com/ourcookbook/ui/theme/Theme.kt" "val secondary: Color = Color(0xFF81C784)" "Secondary color defined"
check_content "./app/src/main/java/com/ourcookbook/ui/theme/Theme.kt" "val LightColorScheme: ColorScheme" "Light color scheme defined"
check_content "./app/src/main/java/com/ourcookbook/ui/theme/Theme.kt" "val DarkColorScheme: ColorScheme" "Dark color scheme defined"
check_content "./app/src/main/java/com/ourcookbook/ui/theme/Theme.kt" "fun CookbookTheme" "CookbookTheme composable exists"
check_content "./app/src/main/java/com/ourcookbook/ui/theme/Theme.kt" "categoryColors: Map<String, Color>" "Category colors defined"

echo ""
echo "📝 Checking Typography System Implementation..."
echo "--------------------------------------------"

# Check typography system
check_content "app/src/main/java/com/ourcookbook/ui/theme/Typography.kt" "val RobotoFamily" "RobotoFamily defined"
check_content "app/src/main/java/com/ourcookbook/ui/theme/Typography.kt" "val CookbookTypography" "CookbookTypography defined"
check_content "app/src/main/java/com/ourcookbook/ui/theme/Typography.kt" "displayLarge" "Display large style defined"
check_content "app/src/main/java/com/ourcookbook/ui/theme/Typography.kt" "headlineMedium" "Headline medium style defined"
check_content "app/src/main/java/com/ourcookbook/ui/theme/Typography.kt" "bodyMedium" "Body medium style defined"
check_content "app/src/main/java/com/ourcookbook/ui/theme/Typography.kt" "object CookbookTextStyles" "CookbookTextStyles object exists"

echo ""
echo "🔺 Checking Shape System Implementation..."
echo "----------------------------------------"

# Check shape system
check_content "app/src/main/java/com/ourcookbook/ui/theme/Shapes.kt" "val CookbookShapes" "CookbookShapes defined"
check_content "app/src/main/java/com/ourcookbook/ui/theme/Shapes.kt" "extraSmall = RoundedCornerShape(4.dp)" "Extra small shape defined"
check_content "app/src/main/java/com/ourcookbook/ui/theme/Shapes.kt" "object ShapeTokens" "ShapeTokens object exists"
check_content "app/src/main/java/com/ourcookbook/ui/theme/Shapes.kt" "object ComponentShapes" "ComponentShapes object exists"

echo ""
echo "📏 Checking Spacing System Implementation..."
echo "------------------------------------------"

# Check spacing system
check_content "app/src/main/java/com/ourcookbook/ui/theme/Spacing.kt" "object CookbookSpacing" "CookbookSpacing object exists"
check_content "app/src/main/java/com/ourcookbook/ui/theme/Spacing.kt" "val xxSmall: Dp = 4.dp" "XXSmall spacing defined"
check_content "app/src/main/java/com/ourcookbook/ui/theme/Spacing.kt" "val medium: Dp = 16.dp" "Medium spacing defined"
check_content "app/src/main/java/com/ourcookbook/ui/theme/Spacing.kt" "object ScreenSpacing" "ScreenSpacing object exists"
check_content "app/src/main/java/com/ourcookbook/ui/theme/Spacing.kt" "object ComponentSpacing" "ComponentSpacing object exists"

echo ""
echo "📈 Checking Elevation System Implementation..."
echo "---------------------------------------------"

# Check elevation system
check_content "app/src/main/java/com/ourcookbook/ui/theme/Elevation.kt" "object CookbookElevation" "CookbookElevation object exists"
check_content "app/src/main/java/com/ourcookbook/ui/theme/Elevation.kt" "val none: Dp = 0.dp" "None elevation defined"
check_content "app/src/main/java/com/ourcookbook/ui/theme/Elevation.kt" "val medium: Dp = 4.dp" "Medium elevation defined"
check_content "app/src/main/java/com/ourcookbook/ui/theme/Elevation.kt" "object ElevationTokens" "ElevationTokens object exists"
check_content "app/src/main/java/com/ourcookbook/ui/theme/Elevation.kt" "object ComponentElevation" "ComponentElevation object exists"
check_content "app/src/main/java/com/ourcookbook/ui/theme/Elevation.kt" "object ElevationStates" "ElevationStates object exists"

echo ""
echo "🧪 Checking Test Implementation..."
echo "--------------------------------"

# Check tests
check_file "app/src/test/java/com/ourcookbook/ui/theme/ThemeTest.kt" "ThemeTest.kt exists"
check_content "app/src/test/java/com/ourcookbook/ui/theme/ThemeTest.kt" "class ThemeTest" "ThemeTest class exists"
check_content "app/src/test/java/com/ourcookbook/ui/theme/ThemeTest.kt" "testCookbookColors_PrimaryColors_NotNull" "Primary colors test exists"
check_content "app/src/test/java/com/ourcookbook/ui/theme/ThemeTest.kt" "testCookbookTypography_NotNull" "Typography test exists"

echo ""
echo "📄 Checking Preview Composables..."
echo "--------------------------------"

# Check preview composables
check_content "app/src/main/java/com/ourcookbook/ui/theme/ThemePreview.kt" "@Preview" "Preview annotation exists"
check_content "app/src/main/java/com/ourcookbook/ui/theme/ThemePreview.kt" "ColorPalettePreview" "Color palette preview exists"
check_content "app/src/main/java/com/ourcookbook/ui/theme/ThemePreview.kt" "TypographyPreview" "Typography preview exists"
check_content "app/src/main/java/com/ourcookbook/ui/theme/ThemePreview.kt" "ShapesPreview" "Shapes preview exists"
check_content "app/src/main/java/com/ourcookbook/ui/theme/ThemePreview.kt" "ElevationPreview" "Elevation preview exists"
check_content "app/src/main/java/com/ourcookbook/ui/theme/ThemePreview.kt" "ComponentsPreview" "Components preview exists"

echo ""
echo "🔍 Checking MainActivity Integration..."
echo "--------------------------------------"

# Check MainActivity uses the theme
check_content "app/src/main/java/com/example/cookbook/MainActivity.kt" "import com.ourcookbook.ui.theme.CookbookTheme" "MainActivity imports CookbookTheme"
check_content "app/src/main/java/com/example/cookbook/MainActivity.kt" "CookbookTheme {" "MainActivity uses CookbookTheme"

echo ""
echo "📚 Checking Documentation..."
echo "----------------------------"

# Check documentation
check_content "app/src/main/java/com/ourcookbook/ui/theme/README.md" "# Cookbook Theme System" "README title exists"
check_content "app/src/main/java/com/ourcookbook/ui/theme/README.md" "Task 1.10" "Task 1.10 reference exists"
check_content "app/src/main/java/com/ourcookbook/ui/theme/README.md" "Material Design 3" "MD3 reference exists"

echo ""
echo "🎯 Checking Design System Compliance..."
echo "---------------------------------------"

# Check compliance with design tokens from UX foundation
check_content "app/src/main/java/com/ourcookbook/ui/theme/Theme.kt" "0xFFE57373" "Primary color matches UX foundation"
check_content "app/src/main/java/com/ourcookbook/ui/theme/Theme.kt" "0xFF81C784" "Secondary color matches UX foundation"
check_content "app/src/main/java/com/ourcookbook/ui/theme/Theme.kt" "0xFFFFC107" "Breakfast category color matches UX foundation"
check_content "app/src/main/java/com/ourcookbook/ui/theme/Theme.kt" "0xFFE57373" "Mains category color matches UX foundation"
check_content "app/src/main/java/com/ourcookbook/ui/theme/Theme.kt" "0xFFE91E63" "Desserts category color matches UX foundation"

echo ""
echo "🏗️ Checking Architecture Compliance..."
echo "-------------------------------------"

# Check architecture compliance
check_content "app/src/main/java/com/ourcookbook/ui/theme/Theme.kt" "project-docs/cookbook-ux-foundation.md" "UX foundation reference exists"
check_content "app/src/main/java/com/ourcookbook/ui/theme/Theme.kt" "project-docs/cookbook-android-architecture.md" "Architecture reference exists"
check_content "app/src/main/java/com/ourcookbook/ui/theme/Theme.kt" "Material Design 3" "MD3 compliance mentioned"

echo ""
echo "📊 Verification Summary"
echo "====================="
echo -e "Total Checks: ${TOTAL}"
echo -e "${GREEN}Passed: ${PASSED}${NC}"
echo -e "${RED}Failed: ${FAILED}${NC}"

if [ $FAILED -eq 0 ]; then
    echo -e ""
    echo -e "${GREEN}🎉 Theme System Verification PASSED!${NC}"
    echo -e "All checks passed. Task 1.10 (Theme and Styling) is ready for EvidenceQA validation."
    exit 0
else
    echo -e ""
    echo -e "${RED}❌ Theme System Verification FAILED${NC}"
    echo -e "$FAILED checks failed. Please fix the issues and try again."
    exit 1
fi