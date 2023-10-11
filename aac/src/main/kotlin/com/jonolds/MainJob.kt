@file:Suppress("MemberVisibilityCanBePrivate")

package com.jonolds

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainJob(
	val jobs: List<AacJob>
) {

	lateinit var mainLogControl: MainLogControl


	suspend fun start() {

		probeVideoSpecs()

		val tasks = jobs.map { job ->

			mainScope.async(aacJobDispatcher) {

				val exitCode = Ffmpeg.aacConversion(job)
				if (exitCode == 0) {
					job.convertedPath = job.origPath.toAacPath()
					Trash.trashVideo(job.origPath)
					if (job.needsConverted && !config.noColorFix)
						MkvPropEdit.removeColorInfo(job)
				}
				else println("\n!!! exitCode=$exitCode")

			}
		}

		mainLogControl = MainLogControl(this, tasks)

		val mainLogTask = mainLogControl.start()

		tasks.awaitAll()

		mainLogTask.await()

		println("\nDone with default process for ${jobs.size} files in ${config.workingDir}.")

	}


	suspend fun probeVideoSpecs() {
		jobs.map { job ->
			mainScope.async(Dispatchers.IO) {
				Ffprobe.probeVideoSpecs(job)
				job.log.appendText(job.buildJobHeader())
			}
		}.awaitAll()
	}


}


suspend fun getMainJob(): MainJob {
	VideoFilenames.cleanup()
	val jobs = getJobs()
	if (jobs.isEmpty())
		exitWithMessage("No video files to convert in ${config.workingDir}.")
	return MainJob(jobs)
}

val datetimeFormatter: DateTimeFormatter = DateTimeFormatter
	.ofPattern("yyyy-MM-dd'T'hh:mm:ss")
fun getTimestamp() = LocalDateTime.now()
	.format(datetimeFormatter)
	.toString()