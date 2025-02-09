package com.github.cvzi.screenshottile.utils

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Point
import androidx.preference.PreferenceManager
import com.github.cvzi.screenshottile.R

/**
 * Created by ipcjs on 2017/8/17.
 * Changes by cuzi (cuzi@openmail.cc)
 */

class PrefManager(private val context: Context, private val pref: SharedPreferences) {

    constructor(context: Context) : this(
        context,
        PreferenceManager.getDefaultSharedPreferences(context)
    )

    var delay: Int
        get() = pref.getString(
            context.getString(R.string.pref_key_delay),
            context.getString(R.string.setting_delay_value_default)
        )?.filter {
            it.isDigit()
        }?.toIntOrNull() ?: 0
        set(value) = pref.edit().putString(
            context.getString(R.string.pref_key_delay),
            value.toString()
        ).apply()

    var showCountDown: Boolean
        get() = pref.getBoolean(context.getString(R.string.pref_key_show_count_down), true)
        set(value) = pref.edit().putBoolean(
            context.getString(R.string.pref_key_show_count_down),
            value
        ).apply()

    var useNative: Boolean
        get() = pref.getBoolean(context.getString(R.string.pref_key_use_native), false)
        set(value) = pref.edit().putBoolean(
            context.getString(R.string.pref_key_use_native),
            value
        ).apply()

    var useSystemDefaults: Boolean
        get() = pref.getBoolean(context.getString(R.string.pref_key_use_system_defaults), true)
        set(value) = pref.edit().putBoolean(
            context.getString(R.string.pref_key_use_system_defaults),
            value
        ).apply()

    var floatingButton: Boolean
        get() = pref.getBoolean(context.getString(R.string.pref_key_floating_button), false)
        set(value) = pref.edit().putBoolean(
            context.getString(R.string.pref_key_floating_button),
            value
        ).apply()

    var hideApp: Boolean
        get() = pref.getBoolean(context.getString(R.string.pref_key_hide_app), false)
        set(value) = pref.edit().putBoolean(
            context.getString(R.string.pref_key_hide_app),
            value
        ).apply()

    var fileFormat: String
        get() = pref.getString(
            context.getString(R.string.pref_key_file_format),
            context.getString(R.string.setting_file_format_value_default)
        ) ?: context.getString(R.string.setting_file_format_value_default)
        set(value) = pref.edit().putString(
            context.getString(R.string.pref_key_file_format),
            value
        ).apply()

    var broadcastSecret: String
        get() = pref.getString(
            context.getString(R.string.pref_key_broadcast_secret),
            context.getString(R.string.setting_broadcast_secret_value_default)
        ) ?: context.getString(R.string.setting_broadcast_secret_value_default)
        set(value) = pref.edit().putString(
            context.getString(R.string.pref_key_broadcast_secret),
            if (value.trim().isEmpty()) {
                context.getString(R.string.setting_broadcast_secret_value_default)
            } else {
                value
            }
        ).apply()

    var screenshotDirectory: String?
        get() {
            val defaultValue = context.getString(R.string.setting_storage_directory_value_default)
            val value = pref.getString(
                context.getString(R.string.pref_key_storage_directory),
                null
            )
            return if (value != defaultValue) {
                value
            } else {
                null
            }
        }
        set(value) = pref.edit().putString(
            context.getString(R.string.pref_key_storage_directory),
            if (value.isNullOrBlank()) {
                context.getString(R.string.setting_storage_directory_value_default)
            } else {
                value
            }
        ).apply()

    var fileNamePattern: String
        get() {
            val defaultValue = context.getString(R.string.setting_file_name_pattern_value_default)
            val value = pref.getString(
                context.getString(R.string.pref_key_file_name_pattern),
                defaultValue
            )
            return if (value.isNullOrBlank()) {
                this.fileNamePattern = defaultValue
                defaultValue
            } else {
                value
            }
        }
        set(value) = pref.edit().putString(
            context.getString(R.string.pref_key_file_name_pattern),
            value.ifBlank {
                context.getString(R.string.setting_file_name_pattern_value_default)
            }
        ).apply()

    var screenshotCount: Int
        get() = pref.getString(
            context.getString(R.string.pref_key_screenshot_count),
            "0"
        )?.toIntOrNull() ?: 0
        set(value) = pref.edit().putString(
            context.getString(R.string.pref_key_screenshot_count),
            value.toString()
        ).apply()

    var returnIfAccessibilityServiceEnabled: String?
        get() = pref.getString(
            context.getString(R.string.pref_key_return_if_accessibility),
            null
        )
        set(value) = pref.edit().putString(
            context.getString(R.string.pref_key_return_if_accessibility),
            value
        ).apply()

    var returnIfVoiceInteractionServiceEnabled: String?
        get() = pref.getString(
            context.getString(R.string.pref_key_return_if_voice_interaction),
            null
        )
        set(value) = pref.edit().putString(
            context.getString(R.string.pref_key_return_if_voice_interaction),
            value
        ).apply()

