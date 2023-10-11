@file:Suppress("MemberVisibilityCanBePrivate")

package com.jonolds

import java.nio.file.Files
import java.nio.file.Path


object AacConfig {

	lateinit var workingDir: Path

	lateinit var binDir: Path


	var overwrite = false

	var noColorFix = false

	var probeAudio = false

	var quiet = false

	var numThreads = 4

	var copyAudio = false

	var timeCnts: IntArray? = null
		set(value) {
			field = if (value?.sum() == 0) null
			else value
		}

	val timeReqStr: String? by lazy { timeCnts?.joinToString(":") { it.toString().padStart(2, '0') } }

	val timeReqSecs: Float? by lazy { timeCnts?.let { tc -> 3600*tc[0] + 60*tc[1] + tc[2].toFloat() } }


	val logDir: Path by lazy { Files.createDirectories(binDir.resolve("logs")) }


}

val config = AacConfig

