@file:Suppress("MemberVisibilityCanBePrivate")

package com.jonolds

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.nio.file.Path
import java.util.*


@OptIn(ExperimentalCoroutinesApi::class)
val aacJobDispatcher = Dispatchers.Default.limitedParallelism(config.numThreads)

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

	val audioFilters: String get() = if (audioLangs.any { it?.lowercase() == "eng" }) ":m:language:eng" else ""

	val subtitleFilters: String get() = if (subtitleLangs.any { it?.lowercase() == "eng" }) ":m:language:eng" else ""

	val needsConverted get() = audioCodec != "aac"

	var convertedFrames = 0
	var speed = 0.0

	val pctComplete get() = convertedFrames.toDouble()/totalFrames

	var jobStatusCode: StatusCode = StatusCode.NOT_STARTED
	var jobMessage: String = ""

	val log: FileWithChannel by lazy { fileWithChannel(config.logDir.resolve("log$jobNum.txt"), true) }

	override fun compareTo(other: AacJob): Int = Arrays.compare(episode, other.episode)


}


enum class StatusCode {
	NOT_STARTED, IN_PROGRESS, SUCCESS, FAILURE;
	companion object {
		fun fromExitCode(exitCode: Int): StatusCode = if (exitCode == 0) SUCCESS else FAILURE
	}
}