MODDIR=${0%%/*}

while [ "$(getprop sys.boot_completed | tr -d "\r")" != "1" ]
do
 sleep 1
done
sleep 5

sh $MODDIR/post-exec.sh

until [ -d /storage/emulated/0/Android ]; do
  sleep 1
done
sleep 3

{{RESTART_SYSUI_AFTER_BOOT}}sleep 6

handle_overlay_am() {
  local amac="IconifyComponentAMAC.overlay"
  local amgc="IconifyComponentAMGC.overlay"

  local overlays
  overlays="$(cmd overlay list)"

  local amac_enabled
  local amgc_enabled

  amac_enabled=$(echo "$overlays" | grep -F "[x] $amac")
  amgc_enabled=$(echo "$overlays" | grep -F "[x] $amgc")

  if [ -z "$amac_enabled" ] && [ -z "$amgc_enabled" ]; then
    cmd overlay enable --user current "$amgc"
    cmd overlay set-priority "$amgc" highest
  fi
}

handle_overlay_am
