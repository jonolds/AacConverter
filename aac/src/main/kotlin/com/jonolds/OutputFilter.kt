package com.jonolds

import java.io.FileOutputStream
import java.nio.ByteBuffer


class FfmpegFileOutputStream(
	private val job: AacJob
): FileOutputStream(job.log, true) {

	private val channel = this.getChannel()

	init {
		job.log.appendBytes(FFMPEG_HEADER)
	}

	private var lastLineStatus = false
	private var lastStatusLen = 0

	override fun write(b: ByteArray, off: Int, len: Int) {
		if (b.startsWith(FRAME_EQ))
			return processStatusLine(b, off, len)

		lastLineStatus = false
		super.write(b, off, len)
	}

	private fun processStatusLine(b: ByteArray, off: Int, len: Int) {
		if (lastLineStatus) {
			val buffer = ByteBuffer.wrap(b, off, len)
			channel.write(buffer, job.log.length()-lastStatusLen)
			channel.force(true)
		}

		else {
			super.write(NEW_LINE_BA)
			super.write(b, off, len)
		}

		lastLineStatus = true
		lastStatusLen = len


		val groups = statusRegex.matchesByGroup(b.toString(off until off+len)).drop(1)

		job.convertedFrames = groups[0].firstOrNull()?.toInt() ?: 0
		job.convertedTime = groups[1].firstOrNull() ?: job.convertedTime
		job.speed = groups[2].firstOrNull()?.toDouble() ?: 0.0

	}

	companion object {
		private val FRAME_EQ = "frame=".toByteArray()

		private val statusRegex = Regex(
			"frame=\\s*([0-9]+)|" +
			"time=([0-9]{2}:[0-9]{2}:[0-9]{2}\\.[0-9]{2}])|" +
			"speed=\\s*([0-9.]+)"
		)

		private val FFMPEG_HEADER = "\n\n\n  *****  Ffmpeg  *****\n\n".toByteArray()

		private val NEW_LINE_BA = "\n".toByteArray()
	}
}
