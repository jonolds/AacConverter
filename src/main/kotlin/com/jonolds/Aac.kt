package com.jonolds

import java.nio.file.Files
import java.nio.file.Path
import java.util.*


fun main(args: Array<String>) {

	val config = parseArgs(args)

	if (config.defaultProcess)
		defaultProcess(config)
	else {
		if (config.removeSpaces)
			removeSpaces(config)
		if (config.fixColors)
			removeColorInfo(config)
	}

}


fun defaultProcess(config: AacConfig) {

	removeSpaces(config)

	val filePaths = getFilePaths(config)

	for (filePath in filePaths) {
		ffmpegConversion(filePath, config)
		removeColorInfo(filePath)
	}

	println("\nDone with default process for ${filePaths.size} files in ${config.dirPath}.")
}



fun getFilePaths(config: AacConfig): List<Path> {
	val allVideoPaths = getAllVideoPaths(config)

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



fun Path.removeVideoExts(): String {
	if (!isVideoFile())
		return toString()
	if (isConverted())
		return toString().dropLast(8)
	return toString().dropLast(4)
}


fun getAllVideoPaths(config: AacConfig): List<Path> =
	Files.list(config.dirPath)?.filter { it.isVideoFile() }?.toList() ?: Collections.emptyList()

fun Path.isVideoFile(): Boolean {
	if (!toFile().isFile) return false
	val fileType = toString().takeLast(3).lowercase()
	return fileType == "mkv" || fileType == "mp4" || fileType == "avi"
}

fun Path.isConverted(): Boolean = toString().lowercase().endsWith(".aac.mkv")
