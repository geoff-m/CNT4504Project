
all: project1

project1:
	mkdir build
	javac -d build src/*.java

clean:
	rm -r build
