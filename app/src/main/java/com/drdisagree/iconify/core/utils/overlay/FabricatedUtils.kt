package com.drdisagree.iconify.core.utils.overlay

import android.util.TypedValue
import com.drdisagree.iconify.app.Iconify.Companion.appContext
import com.drdisagree.iconify.core.di.ResourceManagerEntryPoint
import com.drdisagree.iconify.data.common.Resources
import com.drdisagree.iconify.data.entity.FabricatedResourceEntity
import com.drdisagree.iconify.data.repository.FabricatedResourceRepository
import com.drdisagree.iconify.helpers.TypedValueUtils.createComplexDimension
import com.topjohnwu.superuser.Shell
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Utility object for managing Fabricated Overlays on Android.
 *
 * Fabricated overlays allow for the dynamic creation of runtime resource overlays (RROs)
 * without requiring a physical APK. This utility provides methods to create, enable,
 * disable, and query the status of these overlays using root shell commands.
 *
 * It also handles persistence by saving overlay data to a local database and
 * maintaining a boot-time script (`post-exec.sh`) to ensure overlays persist across reboots.
 *
 * Note: Android 14+ has specific restrictions regarding dimension (dimen) fabricated
 * overlays, which this utility accounts for.
 */
object FabricatedUtils {

    private const val OVERLAY_NAME_PREFIX = "com.android.shell:IconifyComponent"

    data class FabricatedOverlay(
        val targetPackageName: String,
        val overlayName: String,
        val resourceType: FabricatedResourceType,
        val resourceName: String,
        val resourceValue: String,
    )

    enum class FabricatedResourceType(val typeString: String, val hex: String) {
        COLOR("color", "0x1c"),
        DIMEN("dimen", "0x05"),
        BOOL("bool", "0x12"),
        INTEGER("integer", "0x10");

        companion object {
            fun from(typeString: String): FabricatedResourceType =
                entries.find { it.typeString == typeString }
                    ?: throw IllegalArgumentException("Unknown resource type: '$typeString'")
        }
    }

    private fun getEntryPoint(): ResourceManagerEntryPoint {
        return EntryPointAccessors.fromApplication(
            appContext,
            ResourceManagerEntryPoint::class.java
        )
    }

    private fun repository(): FabricatedResourceRepository {
        return getEntryPoint().fabricatedResourceRepository()
    }

    val overlayList: List<String>
        get() = Shell.cmd(
            "cmd overlay list | grep -E '....${OVERLAY_NAME_PREFIX}' | sed -E 's/^....${OVERLAY_NAME_PREFIX}//'"
        ).exec().out

    val enabledOverlayList: List<String>
        get() = Shell.cmd(
            "cmd overlay list | grep -E '.x..${OVERLAY_NAME_PREFIX}' | sed -E 's/^.x..${OVERLAY_NAME_PREFIX}//'"
        ).exec().out

    val disabledOverlayList: List<String>
        get() = Shell.cmd(
            "cmd overlay list | grep -E '. ..${OVERLAY_NAME_PREFIX}' | sed -E 's/^. ..${OVERLAY_NAME_PREFIX}//'"
        ).exec().out

    fun buildAndEnableOverlay(overlay: FabricatedOverlay) {
        require(overlay.resourceType != FabricatedResourceType.DIMEN) {
            "Android 14+ does not support dimen fabricated overlays (overlay: ${overlay.overlayName})."
        }

        val commands = buildCommands(overlay)
        saveInDatabase(overlay)
        updateModuleScript(overlay.overlayName, commands)
        Shell.cmd(commands.first, commands.second).submit()
    }

    fun buildAndEnableOverlay(
        targetPackageName: String,
        overlayName: String,
        resourceType: String,
        resourceName: String,
        resourceValue: String,
    ) = buildAndEnableOverlay(
        FabricatedOverlay(
            targetPackageName,
            overlayName,
            FabricatedResourceType.from(resourceType),
            resourceName,
            resourceValue
        )
    )

    fun buildAndEnableOverlays(vararg overlays: FabricatedOverlay) {
        val moduleCommands = mutableListOf<String>()
        val shellCommands = mutableListOf<String>()

        for (overlay in overlays) {
            require(overlay.resourceType != FabricatedResourceType.DIMEN) {
                "Android 14+ does not support dimen fabricated overlays (overlay: ${overlay.overlayName})."
            }

            val commands = buildCommands(overlay)
            saveInDatabase(overlay)
            moduleCommands += moduleCleanupCommand(overlay.overlayName)
            moduleCommands += "echo -e \"${commands.first}\n${commands.second}\" >> ${Resources.MODULE_DIR}/post-exec.sh"
            shellCommands += commands.first
            shellCommands += commands.second
        }

        Shell.cmd(
            moduleCommands.joinToString("; "),
            shellCommands.joinToString("; ")
        ).submit()
    }

