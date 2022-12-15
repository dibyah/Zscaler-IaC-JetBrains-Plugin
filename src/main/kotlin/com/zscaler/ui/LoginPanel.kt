package com.zscaler.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.zscaler.actions.AuthAction
import com.zscaler.utils.createGridRowCol
import java.awt.Insets
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel

class LoginPanel(project: Project): JPanel() {

    init {
        layout = GridLayoutManager(3, 1, Insets(0, 0, 0, 0), -1, -1)
        add(JLabel("Zscanner Plugin would scan your infrastructure as code files."), createGridRowCol(0,0, GridConstraints.ANCHOR_CENTER))
        add(JLabel("Please Login to start getting results."), createGridRowCol(1,0, GridConstraints.ANCHOR_CENTER))
        val loginButton = JButton("Login")

        loginButton.addActionListener {
            ApplicationManager.getApplication().invokeLater {
                AuthAction.authenticate()
            }
        }

        add(loginButton, createGridRowCol(2, 0, GridConstraints.ANCHOR_CENTER))
    }
}