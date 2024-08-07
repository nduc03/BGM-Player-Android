package app.nduc.bgmplayer

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.IOException

object Utils {
    fun getFileNameFromContentUri(context: Context, contentUri: Uri): String {
        var res = "empty"
        try {
            contentUri.let {
                context.contentResolver.query(it, null, null, null, null)
            }?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                res = cursor.getString(nameIndex)
            }
        } catch (_: SecurityException) {
            // TODO notify uri inaccessible
        }
        return res
    }

    fun isValidWavFile(inputFile: File?): Boolean {
        if (inputFile == null) return false
        val inputStream = inputFile.inputStream()
        try {
            val header = ByteArray(44)
            if (inputStream.read(header) != 44) {
                return false
            }

            val riff = String(header, 0, 4)
            val wave = String(header, 8, 4)
            val fmt = String(header, 12, 4)
            val data = String(header, 36, 4)

            return riff == "RIFF" && wave == "WAVE" && fmt == "fmt " && data == "data"
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }
}