    var floatingButtonHideAfter: Boolean
        get() = pref.getBoolean(
            context.getString(R.string.pref_key_floating_button_hide_after),
            false
        )
        set(value) = pref.edit().putBoolean(
            context.getString(R.string.pref_key_floating_button_hide_after),
            value
        ).apply()

    var floatingButtonShowClose: Boolean
        get() = pref.getBoolean(
            context.getString(R.string.pref_key_floating_button_show_close),
            false
        )
        set(value) = pref.edit().putBoolean(
            context.getString(R.string.pref_key_floating_button_show_close),
            value
        ).apply()

    var floatingButtonCloseEmoji: String
        get() = pref.getString(
            context.getString(R.string.pref_key_floating_button_close_emoji),
            context.getString(R.string.close_buttons_default)
        ) ?: context.getString(R.string.close_buttons_default)
        set(value) = pref.edit().putString(
            context.getString(R.string.pref_key_floating_button_close_emoji),
            value
        ).apply()

    var floatingButtonScale: Int
        get() {
            val d = pref.getString(
                context.getString(R.string.pref_key_floating_button_scale),
                "200"
            )?.filter {
                it.isDigit()
            }?.toIntOrNull() ?: 200
            return if (d > 0) d else 200
        }
        set(value) = pref.edit().putString(
            context.getString(R.string.pref_key_floating_button_scale),
            value.toString()
        ).apply()

    var floatingButtonAlpha: Float
        get() {
            val s = pref.getString(
                context.getString(R.string.pref_key_floating_button_alpha),
                context.getString(R.string.pref_floating_button_alpha_default)
            )?.replaceFirst(',', '.')?.filter {
                it.isDigit() || it == '.'
            }
            val f = s?.toFloatOrNull() ?: 1f
            return f.coerceIn(0f, 1f)
        }
        set(value) = pref.edit().putString(
            context.getString(R.string.pref_key_floating_button_alpha),
            value.coerceIn(0f, 1f).toString()
        ).apply()

    var floatingButtonPosition: Point
        get() {
            val s = pref.getString(
                context.getString(R.string.pref_key_floating_button_position),
                "0,0"
            ) ?: "0,0"
            val parts = s.split(",")
            if (parts.size != 2) {
                return Point(0, 0)
            }
            val x = parts[0].toIntOrNull() ?: 0
            val y = parts[1].toIntOrNull() ?: 0
            return Point(x, y)
        }
        set(point) = pref.edit().putString(
            context.getString(R.string.pref_key_floating_button_position),
            "${point.x},${point.y}"
        ).apply()

    var floatingButtonShutter: Int
        get() = pref.getString(
            context.getString(R.string.pref_key_floating_button_shutter),
            "0"
        )?.filter {
            it.isDigit()
        }?.toIntOrNull() ?: 0
        set(value) = pref.edit().putString(
            context.getString(R.string.pref_key_floating_button_shutter),
            value.toString()
        ).apply()

    var floatingButtonDelay: Int
        get() = pref.getString(
            context.getString(R.string.pref_key_floating_button_delay),
            context.getString(R.string.setting_delay_value_default)
        )?.filter {
            it.isDigit()
        }?.toIntOrNull() ?: 0
        set(value) = pref.edit().putString(
            context.getString(R.string.pref_key_floating_button_delay),
            value.toString()
        ).apply()

    var floatingButtonAction: String
        get() = pref.getString(
            context.getString(R.string.pref_key_floating_action),
            context.getString(R.string.setting_floating_action_value_screenshot)
        ) ?: context.getString(R.string.setting_floating_action_value_screenshot)
        set(value) = pref.edit().putString(
            context.getString(R.string.pref_key_floating_action),
            value
        ).apply()

    var voiceInteractionAction: String
        get() = pref.getString(
            context.getString(R.string.pref_key_voice_interaction_action),
            context.getString(R.string.setting_voice_interaction_action_value_provided)
        ) ?: context.getString(R.string.setting_voice_interaction_action_value_provided)
        set(value) = pref.edit().putString(
            context.getString(R.string.pref_key_voice_interaction_action),
            value
        ).apply()

    var tileAction: String
        get() = pref.getString(
            context.getString(R.string.pref_key_tile_action),
            context.getString(R.string.setting_tile_action_value_screenshot)
        ) ?: context.getString(R.string.setting_tile_action_value_screenshot)
        set(value) = pref.edit().putString(
            context.getString(R.string.pref_key_tile_action),
            value
        ).apply()

    var darkTheme: String
        get() = pref.getString(
            context.getString(R.string.pref_key_dark_theme),
            context.getString(R.string.setting_dark_theme_value_default)
        ) ?: context.getString(R.string.setting_dark_theme_value_default)
        set(value) = pref.edit().putString(
            context.getString(R.string.pref_key_dark_theme),
            value
        ).apply()

    var toasts: Boolean
        get() = pref.getBoolean(context.getString(R.string.pref_key_toasts), true)
        set(value) = pref.edit().putBoolean(
            context.getString(R.string.pref_key_toasts),
            value
        ).apply()

    var naggingToasts: Boolean
        get() = pref.getBoolean(context.getString(R.string.pref_key_nagging_toasts), true)
        set(value) = pref.edit().putBoolean(
            context.getString(R.string.pref_key_nagging_toasts),
            value
        ).apply()

