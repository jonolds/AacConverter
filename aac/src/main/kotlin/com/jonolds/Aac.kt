@file:Suppress("ObjectPropertyName")

package com.jonolds

import kotlinx.coroutines.*
import sun.misc.Signal
import kotlin.coroutines.EmptyCoroutineContext
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
			Cursor.show()
			mainScope.coroutineContext.cancelChildren(cause = CancellationException(e.message, e))
			throw e
		}
	}
}


suspend fun test(): Nothing = withContext(Dispatchers.IO) {



	exitProcess(0)
//	throw Exception("")
}

fun getJobs(): List<AacJob> {

	if (config.overwrite)
		Trash.restoreAllTrashForPath(config.workingDir)

	val jobs = getAllVideoPaths(config.workingDir)
		.filter { !it.isConverted() }
		.map { AacJob(it) }
		.sorted()

	jobs.forEachIndexed { i, job -> job.jobNum = i }

	return jobs
}


fun restore() {

	val converted = getAllVideoPaths(config.workingDir)


	val trashDirPath = config.workingDir.resolve("trash")
	val restoredPaths = getAllVideoPaths(trashDirPath)
		.map { Trash.restoreFromTrash(it) }

	val restoredBaseFilenames = restoredPaths.map { it.fileName.removeVideoExts() }

	val convertedToDelete = converted.filter { restoredBaseFilenames.contains(it.fileName.removeVideoExts()) }

	for (fileToDelete in convertedToDelete)
		fileToDelete.toFile().delete()

	println("\nRestored:")
	for (path in restoredPaths)
		println("\t$path")

	println("\nDeleted:")
	for (path in convertedToDelete)
		println("\t$path")

	exitProcess(0)
}


fun CoroutineScope.handleSigint() {
	Signal.handle(Signal("INT")) {
		try {
			Cursor.show()
			coroutineContext.cancelChildren()
			exitProcess(0)
		} catch (e: Exception) {
			coroutineContext.cancel()
			throw e
		}
	}
}

fun exitWithMessage(msg: String) {
	System.err.println(msg)
	exitProcess(0)
}