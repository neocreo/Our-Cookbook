#!/bin/bash

# Task 1.9 Navigation Setup Validation Script
# This script validates the implementation of Task 1.9

echo "🚀 Starting Task 1.9 Validation..."
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

# Function to check if file exists
check_file() {
    TOTAL=$((TOTAL + 1))
    if [ -f "$1" ]; then
        echo -e "${GREEN}✅${NC} File exists: $1"
        PASSED=$((PASSED + 1))
        return 0
    else
        echo -e "${RED}❌${NC} File missing: $1"
        FAILED=$((FAILED + 1))
        return 1
    fi
}

# Function to check if directory exists
check_dir() {
    TOTAL=$((TOTAL + 1))
    if [ -d "$1" ]; then
        echo -e "${GREEN}✅${NC} Directory exists: $1"
        PASSED=$((PASSED + 1))
        return 0
    else
        echo -e "${RED}❌${NC} Directory missing: $1"
        FAILED=$((FAILED + 1))
        return 1
    fi
}

# Function to check if pattern exists in file
check_pattern() {
    TOTAL=$((TOTAL + 1))
    if grep -q "$2" "$1" 2>/dev/null; then
        echo -e "${GREEN}✅${NC} Pattern found in $1: $2"
        PASSED=$((PASSED + 1))
        return 0
    else
        echo -e "${RED}❌${NC} Pattern missing in $1: $2"
        FAILED=$((FAILED + 1))
        return 1
    fi
}

echo ""
echo "📁 Checking Navigation System Files..."
echo "--------------------------------------"

# Check navigation files
check_file "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/navigation/Route.kt"
check_file "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/navigation/NavGraph.kt"

echo ""
echo "📁 Checking Main Activity..."
echo "----------------------------"

# Check MainActivity
check_file "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/MainActivity.kt"
check_pattern "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/MainActivity.kt" "CookbookNavHost"

echo ""
echo "📁 Checking Screen Files..."
echo "---------------------------"

# Check auth screens
check_file "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/screens/auth/AuthScreen.kt"
check_file "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/screens/auth/DeviceRegistrationScreen.kt"
check_file "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/screens/auth/DriveAuthScreen.kt"

# Check recipe screens
check_file "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeListScreen.kt"
check_file "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeDetailScreen.kt"
check_file "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/screens/recipe/RecipeEditScreen.kt"

# Check cookbook screens
check_file "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/screens/cookbook/CookbookManagementScreen.kt"
check_file "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/screens/cookbook/CookbookListScreen.kt"
check_file "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/screens/cookbook/CookbookDetailScreen.kt"
check_file "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/screens/cookbook/CookbookEditScreen.kt"

# Check search screens
check_file "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/screens/search/SearchScreen.kt"
check_file "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/screens/favorites/FavoritesScreen.kt"

# Check sync screens
check_file "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/screens/sync/SyncStatusScreen.kt"
check_file "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/screens/sync/ConflictResolutionScreen.kt"

# Check utility screens
check_file "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/screens/ocr/OcrScannerScreen.kt"
check_file "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/screens/settings/SettingsScreen.kt"

echo ""
echo "📁 Checking Component Files..."
echo "------------------------------"

# Check components
check_file "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/components/LoadingState.kt"
check_file "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/components/CookbookCard.kt"

echo ""
echo "📁 Checking ViewModel Integration..."
echo "-----------------------------------"

# Check ViewModel usage in NavGraph
check_pattern "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/navigation/NavGraph.kt" "hiltViewModel"
check_pattern "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/navigation/NavGraph.kt" "AuthViewModel"
check_pattern "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/navigation/NavGraph.kt" "RecipeListViewModel"
check_pattern "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/navigation/NavGraph.kt" "RecipeDetailViewModel"

echo ""
echo "📁 Checking Route Definitions..."
echo "-------------------------------"

# Check route definitions
check_pattern "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/navigation/Route.kt" "const val AUTH = \"auth\""
check_pattern "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/navigation/Route.kt" "const val HOME = \"home\""
check_pattern "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/navigation/Route.kt" "const val RECIPE_LIST = \"recipe_list\""
check_pattern "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/navigation/Route.kt" "RECIPE_DETAIL = \"recipe_detail"
check_pattern "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/navigation/Route.kt" "const val SYNC_STATUS = \"sync_status\""
check_pattern "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/navigation/Route.kt" "CONFLICT_RESOLUTION = \"conflict_resolution"

echo ""
echo "📁 Checking Documentation..."
echo "----------------------------"

# Check documentation
check_file "/home/starlord/Repositories/Our Cookbook/TASK_1.9_EVIDENCE_QA.md"
check_file "/home/starlord/Repositories/Our Cookbook/TASK_1.9_IMPLEMENTATION_SUMMARY.md"

echo ""
echo "📁 Checking Directory Structure..."
echo "--------------------------------"

# Check directories
check_dir "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/navigation"
check_dir "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/screens/auth"
check_dir "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/screens/recipe"
check_dir "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/screens/cookbook"
check_dir "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/screens/search"
check_dir "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/screens/favorites"
check_dir "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/screens/sync"
check_dir "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/screens/ocr"
check_dir "/home/starlord/Repositories/Our Cookbook/app/src/main/java/com/ourcookbook/ui/screens/settings"

echo ""
echo "📊 Validation Summary"
echo "===================="
echo "Total Checks: $TOTAL"
echo -e "Passed: ${GREEN}$PASSED${NC}"
echo -e "Failed: ${RED}$FAILED${NC}"

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}🎉 All checks passed! Task 1.9 implementation is complete.${NC}"
    exit 0
else
    echo -e "${RED}⚠️  Some checks failed. Please review the implementation.${NC}"
    exit 1
fi
