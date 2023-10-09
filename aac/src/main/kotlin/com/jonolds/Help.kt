package com.jonolds

import kotlin.system.exitProcess


fun exitWithHelp(): Nothing {
	println("\n$helpStr\n")
	exitProcess(0)
}

val helpStr = """
	=========================== AacConverter Help ============================
		Usage:
			aac [directory*] -[option1] -[option2]...
			* default is working directory
		
		Default process:
			1. remove spaces from video filenames
			2. convert audio codec to aac / move old video to folder trash
			3. remove color info headers from mkv
			
		Options:
			       debug - only convert first 30 seconds/additional output
			   overwrite - overwrite already converted instead of skipping
			   fixColors - remove color information after conversion (disables default)
	
	==========================================================================
""".trimIndent()