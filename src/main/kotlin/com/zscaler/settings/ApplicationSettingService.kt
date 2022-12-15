package com.zscaler.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import org.jetbrains.annotations.NotNull

@State(name = "zscanner-project", storages = [Storage("zscanner.xml")])
class ApplicationSettingService: PersistentStateComponent<ApplicationSettings> {

    private var state = ApplicationSettings()

    companion object {
        fun getInstance(): ApplicationSettings {
            return ApplicationManager.getApplication().getService(ApplicationSettingService::class.java).getState()!!
        }
    }

    @NotNull
    override fun getState(): ApplicationSettings? {
        return state
    }

    override fun loadState(@NotNull state: ApplicationSettings) {
        this.state = state
    }
}