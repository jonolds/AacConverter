package com.jonolds

import java.nio.file.Path


fun removeColorInfo(filePath: Path) {

	val commandParts = getMkvPropEditCommandParts(filePath)

	println("")
	val exitCode = ProcessBuilder(commandParts)
		.inheritIO()
		.redirectError(ProcessBuilder.Redirect.INHERIT)
		.start()
		.waitFor()


	println("\n\tDone removing color info from ${filePath.fileName} with MkvPropEdit.  exit code=$exitCode")

}

private fun getMkvPropEditCommandParts(filePath: Path): List<String> {

	return listOf(
		"mkvpropedit", "\"$filePath\"", "-e", "track:v1",
		"-d", "color-matrix-coefficients",
		"-d", "chroma-siting-horizontal",
		"-d", "chroma-siting-vertical",
		"-d", "color-range",
		"-d", "color-transfer-characteristics",
		"-d", "color-primaries",
	)

}


fun removeColorInfo(config: AacConfig) {
	val filePaths = config.getAllVideoPaths().filter { it.isConverted() }

	for (path in filePaths)
		removeColorInfo(path)

	println("\nDone removing color info from ${filePaths.size} converted files")
}