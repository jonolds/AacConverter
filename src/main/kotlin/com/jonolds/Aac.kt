package com.jonolds

import java.nio.file.Path


fun main(args: Array<String>) {

	checkForHelp(args)

	val config = parseArgs(args)

//	test(config)
//	return

	if (config.defaultProcess)
		defaultProcess(config)
	else {
		if (config.removeSpaces)
			removeSpaces(config)
		if (config.fixColors)
			removeColorInfo(config)
	}

}

fun test(config: AacConfig) {

	val paths = config.getAllVideoPaths()

	for (path in paths)
		trashVideo(path)


}


fun defaultProcess(config: AacConfig) {

	removeSpaces(config)

	val filePaths = getFilePaths(config)

	for (filePath in filePaths) {
		val newFilePath = ffmpegConversion(filePath, config)
		trashVideo(filePath)
		removeColorInfo(newFilePath)
	}

	println("\nDone with default process for ${filePaths.size} files in ${config.dirPath}.")
}



fun getFilePaths(config: AacConfig): List<Path> {
	val allVideoPaths = config.getAllVideoPaths()

	val (unconverted, converted) = allVideoPaths.partition { !it.isConverted() }
		.let { it.first.toMutableList() to it.second }

	if (config.overwrite) {
		val normalUnconverted = unconverted.map { it.removeVideoExts().lowercase() }.toSet()
		for (path in converted)
			if (normalUnconverted.contains(path.removeVideoExts().lowercase()))
				path.toFile().delete()
	}
	else {
		val normalConverted = converted.map { it.removeVideoExts().lowercase() }.toSet()
		unconverted.removeIf { normalConverted.contains(it.removeVideoExts().lowercase()) }
	}
	return unconverted
}


