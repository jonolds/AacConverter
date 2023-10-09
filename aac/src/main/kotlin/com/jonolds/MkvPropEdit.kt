package com.jonolds

import java.io.File
import java.nio.file.Path


object MkvPropEdit {

	fun removeColorInfo(filePath: Path, log: File? = null) {

		val command = getMkvPropEditCommand(filePath)

		val exitCode = ProcessRunner.run(command, log)

		log?.appendText("\nDone removing color info from ${filePath.fileName} with MkvPropEdit.  exit code=$exitCode")

	}

	private fun getMkvPropEditCommand(filePath: Path): List<String> {

		return listOf(
			"mkvpropedit \"$filePath\" -e track:v1",
			"-d color-matrix-coefficients",
			"-d chroma-siting-horizontal",
			"-d chroma-siting-vertical",
			"-d color-range",
			"-d color-transfer-characteristics",
			"-d color-primaries",
		)

	}

}
