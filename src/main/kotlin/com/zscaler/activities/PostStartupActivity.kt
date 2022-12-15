package com.zscaler.activities

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.zscaler.ui.ToolWindowManagerPanel


private val LOG = logger<PostStartupActivity>()

class PostStartupActivity: StartupActivity {
    override fun runActivity(project: Project) {
        LOG.info("Startup activity starting")
        project.service<ToolWindowManagerPanel>().subscribeToInternalEvents(project)
        LOG.info("Startup activity finished")
    }
}
