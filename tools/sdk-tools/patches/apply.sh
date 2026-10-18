#!/usr/bin/env bash
set -euo pipefail

SDK="${1:?usage: apply.sh <android-sdk-tools checkout>}"
PATCHES="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SDK"

git checkout --quiet -- CMakeLists.txt lib build-tools

sed -i -E '/add_subdirectory\((platform-tools|others)\)/d' CMakeLists.txt
sed -i 's/set(CMAKE_CXX_STANDARD 20)/set(CMAKE_CXX_STANDARD 23)/' CMakeLists.txt

sed -i -E '/include\((libsparse|libprocessgroup|libusb|libdiagnoseusb|libmdnssd|libopenscreen)\.cmake\)/d' lib/CMakeLists.txt
sed -i -E '/include\((aapt|aidl|split-select|dexdump)\.cmake\)/d' build-tools/CMakeLists.txt

python3 "$PATCHES/prune_missing_sources.py" .
python3 "$PATCHES/add_sources.py" .

sed -i 's#^\(\s*\)${SRC}/protobuf/src$#&\n\1${SRC}/abseil-cpp\n\1${SRC}/protobuf/third_party/utf8_range#' build-tools/aapt2.cmake

cat >> lib/liblog.cmake <<'EOF'
target_compile_definitions(liblog PRIVATE __ANDROID_UNAVAILABLE_SYMBOLS_ARE_WEAK__)
set_target_properties(liblog PROPERTIES OUTPUT_NAME log)
EOF

if [ ! -e src/protobuf/third_party/abseil-cpp ]; then
    ln -s ../../abseil-cpp src/protobuf/third_party/abseil-cpp
fi

cp "$PATCHES/include/android_content_res.h" src/base/libs/androidfw/include/android_content_res.h
