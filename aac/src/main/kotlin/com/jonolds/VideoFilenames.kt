package com.jonolds

import java.nio.file.Files
import java.nio.file.Path


object VideoFilenames {

	fun cleanup() {
		Files.list(config.workingDir)
			.filter { it.isVideoFile() }
			.forEach { filePath ->
				cleanupFilename(filePath)
			}

		val trashDir = config.workingDir.resolveIfExists("trash") ?: return

		Files.list(trashDir)
			.filter { it.isVideoFile() }
			.forEach { filePath ->
				cleanupFilename(filePath)
			}
	}

	private fun cleanupFilename(filePath: Path) {

		val oldFilename = filePath.fileName.toString()

		val newFilename = oldFilename
			.spacesToPeriod()
			.episodeUppercase()

		if (oldFilename != newFilename)
			filePath.renameFile(newFilename)
	}


	private val blankSpacesRegex = Regex("\\s+")
	private val periodsRegex = Regex("\\.+")
	private fun String.spacesToPeriod(): String = replace(blankSpacesRegex, ".")
		.replace(periodsRegex, ".")
		.replace(".-.", ".")


	private val episodeRegex = Regex("[sS][0-9]{2}[eE][0-9]{2}")
	private fun String.episodeUppercase(): String = episodeRegex
		.find(this)?.value
		?.let { subStr -> replace(subStr, subStr.uppercase(), true) }
		?: this


	fun parseEpisode(path: Path): IntArray? = episodeRegex
		.find(path.toString())?.value
		?.drop(1)?.split(Regex("[eE]"))
		?.let { intArrayOf(it[0].toInt(), it[1].toInt()) }

}

