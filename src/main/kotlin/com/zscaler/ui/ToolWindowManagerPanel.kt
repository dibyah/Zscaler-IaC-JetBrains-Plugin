package com.zscaler.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.JBSplitter
import com.zscaler.listeners.InstallerListener
import com.zscaler.services.InstallerService
import com.zscaler.settings.SettingState
import com.zscaler.listeners.SettingsListener
import com.zscaler.services.ScanService
import com.zscaler.utils.PANELTYPE
import javax.swing.SwingUtilities

@Service
class ToolWindowManagerPanel(val project: Project): SimpleToolWindowPanel(false, true), Disposable {
    val description = ToolWindowDescriptionPanel(project)
    val split = JBSplitter()

    init {
        loadMainPanel()
    }

    fun loadMainPanel(panelType: Int = PANELTYPE.AUTO_CHOOSE_PANEL, fileName: String = "") {
        removeAll()
        when(panelType) {
            PANELTYPE.DO_NOTHING -> {
            }
            PANELTYPE.INSTALATION_STARTED -> {
               add(description.installationDescription())
            }
            PANELTYPE.AUTO_CHOOSE_PANEL -> {
                val setting = SettingState().getInstance()
                when {
                    setting?.apiKey.isNullOrEmpty() -> add(description.configurationDescription())
                    else -> add(description.preScanDescription())
                }
            }
            PANELTYPE.SCAN_FINISHED -> {
                val left = description.emptyDescription()
                val right = description.installationDescription()
                split.firstComponent = left
                split.secondComponent = right
                add(split)
            }
        }

        revalidate()
    }

    fun subscribeToProjectEventChanges() {
        val extensionList = listOf("tf","yaml", "yml", "json")

        if (SwingUtilities.isEventDispatchThread()) {
            project.service<ToolWindowManagerPanel>().loadMainPanel()
        } else {
            ApplicationManager.getApplication().invokeLater {
                project.service<ToolWindowManagerPanel>().loadMainPanel()
            }
        }

        project.messageBus.connect(project).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object :
            FileEditorManagerListener {
            override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                super.fileOpened(source, file)
                if (extensionList.contains(file.extension)) {
                    project.service<ScanService>().scanFile(file.path, project)
                }
            }
        })
    }


    fun subscribeToInternalEvents(project: Project) {

        project.messageBus.connect(this)
            .subscribe(InstallerListener.TOPIC, object : InstallerListener {
                override fun installerFinished() {
                    project.service<ToolWindowManagerPanel>().subscribeToProjectEventChanges()
                }
        })

        project.messageBus.connect(this)
            .subscribe(SettingsListener.TOPIC, object : SettingsListener {
                override fun apiKeyChanged() {
                    project.service<ToolWindowManagerPanel>().loadMainPanel(PANELTYPE.INSTALATION_STARTED)
                    project.service<InstallerService>().install(project)
                }
                override fun loggedOut() {
                    project.service<InstallerService>().deleteBinary()
                    project.service<ToolWindowManagerPanel>().loadMainPanel()
                }
        })
    }

    override fun dispose() = Unit
}