package com.jonolds

object Log {

	var debug = false

	fun d(message: Any) {
		if (debug)
			println(message)
	}
}
