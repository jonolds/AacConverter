package com.jonolds

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import java.io.File
import java.net.URLDecoder
import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.io.*



object Trash {

	@OptIn(ExperimentalCoroutinesApi::class)
	val trashDispatcher = Dispatchers.IO.limitedParallelism(1)

	private val jarDir: Path = URLDecoder.decode(this.javaClass.getProtectionDomain().codeSource.location.path, "UTF-8")
		.removePrefix("/").replace("/", "\\")
		.let { Paths.get(it) }.parent

	private val globalTrashLog = jarDir.createFile("globalTrashLog.txt")

	val trashEntries = globalTrashLog.readLines().toMutableSet()


	private fun addIfAbsent(trashDir: Path) {
		if (trashEntries.add(trashDir.toString()))
			globalTrashLog.appendText("$trashDir\n")
	}


	fun trashVideo(filePath: Path) {

		if (!filePath.toFile().exists()) {
			println("Can't move file to trash. It doesn't exist.  filePath=$filePath")
			return
		}

		val trashDir = Files.createDirectories(filePath.parent.resolve("trash").normalize())
		val trashFilename = filePath.fileName.toString() + ".old"
		Files.move(filePath, trashDir.resolve(trashFilename), StandardCopyOption.REPLACE_EXISTING)

		addIfAbsent(trashDir)
	}


	fun restoreFromTrash(path: Path): Path {
		val trashFileName = path.fileName.toString()
		if (!trashFileName.endsWith(".old") || path.parent.last().toString() != "trash")
			throw IllegalArgumentException("Path is not trash.  path= $path")

		val newPath = path.parent.parent.resolve(trashFileName.removeSuffix(".old"))
		Files.move(path, newPath, StandardCopyOption.REPLACE_EXISTING)
		return newPath
	}


}