package com.zscaler.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.IconLoader
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import java.awt.Insets
import javax.swing.JLabel
import javax.swing.JPanel
import com.zscaler.utils.*
import java.awt.BorderLayout

class ToolWindowDescriptionPanel(val project: Project): SimpleToolWindowPanel(true, true) {

    var descriptionPanel: JPanel = JPanel()

    init {
        configurationDescription()
    }

    fun installationDescription(): JPanel {
        descriptionPanel = JPanel()
        descriptionPanel.layout = GridLayoutManager(2, 1, Insets(0, 0, 0, 0), -1, -1)
        val imagePanel = JPanel()
        imagePanel.add(JLabel(IconLoader.getIcon("/icons/zscaler_logo.svg")), createGridRowCol(0,0,GridConstraints.ANCHOR_NORTHEAST))
        val scanningPanel = JPanel()
        scanningPanel.add(JLabel("Zscanner is being installed"),  createGridRowCol(1,0,GridConstraints.ANCHOR_NORTH))
        scanningPanel.add(LogoutPanel(project), GridConstraints.ANCHOR_CENTER)
        descriptionPanel.add(imagePanel, createGridRowCol(0,0,GridConstraints.ANCHOR_NORTHEAST))
        descriptionPanel.add(scanningPanel, createGridRowCol(1,0,GridConstraints.ANCHOR_NORTH))
        return descriptionPanel
    }

    fun configurationDescription(): JPanel{
        descriptionPanel = JPanel()
        descriptionPanel.layout = GridLayoutManager(2, 1, Insets(0, 0, 0, 0), -1, -1)
        val imagePanel = JPanel()
        imagePanel.add(JLabel(IconLoader.getIcon("/icons/zscaler_logo.svg")))
        val configPanel = JPanel()
        configPanel.add(LoginPanel(project), GridConstraints.ANCHOR_CENTER)
        descriptionPanel.add(imagePanel, createGridRowCol(0, 0, GridConstraints.ANCHOR_NORTHEAST))
        descriptionPanel.add(configPanel, createGridRowCol(1, 0, GridConstraints.ANCHOR_NORTH))
        return descriptionPanel
    }

    fun preScanDescription(): JPanel{
        descriptionPanel = JPanel()
        descriptionPanel.layout = GridLayoutManager(2, 1, Insets(0, 0, 0, 0), -1, -1)
        val imagePanel = JPanel()
        imagePanel.add(JLabel(IconLoader.getIcon("/icons/zscaler_logo.svg")), createGridRowCol(0, 0, GridConstraints.ANCHOR_NORTHEAST))
        val scanningPanel = JPanel()
        //scanningPanel.layout = GridLayoutManager(2, 1, Insets(0, 0, 0, 0), -1, -1)
        scanningPanel.add(JLabel("Scanning would start automatically once an IaC file is opened or saved"), createGridRowCol(1,0,GridConstraints.ANCHOR_NORTH))
        scanningPanel.add(LogoutPanel(project), GridConstraints.ANCHOR_CENTER)
        descriptionPanel.add(imagePanel, createGridRowCol(0,0,GridConstraints.ANCHOR_NORTHEAST))
        descriptionPanel.add(scanningPanel, createGridRowCol(1,0,GridConstraints.ANCHOR_NORTH))
        return descriptionPanel
    }

    fun emptyDescription(): JPanel {
        descriptionPanel = JPanel()
        descriptionPanel.add(JLabel(""), BorderLayout.CENTER)
        return descriptionPanel
    }
}