cmake_minimum_required(VERSION 3.14)
project(rendly_scoring_engine VERSION 1.0.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

# Find Python and pybind11
find_package(Python3 COMPONENTS Interpreter Development REQUIRED)
find_package(pybind11 CONFIG QUIET)

if(NOT pybind11_FOUND)
    # Fetch pybind11 if not found
    include(FetchContent)
    FetchContent_Declare(
        pybind11
        GIT_REPOSITORY https://github.com/pybind/pybind11.git
        GIT_TAG v2.11.1
    )
    FetchContent_MakeAvailable(pybind11)
endif()

# Scoring engine library
add_library(scoring_core STATIC
    src/scoring_engine.cpp
    src/text_analyzer.cpp
    src/intent_matcher.cpp
    src/confidence_calculator.cpp
)

target_include_directories(scoring_core PUBLIC
    ${CMAKE_CURRENT_SOURCE_DIR}/include
)

# Python bindings
pybind11_add_module(scoring_engine
    src/python_bindings.cpp
)

target_link_libraries(scoring_engine PRIVATE scoring_core)

# Install
install(TARGETS scoring_engine
    LIBRARY DESTINATION ${Python3_SITELIB}
)
