import re
import sys
from pathlib import Path

root = Path(sys.argv[1])
src = root / "src"
pattern = re.compile(r"^\s*\$\{SRC\}/(\S+?)\s*\)?\s*$")

for cmake in sorted(list((root / "lib").glob("*.cmake")) + list((root / "build-tools").glob("*.cmake"))):
    lines = cmake.read_text().splitlines(keepends=True)
    kept = []
    removed = []
    for line in lines:
        match = pattern.match(line)
        if match and not line.rstrip().endswith(")"):
            path = match.group(1)
            if "*" not in path and "$" not in path and not (src / path).exists():
                removed.append(path)
                continue
        kept.append(line)
    if removed:
        cmake.write_text("".join(kept))
        for path in removed:
            print(f"{cmake.name}: removed missing {path}")
