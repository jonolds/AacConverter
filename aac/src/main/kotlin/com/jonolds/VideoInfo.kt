package com.jonolds

import java.nio.file.Path

data class VideoInfo(
	val audioCodec: String,
	val duration: String,
	val videoFrames: Int,
	val audioLangs: List<String?>,
	val subtitleLangs: List<String?>

)