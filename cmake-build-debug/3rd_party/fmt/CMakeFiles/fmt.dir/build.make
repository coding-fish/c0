# CMAKE generated file: DO NOT EDIT!
# Generated by "NMake Makefiles" Generator, CMake Version 3.13

# Delete rule output on recipe failure.
.DELETE_ON_ERROR:


#=============================================================================
# Special targets provided by cmake.

# Disable implicit rules so canonical targets will work.
.SUFFIXES:


.SUFFIXES: .hpux_make_needs_suffix_list


# Suppress display of executed commands.
$(VERBOSE).SILENT:


# A target that is always out of date.
cmake_force:

.PHONY : cmake_force

#=============================================================================
# Set environment variables for the build.

!IF "$(OS)" == "Windows_NT"
NULL=
!ELSE
NULL=nul
!ENDIF
SHELL = cmd.exe

# The CMake executable.
CMAKE_COMMAND = "C:\Program Files\JetBrains\CLion 2019.1\bin\cmake\win\bin\cmake.exe"

# The command to remove a file.
RM = "C:\Program Files\JetBrains\CLion 2019.1\bin\cmake\win\bin\cmake.exe" -E remove -f

# Escaping for special characters.
EQUALS = =

# The top-level source directory on which CMake was run.
CMAKE_SOURCE_DIR = C:\Users\123\Desktop\miniplc0-compiler

# The top-level build directory on which CMake was run.
CMAKE_BINARY_DIR = C:\Users\123\Desktop\miniplc0-compiler\cmake-build-debug

# Include any dependencies generated for this target.
include 3rd_party\fmt\CMakeFiles\fmt.dir\depend.make

# Include the progress variables for this target.
include 3rd_party\fmt\CMakeFiles\fmt.dir\progress.make

# Include the compile flags for this target's objects.
include 3rd_party\fmt\CMakeFiles\fmt.dir\flags.make

3rd_party\fmt\CMakeFiles\fmt.dir\src\format.cc.obj: 3rd_party\fmt\CMakeFiles\fmt.dir\flags.make
3rd_party\fmt\CMakeFiles\fmt.dir\src\format.cc.obj: ..\3rd_party\fmt\src\format.cc
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=C:\Users\123\Desktop\miniplc0-compiler\cmake-build-debug\CMakeFiles --progress-num=$(CMAKE_PROGRESS_1) "Building CXX object 3rd_party/fmt/CMakeFiles/fmt.dir/src/format.cc.obj"
	cd C:\Users\123\Desktop\miniplc0-compiler\cmake-build-debug\3rd_party\fmt
	C:\PROGRA~2\MICROS~1\2019\COMMUN~1\VC\Tools\MSVC\1420~1.275\bin\Hostx86\x86\cl.exe @<<
 /nologo /TP $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) /FoCMakeFiles\fmt.dir\src\format.cc.obj /FdCMakeFiles\fmt.dir\fmt.pdb /FS -c C:\Users\123\Desktop\miniplc0-compiler\3rd_party\fmt\src\format.cc
<<
	cd C:\Users\123\Desktop\miniplc0-compiler\cmake-build-debug

3rd_party\fmt\CMakeFiles\fmt.dir\src\format.cc.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing CXX source to CMakeFiles/fmt.dir/src/format.cc.i"
	cd C:\Users\123\Desktop\miniplc0-compiler\cmake-build-debug\3rd_party\fmt
	C:\PROGRA~2\MICROS~1\2019\COMMUN~1\VC\Tools\MSVC\1420~1.275\bin\Hostx86\x86\cl.exe > CMakeFiles\fmt.dir\src\format.cc.i @<<
 /nologo /TP $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -E C:\Users\123\Desktop\miniplc0-compiler\3rd_party\fmt\src\format.cc
<<
	cd C:\Users\123\Desktop\miniplc0-compiler\cmake-build-debug

3rd_party\fmt\CMakeFiles\fmt.dir\src\format.cc.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling CXX source to assembly CMakeFiles/fmt.dir/src/format.cc.s"
	cd C:\Users\123\Desktop\miniplc0-compiler\cmake-build-debug\3rd_party\fmt
	C:\PROGRA~2\MICROS~1\2019\COMMUN~1\VC\Tools\MSVC\1420~1.275\bin\Hostx86\x86\cl.exe @<<
 /nologo /TP $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) /FoNUL /FAs /FaCMakeFiles\fmt.dir\src\format.cc.s /c C:\Users\123\Desktop\miniplc0-compiler\3rd_party\fmt\src\format.cc
<<
	cd C:\Users\123\Desktop\miniplc0-compiler\cmake-build-debug

3rd_party\fmt\CMakeFiles\fmt.dir\src\posix.cc.obj: 3rd_party\fmt\CMakeFiles\fmt.dir\flags.make
3rd_party\fmt\CMakeFiles\fmt.dir\src\posix.cc.obj: ..\3rd_party\fmt\src\posix.cc
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=C:\Users\123\Desktop\miniplc0-compiler\cmake-build-debug\CMakeFiles --progress-num=$(CMAKE_PROGRESS_2) "Building CXX object 3rd_party/fmt/CMakeFiles/fmt.dir/src/posix.cc.obj"
	cd C:\Users\123\Desktop\miniplc0-compiler\cmake-build-debug\3rd_party\fmt
	C:\PROGRA~2\MICROS~1\2019\COMMUN~1\VC\Tools\MSVC\1420~1.275\bin\Hostx86\x86\cl.exe @<<
 /nologo /TP $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) /FoCMakeFiles\fmt.dir\src\posix.cc.obj /FdCMakeFiles\fmt.dir\fmt.pdb /FS -c C:\Users\123\Desktop\miniplc0-compiler\3rd_party\fmt\src\posix.cc
