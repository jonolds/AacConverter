package com.jonolds

object ByteArrayFuns {

	private val cr = "\r".toByteArray().first()
	private val frameEq = "frame=".toByteArray()
	val blankSpace = "\r\n \r\n \r\n".toByteArray()
	val exclamations = "!!!!!!!!!".toByteArray()

	fun process(job: AacJob,b: ByteArray, off: Int, len: Int) {

		if (b[len-1] == cr && b.startsWith(frameEq)) {
			val str = b.toString(off until off+len)
			job.convertedFrames = getFrame(str)
			job.speed = getSpeed(str)
		}
	}

	private fun getFrame(str: String): Int {
		val subStr = str.substring(6 until str.indexOf("fps", 7))
		return try {
			subStr.trim().ifBlank { "0" }.toInt()
		} catch (e: NumberFormatException) {
			e.printStackTrace()
			System.err.println("getFrame \n\tsubStr=${subStr} \n\tstr=$str\n")
			0
		}
	}


	private val speedRegex = Regex("speed=\\s*([0-9.]+)|speed=(N/A)")
	private fun getSpeed(str: String): Double {
		return try {

			val groups = speedRegex.matchesByGroup(str).drop(1)

			if (groups.first().isEmpty())
				return 0.0

			return groups.first().first().toDouble()

//			str.substring(str.lastIndexOf("speed=")+6 until str.lastIndexOf('x', str.length)).trim().ifBlank { "0.0" }.toDouble()

		} catch (e: Exception) {
			e.printStackTrace()
			println("ByteArrayFuns.getSpeed   \nmessage=${e.message}\nstr=$str")

			0.0

		}
	}

	private const val spaceByte = (32).toByte()
	fun getFrame(b: ByteArray): Int {
		var firstDigitPos = -1
		for (i in 7 until b.size)
			if (b[i] != spaceByte) {
				firstDigitPos = i
				break
			}

		var endPos = -1
		for (i in firstDigitPos+1 until b.size)
			if (b[i] == spaceByte) {
				endPos = i
				break
			}

		return String(b.slice(firstDigitPos..<endPos).toByteArray()).trim().ifBlank { "0" }.toInt()
	}


}