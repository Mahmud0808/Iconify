package com.drdisagree.iconify.utils.overlay

import android.os.Build
import android.util.TypedValue
import com.drdisagree.iconify.common.Resources
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.utils.helper.TypedValueUtils.createComplexDimension
import com.topjohnwu.superuser.Shell

object FabricatedUtils {

    val overlayList: List<String>
        get() = Shell.cmd(
            "cmd overlay list |  grep -E '....com.android.shell:IconifyComponent' | sed -E 's/^....com.android.shell:IconifyComponent//'"
        ).exec().out

    val enabledOverlayList: List<String>
        get() = Shell.cmd(
            "cmd overlay list |  grep -E '.x..com.android.shell:IconifyComponent' | sed -E 's/^.x..com.android.shell:IconifyComponent//'"
        ).exec().out

    val disabledOverlayList: List<String>
        get() = Shell.cmd(
            "cmd overlay list |  grep -E '. ..com.android.shell:IconifyComponent' | sed -E 's/^. ..com.android.shell:IconifyComponent//'"
        ).exec().out

    fun buildAndEnableOverlay(
        target: String?,
        name: String?,
        type: String?,
        resourceName: String?,
        `val`: String?
    ) {
        require(!(target == null || name == null || type == null || resourceName == null || `val` == null)) { "One or more arguments are null\ntarget: $target\nname: $name\ntype: $type\nresourceName: $resourceName\nval: $`val`" }
        require(!(Build.VERSION.SDK_INT >= 34 && type == "dimen")) { "Android 14+ does not support dimen fabricated overlays." }

        val commands = buildCommands(target, name, type, resourceName, `val`)

        RPrefs.putBoolean("fabricated$name", true)
        RPrefs.putString("FOCMDtarget$name", target)
        RPrefs.putString("FOCMDname$name", name)
        RPrefs.putString("FOCMDtype$name", type)
        RPrefs.putString("FOCMDresourceName$name", resourceName)
        RPrefs.putString("FOCMDval$name", `val`)

        Shell.cmd(
            "mv " + Resources.MODULE_DIR + "/post-exec.sh " + Resources.MODULE_DIR + "/post-exec.txt; grep -v \"IconifyComponent" + name + "\" " + Resources.MODULE_DIR + "/post-exec.txt > " + Resources.MODULE_DIR + "/post-exec.txt.tmp && mv " + Resources.MODULE_DIR + "/post-exec.txt.tmp " + Resources.MODULE_DIR + "/post-exec.sh; rm -rf " + Resources.MODULE_DIR + "/post-exec.txt; rm -rf " + Resources.MODULE_DIR + "/post-exec.txt.tmp"
        ).submit()
        Shell.cmd(
            "echo -e \"${commands[0]}\n${commands[1]}\" >> ${Resources.MODULE_DIR}/post-exec.sh"
        ).submit()
        Shell.cmd(commands[0], commands[1]).submit()
    }

    fun buildAndEnableOverlays(vararg args: Array<Any>) {
        val commands: MutableList<String> = ArrayList()
        val module: MutableList<String> = ArrayList()

        for (arg in args) {
            require(arg.size % 5 == 0) { "Mismatch in number of arguments." }
        }

        for (arg in args) {
            val tempCommands = buildCommands(
                arg[0] as String,
                arg[1] as String,
                arg[2] as String,
                arg[3] as String,
                arg[4] as String
            )

            RPrefs.putBoolean("fabricated" + arg[1], true)
            RPrefs.putString("FOCMDtarget" + arg[1], arg[0] as String)
            RPrefs.putString("FOCMDname" + arg[1], arg[1] as String)
            RPrefs.putString("FOCMDtype" + arg[1], arg[2] as String)
            RPrefs.putString("FOCMDresourceName" + arg[1], arg[3] as String)
            RPrefs.putString("FOCMDval" + arg[1], arg[4] as String)

            module.add("mv " + Resources.MODULE_DIR + "/post-exec.sh " + Resources.MODULE_DIR + "/post-exec.txt; grep -v \"IconifyComponent" + arg[1] + "\" " + Resources.MODULE_DIR + "/post-exec.txt > " + Resources.MODULE_DIR + "/post-exec.txt.tmp && mv " + Resources.MODULE_DIR + "/post-exec.txt.tmp " + Resources.MODULE_DIR + "/post-exec.sh; rm -rf " + Resources.MODULE_DIR + "/post-exec.txt; rm -rf " + Resources.MODULE_DIR + "/post-exec.txt.tmp")
            module.add("echo -e \"${tempCommands[0]}\n${tempCommands[1]}\" >> ${Resources.MODULE_DIR}/post-exec.sh")

            commands.add(tempCommands[0])
            commands.add(tempCommands[1])
        }

        Shell.cmd(java.lang.String.join("; ", module), java.lang.String.join("; ", commands))
            .submit()
    }

