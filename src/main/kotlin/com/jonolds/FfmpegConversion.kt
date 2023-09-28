package com.jonolds

import java.nio.file.Path




fun ffmpegConversion(filePath: Path, config: AacConfig) {

	val commandParts = getFfmpegCommandParts(filePath, config)

	println("\ncommandParts=\n$commandParts\n")

	val exitCode = ProcessBuilder(commandParts)
		.inheritIO()
		.start()
		.waitFor()

	println("\n\tDone converting ${filePath.fileName} with ffmpeg.  exit code=$exitCode")
}

fun getFfmpegCommandParts(filePath: Path, config: AacConfig): List<String> {

	val nextFileName = filePath.toString().dropLast(4) + ".aac.mkv"

	return buildList {
		addAll(listOf("ffmpeg", "-i", "\"$filePath\""))
		if (config.debug)
			addAll(listOf("-t", "00:00:30"))

		addAll(listOf(
			"-map", "0:v", "-c", "copy",
			"-map", " 0:a:m:language:eng", "-c:a", "aac", "-b:a", "512k",
			"-map", "0:s:m:language:eng?",
			"\"$nextFileName\""
		))

	}
}