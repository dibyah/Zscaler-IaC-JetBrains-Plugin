package com.zscaler.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.Sets
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.zscaler.listeners.InstallerListener
import com.zscaler.settings.SettingState
import com.zscaler.utils.API_URL
import com.zscaler.utils.AUTH_URL
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.SystemUtils
import java.io.BufferedInputStream
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import java.time.format.DateTimeFormatter


private val LOG = logger<InstallerService>()
val binaryLoc = PathManager.getPluginTempPath() + File.separator + "zpc"

@Service
class InstallerService() {

    fun install(project: Project) {
        LOG.info("Binary Location: $binaryLoc")
        val zscanner : Path = Paths.get(binaryLoc, "zscanner")
        val doesZscannerExists: Boolean = Files.exists(zscanner)
        if (!doesZscannerExists) {
            try {
                downloadScanner()
            } catch (ex: IOException) {
                LOG.error("Failed to download zscanner due to - " + ex.message)
                // Delete the scanner binary if exists
                deleteBinary()
            }
        }

        try {
            initScanner(project)
        } catch (ex: IOException) {
            LOG.error("Failed to initialise the scanner" + ex.message)
        }
    }

    @Throws(IOException::class)
    private fun initScanner(project: Project) {
        LOG.info("Initialising Zscaler IaC scanner")
        storeCliConfig("access_token", SettingState().getInstance()?.apiKey.toString())
        //val expiresIn: Long? = SettingState().getInstance()!!.expiresIn + System.currentTimeMillis()/1000
        //storeCliConfig("expires_at", expiresIn.toString())

        val scannerConfig = ScannerConfig()
        val authConfig = ScannerConfig.AuthConfig()
        authConfig?.host = AUTH_URL
        authConfig?.scope = "offline_access"
        authConfig?.audience = "https://api.zscwp.io/iac"
        scannerConfig.authConfig = authConfig
        scannerConfig.host = API_URL
        scannerConfig.appHost = "https://main.dev.app.zscwp.io"
        val objectMapper = ObjectMapper()
        storeCliConfig("custom_region", objectMapper.writeValueAsString(scannerConfig))
        storeCliConfig("region", "CUSTOM")
        showConfig()
        LOG.info("Zscanner installed successfully")
        project.messageBus.syncPublisher(InstallerListener.TOPIC).installerFinished()
    }

    private fun storeCliConfig(key:String, value: String) {
        val processBuilder = ProcessBuilder()
        val command: ArrayList<String> = arrayListOf(
            "./zscanner",
            "config",
            "add",
            "-m",
            "intellij",
            "-k",
            key,
            "-v",
            value
        )

        LOG.info("Command :: $command\nBinaryLoc :: $binaryLoc")

        val exec: Process = processBuilder.command(command).directory(File(binaryLoc)).start()
        exec.errorStream.use { errorStream ->
            exec.inputStream.use { resultStream ->
                if (errorStream.available() > 0) {
                    LOG.info(IOUtils.toString(errorStream, Charset.defaultCharset()))
                }
                LOG.info(IOUtils.toString(resultStream, Charset.defaultCharset()))
                exec.destroy()
            }
        }
    }

    private fun showConfig() {
        val processBuilder = ProcessBuilder()
        val command: ArrayList<String> = arrayListOf(
            "./zscanner",
            "config",
            "-m",
            "intellij",
            "list",
            "-a"
        )

        LOG.info("Command :: $command\nBinaryLoc :: $binaryLoc")

        val exec: Process = processBuilder.command(command).directory(File(binaryLoc)).start()
        exec.errorStream.use { errorStream ->
            exec.inputStream.use { resultStream ->
                if (errorStream.available() > 0) {
                    LOG.info(IOUtils.toString(errorStream, Charset.defaultCharset()))
                }
                LOG.info(IOUtils.toString(resultStream, Charset.defaultCharset()))
                exec.destroy()
            }
        }
    }

     fun deleteBinary() {
        val zscannerPath = Paths.get(binaryLoc, "zscanner")
        if (zscannerPath.toFile().exists()) {
            try {
                FileUtils.forceDelete(zscannerPath.toFile())
            } catch (e: IOException) {
                LOG.error("Failed to delete zscanner binary due to - " + e.message)
            }
        }
         LOG.info("binary deleted successfully")
    }

    @Throws(IOException::class)
    private fun downloadScanner() {
        LOG.info("Downloading Zscaler IaC scanner")
        var body = "{\"platform\": \"%s\",\"arch\":\"%s\"}"

        var osArch = SystemUtils.OS_ARCH
        if (osArch == "amd64") {
            osArch = "x86_64"
        } else if (osArch == "aarch64") {
            osArch = "arm64"
        }
        var osName = SystemUtils.OS_NAME
        if (osName.lowercase().contains("mac")) {
            osName = "Darwin"
        }
        body = String.format(body!!, osName, osArch)

        val cwpService: CWPService = CWPClient.getClient()?.create(CWPService::class.java)!!

        val download = cwpService.downloadScanner(body)!!.execute()
        val scannerCompressedFilePath: String = binaryLoc + File.separator + "zscanner.tar.gz"
        try {
            val scannerFile = File(scannerCompressedFilePath)
            scannerFile.parentFile.mkdirs()
            val isCreated = scannerFile.createNewFile()
            if (isCreated) {
                if (download.body() != null) {
                    FileUtils.copyInputStreamToFile(download.body()!!.byteStream(), scannerFile)
                } else {
                    LOG.warn("Failed to download scanner")
                }
            }
            decompressTarGzipFile(Paths.get(scannerCompressedFilePath), Paths.get(binaryLoc))
        } catch (e: Exception) {
            LOG.info(e)
        } finally {
            Files.delete(Paths.get(scannerCompressedFilePath))
        }

        createBaseDir()
    }

    @Throws(IOException::class)
    private fun decompressTarGzipFile(source: Path, target: Path) {
        if (Files.notExists(source)) {
            throw IOException("File doesn't exists!")
        }

        Files.newInputStream(source).use { fi ->
            BufferedInputStream(fi).use { bi ->
                GzipCompressorInputStream(bi).use { gzi ->
                    TarArchiveInputStream(gzi).use { ti ->
                        var entry: ArchiveEntry
                        while (ti.nextEntry.also { entry = it } != null) {
                            val newPath: Path? =
                                zipSlipProtect(entry, target)
                            if (entry.isDirectory) {
                                Files.createDirectories(newPath)
                            } else {
                                val parent = newPath?.parent
                                if (parent != null) {
                                    if (Files.notExists(parent)) {
                                        Files.createDirectories(parent)
                                    }
                                }
                                Files.copy(ti, newPath, StandardCopyOption.REPLACE_EXISTING)
                                Files.setPosixFilePermissions(
                                    newPath,
                                    Sets.newHashSet(
                                        PosixFilePermission.OWNER_READ,
                                        PosixFilePermission.OWNER_EXECUTE
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun zipSlipProtect(entry: ArchiveEntry, targetDir: Path): Path? {
        val targetDirResolved = targetDir.resolve(entry.name)
        val normalizePath = targetDirResolved.normalize()
        if (!normalizePath.startsWith(targetDir)) {
            throw IOException("Bad entry: " + entry.name)
        }
        return normalizePath
    }

    @Throws(IOException::class)
    private fun createBaseDir() {
        val baseDir = Paths.get(binaryLoc, "zscaler")
        if (!baseDir.toFile().exists()) {
            Files.createDirectory(
                baseDir,
                PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr--r--"))
            )
        }
    }
}