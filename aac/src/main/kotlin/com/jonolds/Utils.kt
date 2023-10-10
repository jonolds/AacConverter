package com.jonolds

import java.io.File
import java.nio.file.*
import java.util.*


fun getAllVideoPaths(path: Path): List<Path> {

	val file = path.toFile()
	if (!file.exists())
		return Collections.emptyList()
	if (!file.isDirectory)
		throw NotDirectoryException("path= $path")


	return Files.list(path)
		.filter { it.toFile().isFile }
		?.filter { it.isVideoFile() }
		?.toList().orEmpty()
}


fun ByteArray.toString(indices: IntRange): String = String(sliceArray(indices))

fun ByteArray.startsWith(other: ByteArray): Boolean {
	if (other.size > size) return false
	return Arrays.equals(this, 0, other.size, other, 0, other.size)
}




fun Path.createFile(filename: String): File {
	val fullPath = this.resolve(filename)

	return Files.createDirectories(fullPath.toAbsolutePath().parent)
		.resolve(fullPath.last())
		.toFile()
		.also { it.createNewFile() }
}

fun Path.errorUnlessExists(pathDesc: String? = null): Path = if (toFile().exists()) this
else throw InvalidPathException(toString(), "${pathDesc ?: "Resolved"} path does not exist")

fun String.hasVideoExt(): Boolean =
	takeLast(3).lowercase().let { fileType -> fileType == "mkv" || fileType == "mp4" || fileType == "avi" || fileType == "old" }

fun Path.isConverted(): Boolean = toString().lowercase().endsWith(".aac.mkv")

fun Path.isVideoFile(): Boolean {
	if (!toFile().isFile)
		return false

	return toString().hasVideoExt()
}

fun Path.removeVideoExts(): String {

	var currentName = this.toString()
	while (currentName.hasVideoExt() || currentName.lowercase().endsWith(".aac")) {
		currentName = currentName.dropLast(4)
	}
	return currentName
}

fun Path.renameFile(newFileName: String): Boolean = toFile().renameTo(parent.resolve(newFileName).toFile())

fun Path.resolveIfExists(other: String): Path? {
	val newPath = resolve(other)
	if (!newPath.toFile().exists())
		return null
	return newPath
}

fun Path.toAacPath(): Path = Paths.get(toString().dropLast(4) + ".aac.mkv")



fun Regex.listOfMatches(input: String): List<String> =
	findAll(input).map { it.value }.filter { it.isNotBlank() }.toList()


val regex = Regex("[^0-9]*([0-9]+)")
fun String.firstInt(): Int = regex.find(this)?.groupValues?.get(1)?.toInt()!!


fun clearConsole() { ProcessRunner.run("cmd /c cls") }

fun clearConsole2() {
	ProcessBuilder("cmd", "/c", "cls")
		.inheritIO().start().waitFor()

}


fun IntArray.toEpisodeStr(): String = "S${this[0].toString().padStart(2, '0')}E${this[1].toString().padStart(2, '0')}"


fun toDurationStr(duration: Float): String {
	val dInt = duration.toInt()
	return listOf(dInt/3600, (dInt%3600)/60, dInt%60).joinToString(":") { it.toString().padStart(2, '0')}
}

fun Regex.matchesByGroup(input: String): List<List<String>> {

	val groupLists = findAll(input).map { it.groupValues  }.toList()
	val numGroups = groupLists.firstOrNull()?.size ?: return emptyList()

	return List(numGroups) { c -> groupLists.mapNotNull { it[c].ifBlank { null } } }
}

fun IntArray.sumVertically(other: IntArray): IntArray = listOf(this, other)
	.sortedByDescending { it.size }
	.let { listOf(it.first().copyOf(), it.last()) }
	.also { (copy, small) -> small.forEachIndexed { i, value -> copy[i]+=value } }.first()

fun <T, R> Iterable<Iterable<T>>.transform(tranform: (T) -> R): List<List<R>> =
	map { row -> row.map { tranform(it) } }

fun File.appendLines(list: List<String>) {
	appendText(list.joinToString("\n"))
}