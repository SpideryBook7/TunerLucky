package com.spiderybook.tunerlucky.shizuku

import com.spiderybook.tunerlucky.IShellService
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.system.exitProcess

class ShellService : IShellService.Stub() {
    override fun runCommand(cmd: String): String {
        return try {
            val process = Runtime.getRuntime().exec(cmd)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            process.waitFor()
            output.toString().trim()
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    override fun destroy() {
        exitProcess(0)
    }
}
