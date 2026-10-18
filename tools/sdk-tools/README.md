# SDK tools (aapt2 + zipalign)

Iconify compiles overlays on device with `aapt2` and `zipalign`. Both are built from AOSP
source as fully static Android executables and shipped as native libraries:

```
app/src/main/jniLibs/<abi>/libaapt2.so
app/src/main/jniLibs/<abi>/libzipalign.so
```

`useLegacyPackaging = true` in `app/build.gradle.kts` makes Android extract them to
`applicationInfo.nativeLibraryDir` on install, where they are executable. The app runs them
from there (`Dynamic.AAPT2` / `Dynamic.ZIPALIGN`), nothing is extracted at runtime.

The tools are built from the Android 17 sources (`android-17.0.0_r1`, aapt2 `2.20`) and run on
Android 11 (API 30) and newer. Every binary is built with 16 KB ELF segment alignment, so the app is compatible with 16 KB
page size devices and Android doesn't show the 16 KB compatibility warning.

## Requirements

- Docker (Docker Desktop on Windows, or Docker Engine on Linux / WSL)
- Bash (Git Bash on Windows, or any Linux / WSL shell)
- About 15 GB of free disk space for the AOSP sources and build cache

No local NDK, CMake or compiler is needed, everything runs inside the container.

## Rebuild

```bash
bash tools/sdk-tools/build.sh
```

The script:

1. Builds the toolchain image from the pinned [lzhiyong/android-sdk-tools](https://github.com/lzhiyong/android-sdk-tools)
   commit (Ubuntu + NDK, CMake, Ninja, Go).
2. Fetches the AOSP sources of `AOSP_TAG` into the `iconify-sdk-tools-work` Docker volume
   (only on the first run or when the tag changes).
3. Applies `patches/apply.sh` (see [Patches](#patches)).
4. Builds `aapt2` and `zipalign` for `arm64-v8a`, `armeabi-v7a`, `x86_64` and `x86`.
5. Fails if any binary is not statically linked or has a LOAD segment aligned below 16 KB.
6. Copies the binaries into `app/src/main/jniLibs/<abi>/`.

Then build the app and commit the updated `jniLibs`.

## Updating to newer tools

All versions are variables at the top of `build.sh` and can be overridden per run:

| Variable | Default | Meaning |
| --- | --- | --- |
| `AOSP_TAG` | `android-17.0.0_r1` | AOSP tag the tools are built from |
| `UPSTREAM_REF` | pinned commit | commit of lzhiyong/android-sdk-tools (build scripts + patches) |
| `NDK_VERSION` | `r29` | NDK used to compile |
| `API_LEVEL` | `30` | minimum Android API of the executables |
| `ABIS` | all four | ABIs to build |

Example:

```bash
AOSP_TAG=android-17.0.0_r2 bash tools/sdk-tools/build.sh
```

To update:

1. List the newest AOSP tags:
   ```bash
   git ls-remote --tags https://android.googlesource.com/platform/frameworks/base 'refs/tags/android-*' | tail
   ```
2. Check for newer commits of [lzhiyong/android-sdk-tools](https://github.com/lzhiyong/android-sdk-tools/commits)
   and update `UPSTREAM_REF` (its patches have to support the AOSP tag).
3. Run the script with the new values. If it builds, make the new values the defaults in `build.sh`.

If a newer tag fails to build, check the error and extend `patches/` (usually a moved, removed or
new source file, see below). `AOSP_TAG=platform-tools-35.0.2` with NDK `r27c` is the last tag the
upstream project supports without our patches.

## Patches

The upstream CMake files target an older AOSP release, `patches/apply.sh` adapts them to the newer
sources. It is idempotent and runs on every build:

| Patch | Why |
| --- | --- |
| Only `aapt2` + `zipalign` and their libraries are configured | the other tools don't build with the new sources and aren't needed |
| C++23 | libbase uses `std::string::resize_and_overwrite` |
| `prune_missing_sources.py` | drops files AOSP removed from the CMake source lists |
| `add_sources.py` | adds files AOSP added (`FlaggedXmlVersioner`, `FlagNotEnabledResourceRemover`, `LocaleDataLookup`) |
| abseil + utf8_range include paths, abseil linked into protobuf | protobuf 33 depends on abseil |
| liblog built as `liblog.a` with weak unavailable symbols | abseil links `-llog`, and liblog uses API 37 symbols it defines itself |
| `include/android_content_res.h` | aconfig flags header that AOSP generates at build time (`resource_readwrite_flags` on, so flagged resources in new framework-res files are parsed) |

Extra compiler / linker flags are passed in `build.sh`: `HAVE_ARC4RANDOM_BUF` (expat entropy),
`HAVE_REALLOCARRAY` (libselinux vs bionic), `protobuf_INSTALL=OFF` / `ABSL_ENABLE_INSTALL=OFF`
and the 16 KB page size flags.

When a new tag adds source files, the build fails at link time with `undefined symbol` errors.
Find the file that defines the symbol in `src/` and add it to `add_sources.py`.

## Verify

Check alignment and linking of a binary (with any `llvm-readelf` / `readelf`):

```bash
readelf -lW app/src/main/jniLibs/arm64-v8a/libaapt2.so | grep LOAD   # align must be 0x4000 or higher
readelf -dW app/src/main/jniLibs/arm64-v8a/libaapt2.so               # must have no NEEDED entries
```

On a device (use `com.drdisagree.iconify.debug` for debug builds):

```bash
adb shell
su
LIB=$(dumpsys package com.drdisagree.iconify | grep -m1 legacyNativeLibraryDir= | sed 's/.*legacyNativeLibraryDir=//')/$(getprop ro.product.cpu.abi)
ls -l $LIB
$LIB/libaapt2.so version
```

## Clean up

The sources (~10 GB) stay in the Docker volume to speed up the next build. Remove them when done:

```bash
docker volume rm iconify-sdk-tools-work
docker image rm iconify-sdk-tools:r29
docker builder prune -f
```
