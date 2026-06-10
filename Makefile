JAVAC ?= javac
JAVA ?= java
JAR ?= jar
JPACKAGE ?= jpackage
APP_VERSION ?= 1.0

SRC_DIR := sources
BUILD_DIR := build
CLASSES_DIR := $(BUILD_DIR)/classes
JAR_FILE := $(BUILD_DIR)/eviewbox.jar
SOURCES := $(shell find $(SRC_DIR) -name '*.java' ! -name 'e_ViewBox_Applet.java')
ICONSET := packaging/EViewBox.iconset
ICNS := packaging/EViewBox.icns

.PHONY: all compile jar run clean smoke icon package

all: jar

compile:
	mkdir -p $(CLASSES_DIR)
	$(JAVAC) -encoding UTF-8 -d $(CLASSES_DIR) $(SOURCES)

jar: compile
	$(JAR) --create --file $(JAR_FILE) --main-class Main -C $(CLASSES_DIR) . -C $(SRC_DIR) Icon

run: jar
	$(JAVA) -Xdock:name=EViewBox -Xdock:icon=packaging/EViewBox.png -jar $(JAR_FILE)

# Regenerate the application icon (Java2D) and the macOS .icns
icon:
	mkdir -p $(BUILD_DIR)/icontool $(ICONSET)
	$(JAVAC) -d $(BUILD_DIR)/icontool packaging/MakeIcon.java
	$(JAVA) -cp $(BUILD_DIR)/icontool MakeIcon $(ICONSET)
	iconutil --convert icns --output $(ICNS) $(ICONSET)

# Distributable macOS package (.dmg) built with jpackage
package: jar icon
	rm -rf $(BUILD_DIR)/package-input $(BUILD_DIR)/dist
	mkdir -p $(BUILD_DIR)/package-input $(BUILD_DIR)/dist
	cp $(JAR_FILE) $(BUILD_DIR)/package-input/
	$(JPACKAGE) --type dmg --name EViewBox --app-version $(APP_VERSION) \
		--input $(BUILD_DIR)/package-input --main-jar eviewbox.jar \
		--icon $(ICNS) --dest $(BUILD_DIR)/dist

smoke: compile

clean:
	rm -rf $(BUILD_DIR)
