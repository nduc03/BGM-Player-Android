package app.nduc.bgmplayer

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
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
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import app.nduc.bgmplayer.ui.theme.BGMPlayerTheme

class MainActivity : ComponentActivity() {
    private lateinit var player: Player

    private var introFileName by mutableStateOf("empty")
    private var loopFileName by mutableStateOf("empty")

    private var stopCalled = false
    private var isCurrentModeBgm = false
    private val bgmNextTrackListener = object : Player.Listener {
        override fun onMediaItemTransition(
            mediaItem: MediaItem?,
            @Player.MediaItemTransitionReason reason: Int,
        ) {
            player.repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    private var requestIntro = true
    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri == null) return@registerForActivityResult
            if (requestIntro) {
                BgmFilesManager.createNewIntro(this, uri)
                introFileName = BgmFilesManager.introDisplayName

            } else {
                BgmFilesManager.createNewLoop(this, uri)
                loopFileName = BgmFilesManager.loopDisplayName
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
                        Text(text = "Intro file name: $introFileName")
                        ButtonWithText("Set loop file") {
                            openWavAudioFile(false)
                        }
                        Text(text = "Loop file name: $loopFileName")
                        ButtonWithText("Play") { onClickPlay() }
                        ButtonWithText("Pause") { pause() }
                        ButtonWithText("Stop") { stop() }
                        ButtonWithText("Clear") { clearPlaylist() }
                    }
                }
            }
        }

        BgmFilesManager.loadDisplayName(this)
        if (BgmFilesManager.checkIntro(this))
            introFileName = BgmFilesManager.introDisplayName
        if (BgmFilesManager.checkLoop(this))
            loopFileName = BgmFilesManager.loopDisplayName

        player = ExoPlayer.Builder(this).build()
        setOffloadPlayback()
    }

    private fun isEmptyFilename(filename: String): Boolean {
        return filename == "" || filename == "empty"
    }

    private fun onClickPlay() {
        if (isEmptyFilename(introFileName) && isEmptyFilename(loopFileName)) {
            Toast.makeText(this, "No file to play!", Toast.LENGTH_LONG).show()
            return
        }
        if (!isEmptyFilename(introFileName) && !isEmptyFilename(loopFileName)) {
            playBgm(BgmFilesManager.getIntroUri(this)!!, BgmFilesManager.getLoopUri(this)!!)
            return
        }
        if (!isEmptyFilename(introFileName)) {
            playLoop(BgmFilesManager.getIntroUri(this)!!)
            return
        }
        playLoop(BgmFilesManager.getLoopUri(this)!!)
    }

    private fun playBgm(introUri: Uri, loopUri: Uri) {
        val intro = MediaItem.fromUri(introUri)
        val loop = MediaItem.fromUri(loopUri)
        player.addMediaItem(0, intro)
        player.addMediaItem(1, loop)
        player.addListener(bgmNextTrackListener)
        player.prepare()
        player.play()
        isCurrentModeBgm = true
        stopCalled = false
    }

    private fun playLoop(loopUri: Uri) {
        val loop = MediaItem.fromUri(loopUri)
        player.addMediaItem(loop)
        player.repeatMode = Player.REPEAT_MODE_ONE
        player.prepare()
        player.play()
        stopCalled = false
    }

    private fun pause() {
        if (player.isPlaying) player.pause()
    }

    private fun stop() {
        if (player.isPlaying) player.stop()
        stopCalled = true
    }

    private fun clearPlaylist() {
        if (!stopCalled) {
            stop()
            stopCalled = false
        }
        player.clearMediaItems()
        if (isCurrentModeBgm) player.removeListener(bgmNextTrackListener)
        player.repeatMode = Player.REPEAT_MODE_OFF
        isCurrentModeBgm = false
    }

    private fun openWavAudioFile(isRequestIntro: Boolean) {
        requestIntro = isRequestIntro
        getContent.launch("audio/x-wav")
    }

    /**
     * For battery saving
     */
    @OptIn(UnstableApi::class)
    private fun setOffloadPlayback() {
        val audioOffloadPreferences =
            TrackSelectionParameters.AudioOffloadPreferences.Builder()
                .setAudioOffloadMode(TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_ENABLED)
                .setIsGaplessSupportRequired(true)
                .build()
        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .setAudioOffloadPreferences(audioOffloadPreferences)
            .build()

    }
}

@Composable
fun ButtonWithText(buttonText: String, onclick: () -> Unit) {
    Button(onClick = onclick) {
        Text(text = buttonText)
    }
}