    fun disableOverlay(name: String) {
        deleteFromDatabase(name)
        Shell.cmd(
            moduleCleanupCommand(name),
            "cmd overlay disable --user current $OVERLAY_NAME_PREFIX$name"
        ).submit()
    }

    fun disableOverlays(vararg names: String) {
        val disableCommands = names.joinToString("; ") {
            deleteFromDatabase(it)
            "cmd overlay disable --user current $OVERLAY_NAME_PREFIX$it"
        }
        val cleanupCommands = names.joinToString("; ") { moduleCleanupCommand(it) }

        Shell.cmd(cleanupCommands, disableCommands).submit()
    }

    fun isOverlayEnabled(name: String): Boolean = Shell.cmd(
        $$"[[ $(cmd overlay list | grep -o '\\[x\\] $$OVERLAY_NAME_PREFIX$$name') ]] && echo 1 || echo 0"
    ).exec().out[0] == "1"

    fun isOverlayDisabled(name: String): Boolean = Shell.cmd(
        $$"[[ $(cmd overlay list | grep -o '\\[ \\] $$OVERLAY_NAME_PREFIX$$name') ]] && echo 1 || echo 0"
    ).exec().out[0] == "1"

    fun buildCommands(overlay: FabricatedOverlay): Pair<String, String> = buildCommands(
        overlay.targetPackageName,
        overlay.overlayName,
        overlay.resourceType.typeString,
        overlay.resourceName,
        overlay.resourceValue
    )

    fun buildCommands(
        targetPackageName: String,
        overlayName: String,
        resourceType: String,
        resourceName: String,
        resourceValue: String,
    ): Pair<String, String> {
        val resolvedResourceType = FabricatedResourceType.from(resourceType)
        val resolvedVal =
            if (FabricatedResourceType.from(resourceType) == FabricatedResourceType.DIMEN)
                resolveDimenValue(resourceValue)
            else
                resourceValue

        return Pair(
            "cmd overlay fabricate --target $targetPackageName --name IconifyComponent$overlayName" +
                    " $targetPackageName:${resolvedResourceType.typeString}/$resourceName ${resolvedResourceType.hex} $resolvedVal",
            "cmd overlay enable --user current $OVERLAY_NAME_PREFIX$overlayName",
        )
    }

    suspend fun getAllLatestResources(): List<FabricatedResourceEntity> =
        repository().getAllLatestResources()

    private fun resolveDimenValue(raw: String): String {
        val (unit, stripped) = when {
            raw.endsWith("dip") -> TypedValue.COMPLEX_UNIT_DIP to raw.removeSuffix("dip")
            raw.endsWith("dp") -> TypedValue.COMPLEX_UNIT_DIP to raw.removeSuffix("dp")
            raw.endsWith("sp") -> TypedValue.COMPLEX_UNIT_SP to raw.removeSuffix("sp")
            raw.endsWith("px") -> TypedValue.COMPLEX_UNIT_PX to raw.removeSuffix("px")
            raw.endsWith("in") -> TypedValue.COMPLEX_UNIT_IN to raw.removeSuffix("in")
            raw.endsWith("pt") -> TypedValue.COMPLEX_UNIT_PT to raw.removeSuffix("pt")
            raw.endsWith("mm") -> TypedValue.COMPLEX_UNIT_MM to raw.removeSuffix("mm")
            else -> -1 to raw
        }
        return createComplexDimension(stripped.toInt(), unit).toString()
    }

    private fun moduleCleanupCommand(name: String): String {
        val dir = Resources.MODULE_DIR
        return "mv $dir/post-exec.sh $dir/post-exec.txt; " +
                "grep -v \"IconifyComponent$name\" $dir/post-exec.txt > $dir/post-exec.txt.tmp && " +
                "mv $dir/post-exec.txt.tmp $dir/post-exec.sh; " +
                "rm -rf $dir/post-exec.txt; " +
                "rm -rf $dir/post-exec.txt.tmp"
    }

    private fun saveInDatabase(overlay: FabricatedOverlay) {
        CoroutineScope(Dispatchers.IO).launch {
            repository().insertResources(
                listOf(
                    FabricatedResourceEntity(
                        targetPackageName = overlay.targetPackageName,
                        overlayName = overlay.overlayName,
                        resourceType = overlay.resourceType.typeString,
                        resourceName = overlay.resourceName,
                        resourceValue = overlay.resourceValue,
                    )
                )
            )
        }
    }

    private fun deleteFromDatabase(vararg names: String) {
        CoroutineScope(Dispatchers.IO).launch {
            repository().deleteOverlays(*names)
        }
    }

    private fun updateModuleScript(name: String, commands: Pair<String, String>) {
        Shell.cmd(
            moduleCleanupCommand(name),
            "echo -e \"${commands.first}\n${commands.second}\" >> ${Resources.MODULE_DIR}/post-exec.sh"
        ).exec()
    }
}