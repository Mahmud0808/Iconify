BASEPKGNAME="com.drdisagree.iconify"
PKGNAME="$BASEPKGNAME.debug"
LSPDDBPATH="/data/adb/lspd/config/modules_config.db"
MAGISKDBPATH="/data/adb/magisk.db"

checkAndroidVersion() {
  SDK=$(getprop ro.build.version.sdk)

  if [ "$SDK" -lt 36 ]; then
    ui_print "! Android 16 (SDK 36) or higher is required."
    ui_print "! Detected SDK level: $SDK"
    abort "! Aborting installation."
  fi
}

prepareAPK() {
  ui_print '- Unzipping APK...'
	unzip $ZIPFILE 'Iconify.apk' -d $TMPDIR/ > /dev/null
	APKPATH="$TMPDIR/Iconify.apk"
}

prepareSQL() {
	unzip $ZIPFILE sqlite3 -d $TMPDIR/ > /dev/null
	chmod +x $TMPDIR/sqlite3
	SQLITEPATH="$TMPDIR/sqlite3"
}

installAPK() {
  ui_print "- Installing Iconify.apk..."

  if pm install "$APKPATH" > /dev/null 2>&1; then
    ui_print "- Installation successful."
  else
    if pm list packages | grep -q "$PKGNAME"; then
      ui_print "! Version code / signature mismatch."
      abort "! Uninstall existing Iconify app and flash the module again."
    else
      ui_print "! Installation failed."
      abort "! Please unzip and install the APK manually."
    fi
  fi
}

launchApp() {
  sleep 1
  if pm list packages | grep -q "$PKGNAME"; then
    ui_print "- Launching Iconify..."
    am start -n $PKGNAME/$BASEPKGNAME.app.MainActivity
  fi
}

runSQL() {
	SQLRESULT=$($SQLITEPATH $DBPATH "$CMD")
}

grantRootUID() {
	DBPATH=$MAGISKDBPATH

	CMD="insert into policies (uid, package_name, policy, until, logging, notification) values ($1, '$2', 2, 0, 1, 0);" && runSQL
	CMD="insert into policies (uid, policy, until, logging, notification) values ($1, 2, 0, 1, 0);" && runSQL
	CMD="update policies set policy = 2, until = 0, logging = 1, notification = 0 where uid = $1;" && runSQL
}

grantRootPkg() {
  [ -f "$MAGISKDBPATH" ] || return

  ui_print "- Granting root access to $1..."
  UID=$(pm list packages -U $1 --user 0 | grep ":$1 " | awk -F 'uid:' '{ print $2 }' | cut -d ',' -f 1)

  grantRootUID $UID $1
}

grantRootApps() {
	grantRootPkg $PKGNAME
	grantRootPkg "com.android.systemui"
}

activateModuleLSPD() {
  if [ -f "$LSPDDBPATH" ]; then
    DBPATH=$LSPDDBPATH
    PKGPATH=$(pm path $PKGNAME | cut -d':' -f2)

    ui_print '- Trying to activate the module in LSPosed...'

    CMD="select mid from modules where module_pkg_name like \"$PKGNAME\";" && runSQL
    OLDMID=$(echo $SQLRESULT | xargs)

    if [ $(($OLDMID+0)) -gt 0 ]; then
      CMD="select mid from modules where mid = $OLDMID and apk_path like \"$PKGPATH\" and enabled = 1;" && runSQL
      REALMID=$(echo $SQLRESULT | xargs)

      if [ $(($REALMID+0)) = 0 ]; then
        CMD="delete from scope where mid = $OLDMID;" && runSQL
        CMD="delete from modules where mid = $OLDMID;" && runSQL
      fi
    fi

    CMD="insert into modules (\"module_pkg_name\", \"apk_path\", \"enabled\") values (\"$PKGNAME\",\"$PKGPATH\", 1);" && runSQL

    CMD="select mid as ss from modules where module_pkg_name = \"$PKGNAME\";" && runSQL

    NEWMID=$(echo $SQLRESULT | xargs)

    CMD="insert into scope (mid, app_pkg_name, user_id) values ($NEWMID, \"android\",0);" && runSQL
    CMD="insert into scope (mid, app_pkg_name, user_id) values ($NEWMID, \"com.android.systemui\",0);" && runSQL
    CMD="insert into scope (mid, app_pkg_name, user_id) values ($NEWMID, \"com.google.android.apps.nexuslauncher\",0);" && runSQL
    CMD="insert into scope (mid, app_pkg_name, user_id) values ($NEWMID, \"com.android.launcher3\",0);" && runSQL
    CMD="insert into scope (mid, app_pkg_name, user_id) values ($NEWMID, \"$PKGNAME\",0);" && runSQL
  else
    ui_print '! LSPosed not found!'
    ui_print '! This module will not work without LSPosed.'
    ui_print '! Please install LSPosed and reboot.'
    abort "! Aborting process."
  fi
}

finishInstallation() {
	launchApp
	ui_print ''
	ui_print ''
	ui_print '  ***************************************'
	ui_print '  *       ~INSTALLATION COMPLETE~       *'
	ui_print '  * IGNORE THE FOLLOWING ERROR MESSAGE. *'
	ui_print '  ***************************************'
	ui_print ''
	abort ''
}

checkAndroidVersion
prepareAPK
installAPK
prepareSQL
grantRootApps
activateModuleLSPD
finishInstallation
