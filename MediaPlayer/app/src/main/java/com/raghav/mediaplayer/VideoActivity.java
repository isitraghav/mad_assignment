package com.raghav.mediaplayer;

import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class VideoActivity extends AppCompatActivity {

    private ExoPlayer player;
    private MaterialButton btnPlay, btnPause, btnStop, btnRestart;

    @Override
    @OptIn(markerClass = UnstableApi.class)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        PlayerView playerView = findViewById(R.id.playerView);
        btnPlay = findViewById(R.id.btnPlay);
        btnPause = findViewById(R.id.btnPause);
        btnStop = findViewById(R.id.btnStop);
        btnRestart = findViewById(R.id.btnRestart);
        TextInputEditText etUrl = findViewById(R.id.etUrl);
        etUrl.setText("https://www.w3schools.com/tags/mov_bbb.mp4");
        MaterialButton btnOpenUrl = findViewById(R.id.btnOpenUrl);

        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this)
                .setEnableDecoderFallback(true);
        player = new ExoPlayer.Builder(this, renderersFactory).build();
        playerView.setPlayer(player);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY) {
                    btnPlay.setEnabled(true);
                    btnPause.setEnabled(true);
                    btnStop.setEnabled(true);
                    btnRestart.setEnabled(true);
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Toast.makeText(VideoActivity.this, "Could not play this URL", Toast.LENGTH_SHORT).show();
            }
        });

        btnOpenUrl.setOnClickListener(v -> {
            String url = etUrl.getText() != null ? etUrl.getText().toString().trim() : "";
            if (url.isEmpty()) {
                Toast.makeText(this, "Please enter a URL", Toast.LENGTH_SHORT).show();
                return;
            }
            hideKeyboard(etUrl);
            loadVideo(url);
        });

        btnPlay.setOnClickListener(v -> player.play());
        btnPause.setOnClickListener(v -> player.pause());

        btnStop.setOnClickListener(v -> {
            player.stop();
            player.clearMediaItems();
            btnPlay.setEnabled(false);
            btnPause.setEnabled(false);
            btnStop.setEnabled(false);
            btnRestart.setEnabled(false);
        });

        btnRestart.setOnClickListener(v -> {
            player.seekTo(0);
            player.play();
        });
    }

    private void loadVideo(String url) {
        player.setMediaItem(MediaItem.fromUri(url));
        player.prepare();
        player.play();
    }

    private void hideKeyboard(TextInputEditText view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        player.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
        player = null;
    }
}
