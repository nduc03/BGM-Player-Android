package app.nduc.bgmplayer

import org.junit.Test

import java.io.File

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        Utils.isValidWavFile(File("D:\\Projects\\Pet_project\\Android\\BGM_Player\\app\\src\\test\\java\\app\\nduc\\bgmplayer\\test.wav"))
    }
}