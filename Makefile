# Common tasks:
# make                 -- Full build
# make clean           -- Clean up built files

LIBRARY=webbit
CLASSPATH=$(shell echo $(wildcard lib/*.jar) | sed -e 's/ /:/g')

# Non file targets
.PHONY: all jar test clean

# Default target: Compile, run tests and build tarball
all: jar test
jar: build/$(LIBRARY).jar build/$(LIBRARY)-src.jar
test: build/.tests-pass

# Function to find files in directory with suffix. $(call find,dir,ext)
find = $(shell find $(1) -name '*.$(2)')

# Function to extract Test class names from a jar. $(call extracttests,foo.jar)
extracttests = $(shell jar tf $(1) | grep 'Test.class$$' | sed -e 's|/|.|g;s|.class$$||')

# Compile Jar
build/$(LIBRARY).jar: $(call find,src/main/java,java)
	@mkdir -p build/main/classes
	javac -g -cp $(CLASSPATH) -d build/main/classes $(call find,src/main/java,java)
	jar cf $@ -C build/main/classes .

# Assemble source jar
build/$(LIBRARY)-src.jar: $(call find,src/main/java,java)
	@mkdir -p build
	jar cf $@ -C src/main/java .

# Compile tests
build/$(LIBRARY)-tests.jar: build/$(LIBRARY).jar $(call find,src/test/java,java)
	@mkdir -p build/test/classes
	javac -g -cp $(CLASSPATH):build/$(LIBRARY).jar -d build/test/classes $(call find,src/test/java,java)
	jar cf $@ -C build/test/classes .

# Run tests, and create .tests-pass if they succeed
build/.tests-pass: build/$(LIBRARY)-tests.jar
	@rm -f $@
	java -cp $(CLASSPATH):build/$(LIBRARY).jar:build/$(LIBRARY)-tests.jar org.junit.runner.JUnitCore $(call extracttests,build/$(LIBRARY)-tests.jar)
	@touch $@

# Clean up
clean:
	rm -rf build out

