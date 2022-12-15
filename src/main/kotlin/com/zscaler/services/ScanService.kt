package com.zscaler.services

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.zscaler.listeners.SettingsListener
import com.zscaler.settings.SettingState
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.nio.charset.Charset

private val LOG = logger<ScanService>()

@Service
class ScanService {
    private val settings = SettingState().getInstance()
    private var currentFile = ""

    fun scanFile(filePath: String, project: Project) {
        val accessToken = settings?.apiKey
        if (accessToken.isNullOrEmpty()) {
            project.messageBus.syncPublisher(SettingsListener.TOPIC).apiKeyChanged()
            LOG.warn("Wasn't able to get access token")
            return
        }

        currentFile = filePath
        val command: ArrayList<String> = getExecCommand(filePath, accessToken)
        LOG.info("Command :: $command\nBinaryLoc :: $binaryLoc")
        val exec: Process = ProcessBuilder().command(command).directory(File(binaryLoc)).start()
        exec.errorStream.use { errorStream ->
            exec.inputStream.use { resultStream ->
                if (errorStream.available() > 0) {
                    LOG.info("SCAN ERRORS "+IOUtils.toString(errorStream, Charset.defaultCharset()))
                }
                LOG.info("SCAN RESULTS: "+IOUtils.toString(resultStream, Charset.defaultCharset()))
                exec.destroy()
            }
        }
    }

    private fun getExecCommand(filePath: String, apiToken: String): ArrayList<String> {
        val relevantFilePath = FilenameUtils.separatorsToSystem(filePath)
        return arrayListOf("./zscanner", "-m", "intellij", "scan", "-f", relevantFilePath)
    }
}