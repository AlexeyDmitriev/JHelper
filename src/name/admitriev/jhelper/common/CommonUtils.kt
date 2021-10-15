@file:JvmName("CommonUtils")

package name.admitriev.jhelper.common

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import name.admitriev.jhelper.configuration.TaskConfiguration
import name.admitriev.jhelper.exceptions.NotificationException
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets


object CommonUtils {

    @JvmStatic
    fun getStringFromInputStream(stream: InputStream): String {
        BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { reader ->
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line).append('\n')
            }
            return sb.toString()
        }
    }

    @JvmStatic
    fun generatePSIFromTask(project: Project, taskConfiguration: TaskConfiguration): PsiFile {
        val pathToClassFile = taskConfiguration.cppPath
        val virtualFile = project.baseDir.findFileByRelativePath(pathToClassFile)
            ?: throw NotificationException("Task file not found", "Seems your task is in inconsistent state")
        return PsiManager.getInstance(project).findFile(virtualFile)
            ?: throw NotificationException("Couldn't get PSI file for input file")
    }

}