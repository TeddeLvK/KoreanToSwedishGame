UNAME_S := $(shell uname -s)

ifeq ($(UNAME_S),Darwin)
    JAVA_HOME ?= $(shell /usr/libexec/java_home)
    JNI_INC := -I"$(JAVA_HOME)/include" -I"$(JAVA_HOME)/include/darwin"
    LIB_EXT := dylib
    SHARED_FLAG := -dynamiclib
    CC := clang
else
    JAVA_HOME ?= $(shell dirname $(shell dirname $(shell readlink -f $(shell which javac))))
    JNI_INC := -I"$(JAVA_HOME)/include" -I"$(JAVA_HOME)/include/linux"
    LIB_EXT := so
    SHARED_FLAG := -shared
    CC := gcc
endif

BUILD_DIR := build
CLASSES_DIR := $(BUILD_DIR)/classes
NATIVE_LIB := $(BUILD_DIR)/libswedishgame.$(LIB_EXT)
C_SRCS := src/native/engine.c src/native/jni_bridge.c
JAVA_SRCS := $(wildcard src/java/com/swedishgame/*.java)

.PHONY: all native java run clean jar icon app dmg

all: native java

native: $(NATIVE_LIB)

$(NATIVE_LIB): $(C_SRCS) src/native/engine.h
	@mkdir -p $(BUILD_DIR)
	$(CC) -std=c11 -O2 -Wall -Wextra -fPIC $(SHARED_FLAG) $(JNI_INC) $(C_SRCS) -lm -o $(NATIVE_LIB)

java:
	@mkdir -p $(CLASSES_DIR)
	javac -encoding UTF-8 -d $(CLASSES_DIR) $(JAVA_SRCS)

run: all
	java -Djava.library.path=$(BUILD_DIR) -cp $(CLASSES_DIR) com.swedishgame.Main

clean:
	rm -rf $(BUILD_DIR) dist Swemon.jar

jar: java
	@rm -rf build/jar_stage
	@mkdir -p build/jar_stage/data
	@cp -R build/classes/* build/jar_stage/
	@cp -R Assets build/jar_stage/
	@cp src/data/levels.txt src/data/glossary.txt build/jar_stage/data/
	jar --create --file Swemon.jar --main-class com.swedishgame.Main -C build/jar_stage .

icon:
	@rm -rf build/Swemon.iconset build/Swemon.icns
	@mkdir -p build/Swemon.iconset
	@sips -z 16 16     Assets/sprites/player_crown_big.png --out build/Swemon.iconset/icon_16x16.png >/dev/null
	@sips -z 32 32     Assets/sprites/player_crown_big.png --out build/Swemon.iconset/icon_16x16@2x.png >/dev/null
	@sips -z 32 32     Assets/sprites/player_crown_big.png --out build/Swemon.iconset/icon_32x32.png >/dev/null
	@sips -z 64 64     Assets/sprites/player_crown_big.png --out build/Swemon.iconset/icon_32x32@2x.png >/dev/null
	@sips -z 128 128   Assets/sprites/player_crown_big.png --out build/Swemon.iconset/icon_128x128.png >/dev/null
	@sips -z 256 256   Assets/sprites/player_crown_big.png --out build/Swemon.iconset/icon_128x128@2x.png >/dev/null
	@sips -z 256 256   Assets/sprites/player_crown_big.png --out build/Swemon.iconset/icon_256x256.png >/dev/null
	@sips -z 512 512   Assets/sprites/player_crown_big.png --out build/Swemon.iconset/icon_256x256@2x.png >/dev/null
	@sips -z 512 512   Assets/sprites/player_crown_big.png --out build/Swemon.iconset/icon_512x512.png >/dev/null
	@iconutil -c icns build/Swemon.iconset -o build/Swemon.icns

app: jar icon
	@rm -rf build/jpackage_in dist/Swemon.app
	@mkdir -p build/jpackage_in dist
	@cp Swemon.jar build/jpackage_in/
	jpackage --type app-image --name Swemon --app-version 1.0.0 --input build/jpackage_in --main-jar Swemon.jar --main-class com.swedishgame.Main --icon build/Swemon.icns --dest dist --java-options "--enable-native-access=ALL-UNNAMED" --java-options "-Xdock:name=Swemon"

dmg: jar icon
	@rm -rf build/jpackage_in dist/Swemon-1.0.0.dmg
	@mkdir -p build/jpackage_in dist
	@cp Swemon.jar build/jpackage_in/
	jpackage --type dmg --name Swemon --app-version 1.0.0 --input build/jpackage_in --main-jar Swemon.jar --main-class com.swedishgame.Main --icon build/Swemon.icns --dest dist --java-options "--enable-native-access=ALL-UNNAMED" --java-options "-Xdock:name=Swemon"
