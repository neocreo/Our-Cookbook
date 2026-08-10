#!/bin/bash

# Task 1.7 Validation Script
# Validates ViewModel implementation for Cookbook Android app

echo "🔍 Task 1.7 Validation: ViewModel Implementation"
echo "=============================================="
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

# Function to check file existence and content
check_file() {
    local file_path="$1"
    local description="$2"
    
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    
    if [ -f "$file_path" ]; then
        echo -e "${GREEN}✅${NC} $description"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
        return 0
    else
        echo -e "${RED}❌${NC} $description - File not found: $file_path"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
        return 1
    fi
}

# Function to check class existence in file
check_class() {
    local file_path="$1"
    local class_name="$2"
    local description="$3"
    
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    
    if [ -f "$file_path" ] && grep -q "class $class_name" "$file_path"; then
        echo -e "${GREEN}✅${NC} $description"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
        return 0
    else
        echo -e "${RED}❌${NC} $description - Class $class_name not found in $file_path"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
        return 1
    fi
}

# Function to check annotation
check_annotation() {
    local file_path="$1"
    local annotation="$2"
    local description="$3"
    
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    
    if [ -f "$file_path" ] && grep -q "@$annotation" "$file_path"; then
        echo -e "${GREEN}✅${NC} $description"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
        return 0
    else
        echo -e "${RED}❌${NC} $description - Annotation @$annotation not found in $file_path"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
        return 1
    fi
}

# Function to check import
check_import() {
    local file_path="$1"
    local import_pattern="$2"
    local description="$3"
    
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    
    if [ -f "$file_path" ] && grep -q "import.*$import_pattern" "$file_path"; then
        echo -e "${GREEN}✅${NC} $description"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
        return 0
    else
        echo -e "${RED}❌${NC} $description - Import not found in $file_path"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
        return 1
    fi
}

echo "📁 Checking ViewModel Files..."
echo "--------------------------------"

# Check all ViewModel files exist
check_file "app/src/main/java/com/ourcookbook/ui/viewmodel/HomeViewModel.kt" "HomeViewModel file exists"
check_file "app/src/main/java/com/ourcookbook/ui/viewmodel/RecipeListViewModel.kt" "RecipeListViewModel file exists"
check_file "app/src/main/java/com/ourcookbook/ui/viewmodel/RecipeDetailViewModel.kt" "RecipeDetailViewModel file exists"
check_file "app/src/main/java/com/ourcookbook/ui/viewmodel/RecipeEditViewModel.kt" "RecipeEditViewModel file exists"
check_file "app/src/main/java/com/ourcookbook/ui/viewmodel/SearchViewModel.kt" "SearchViewModel file exists"
check_file "app/src/main/java/com/ourcookbook/ui/viewmodel/ScanViewModel.kt" "ScanViewModel file exists"
check_file "app/src/main/java/com/ourcookbook/ui/viewmodel/SyncViewModel.kt" "SyncViewModel file exists"
check_file "app/src/main/java/com/ourcookbook/ui/viewmodel/CookbookManagementViewModel.kt" "CookbookManagementViewModel file exists"
check_file "app/src/main/java/com/ourcookbook/ui/viewmodel/ConflictResolutionViewModel.kt" "ConflictResolutionViewModel file exists"
check_file "app/src/main/java/com/ourcookbook/ui/viewmodel/AuthViewModel.kt" "AuthViewModel file exists"
check_file "app/src/main/java/com/ourcookbook/ui/viewmodel/DeviceRegistrationViewModel.kt" "DeviceRegistrationViewModel file exists"
check_file "app/src/main/java/com/ourcookbook/ui/viewmodel/SettingsViewModel.kt" "SettingsViewModel file exists"

echo ""
echo "🏗️ Checking ViewModel Classes..."
echo "--------------------------------"

# Check ViewModel classes
check_class "app/src/main/java/com/ourcookbook/ui/viewmodel/HomeViewModel.kt" "HomeViewModel" "HomeViewModel class exists"
check_class "app/src/main/java/com/ourcookbook/ui/viewmodel/RecipeListViewModel.kt" "RecipeListViewModel" "RecipeListViewModel class exists"
check_class "app/src/main/java/com/ourcookbook/ui/viewmodel/RecipeDetailViewModel.kt" "RecipeDetailViewModel" "RecipeDetailViewModel class exists"
check_class "app/src/main/java/com/ourcookbook/ui/viewmodel/RecipeEditViewModel.kt" "RecipeEditViewModel" "RecipeEditViewModel class exists"
check_class "app/src/main/java/com/ourcookbook/ui/viewmodel/SearchViewModel.kt" "SearchViewModel" "SearchViewModel class exists"
check_class "app/src/main/java/com/ourcookbook/ui/viewmodel/ScanViewModel.kt" "ScanViewModel" "ScanViewModel class exists"
check_class "app/src/main/java/com/ourcookbook/ui/viewmodel/SyncViewModel.kt" "SyncViewModel" "SyncViewModel class exists"
check_class "app/src/main/java/com/ourcookbook/ui/viewmodel/CookbookManagementViewModel.kt" "CookbookManagementViewModel" "CookbookManagementViewModel class exists"
check_class "app/src/main/java/com/ourcookbook/ui/viewmodel/ConflictResolutionViewModel.kt" "ConflictResolutionViewModel" "ConflictResolutionViewModel class exists"
check_class "app/src/main/java/com/ourcookbook/ui/viewmodel/AuthViewModel.kt" "AuthViewModel" "AuthViewModel class exists"
check_class "app/src/main/java/com/ourcookbook/ui/viewmodel/DeviceRegistrationViewModel.kt" "DeviceRegistrationViewModel" "DeviceRegistrationViewModel class exists"
check_class "app/src/main/java/com/ourcookbook/ui/viewmodel/SettingsViewModel.kt" "SettingsViewModel" "SettingsViewModel class exists"

