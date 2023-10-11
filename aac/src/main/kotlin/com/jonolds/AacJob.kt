@file:Suppress("MemberVisibilityCanBePrivate")

package com.jonolds

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File
import java.nio.file.Path
import java.util.*


@OptIn(ExperimentalCoroutinesApi::class)
val aacJobDispatcher by lazy { Dispatchers.IO.limitedParallelism(config.numThreads) }

class AacJob(
	var origPath: Path,
	var convertedPath: Path? = null,
	var jobNum: Int = 0
): Comparable<AacJob> {

	val episode: IntArray? = VideoFilenames.parseEpisode(origPath)

	var audioCodec = ""
	var totalFrames: Int = 1
	var totalDuration = "00:00:00"
	var totalDurationSecs = 0f
	var audioLangs: List<String?> = emptyList()
	var subtitleLangs: List<String?> = emptyList()

	val needsConverted get() = audioCodec != "aac"

	var convertedFrames = 0
	var convertedTime = "00:00:00.00"
	var speed = 0.0

	val pctComplete get() = convertedFrames.toDouble()/totalFrames

	var jobStatusCode: StatusCode = StatusCode.NOT_STARTED
	var jobMessage: String = ""

	val log: File by lazy {
		config.logDir.resolve("log$jobNum.txt").let { path ->
			path.toFile().delete()
			path.toFile().also { it.createNewFile() }
		}
	}


	fun buildJobHeader(): String = StringBuilder("\n")
		.appendLine("=".repeat(100))
		.appendLine("job_num=${jobNum}\n\n")
		.append("filename = ".padStart(23, ' ')).appendLine(origPath.fileName)
		.append("episode = ".padStart(23, ' ')).appendLine(episode?.toEpisodeStr())
		.append("path = ".padStart(23, ' ')).appendLine(origPath)
		.append("output path = ".padStart(23, ' ')).appendLine(origPath.toAacPath())
		.append("audio codec = ".padStart(23, ' ')).appendLine(audioCodec)
		.append("frames to convert = ".padStart(23, ' ')).appendLine(totalFrames)
		.append("duration to convert = ".padStart(23, ' ')).appendLine(totalDuration)
		.append("needs converted = ".padStart(23, ' ')).appendLine(needsConverted)
		.append("audio languages = ".padStart(23, ' ')).appendLine(audioLangs)
		.append("subtitle languages = ".padStart(23, ' ')).appendLine(subtitleLangs)
		.toString()


	override fun compareTo(other: AacJob): Int = Arrays.compare(episode, other.episode)

}


enum class StatusCode {
	NOT_STARTED, IN_PROGRESS, SUCCESS, FAILURE;
	companion object {
		fun fromExitCode(exitCode: Int): StatusCode = if (exitCode == 0) SUCCESS else FAILURE
	}
}