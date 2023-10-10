package com.jonolds

import java.nio.file.Path


object Ffprobe {

	private suspend fun audioCodec(path: Path): String? {
		val command = "ffprobe -v error -select_streams a:0 -show_entries stream=codec_name -of default=noprint_wrappers=1:nokey=1 \"$path\""
		val audioCodec = ProcessRunner.execForSingleResult(command)
		Log.d("audio_codec= $audioCodec   ${path.fileName}")
		return audioCodec
	}

	private suspend fun getDuration(path: Path): Float {
		val command = "ffprobe -v 0 -hide_banner -of compact=p=0:nk=1 -show_entries packet=pts_time -read_intervals 99999%+#1000 \"$path\""
		val duration = ProcessRunner.execForSingleResult(command)?.toFloat()
		Log.d("duration= $duration   ${path.fileName}")
		return duration ?: Float.MAX_VALUE
	}

	private suspend fun getFrames(path: Path): Int? {
		val command = "ffprobe -v error " +
			(config.timeReqStr?.let { "-read_intervals %$it " } ?: "") +
			"-select_streams v:0 -count_packets -show_entries stream=nb_read_packets -of csv=p=0 \"$path\""
		val frames = ProcessRunner.execForSingleResult(command)
		Log.d("frames= $frames   ${path.fileName}")
		return frames?.ifBlank { "0" }?.toInt()
	}

	private suspend fun getLanguagesForStreamType(path: Path, streamType: Char): List<String?> {
		val command =  "ffprobe \"$path\" -show_entries stream=index:stream_tags=language -select_streams $streamType -v 0 -of compact=p=0:nk=1"
		val list = ProcessRunner.execForMultiResult(command)
			?.map { idxLang -> idxLang.split("|").getOrNull(1) }
		Log.d("streamType=$streamType langs= $list   ${path.fileName}")
		return list.orEmpty()
	}


	suspend fun probeVideoSpecs(job: AacJob) {
		job.audioCodec = audioCodec(job.origPath)?.lowercase().orEmpty()
		job.totalFrames = getFrames(job.origPath) ?: Int.MAX_VALUE

		job.audioLangs = getLanguagesForStreamType(job.origPath, 'a')
		job.subtitleLangs = getLanguagesForStreamType(job.origPath, 's')

		val maxDuration = getDuration(job.origPath)

		job.totalDurationSecs = minOf(maxDuration, config.timeReqSecs ?: Float.MAX_VALUE)
		job.totalDuration = config.timeReqStr ?: toDurationStr(job.totalDurationSecs)

	}
}