    fun buildCommands(
        target: String,
        name: String,
        type: String,
        resourceName: String,
        `val`: String
    ): List<String> {
        var localTarget = target
        var localVal = `val`
        var resourceType = "0x1c"
        if (localTarget == "systemui" || localTarget == "sysui") {
            localTarget = "com.android.systemui"
        }

        when (type) {
            "color" -> resourceType = "0x1c"
            "dimen" -> resourceType = "0x05"
            "bool" -> resourceType = "0x12"
            "integer" -> resourceType = "0x10"
        }

        if (type == "dimen") {
            var valType = -1

            when {
                localVal.contains("dp") || localVal.contains("dip") -> {
                    valType = TypedValue.COMPLEX_UNIT_DIP
                    localVal = localVal.replace("dp", "").replace("dip", "")
                }

                localVal.contains("sp") -> {
                    valType = TypedValue.COMPLEX_UNIT_SP
                    localVal = localVal.replace("sp", "")
                }

                localVal.contains("px") -> {
                    valType = TypedValue.COMPLEX_UNIT_PX
                    localVal = localVal.replace("px", "")
                }

                localVal.contains("in") -> {
                    valType = TypedValue.COMPLEX_UNIT_IN
                    localVal = localVal.replace("in", "")
                }

                localVal.contains("pt") -> {
                    valType = TypedValue.COMPLEX_UNIT_PT
                    localVal = localVal.replace("pt", "")
                }

                localVal.contains("mm") -> {
                    valType = TypedValue.COMPLEX_UNIT_MM
                    localVal = localVal.replace("mm", "")
                }
            }

            localVal = createComplexDimension(localVal.toInt(), valType).toString()
        }

        val commands: MutableList<String> = ArrayList()
        commands.add("cmd overlay fabricate --target $localTarget --name IconifyComponent$name $localTarget:$type/$resourceName $resourceType $localVal")
        commands.add("cmd overlay enable --user current com.android.shell:IconifyComponent$name")

        return commands
    }

    fun disableOverlay(name: String) {
        RPrefs.putBoolean("fabricated$name", false)
        RPrefs.clearPrefs(
            "FOCMDtarget$name",
            "FOCMDname$name",
            "FOCMDtype$name",
            "FOCMDresourceName$name",
            "FOCMDval$name"
        )

        val disableCmd =
            "cmd overlay disable --user current com.android.shell:IconifyComponent$name"

        Shell.cmd(
            "mv " + Resources.MODULE_DIR + "/post-exec.sh " + Resources.MODULE_DIR + "/post-exec.txt; grep -v \"IconifyComponent" + name + "\" " + Resources.MODULE_DIR + "/post-exec.txt > " + Resources.MODULE_DIR + "/post-exec.txt.tmp && mv " + Resources.MODULE_DIR + "/post-exec.txt.tmp " + Resources.MODULE_DIR + "/post-exec.sh; rm -rf " + Resources.MODULE_DIR + "/post-exec.txt; rm -rf " + Resources.MODULE_DIR + "/post-exec.txt.tmp"
        ).submit()
        Shell.cmd(disableCmd).submit()
    }

    fun disableOverlays(vararg names: String) {
        val command = StringBuilder()

        for (name in names) {
            RPrefs.putBoolean("fabricated$name", false)
            RPrefs.clearPrefs(
                "FOCMDtarget$name",
                "FOCMDname$name",
                "FOCMDtype$name",
                "FOCMDresourceName$name",
                "FOCMDval$name"
            )

            command.append("cmd overlay disable --user current com.android.shell:IconifyComponent")
                .append(name).append("; ")

            Shell.cmd(
                "mv " + Resources.MODULE_DIR + "/post-exec.sh " + Resources.MODULE_DIR + "/post-exec.txt; grep -v \"IconifyComponent" + name + "\" " + Resources.MODULE_DIR + "/post-exec.txt > " + Resources.MODULE_DIR + "/post-exec.txt.tmp && mv " + Resources.MODULE_DIR + "/post-exec.txt.tmp " + Resources.MODULE_DIR + "/post-exec.sh; rm -rf " + Resources.MODULE_DIR + "/post-exec.txt; rm -rf " + Resources.MODULE_DIR + "/post-exec.txt.tmp"
            ).submit()
        }

        Shell.cmd(command.toString().trim { it <= ' ' }).submit()
    }

    fun isOverlayEnabled(name: String): Boolean {
        return Shell.cmd(
            "[[ $(cmd overlay list | grep -o '\\[x\\] com.android.shell:IconifyComponent$name') ]] && echo 1 || echo 0"
        ).exec().out[0] == "1"
    }

    fun isOverlayDisabled(name: String): Boolean {
        return Shell.cmd(
            "[[ $(cmd overlay list | grep -o '\\[ \\] com.android.shell:IconifyComponent$name') ]] && echo 1 || echo 0"
        ).exec().out[0] == "1"
    }
}
