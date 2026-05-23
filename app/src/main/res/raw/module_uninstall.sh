#!/system/bin/sh

# Uninstall Iconify components
pm list packages -3 | cut -d':' -f2 | grep "^IconifyComponent" | while read -r pkg; do
    pm uninstall "$pkg"
done
