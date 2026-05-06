cmake_minimum_required(VERSION 3.21)
project(SMTC4J LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(SMTC4J SHARED
        native/SMTC4J.cpp
)

# JNI
find_package(JNI REQUIRED)
target_include_directories(SMTC4J PRIVATE ${JNI_INCLUDE_DIRS})

# Windows / WinRT
target_compile_definitions(SMTC4J PRIVATE WIN32_LEAN_AND_MEAN)
target_link_libraries(SMTC4J
        windowsapp
)

# MSVC specifics
if (MSVC)
    target_compile_options(SMTC4J PRIVATE /await)
endif()

set_target_properties(SMTC4J PROPERTIES
    OUTPUT_NAME "SMTC4J"
)