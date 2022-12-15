package com.zscaler.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.zscaler.listeners.SettingsListener
import java.util.*

@State(
    name = "com.zscaler.settings.SettingState",
    storages = arrayOf(Storage("SettingsState.xml"))
)
class SettingState(): PersistentStateComponent<SettingState> {
    var apiKey: String = ""
    var expiresIn: Long = 0

    fun getInstance(): SettingState? {
        return ApplicationManager.getApplication().getService(SettingState::class.java)
    }

    override fun getState(): SettingState = this

    override fun loadState(state: SettingState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    fun setApiKeyNotifying(apiKey: String) {
        var changed = !Objects.equals(this.apiKey, apiKey)
        this.apiKey = apiKey

        if (changed) {
            settingsPublisher().apiKeyChanged()
        }
    }

    fun nullifyApiKeyNotifying() {
        this.apiKey = ""
        settingsPublisher().loggedOut()
    }

    companion object {
        fun settingsPublisher(): SettingsListener {
            return ApplicationManager.getApplication().messageBus.syncPublisher(SettingsListener.TOPIC)
        }
    }
}