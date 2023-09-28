package com.jonolds

import java.nio.file.Files
import java.nio.file.Path


fun trashVideo(filePath: Path) {

	if (!filePath.toFile().exists()) {
		println("Can't move file to trash. It doesn't exist.  filePath=$filePath")
		return
	}

	val trashDir = Files.createDirectories(filePath.parent.resolve("trash").normalize())

	val newFilename = filePath.fileName.toString() + ".old"

	Files.move(filePath, trashDir.resolve(newFilename))
	
}