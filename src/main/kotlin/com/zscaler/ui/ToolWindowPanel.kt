package com.zscaler.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel

class ToolWindowPanel(val project: Project): SimpleToolWindowPanel(false, true), Disposable {

    init {
        val main = project.service<ToolWindowManagerPanel>()
        setContent(main)
    }
    override fun dispose() = Unit
}