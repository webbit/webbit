# Common tasks:
# make                 -- Full build
# make clean           -- Clean up built files
# make again           -- Clean and rebuild
SHELL := /bin/bash

LIBRARY=webbit
CLASSPATH=$(shell echo $(wildcard lib/*.jar) | sed -e 's/ /:/g')
JARJARRULES='rule org.jboss.netty.** org.webbitserver.dependencies.org.jboss.netty.@1'

# Default target: Compile, run tests and build tarball
all: jar test
jar: dist/$(LIBRARY).jar dist/$(LIBRARY)-src.jar dist/$(LIBRARY)-all-in-one.jar
test: build/.tests-pass
.PHONY: all jar test

# Run sample chatroom
chatroom: test dist/$(LIBRARY)-all-in-one.jar
	java -cp $(CLASSPATH):dist/$(LIBRARY)-all-in-one.jar:build/$(LIBRARY)-tests.jar samples.chatroom.Main
.PHONY: chatroom

# Run echo server - used by Autobahn
echo: test dist/$(LIBRARY)-all-in-one.jar
	java -Xmx256m -cp $(CLASSPATH):dist/$(LIBRARY)-all-in-one.jar:build/$(LIBRARY)-tests.jar samples.echo.Main
.PHONY: echo

# Function to find files in directory with suffix. $(call find,dir,ext)
find = $(shell find $(1) -name '*.$(2)')

# Function to extract Test class names from a jar. $(call extracttests,foo.jar)
extracttests = $(shell jar tf $(1) | grep 'Test.class$$' | sed -e 's|/|.|g;s|.class$$||')

# Compile core Jar (just classes, no dependencies)
dist/$(LIBRARY).jar: $(call find,src/main/java,java)
	@mkdir -p build/main/classes
	@mkdir -p dist
	javac -g -cp $(CLASSPATH) -d build/main/classes $(call find,src/main/java,java)
	jar cf $@ -C build/main/classes .

# Merge dependencies with core jar into an all-in-one jar.
dist/$(LIBRARY)-all-in-one.jar: dist/$(LIBRARY).jar
	@mkdir -p dist
	@echo Packaging everything together into one jar...
	java -jar lib/autojar.jar -o build/$(LIBRARY)-merged.jar -c $(CLASSPATH) dist/$(LIBRARY).jar
	java -jar lib/jarjar-1.1.jar process <(echo $(JARJARRULES)) build/$(LIBRARY)-merged.jar $@

# Assemble source jar
dist/$(LIBRARY)-src.jar: $(call find,src/main/java,java)
	@mkdir -p dist
	jar cf $@ -C src/main/java .

# Compile tests
build/$(LIBRARY)-tests.jar: dist/$(LIBRARY).jar $(call find,src/test/java,java)
	@mkdir -p build/test/classes
	cp -R src/test/resources/* build/test/classes
	javac -g -cp $(CLASSPATH):dist/$(LIBRARY).jar -d build/test/classes $(call find,src/test/java,java)
	jar cf $@ -C build/test/classes .

# Run tests, and create .tests-pass if they succeed
build/.tests-pass: build/$(LIBRARY)-tests.jar
	@rm -f $@
	java -cp dist/$(LIBRARY).jar:build/$(LIBRARY)-tests.jar:$(CLASSPATH) org.junit.runner.JUnitCore $(call extracttests,build/$(LIBRARY)-tests.jar)
	@touch $@

# Run Autobahn tests
autobahn: bin/python
	PYTHONPATH=src/test/Autobahn/lib/python bin/python src/test/Autobahn/testsuite/websockets/fuzzing_client.py

.PHONY: autobahn

bin/python:
	python virtualenv.py --no-site-packages .

# Clean up
clean:
	rm -rf build dist out

.PHONY: clean

release:
	@(echo | gpg -ab --batch > /dev/null 2>&1) || (echo "ERROR: gpg-agent not running"; /bin/false)
	mvn release:clean
	mvn --batch-mode -P release-sign-artifacts release:prepare
	mvn --batch-mode -P release-sign-artifacts release:perform
	git checkout HEAD~1
	make clean
	make
	mvn -P release-sign-artifacts gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=pom.xml -Dfile=dist/webbit-all-in-one.jar -Dclassifier=full
	git checkout master

.PHONY: release

again: clean all
.PHONY: again
