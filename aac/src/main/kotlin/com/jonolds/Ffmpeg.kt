@file:Suppress("ConstPropertyName", "DuplicatedCode")

package com.jonolds

import java.nio.file.Path


object Ffmpeg {

	fun aacConversion(job: AacJob): Int {

		val newFilePath = job.origPath.toAacPath()

		newFilePath.toFile().delete()

		val command = getFfmpegCommand(job, newFilePath)

		val os = FfmpegFileOutputStream(job)

		val exitCode = ProcessRunner.run(command, os)

		if (exitCode == 0)
			job.convertedFrames = job.totalFrames

		os.close()

		return exitCode

	}


	private fun getFfmpegCommand(
		job: AacJob,
		convertedPath: Path,
	): List<String> =
		listOfNotNull(
			"ffmpeg -hide_banner",
			"-i ${quoted(job.origPath)}",
			timeMapping,
			"-map 0:v -c:v copy",
			audioMapping(job),
			getSubtitleMapping(job),
			quoted(convertedPath)
	)

	private val timeMapping: String? get() = config.timeReqStr?.let { "-t $it" }


	private fun audioMapping(job: AacJob): String {
		val audioFilters = if (job.audioLangs.any { it?.lowercase() == "eng" }) ":m:language:eng" else ""
		return if (job.audioCodec == "aac" || config.copyAudio) "-map 0:a$audioFilters -c:a copy"
			else "-map 0:a$audioFilters -c:a aac -b:a 512k"
	}

	private fun getSubtitleMapping(job: AacJob): String {
		val subtitleFilter = if (job.subtitleLangs.any { it?.lowercase() == "eng" }) ":m:language:eng" else ""
		return "-map 0:s$subtitleFilter -c:s copy"
	}
}
