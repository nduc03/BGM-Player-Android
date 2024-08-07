package app.nduc.bgmplayer

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import java.io.File

object BgmFilesManager {
    private const val INTRO_FILE = "intro.wav"
    private const val LOOP_FILE = "loop.wav"
    private const val DATA_FILE = "data.txt"

    var introFileName = GlobalConst.EMPTY_DISPLAY_NAME
        private set
    var loopFileName = GlobalConst.EMPTY_DISPLAY_NAME
        private set

    fun getDisplayNamePattern() {
        TODO()
    }

    fun checkIntro(context: Context): Boolean {
        return context.fileList().contains(INTRO_FILE) &&
                Utils.isValidWavFile(File(context.filesDir, INTRO_FILE))
    }

    fun checkLoop(context: Context): Boolean {
        return context.fileList().contains(LOOP_FILE) &&
                Utils.isValidWavFile(File(context.filesDir, LOOP_FILE))
    }

    fun getIntroUri(context: Context): Uri? {
        val file = File(context.filesDir, INTRO_FILE)
        if (file.exists()) return file.toUri()
        return null
    }

    fun getLoopUri(context: Context): Uri? {
        val file = File(context.filesDir, LOOP_FILE)
        if (file.exists()) return file.toUri()
        return null
    }

    fun createNewIntro(context: Context, contentUri: Uri) {
        introFileName = Utils.getFileNameFromContentUri(context, contentUri)
        context.contentResolver.openInputStream(contentUri)?.use { content ->
            context.openFileOutput(INTRO_FILE, Context.MODE_PRIVATE)?.use {
                it.write(content.readBytes())
            }
        }
    }

    fun createNewLoop(context: Context, contentUri: Uri) {
        loopFileName = Utils.getFileNameFromContentUri(context, contentUri)
        context.contentResolver.openInputStream(contentUri)?.use { content ->
            context.openFileOutput(LOOP_FILE, Context.MODE_PRIVATE)?.use {
                it.write(content.readBytes())
            }
        }
    }

    /**
     * Should only use when app initialize, this function might take long time to execute
     */
    fun loadDisplayName(context: Context) {
        if (!context.fileList().contains(DATA_FILE)) return
        context.openFileInput(DATA_FILE)?.use {
            val listName = String(it.readBytes()).split('\n')
            if (listName.size != 2) return
            if (checkIntro(context)) introFileName = listName[0]
            if (checkLoop(context)) loopFileName = listName[1]
        }
    }

    fun saveDisplayName(context: Context) {
        context.openFileOutput(DATA_FILE, Context.MODE_PRIVATE)?.use {
            it.write("$introFileName\n$loopFileName".toByteArray())
        }
    }
}