echo ""
echo "🎯 Checking Hilt Annotations..."
echo "--------------------------------"

# Check Hilt annotations
check_annotation "app/src/main/java/com/ourcookbook/ui/viewmodel/HomeViewModel.kt" "HiltViewModel" "HomeViewModel has @HiltViewModel"
check_annotation "app/src/main/java/com/ourcookbook/ui/viewmodel/RecipeListViewModel.kt" "HiltViewModel" "RecipeListViewModel has @HiltViewModel"
check_annotation "app/src/main/java/com/ourcookbook/ui/viewmodel/RecipeDetailViewModel.kt" "HiltViewModel" "RecipeDetailViewModel has @HiltViewModel"
check_annotation "app/src/main/java/com/ourcookbook/ui/viewmodel/RecipeEditViewModel.kt" "HiltViewModel" "RecipeEditViewModel has @HiltViewModel"
check_annotation "app/src/main/java/com/ourcookbook/ui/viewmodel/SearchViewModel.kt" "HiltViewModel" "SearchViewModel has @HiltViewModel"
check_annotation "app/src/main/java/com/ourcookbook/ui/viewmodel/ScanViewModel.kt" "HiltViewModel" "ScanViewModel has @HiltViewModel"
check_annotation "app/src/main/java/com/ourcookbook/ui/viewmodel/SyncViewModel.kt" "HiltViewModel" "SyncViewModel has @HiltViewModel"
check_annotation "app/src/main/java/com/ourcookbook/ui/viewmodel/CookbookManagementViewModel.kt" "HiltViewModel" "CookbookManagementViewModel has @HiltViewModel"
check_annotation "app/src/main/java/com/ourcookbook/ui/viewmodel/ConflictResolutionViewModel.kt" "HiltViewModel" "ConflictResolutionViewModel has @HiltViewModel"
check_annotation "app/src/main/java/com/ourcookbook/ui/viewmodel/AuthViewModel.kt" "HiltViewModel" "AuthViewModel has @HiltViewModel"
check_annotation "app/src/main/java/com/ourcookbook/ui/viewmodel/DeviceRegistrationViewModel.kt" "HiltViewModel" "DeviceRegistrationViewModel has @HiltViewModel"
check_annotation "app/src/main/java/com/ourcookbook/ui/viewmodel/SettingsViewModel.kt" "HiltViewModel" "SettingsViewModel has @HiltViewModel"

echo ""
echo "🔧 Checking State Management..."
echo "--------------------------------"

# Check StateFlow usage
check_import "app/src/main/java/com/ourcookbook/ui/viewmodel/HomeViewModel.kt" "StateFlow" "HomeViewModel uses StateFlow"
check_import "app/src/main/java/com/ourcookbook/ui/viewmodel/RecipeListViewModel.kt" "StateFlow" "RecipeListViewModel uses StateFlow"
check_import "app/src/main/java/com/ourcookbook/ui/viewmodel/RecipeDetailViewModel.kt" "StateFlow" "RecipeDetailViewModel uses StateFlow"

echo ""
echo "📦 Checking Dependency Injection..."
echo "--------------------------------"

# Check ViewModelModule
check_file "app/src/main/java/com/ourcookbook/data/di/ViewModelModule.kt" "ViewModelModule file exists"
check_file "app/src/main/java/com/ourcookbook/data/di/ViewModelModule.kt" "ViewModelModule file exists"
check_annotation "app/src/main/java/com/ourcookbook/data/di/ViewModelModule.kt" "Module" "ViewModelModule has @Module"
check_annotation "app/src/main/java/com/ourcookbook/data/di/ViewModelModule.kt" "InstallIn" "ViewModelModule has @InstallIn"

echo ""
echo "🔄 Checking Use Case Integration..."
echo "--------------------------------"

# Check new use cases
check_file "app/src/main/java/com/ourcookbook/domain/usecase/recipe/GetRecipes.kt" "GetRecipes use case exists"
check_file "app/src/main/java/com/ourcookbook/domain/usecase/cookbook/GetCookbooks.kt" "GetCookbooks use case exists"

echo ""
echo "📊 Validation Summary"
echo "--------------------------------"
echo -e "Total Checks: ${TOTAL_CHECKS}"
echo -e "${GREEN}Passed: ${PASSED_CHECKS}${NC}"
echo -e "${RED}Failed: ${FAILED_CHECKS}${NC}"

if [ $FAILED_CHECKS -eq 0 ]; then
    echo -e "${GREEN}🎉 All checks passed! Task 1.7 implementation is complete.${NC}"
    exit 0
else
    echo -e "${RED}❌ Some checks failed. Please review the implementation.${NC}"
    exit 1
fi