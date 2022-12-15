package com.zscaler.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.zscaler.settings.SettingState
import com.zscaler.utils.createGridRowCol
import java.awt.Insets
import javax.swing.JButton
import javax.swing.JPanel

class LogoutPanel(project: Project): JPanel()  {
    init {
        layout = GridLayoutManager(3, 1, Insets(0, 0, 0, 0), -1, -1)
        val logoutButton = JButton("Logout")

        logoutButton.addActionListener {
            ApplicationManager.getApplication().invokeLater {
                var settings = SettingState().getInstance()
                settings?.nullifyApiKeyNotifying()
            }
        }

        add(logoutButton, createGridRowCol(2, 0, GridConstraints.ANCHOR_CENTER))
    }
}