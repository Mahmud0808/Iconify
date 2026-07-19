#!/system/bin/sh

# Uninstall Iconify components
pm list packages -3 | cut -d':' -f2 | grep "^IconifyComponent" | while read -r pkg; do
    pm uninstall "$pkg"
done

# Resolve internal storage path dynamically
INTERNAL_STORAGE=$(readlink -f /sdcard 2>/dev/null) || INTERNAL_STORAGE="/storage/emulated/0"

# Delete Iconify folder from Downloads directory
rm -rf "${INTERNAL_STORAGE}/Download/Iconify"
