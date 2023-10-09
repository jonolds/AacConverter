package com.jonolds

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader


@OptIn(ExperimentalCoroutinesApi::class)
val logDispatcher = Dispatchers.IO.limitedParallelism(1)

class LogStream(
	val streamId: String,
	val file: File,
	val isOpen: () -> Boolean = { true }
) {
	val br = BufferedReader(InputStreamReader(FileInputStream(file)))
}

class LogWriter(
	val sessionId: String
)  {

	private val logs = ArrayList<LogStream>()

	var isOpen = true

	suspend fun update() = withContext(logDispatcher) {
		for (log in logs) {
			var line = log.br.readLine()
			while (line != null) {
				/* Do something */
				line = log.br.readLine()
			}
		}

		logs.removeIf { !it.isOpen() }
	}

	private suspend fun run() = withContext(Dispatchers.IO) {
		while (isOpen) {
			update()
			delay(500)
		}
		update()
	}

	suspend fun addLogStream(streamId: String, file: File) = withContext(logDispatcher) {
		logs.add(LogStream(streamId, file))
	}


}