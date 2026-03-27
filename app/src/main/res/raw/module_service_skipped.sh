MODDIR=${0%%/*}

while [ "$(getprop sys.boot_completed | tr -d "\r")" != "1" ]
do
  sleep 1
done
sleep 5

sh $MODDIR/post-exec.sh
