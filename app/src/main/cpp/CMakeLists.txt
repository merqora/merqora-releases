cmake_minimum_required(VERSION 3.22.1)
project("rendly_media_optimizer")

# C++17 with aggressive optimization
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(
    rendly_media_optimizer
    SHARED
    media_optimizer_jni.cpp
    feed_engine.cpp
    feed_engine_jni.cpp
)

# Link Android libraries
find_library(log-lib log)
find_library(jnigraphics-lib jnigraphics)

target_link_libraries(
    rendly_media_optimizer
    ${log-lib}
    ${jnigraphics-lib}
)
