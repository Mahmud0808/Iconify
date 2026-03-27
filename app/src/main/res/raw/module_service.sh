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

handle_overlay() {
  local overlay_name="$1"

  local overlay=$(cmd overlay list | grep -E "^.x..${overlay_name}.overlay" | sed -E "s/^.x..//")
  local disableMonet=$(cmd overlay list | grep -E "^.x..IconifyComponentDM.overlay" | sed -E "s/^.x..//")

  if ([ ! -z "${overlay}" ] && [ -z "${disableMonet}" ])
  then
    cmd overlay disable --user current "${overlay_name}.overlay"
    cmd overlay enable --user current "${overlay_name}.overlay"
    cmd overlay set-priority "${overlay_name}.overlay" highest
  fi
}

handle_overlay "IconifyComponentQSPBD"
handle_overlay "IconifyComponentQSPBA"
