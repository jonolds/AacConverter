@file:Suppress("MemberVisibilityCanBePrivate")

package com.jonolds

import kotlinx.coroutines.*

class MainJob(
	val jobs: List<AacJob>
) {


	lateinit var systemPrint: SystemPrint

	suspend fun start() {

		probeVideoSpecs()

		val tasks = jobs.map { job ->

			mainScope.async(aacJobDispatcher) {

				val exitCode = Ffmpeg.aacConversion(job)
				if (exitCode == 0) {
					job.convertedPath = job.origPath.toAacPath()
					Trash.trashVideo(job.origPath)
					if (job.needsConverted)
						MkvPropEdit.removeColorInfo(job.convertedPath!!, job.log)
				}
				else println("\n!!! exitCode=$exitCode")

			}
		}

		systemPrint = SystemPrint(this, tasks)


		val printTask = systemPrint.start()

		tasks.awaitAll()

		printTask.await()

		println("\nDone with default process for ${jobs.size} files in ${config.workingDir}.")

	}



	suspend fun probeVideoSpecs() {
		jobs.map { job ->
			mainScope.async(Dispatchers.IO) {

				Ffprobe.probeVideoSpecs(job)

				val lines = listOf(
					"=".repeat(100),
					"job_num=${job.jobNum}\n\n",
					"            filename = ${job.origPath.fileName}",
					"             episode = ${job.episode?.toEpisodeStr()}",
					"                path = ${job.origPath}",
					"         output path = ${job.origPath.toAacPath()}",
					"         audio codec = ${job.audioCodec}",
					"   frames to convert = ${job.totalFrames}",
					" duration to convert = ${job.totalDuration}",
					"     needs converted = ${job.needsConverted}"
				)

				job.log.appendLines(lines)
			}
		}.awaitAll()
	}
}


suspend fun getMainJob(): MainJob {
	VideoFilenames.cleanup()
	val jobs = getJobs()
	return MainJob(jobs)
}