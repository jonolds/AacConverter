@file:Suppress("UnnecessaryVariable")

package com.jonolds

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStream
import kotlin.jvm.optionals.getOrNull


object ProcessRunner {

	fun run(command: List<String>, os: OutputStream): Int {

		Log.d("\ncommand=\n\t${command.joinForConsole()}\n")

		val processBuilder = ProcessBuilder(command.joinForProcessBuilder())
			.redirectInput(ProcessBuilder.Redirect.INHERIT)
			.redirectErrorStream(true)

		val process = processBuilder.start()

		process.inputStream.transferTo(os)

		val exitCode = process.waitFor()

//		os.close()

		return exitCode
	}

	fun run(command: List<String>, log: File? = null): Int {

		Log.d("\ncommand=\n\t${command.joinForConsole()}\n")

		var processBuilder = ProcessBuilder(command.joinForProcessBuilder())
//			.inheritIO()
//			.redirectErrorStream(true)
//			.redirectOutput(ProcessBuilder.Redirect.PIPE)

		processBuilder = if (log != null) processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(log)).redirectErrorStream(true)
		else processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD).redirectError(ProcessBuilder.Redirect.DISCARD)

		val process = processBuilder.start()

//		if (log != null) {
//			val ps = Special(log)
//			process.errorStream.transferTo(ps)
//
//			ps.close()
//		}
//		else {
//			process.errorStream.transferTo(System.out)
//		}


		return process.waitFor()
	}

	fun run(vararg command: String, log: File? = null): Int = run(command.toList(), log)



	private val cmdPartsRegex = Regex("([^\\s\"]+|\"[^\"]*\")+")

	private fun List<String>.joinForProcessBuilder(): List<String> = flatMap { cmdPartsRegex.listOfMatches(it) }

	private fun List<String>.joinForConsole(): String = joinToString(" ^\n") { it.trim() }


	suspend fun execForSingleResult(command: String): String? = withContext(Dispatchers.IO) {

		val p = Runtime.getRuntime().exec(command)

		val result: String? = p.inputReader()
			.lines()
			.reduce { t, u -> u ?: t }
			.getOrNull()

		p.waitFor()
		result
	}

	suspend fun execForMultiResult(command: String): List<String>? = withContext(Dispatchers.IO) {

		val p = Runtime.getRuntime().exec(command)

		val result: List<String>? = p.inputReader()
			.lines().filter { it != null }.toList()

		p.waitFor()
		result
	}



}

