package com.jonolds

import java.net.URLDecoder
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*


class AacConfig(
	val dirPath: Path,
	flags: Set<String>
) {
	private val optionsMap = mutableMapOf(
		"overwrite" to false,
		"fixColors" to false,
		"removeSpaces" to false,
		"debug" to false,
	)

	init {
		for (flag in flags) {
			val flagNormalized = flag.trim().removePrefix("-")
			if (!optionsMap.containsKey(flagNormalized))
				throw Exception("Unknown Flags Exception: $flag")
			optionsMap[flagNormalized] = true
		}
	}

	val overwrite by optionsMap
	val fixColors by optionsMap
	val removeSpaces by optionsMap
	val debug by optionsMap


	val defaultProcess get() = !removeSpaces && !fixColors


	fun getAllVideoPaths(): List<Path> =
		Files.list(dirPath)?.filter { it.isVideoFile() }?.toList() ?: Collections.emptyList()


	val jarPath: String get() = URLDecoder.decode(this.javaClass.getProtectionDomain().codeSource.location.path, "UTF-8")

}


fun parseArgs(args: Array<String>): AacConfig = when {
	args.isEmpty() -> AacConfig(resolvePath(""), Collections.emptySet())
	args.first().startsWith("-") -> AacConfig(resolvePath(""), args.toSet())
	else -> AacConfig(resolvePath(args.first()), args.drop(1).toSet())
}

fun resolvePath(arg: String? = null): Path {
	val path = Paths.get("").toAbsolutePath().resolve(arg ?: "")
	if (!path.toFile().exists())
		throw InvalidPathException(path.toString(), "Resolved path does not exist")
	return path
}

