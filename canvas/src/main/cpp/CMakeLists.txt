
cmake_minimum_required(VERSION 3.6.0)

Set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -fno-rtti -fno-exceptions -Wall")

if(${ANDROID_PLATFORM_LEVEL} LESS 18)
	add_definitions("-DDYNAMIC_ES3")
	set(GL3STUB_SRC gl3stub.c)
	set(OPENGL_LIB GLESv2)
else()
	set(OPENGL_LIB GLESv3)
endif()

#add_library(canvasnative STATIC IMPORTED)
#set_target_properties( canvasnative PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libcanvasnative.so )

add_library(canvasextras SHARED ${GL3STUB_SRC} canvas_extras.cpp)
target_link_libraries(canvasextras ${OPENGL_LIB} android log EGL)