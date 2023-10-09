@file:Suppress("ObjectPropertyName")

package com.jonolds

import kotlinx.coroutines.*
import sun.misc.Signal
import java.io.*
import java.nio.channels.FileChannel
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.random.Random
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis
import kotlin.time.DurationUnit
import kotlin.time.toDuration


var mainScope = CoroutineScope(EmptyCoroutineContext)

suspend fun main(args: Array<String>) = supervisorScope {
	mainScope = this
	handleSigint()

	withContext(mainScope.coroutineContext) {

		parseArgs(args)

		try {
			val elapsed = measureTimeMillis {
				getMainJob().start()
			}
			println(elapsed.toDuration(DurationUnit.MILLISECONDS))
		} catch (e: Exception) {
			mainScope.coroutineContext.cancelChildren(cause = CancellationException(e.message, e))
			throw e
		}
	}
}


suspend fun test(): Nothing = withContext(Dispatchers.IO) {


	val cmd = CmdBuilder()

	val firstText = (0..20).joinToString("") { "$it\n" }.toByteArray()

	cmd
		.freshStart()
//		.delay(1000)
		.append("hat\n")
//		.backup(10)
//		.delay(1000)
//		.clearCursorToEnd()
//		.delay(1000)
//		.append("hat")
//		.delay(1000)
//		.cursorPos(1, 1)
//		.delay(1000)
//		.bell()
		.position()
		.delay(1000)
		.clearex()

	exitProcess(0)
//	throw Exception("")
}

fun getJobs(): List<AacJob> {

	if (config.overwrite)
		for (trashPath in getAllVideoPaths(config.workingDir.resolve("trash")))
			Trash.restoreFromTrash(trashPath)

	val jobs = getAllVideoPaths(config.workingDir)
		.filter { !it.isConverted() }
		.map { AacJob(it) }
		.sorted()

	jobs.forEachIndexed { i, job -> job.jobNum = i }

	return jobs
}

const val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

val rando = Random(System.nanoTime())
fun nextChar(): Char = chars[rando.nextInt(26)]
fun mmf(seed: Int) {

	val file = RandomAccessFile(config.workingDir.resolve("test4.txt").toString(), "rw")

	val pos = seed*200

	val mbb = file.channel.map(FileChannel.MapMode.READ_WRITE, pos.toLong(), pos+500L)

	val c = nextChar()
	for (i in 0..<100) {
		mbb.put(i, c.code.toByte())
	}

	file.channel.truncate(100)
	file.close()

}


fun CoroutineScope.handleSigint() {
	Signal.handle(Signal("INT")) {
		coroutineContext.cancelChildren()
		exitProcess(0)
	}
}