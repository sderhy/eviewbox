JAVAC ?= javac
JAVA ?= java
JAR ?= jar
JPACKAGE ?= jpackage
APP_VERSION ?= 1.0

# --- macOS signing / notarization (optional) ---------------------------------
# make package SIGN_IDENTITY="Serge Derhy (TEAMID)"   <- name part of the
#   "Developer ID Application" certificate (jpackage adds the prefix itself)
# make notarize   <- needs credentials stored once with :
#   xcrun notarytool store-credentials eviewbox-notary \
#       --apple-id <email> --team-id <TEAMID>
SIGN_IDENTITY ?=
NOTARY_PROFILE ?= eviewbox-notary
BUNDLE_ID := com.sderhy.eviewbox
SIGN_FLAGS :=
ifneq ($(SIGN_IDENTITY),)
SIGN_FLAGS := --mac-sign --mac-signing-key-user-name "$(SIGN_IDENTITY)"
endif
DMG = $(BUILD_DIR)/dist/EViewBox-$(APP_VERSION).dmg# lazy : BUILD_DIR is defined below

SRC_DIR := sources
BUILD_DIR := build
CLASSES_DIR := $(BUILD_DIR)/classes
JAR_FILE := $(BUILD_DIR)/eviewbox.jar
SOURCES := $(shell find $(SRC_DIR) -name '*.java' ! -name 'e_ViewBox_Applet.java')
ICONSET := packaging/EViewBox.iconset
ICNS := packaging/EViewBox.icns

.PHONY: all compile jar run clean smoke icon package notarize

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
		--mac-package-identifier $(BUNDLE_ID) \
		--input $(BUILD_DIR)/package-input --main-jar eviewbox.jar \
		--icon $(ICNS) --dest $(BUILD_DIR)/dist $(SIGN_FLAGS)
	# jpackage signs the .app but not the dmg container itself
	if [ -n "$(SIGN_IDENTITY)" ]; then \
		codesign --force --sign "Developer ID Application: $(SIGN_IDENTITY)" "$(DMG)"; \
	fi

# Send the signed dmg to Apple, then staple the notarization ticket to it
notarize:
	xcrun notarytool submit $(DMG) --keychain-profile $(NOTARY_PROFILE) --wait
	xcrun stapler staple $(DMG)
	xcrun stapler validate $(DMG)

smoke: compile

clean:
	rm -rf $(BUILD_DIR)
