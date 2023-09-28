package com.jonolds

import java.nio.file.Path


fun Path.removeVideoExts(): String {
	if (!isVideoFile())
		return toString()
	if (isConverted())
		return toString().dropLast(8)
	return toString().dropLast(4)
}

fun Path.isVideoFile(): Boolean {
	if (!toFile().isFile) return false
	val fileType = toString().takeLast(3).lowercase()
	return fileType == "mkv" || fileType == "mp4" || fileType == "avi"
}

fun Path.isConverted(): Boolean = toString().lowercase().endsWith(".aac.mkv")