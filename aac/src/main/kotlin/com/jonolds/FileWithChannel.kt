package com.jonolds

import java.io.File
import java.nio.file.Path

class FileWithChannel internal constructor(
	path: String
): File(path) {

}

fun fileWithChannel(path: Path, replaceExisting: Boolean = false): FileWithChannel {
	if (replaceExisting)
		path.toFile().delete()
	return FileWithChannel(path.toString()).also { it.createNewFile() }
}