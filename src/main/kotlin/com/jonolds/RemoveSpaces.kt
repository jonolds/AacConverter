package com.jonolds

import java.nio.file.Files
import java.nio.file.Path


fun removeSpaces(config: AacConfig) {

	val filePaths = Files.list(config.dirPath)
		?.filter { it.isVideoFile() }
		?.toList()
		.orEmpty()

	filePaths.forEach { filePath ->
		val oldFilename = filePath.fileName.toString()
		val newFilename = oldFilename
			.replace(Regex("\\s+"), ".")
			.replace(Regex("\\.+"), ".")

		if (oldFilename == newFilename)
			return@forEach

		val newPath = config.dirPath.resolve(newFilename)
		println("\nold=$filePath \nnew=$newPath\n")
		Files.move(filePath, newPath)
	}

	println("\nDone removing spaces from ${filePaths.size} files.")

}