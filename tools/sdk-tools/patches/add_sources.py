import re
import sys
from pathlib import Path

root = Path(sys.argv[1])
extra = {
    ("build-tools/aapt2.cmake", "libaapt2"): [
        "base/tools/aapt2/link/FlaggedXmlVersioner.cpp",
        "base/tools/aapt2/link/FlagNotEnabledResourceRemover.cpp",
    ],
    ("lib/libandroidfw.cmake", "libandroidfw"): [
        "base/libs/androidfw/LocaleDataLookup.cpp",
    ],
}

for (cmake_file, target), files in extra.items():
    cmake = root / cmake_file
    text = cmake.read_text()
    match = re.search(r"add_library\(\s*" + re.escape(target) + r"\s+STATIC\s*\n", text)
    if not match:
        sys.exit(f"{cmake_file}: target {target} not found")
    additions = [
        f"    ${{SRC}}/{path}\n"
        for path in files
        if (root / "src" / path).exists() and f"${{SRC}}/{path}" not in text
    ]
    if additions:
        text = text[:match.end()] + "".join(additions) + text[match.end():]
        cmake.write_text(text)
        for line in additions:
            print(f"{cmake_file}: added {line.strip()}")
