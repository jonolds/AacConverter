package com.jonolds

import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream

interface OutputFilter {

	fun write(b: Int)
	fun write(b: ByteArray)
	fun write(b: ByteArray, off: Int, len: Int)

	fun flush()
	fun close()
}

class Special(
	val job: AacJob
): PrintStream(System.out), OutputFilter {
	val fos = FileOutputStream(job.log, true)

	override fun write(b: Int) {
//		super.write(b)
		fos.write(b)
	}

	var count = 0
	override fun write(b: ByteArray) {
//		fos.write("\n\n!!!!!! count=${count++}".toByteArray())
//		super.write(b)
		fos.write(b)
	}

	override fun write(b: ByteArray, off: Int, len: Int) {
		ByteArrayFuns.process(job, b, off, len)
//		super.write(b, off, len)
		fos.write(b, off, len)
	}

	override fun flush() {
//		super.flush()
		fos.flush()
	}

	override fun close() {
//		super.close()
		fos.close()
	}

}