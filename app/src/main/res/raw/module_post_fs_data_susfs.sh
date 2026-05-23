#!/usr/bin/env sh
MODDIR="${0%%/*}"
modid="Iconify"
SUSFS_BIN=/data/adb/ksu/bin/ksu_susfs

if [ $KSU_MAGIC_MOUNT = true ]; then
    exit 0
fi

[ ! -f $MODDIR/skip_mount ] && touch $MODDIR/skip_mount

[ -w /mnt ] && basefolder=/mnt
[ -w /mnt/vendor ] && basefolder=/mnt/vendor

mkdir $basefolder/$modid

cd $MODDIR

for i in $(ls -d */*); do
    mkdir -p $basefolder/$modid/$i
    mount --bind $MODDIR/$i $basefolder/$modid/$i
    mount -t overlay -o "lowerdir=$basefolder/$modid/$i:/$i" overlay /$i
    ${SUSFS_BIN} add_sus_mount /$i
done