cmake_minimum_required(VERSION 3.22.1)
project("tiptop")
add_library(
        tiptop
        SHARED
        native-lib.cpp
)
add_library(utils SHARED utils.cpp)
find_library(
        log-lib
        log
)
target_link_libraries(
        tiptop
        ${log-lib}
)
