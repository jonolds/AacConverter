package com.jonolds

import java.nio.file.Path
import java.nio.file.Paths


fun ffmpegConversion(origFilePath: Path, config: AacConfig): Path {

	val newFilePath = Paths.get(origFilePath.toString().dropLast(4) + ".aac.mkv")

	val commandParts = getFfmpegCommandParts(origFilePath, newFilePath, config)

	if (config.debug)
		println("\ncommandParts=\n$commandParts\n")

	val exitCode = ProcessBuilder(commandParts)
		.inheritIO()
		.start()
		.waitFor()

	println("\n\tDone converting ${origFilePath.fileName} with ffmpeg.  exit code=$exitCode")

	return newFilePath
}

fun getFfmpegCommandParts(filePath: Path, newFilePath: Path, config: AacConfig): List<String> {

	return buildList {
		addAll(listOf("ffmpeg", "-i", "\"$filePath\""))
		if (config.debug)
			addAll(listOf("-t", "00:00:30"))

		addAll(listOf(
			"-map", "0:v", "-c", "copy",
			"-map", " 0:a:m:language:eng", "-c:a", "aac", "-b:a", "512k",
			"-map", "0:s:m:language:eng?",
			"\"$newFilePath\""
		))

	}
}