package app.nduc.bgmplayer

import android.content.ComponentName
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import app.nduc.bgmplayer.ui.theme.BGMPlayerTheme
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

class MainActivity : ComponentActivity() {
    private lateinit var player: Player
    private lateinit var controllerFuture: ListenableFuture<MediaController>

    private var introDisplayName by mutableStateOf(GlobalConst.EMPTY_DISPLAY_NAME)
    private var loopDisplayName by mutableStateOf(GlobalConst.EMPTY_DISPLAY_NAME)

    private var isCurrentModeBgm = false
    private val bgmNextTrackListener = object : Player.Listener {
        override fun onMediaItemTransition(
            mediaItem: MediaItem?,
            @Player.MediaItemTransitionReason reason: Int,
        ) {
            // TODO check that only repeat if index is 1
            player.repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    private var requestIntro = true
    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri == null) return@registerForActivityResult
            if (requestIntro) {
                BgmFilesManager.createNewIntro(this, uri)
                introDisplayName = BgmFilesManager.introFileName

            } else {
                BgmFilesManager.createNewLoop(this, uri)
                loopDisplayName = BgmFilesManager.loopFileName
            }
            BgmFilesManager.saveDisplayName(this)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BGMPlayerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column {
                        Text(
                            text = "BGM player",
                            modifier = Modifier.padding(innerPadding)
                        )
                        ButtonWithText("Set intro file") {
                            openWavAudioFile(true)
                        }
                        Text(text = "Intro file name: $introDisplayName")
                        ButtonWithText("Set loop file") {
                            openWavAudioFile(false)
                        }
                        Text(text = "Loop file name: $loopDisplayName")
                        ButtonWithText("Play") { onClickPlay() }
                        ButtonWithText("Pause") { pause() }
                        ButtonWithText("Stop and Clear playlist") { stopAndClearPlaylist() }
                    }
                }
            }
        }

        BgmFilesManager.loadDisplayName(this)
        if (BgmFilesManager.checkIntro(this))
            introDisplayName = BgmFilesManager.introFileName
        if (BgmFilesManager.checkLoop(this))
            loopDisplayName = BgmFilesManager.loopFileName
    }

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(this, ComponentName(this, BgmPlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                player = controllerFuture.get()
            },
            MoreExecutors.directExecutor()
        )
    }

    override fun onStop() {
        MediaController.releaseFuture(controllerFuture)
        super.onStop()
    }

    private fun isEmptyFilename(filename: String): Boolean {
        return filename == "" || filename == GlobalConst.EMPTY_DISPLAY_NAME
    }

    private fun onClickPlay() {
        if (player.isPlaying) {
            player.play()
            return
        }

        if (isEmptyFilename(introDisplayName) && isEmptyFilename(loopDisplayName)) {
            Toast.makeText(this, getString(R.string.notify_no_file_selected), Toast.LENGTH_LONG)
                .show()
            return
        }
        if (!isEmptyFilename(introDisplayName) && !isEmptyFilename(loopDisplayName)) {
            playNewBgm(BgmFilesManager.getIntroUri(this)!!, BgmFilesManager.getLoopUri(this)!!)
            return
        }
        if (!isEmptyFilename(introDisplayName)) {
            playNewLoop(BgmFilesManager.getIntroUri(this)!!)
            return
        }
        playNewLoop(BgmFilesManager.getLoopUri(this)!!)
    }

    private fun playNewBgm(introUri: Uri, loopUri: Uri) {
        val intro = MediaItem.fromUri(introUri)
        val loop = MediaItem.fromUri(loopUri)
        player.addMediaItem(0, intro)
        player.addMediaItem(1, loop)
        player.addListener(bgmNextTrackListener)
        player.prepare()
        player.play()
        isCurrentModeBgm = true
    }

    private fun playNewLoop(loopUri: Uri) {
        val loop = MediaItem.fromUri(loopUri)
        player.addMediaItem(loop)
        player.repeatMode = Player.REPEAT_MODE_ONE
        player.prepare()
        player.play()
    }

    private fun pause() {
        if (player.isPlaying) player.pause()
    }

    private fun stopAndClearPlaylist() {
        player.stop()
        player.clearMediaItems()
        if (isCurrentModeBgm) player.removeListener(bgmNextTrackListener)
        player.repeatMode = Player.REPEAT_MODE_OFF
        isCurrentModeBgm = false
    }

    private fun openWavAudioFile(isRequestIntro: Boolean) {
        requestIntro = isRequestIntro
        getContent.launch("audio/x-wav")
    }
}

@Composable
fun ButtonWithText(buttonText: String, onclick: () -> Unit) {
    Button(onClick = onclick) {
        Text(text = buttonText)
    }
}