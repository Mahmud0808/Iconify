#!/system/bin/sh
# Do NOT assume where your module will be located.
# ALWAYS use $MODDIR if you need to know where this script
# and module is placed.
# This will make sure your module will still work
# if Magisk change its mount point in the future
MODDIR=${0%/*}
(
  until [ -d $MODPATH ]; do
    sleep 1
  done
  if [ -f "$MODPATH/mypath/myname.apk" ]; then
  mkdir $MODPATH/temp
  cp -f $MODPATH/mypath/myname.apk $MODPATH/temp/
  cp -f $MODPATH/OG/myname.apk $MODPATH/mypath/
  
  until [ $(getprop sys.boot_completed) == "1" ] && [ -d /storage/emulated/0/Android ]; do
    sleep 1
  done
  sleep 3
  cp -f $MODPATH/temp/myname.apk $MODPATH/mypath
  rm -r $MODPATH/temp
  killall com.android.systemui
  fi
)&


# This script will be executed in post-fs-data mode