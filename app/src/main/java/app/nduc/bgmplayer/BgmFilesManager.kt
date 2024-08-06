package app.nduc.bgmplayer

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import java.io.File

object BgmFilesManager {
    private const val INTRO_FILENAME = "intro.wav"
    private const val LOOP_FILENAME = "loop.wav"
    private const val DATA_FILENAME = "data.txt"

    var introDisplayName = "empty"
        private set
    var loopDisplayName = "empty"
        private set

    fun getDisplayNamePattern() {
        TODO()
    }

    fun checkIntro(context: Context): Boolean {
        return context.fileList().contains(INTRO_FILENAME) &&
                Utils.isValidWavFile(File(context.filesDir, INTRO_FILENAME))
    }

    fun checkLoop(context: Context): Boolean {
        return context.fileList().contains(LOOP_FILENAME) &&
                Utils.isValidWavFile(File(context.filesDir, LOOP_FILENAME))
    }

    fun getIntroUri(context: Context): Uri? {
        val file = File(context.filesDir, INTRO_FILENAME)
        if (file.exists()) return file.toUri()
        return null
    }

    fun getLoopUri(context: Context): Uri? {
        val file = File(context.filesDir, LOOP_FILENAME)
        if (file.exists()) return file.toUri()
        return null
    }

    fun createNewIntro(context: Context, contentUri: Uri) {
        introDisplayName = Utils.getFileNameFromContentUri(context, contentUri)
        context.contentResolver.openInputStream(contentUri)?.use { content ->
            context.openFileOutput(INTRO_FILENAME, Context.MODE_PRIVATE)?.use {
                it.write(content.readBytes())
            }
        }
    }

    fun createNewLoop(context: Context, contentUri: Uri) {
        loopDisplayName = Utils.getFileNameFromContentUri(context, contentUri)
        context.contentResolver.openInputStream(contentUri)?.use { content ->
            context.openFileOutput(LOOP_FILENAME, Context.MODE_PRIVATE)?.use {
                it.write(content.readBytes())
            }
        }
    }

    /**
     * Should only use when app initialize, this function might take long time to execute
     */
    fun loadDisplayName(context: Context) {
        if (!context.fileList().contains(DATA_FILENAME)) return
        context.openFileInput(DATA_FILENAME)?.use {
            val listName = String(it.readBytes()).split('\n')
            if (listName.size != 2) return
            if (checkIntro(context)) introDisplayName = listName[0]
            if (checkLoop(context)) loopDisplayName = listName[1]
        }
    }

    fun saveDisplayName(context: Context) {
        context.openFileOutput(DATA_FILENAME, Context.MODE_PRIVATE)?.use {
            it.write("$introDisplayName\n$loopDisplayName".toByteArray())
        }
    }
}