@file:Suppress("MemberVisibilityCanBePrivate")

package com.jonolds

import kotlinx.coroutines.*
import java.awt.Robot
import java.awt.event.KeyEvent
import java.io.*
import java.text.DecimalFormat


class SystemPrint(
	val mainJob: MainJob,
	val tasks: List<Deferred<Unit>>
) {

	val jobs = mainJob.jobs

	val pctLen = 10

	val filenameLen = jobs.map { it.origPath.fileName.toString() }.maxOf { it.length }
	val filenameStrs = jobs.map { it.origPath.fileName.toString().padEnd(filenameLen, ' ') }


	private val formatPct = DecimalFormat.getPercentInstance()
		.also { it.maximumFractionDigits = 1 }

	suspend fun start() = mainScope.async(Dispatchers.IO) {
		buildOutput()

		while (tasks.any { it.isActive }) {
			delay(1000)
			updateOutput()
		}

		updateOutput()
	}

	fun updateOutput() {
//		println("output  ${jobs.map { formatPct.format(it.pctComplete).padStart(pctLen, ' ') }}")
		System.out.print("\u001B[${jobs.size}F")

		for (job in jobs) {
			System.out.print("\u001B[${filenameLen}C")
			System.out.print(formatPct.format(job.pctComplete).padStart(pctLen, ' '))
			System.out.print("\u001B[E")
		}

	}

	fun buildOutput() {
		for (i in jobs.indices) {
			val fileStr = filenameStrs[i]
			println(fileStr + formatPct.format(jobs[i].pctComplete).padStart(pctLen, ' '))
		}
	}


}


class CmdBuilder {

	private val cmds = ArrayList<Cmd>()

	private interface Cmd {
		fun add()
		suspend fun exec()
	}


	inner class ActionCmd(val action: suspend () -> Unit): Cmd {
		override fun add() {
			cmds.add(this)
		}
		override suspend fun exec() { action() }
	}

	inner class BytesCmd(var ba: ByteArray): Cmd {

		constructor(str: String): this(str.toByteArray())

		override fun add() {
			val last = cmds.lastOrNull()
			if (last is BytesCmd)
				last.ba+=ba
			else
				cmds.add(this)
		}

		override suspend fun exec() { System.out.writeBytes(ba) }
	}


	private fun add(cmd: Cmd): CmdBuilder {
		cmd.add()
		return this
	}

	private fun addBytes(str: String): CmdBuilder {
		BytesCmd(str).add()
		return this
	}



	suspend fun exec(): CmdBuilder {
		for (cmd in cmds)
			cmd.exec()
		return this
	}

	suspend fun clearex(): CmdBuilder {
		exec()
		return clearCmd()
	}

	fun clearCmd(): CmdBuilder {
		cmds.clear()
		return this
	}



	fun delay(ms: Long) = add(ActionCmd { kotlinx.coroutines.delay(ms) })

	fun backup(n: Int) = add(BytesCmd("\u001B[${n}F"))


	fun cursorUp(n: Int) = add(BytesCmd("\u001B[${n}A"))

	private val eraseScrollbackCmd = BytesCmd("\u001B[3J")
	fun eraseScrollback() = add(eraseScrollbackCmd)

	private val clearCursorToEndCmd = BytesCmd("\u001B[0J")
	fun clearCursorToEnd() = add(clearCursorToEndCmd)


	private val clearCursorToTopCmd = BytesCmd("\u001B[2J")
	fun clearCursorToTop() = add(clearCursorToTopCmd)

	fun append(text: String) = append(text.toByteArray())

	fun append(ba: ByteArray): CmdBuilder = add(BytesCmd(ba))

	fun cursorPos(row: Int, col: Int = 1) = add(BytesCmd("\u001B[${row};${col}H"))

	fun bell() = addBytes("\u0007")

	fun position() = add(ActionCmd {
//		val sc = Scanner(System.`in`)


		System.out.printf("\u001B[6n")
		pressEnter()
		println("Squirrel")
		val br = BufferedReader(InputStreamReader(System.`in`))


		System.setOut(null)
		br.read(CharArray(10))


//		val fos = getTempFos()
//		System.setOut(PrintStream(fos))
//		fos.close()

//		System.`in`.readNBytes(2)
		println("!!!!!! ")
	})


	fun getTempFis(): FileInputStream {
		val file = File("temp.txt")
		file.createNewFile()
		return FileInputStream(file)
	}

	fun getTempFos(): FileOutputStream {
		val file = File("temp.txt")
		file.createNewFile()
		return FileOutputStream(file)
	}


	fun pressEnter() {
		val r = Robot()
		r.keyPress(KeyEvent.VK_ENTER)
		r.keyRelease(KeyEvent.VK_ENTER)
	}


	fun echo(en: Boolean) {
		val value = if (en) "on" else "off"
		ProcessBuilder("cmd", "/c", "echo", value).inheritIO().start().waitFor()
	}


	fun freshStart() = eraseScrollback().clearCursorToTop().cursorPos(1, 1)

}