package com.zscaler.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.zscaler.services.AuthRequest
import com.zscaler.services.AuthService
import com.zscaler.settings.SettingState
import java.io.IOException

class AuthAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        authenticate()
    }

    companion object {
        fun authenticate() {
            var response = service<AuthService>().authorize(AuthRequest())
            try {
                var responseData = response.get()
                var settings = SettingState().getInstance()
                settings?.setApiKeyNotifying(responseData.accessToken)
            } catch (ex : IOException) {
                Logger.getInstance(AuthAction::class.java).warn("Error authenticating with zscaler server", ex)
            }
        }
    }
}