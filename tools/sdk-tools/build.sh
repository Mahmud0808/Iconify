#!/usr/bin/env bash
set -euo pipefail
export MSYS_NO_PATHCONV=1

UPSTREAM_REPO="${UPSTREAM_REPO:-https://github.com/lzhiyong/android-sdk-tools.git}"
UPSTREAM_REF="${UPSTREAM_REF:-fe637bf075f1b734b4b01d281f9f58899245ed92}"
AOSP_TAG="${AOSP_TAG:-android-17.0.0_r1}"
NDK_VERSION="${NDK_VERSION:-r29}"
API_LEVEL="${API_LEVEL:-30}"
ABIS="${ABIS:-arm64-v8a armeabi-v7a x86_64 x86}"
IMAGE="${IMAGE:-iconify-sdk-tools:${NDK_VERSION}}"
VOLUME="${VOLUME:-iconify-sdk-tools-work}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
OUT_DIR="$SCRIPT_DIR/out"
JNI_LIBS_DIR="$REPO_ROOT/app/src/main/jniLibs"

host_path() {
    if command -v cygpath >/dev/null 2>&1; then cygpath -m "$1"; else echo "$1"; fi
}

log() { printf '\033[1;32m==> %s\033[0m\n' "$*"; }

command -v docker >/dev/null 2>&1 || { echo "docker not found" >&2; exit 1; }
docker info >/dev/null 2>&1 || { echo "docker daemon is not running" >&2; exit 1; }

log "Building toolchain image $IMAGE (upstream $UPSTREAM_REF, NDK $NDK_VERSION)"
docker build --platform linux/amd64 \
    --build-arg "NDK_VERSION=$NDK_VERSION" \
    --build-arg "ZIG_VERSION=" \
    -t "$IMAGE" \
    "$UPSTREAM_REPO#$UPSTREAM_REF"

docker volume create "$VOLUME" >/dev/null
rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"

log "Building aapt2 + zipalign from $AOSP_TAG for: $ABIS"
docker run --rm --platform linux/amd64 \
    -v "$VOLUME:/work" \
    -v "$(host_path "$OUT_DIR"):/out" \
    -v "$(host_path "$SCRIPT_DIR/patches"):/patches:ro" \
    -e UPSTREAM_REPO="$UPSTREAM_REPO" \
    -e UPSTREAM_REF="$UPSTREAM_REF" \
    -e AOSP_TAG="$AOSP_TAG" \
    -e API_LEVEL="$API_LEVEL" \
    -e ABIS="$ABIS" \
    "$IMAGE" bash -euo pipefail -c '
        cd /work
        [ -d sdk/.git ] || git clone "$UPSTREAM_REPO" sdk
        cd sdk
        git fetch --quiet origin
        git checkout --quiet --force "$UPSTREAM_REF"

        if [ "$(cat .aosp_tag 2>/dev/null)" != "$AOSP_TAG" ]; then
            rm -rf src build
            python3 get_source.py --tags "$AOSP_TAG"
            echo "$AOSP_TAG" > .aosp_tag
        fi

        bash /patches/apply.sh /work/sdk

        READELF="$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/linux-x86_64/bin/llvm-readelf"

        for abi in $ABIS; do
            python3 build.py --arch "$abi" --api "$API_LEVEL" --build "build/$abi" \
                --target aapt2 zipalign -- \
                -DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON \
                "-DCMAKE_EXE_LINKER_FLAGS=-Wl,-z,max-page-size=16384 -L/work/sdk/build/$abi/lib" \
                "-DCMAKE_C_FLAGS=-DHAVE_ARC4RANDOM_BUF=1 -DHAVE_REALLOCARRAY=1" \
                -Dprotobuf_INSTALL=OFF \
                -DABSL_ENABLE_INSTALL=OFF

            mkdir -p "/out/$abi"
            for tool in aapt2 zipalign; do
                src="build/$abi/bin/$tool"
                dst="/out/$abi/lib$tool.so"
                cp "$src" "$dst"

                for align in $("$READELF" -lW "$dst" | grep LOAD | sed "s/.* //"); do
                    if (( align < 16384 )); then
                        echo "$dst is not 16KB aligned ($align)" >&2
                        exit 1
                    fi
                done
                if "$READELF" -dW "$dst" 2>/dev/null | grep -q NEEDED; then
                    echo "$dst is not statically linked" >&2
                    exit 1
                fi
            done
        done

        chmod -R a+rwX /out
    '

for abi in $ABIS; do
    mkdir -p "$JNI_LIBS_DIR/$abi"
    cp "$OUT_DIR/$abi/libaapt2.so" "$OUT_DIR/$abi/libzipalign.so" "$JNI_LIBS_DIR/$abi/"
done
log "Updated $JNI_LIBS_DIR"
