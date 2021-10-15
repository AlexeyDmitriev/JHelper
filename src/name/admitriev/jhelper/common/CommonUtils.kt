@file:JvmName("CommonUtils")

package name.admitriev.jhelper.common

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets


object CommonUtils {

    @JvmStatic fun getStringFromInputStream(stream: InputStream): String {
        BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { reader ->
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line).append('\n')
            }
            return sb.toString()
        }
    }
}