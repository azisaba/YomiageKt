package net.azisaba.yomiagekt.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

object FfmpegUtil {
    suspend fun createOtowareFile(inputFile: File): File =
        withContext(Dispatchers.IO) {
            val clippedFile = File.createTempFile("yomiagekt-otoware-clipped", ".wav")
            val outputFile = File.createTempFile("yomiagekt-otoware-safe", ".wav")

            try {
                runFfmpeg(
                    inputFile.absolutePath,
                    "volume=91dB",
                    clippedFile.absolutePath,
                )
                runFfmpeg(
                    clippedFile.absolutePath,
                    "volume=-28dB",
                    outputFile.absolutePath,
                )
                outputFile
            } catch (e: Exception) {
                outputFile.delete()
                throw e
            } finally {
                clippedFile.delete()
            }
        }

    private fun runFfmpeg(
        inputPath: String,
        audioFilter: String,
        outputPath: String,
    ) {
        val process =
            ProcessBuilder(
                "ffmpeg",
                "-y",
                "-loglevel",
                "error",
                "-i",
                inputPath,
                "-af",
                audioFilter,
                "-c:a",
                "pcm_s16le",
                outputPath,
            ).redirectErrorStream(true)
                .start()

        val output = process.inputStream.bufferedReader().use { it.readText() }
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            File(outputPath).delete()
            throw IOException("ffmpeg exited with code $exitCode: $output")
        }
    }
}
