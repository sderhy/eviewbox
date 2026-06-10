JAVAC ?= javac
JAVA ?= java
JAR ?= jar

SRC_DIR := sources
BUILD_DIR := build
CLASSES_DIR := $(BUILD_DIR)/classes
JAR_FILE := $(BUILD_DIR)/eviewbox.jar
SOURCES := $(shell find $(SRC_DIR) -name '*.java' ! -name 'e_ViewBox_Applet.java')

.PHONY: all compile jar run clean smoke

all: jar

compile:
	mkdir -p $(CLASSES_DIR)
	$(JAVAC) -d $(CLASSES_DIR) $(SOURCES)

jar: compile
	$(JAR) --create --file $(JAR_FILE) --main-class Main -C $(CLASSES_DIR) . -C $(SRC_DIR) Icon

run: jar
	$(JAVA) -Xdock:name=EViewBox -jar $(JAR_FILE)

smoke: compile

clean:
	rm -rf $(BUILD_DIR)