    var successToasts: Boolean
        get() = pref.getBoolean(context.getString(R.string.pref_key_success_toasts), true)
        set(value) = pref.edit().putBoolean(
            context.getString(R.string.pref_key_success_toasts),
            value
        ).apply()

    var errorToasts: Boolean
        get() = pref.getBoolean(context.getString(R.string.pref_key_error_toasts), true)
        set(value) = pref.edit().putBoolean(
            context.getString(R.string.pref_key_error_toasts),
            value
        ).apply()

    var selectAreaShutterDelay: Long
        get() {
            val d = pref.getString(
                context.getString(R.string.pref_key_select_area_shutter_delay),
                context.getString(R.string.pref_select_area_shutter_delay_default)
            )?.toLongOrNull() ?: 0
            return if (d >= 0) d else 0
        }
        set(value) = pref.edit().putString(
            context.getString(R.string.pref_key_select_area_shutter_delay),
            value.toString()
        ).apply()

    var originalAfterPermissionDelay: Long
        get() {
            val d = pref.getString(
                context.getString(R.string.pref_key_original_after_permission_delay),
                context.getString(R.string.pref_original_after_permission_delay_default)
            )?.toLongOrNull() ?: 300
            return if (d >= 0) d else 0
        }
        set(value) = pref.edit().putString(
            context.getString(R.string.pref_key_original_after_permission_delay),
            value.toString()
        ).apply()

    var failedVirtualDisplayDelay: Long
        get() {
            val d = pref.getString(
                context.getString(R.string.pref_key_failed_virtual_display_delay),
                context.getString(R.string.pref_failed_virtual_display_delay_default)
            )?.toLongOrNull() ?: 0
            return if (d >= 0) d else 0
        }
        set(value) = pref.edit().putString(
            context.getString(R.string.pref_key_failed_virtual_display_delay),
            value.toString()
        ).apply()


    private var fileNamesRecent: Array<String>
        get() {
            return pref.getString(
                context.getString(R.string.pref_key_file_names_recent),
                ""
            )?.split("\n")?.filter { it.isNotBlank() }?.toTypedArray() ?: arrayOf()
        }
        set(value) = pref.edit().putString(
            context.getString(R.string.pref_key_file_names_recent),
            value.joinToString("\n")
        ).apply()

    private fun cleanFileName(name: String): String {
        return name.removeSuffix(".png")
            .removeSuffix(".gif")
            .removeSuffix(".jpg")
            .removeSuffix(".jpeg")
            .removeSuffix(".webp")
            .replace("\n", "")
    }

    fun addRecentFileName(name: String) {
        val cleanName = cleanFileName(name)
        fileNamesRecent = fileNamesRecent.toMutableList().apply {
            minus(cleanName)
            add(0, cleanName)
        }.toTypedArray()
    }

    private fun removeRecentFileName(index: Int) {
        fileNamesRecent = fileNamesRecent.toMutableList().apply {
            if (index < size) {
                removeAt(index)
            }
        }.toTypedArray()
    }

    private var fileNamesStarred: Array<String>
        get() {
            return pref.getString(
                context.getString(R.string.pref_key_file_names_starred),
                ""
            )?.split("\n")?.filter { it.isNotBlank() }?.toTypedArray() ?: arrayOf()
        }
        set(value) = pref.edit().putString(
            context.getString(R.string.pref_key_file_names_starred),
            value.joinToString("\n")
        ).apply()

    fun addStarredFileName(name: String) {
        val cleanName = cleanFileName(name)
        if (cleanName !in fileNamesStarred) {
            fileNamesStarred += cleanName
        }
    }

    private fun removeStarredFileName(index: Int) {
        fileNamesStarred = fileNamesStarred.toMutableList().apply {
            if (index < size) {
                removeAt(index)
            }
        }.toTypedArray()
    }

    fun getFileNameSuggestions(): Array<FileNameSuggestion> {
        return (fileNamesStarred.mapIndexed { index, value ->
            FileNameSuggestion(value, true, index)
        } + fileNamesRecent.mapIndexed { index, value ->
            FileNameSuggestion(value, false, index)
        }).toTypedArray()
    }

    fun removeFileName(fileNameSuggestion: FileNameSuggestion) {
        return if (fileNameSuggestion.starred) {
            removeStarredFileName(fileNameSuggestion.dataIndex)
        } else {
            removeRecentFileName(fileNameSuggestion.dataIndex)
        }
    }

    var notificationActions: MutableSet<String>
        get() = pref.getStringSet(
            context.getString(R.string.pref_key_notification_actions),
            context.resources.getStringArray(R.array.setting_notification_actions_default_values)
                .toMutableSet()
        ) ?: arrayOf(
            context.getString(R.string.setting_notification_action_share),
            context.getString(R.string.setting_notification_action_edit),
            context.getString(R.string.setting_notification_action_delete)
        ).toMutableSet()
        set(value) = pref.edit()
            .putStringSet(context.getString(R.string.pref_key_notification_actions), value).apply()
}
