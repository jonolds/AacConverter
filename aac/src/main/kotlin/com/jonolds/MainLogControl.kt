@file:Suppress("MemberVisibilityCanBePrivate")

package com.jonolds

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class SystemPrint(
	private val jobs: List<AacJob>
) {

	private val filenameLen = jobs.map { it.origPath.fileName.toString() }.maxOf { it.length }
	
	fun updateOutput() {
		val sb = StringBuilder(Cursor.prevStartCmd(jobs.size))
		for (job in jobs) {
			sb.append(Cursor.forwardCmd(filenameLen))
				.append(formatPct.format(job.pctComplete).padStart(PCT_LEN, ' '))
				.append(Cursor.nextStartCmd())
		}
		print(sb.toString())
	}

	init {

		Cursor.hide()

		val filenameStrs = jobs.map { it.origPath.fileName.toString().padEnd(filenameLen, ' ') }
		for (i in jobs.indices) {
			val fileStr = filenameStrs[i]
			println(fileStr + formatPct.format(jobs[i].pctComplete).padStart(PCT_LEN, ' '))
		}
	}

	companion object {

		private const val PCT_LEN = 10

		private val formatPct = DecimalFormat.getPercentInstance()
			.also { it.maximumFractionDigits = 1 }
	}

}


object Cursor {

	fun forward(n: Int) = print("\u001B[${n}C")
	fun forwardCmd(n: Int) = "\u001B[${n}C"

	fun prevStart(n: Int = 1) = print("\u001B[${n}F")
	fun prevStartCmd(n: Int = 1) = "\u001B[${n}F"

	fun nextStart(n: Int = 1) = print("\u001B[${n}E")
	fun nextStartCmd(n: Int = 1) = "\u001B[${n}E"

	fun hide() = print("\u001B[?25l")
	fun show() = print("\u001B[?25h")
}


class MainLogControl(
	val mainJob: MainJob,
	val tasks: List<Deferred<Unit>>
) {

	val systemPrint = SystemPrint(mainJob.jobs)

	val mainLog = buildMainLog(mainJob.jobs)

	val channel = mainLog.channel

	var mainLogStartPos = mainLog.filePointer


	suspend fun start() = mainScope.async(Dispatchers.IO) {

		var count = 0
		while (tasks.any { !it.isCancelled && !it.isCompleted }) {
			delay(1000)
			systemPrint.updateOutput()
			if (count++ % 3 == 0)
				updateMainLog()
		}

		systemPrint.updateOutput()

		Cursor.show()
	}

	val jobChannels = mainJob.jobs.map { FileChannel.open(it.log.toPath(), StandardOpenOption.READ) }

	fun updateMainLog() {
		channel.position(mainLogStartPos)

		for (jobCh in jobChannels) {
			jobCh.transferTo(0, jobCh.size(), channel)
		}
		channel.force(true)
	}

	companion object {

		fun getMainLogHeader(jobs: List<AacJob>): ByteArray {
			val sb = StringBuilder("\n\n")
				.appendLine("=".repeat(100))
				.appendLine(getTimestamp().padEnd(100, '='))
				.appendLine()
				.appendLine("\tworking directory = ${config.workingDir}")
				.appendLine("\tjobs:")

			for (job in jobs)
				sb.appendLine("\t\t${job.origPath}")

			return sb.append("\n\n").toString().toByteArray()
		}

		fun buildMainLog(jobs: List<AacJob>): RandomAccessFile {
			//TODO Change to pruning log
			config.logDir.resolve("log.txt").toFile().delete()

			val file = RandomAccessFile(config.logDir.resolve("log.txt").toString(), "rw")
			file.channel.position(file.length())
			file.channel.write(ByteBuffer.wrap(getMainLogHeader(jobs)))
			file.channel.force(true)
			return file
		}


		private val datetimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss")
		private fun getTimestamp() = LocalDateTime.now()
			.format(datetimeFormatter)
			.toString()
	}

}
