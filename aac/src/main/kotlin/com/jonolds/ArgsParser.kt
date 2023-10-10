package com.jonolds

import java.nio.file.Paths
import kotlin.system.exitProcess

object ArgsParser {


	internal suspend fun parseWorkingDirArgs(args: Array<String>) {

		config.binDir = Paths.get(args[0])
			.errorUnlessExists("binDir")

		config.workingDir = Paths.get("").toAbsolutePath()
			.resolve(args.drop(1).firstOrNull { !it.startsWith("-") } ?: "")
			.errorUnlessExists("workingDir")

		parseArgs(args.filter { it.startsWith("-") })
	}

	private suspend fun parseArgs(args: List<String>) {

		var test = false
		for (arg in args) {
			val argNormal = arg.trim().removePrefix("-").lowercase()
			when  {
				argNormal == "help" -> exitWithHelp()
				argNormal == "exit" -> exitProcess(0)
				argNormal == "debug" -> Log.debug = true
				argNormal.startsWith("numthreads") -> config.numThreads = argNormal.firstInt()
				argNormal == "fixcolors" -> config.fixColors = true
				argNormal == "overwrite" -> config.overwrite = true
				argNormal == "restore" -> restore()
				argNormal == "probeaudio" -> config.probeAudio = true
				argNormal == "quiet" || argNormal == "q" -> config.quiet = true
				argNormal == "test" -> test = true
				argNormal.startsWith("t=") -> parseTimeArg(argNormal)
				else -> throw IllegalArgumentException("IllegalArgumentException: '$arg' is unknown")
			}
		}

		if (test)
			test()
	}


	private val timeRegex = Regex("([0-9]+)h|([0-9]+)m|([0-9]+)s")
	private fun parseTimeArg(arg: String) {

		val timeCounts = timeRegex.matchesByGroup(arg).drop(1)
			.let { lol -> IntArray(3) { i -> lol[i].sumOf { it.toInt() } + (config.timeCnts?.get(i) ?: 0) } }

		for (i in 2 downTo 1) {
			timeCounts[i-1]+=timeCounts[i]/60
			timeCounts[i]%=60
		}

		config.timeCnts = timeCounts
	}


}



suspend fun parseArgs(args: Array<String>) = ArgsParser.parseWorkingDirArgs(args)