<<
	cd C:\Users\123\Desktop\miniplc0-compiler\cmake-build-debug

3rd_party\fmt\CMakeFiles\fmt.dir\src\posix.cc.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing CXX source to CMakeFiles/fmt.dir/src/posix.cc.i"
	cd C:\Users\123\Desktop\miniplc0-compiler\cmake-build-debug\3rd_party\fmt
	C:\PROGRA~2\MICROS~1\2019\COMMUN~1\VC\Tools\MSVC\1420~1.275\bin\Hostx86\x86\cl.exe > CMakeFiles\fmt.dir\src\posix.cc.i @<<
 /nologo /TP $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -E C:\Users\123\Desktop\miniplc0-compiler\3rd_party\fmt\src\posix.cc
<<
	cd C:\Users\123\Desktop\miniplc0-compiler\cmake-build-debug

3rd_party\fmt\CMakeFiles\fmt.dir\src\posix.cc.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling CXX source to assembly CMakeFiles/fmt.dir/src/posix.cc.s"
	cd C:\Users\123\Desktop\miniplc0-compiler\cmake-build-debug\3rd_party\fmt
	C:\PROGRA~2\MICROS~1\2019\COMMUN~1\VC\Tools\MSVC\1420~1.275\bin\Hostx86\x86\cl.exe @<<
 /nologo /TP $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) /FoNUL /FAs /FaCMakeFiles\fmt.dir\src\posix.cc.s /c C:\Users\123\Desktop\miniplc0-compiler\3rd_party\fmt\src\posix.cc
<<
	cd C:\Users\123\Desktop\miniplc0-compiler\cmake-build-debug

# Object files for target fmt
fmt_OBJECTS = \
"CMakeFiles\fmt.dir\src\format.cc.obj" \
"CMakeFiles\fmt.dir\src\posix.cc.obj"

# External object files for target fmt
fmt_EXTERNAL_OBJECTS =

3rd_party\fmt\fmtd.lib: 3rd_party\fmt\CMakeFiles\fmt.dir\src\format.cc.obj
3rd_party\fmt\fmtd.lib: 3rd_party\fmt\CMakeFiles\fmt.dir\src\posix.cc.obj
3rd_party\fmt\fmtd.lib: 3rd_party\fmt\CMakeFiles\fmt.dir\build.make
3rd_party\fmt\fmtd.lib: 3rd_party\fmt\CMakeFiles\fmt.dir\objects1.rsp
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --bold --progress-dir=C:\Users\123\Desktop\miniplc0-compiler\cmake-build-debug\CMakeFiles --progress-num=$(CMAKE_PROGRESS_3) "Linking CXX static library fmtd.lib"
	cd C:\Users\123\Desktop\miniplc0-compiler\cmake-build-debug\3rd_party\fmt
	$(CMAKE_COMMAND) -P CMakeFiles\fmt.dir\cmake_clean_target.cmake
	cd C:\Users\123\Desktop\miniplc0-compiler\cmake-build-debug
	cd C:\Users\123\Desktop\miniplc0-compiler\cmake-build-debug\3rd_party\fmt
	C:\PROGRA~2\MICROS~1\2019\COMMUN~1\VC\Tools\MSVC\1420~1.275\bin\Hostx86\x86\link.exe /lib /nologo /machine:X86 /out:fmtd.lib @CMakeFiles\fmt.dir\objects1.rsp 
	cd C:\Users\123\Desktop\miniplc0-compiler\cmake-build-debug

# Rule to build all files generated by this target.
3rd_party\fmt\CMakeFiles\fmt.dir\build: 3rd_party\fmt\fmtd.lib

.PHONY : 3rd_party\fmt\CMakeFiles\fmt.dir\build

3rd_party\fmt\CMakeFiles\fmt.dir\clean:
	cd C:\Users\123\Desktop\miniplc0-compiler\cmake-build-debug\3rd_party\fmt
	$(CMAKE_COMMAND) -P CMakeFiles\fmt.dir\cmake_clean.cmake
	cd C:\Users\123\Desktop\miniplc0-compiler\cmake-build-debug
.PHONY : 3rd_party\fmt\CMakeFiles\fmt.dir\clean

3rd_party\fmt\CMakeFiles\fmt.dir\depend:
	$(CMAKE_COMMAND) -E cmake_depends "NMake Makefiles" C:\Users\123\Desktop\miniplc0-compiler C:\Users\123\Desktop\miniplc0-compiler\3rd_party\fmt C:\Users\123\Desktop\miniplc0-compiler\cmake-build-debug C:\Users\123\Desktop\miniplc0-compiler\cmake-build-debug\3rd_party\fmt C:\Users\123\Desktop\miniplc0-compiler\cmake-build-debug\3rd_party\fmt\CMakeFiles\fmt.dir\DependInfo.cmake --color=$(COLOR)
.PHONY : 3rd_party\fmt\CMakeFiles\fmt.dir\depend

