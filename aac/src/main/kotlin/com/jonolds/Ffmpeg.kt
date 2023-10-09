@file:Suppress("ConstPropertyName", "DuplicatedCode")

package com.jonolds

import java.io.File
import java.io.OutputStream
import java.nio.file.Path
import kotlin.system.exitProcess


object Ffmpeg {

	private val ffmpegHeader = "\n\n\n  *****  Ffmpeg  *****\n\n".toByteArray()

	fun aacConversion(job: AacJob): Int {

		val newFilePath = job.origPath.toAacPath()

		newFilePath.toFile().delete()

		val command = getFfmpegCommand(job.origPath, newFilePath, job.audioCodec, job.audioFilters, job.subtitleFilters)

		val os = Special(job)

		job.log.appendBytes(ffmpegHeader)


		val exitCode = ProcessRunner.run(command, os)

		if (exitCode == 0)
			job.convertedFrames = job.totalFrames

		os.close()

//		println("\n\tDone converting ${job.origPath.fileName} with ffmpeg.  exit code=$exitCode")

		return exitCode

	}


	private fun getFfmpegCommand(
		path: Path, convertedPath: Path,
		audioCodec: String? = null,
		audioFilters: String,
		subtitleFilters: String,
	): List<String> =
		listOfNotNull(
//			"ffmpeg",
			"ffmpeg -hide_banner",
	//		" -loglevel warning -stats",
			"-i \"$path\"",
			timeMapping,
			"-map 0:v -c:v copy",
			audioMappings(audioCodec, audioFilters),
			"-map 0:s$subtitleFilters -c:s copy",
			"\"$convertedPath\""
	)

	private val timeMapping: String? get() = config.timeReqStr?.let { "-t $it" }


	private fun audioMappings(audioCodec: String?, audioFilters: String) =
		if (audioCodec == "aac") "-map 0:a$audioFilters -c:a copy"
		else "-map 0:a$audioFilters -c:a aac -b:a 512k"

}
