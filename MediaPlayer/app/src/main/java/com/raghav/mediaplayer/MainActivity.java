package com.raghav.mediaplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private TextView tvFileName;
    private MaterialButton btnPlay, btnPause, btnStop, btnRestart;

    private final ActivityResultLauncher<String> filePicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    loadAudio(uri);
                }
            });

    private final ActivityResultLauncher<String> permissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    filePicker.launch("audio/*");
                } else {
                    Toast.makeText(this, "Storage permission is needed to open audio files", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvFileName = findViewById(R.id.tvFileName);
        btnPlay = findViewById(R.id.btnPlay);
        btnPause = findViewById(R.id.btnPause);
        btnStop = findViewById(R.id.btnStop);
        btnRestart = findViewById(R.id.btnRestart);
        MaterialButton btnOpenFile = findViewById(R.id.btnOpenFile);

        btnOpenFile.setOnClickListener(v -> openFile());
        btnPlay.setOnClickListener(v -> mediaPlayer.start());
        btnPause.setOnClickListener(v -> mediaPlayer.pause());

        btnStop.setOnClickListener(v -> {
            mediaPlayer.stop();
            try {
                mediaPlayer.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        btnRestart.setOnClickListener(v -> {
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
        });
    }

    private void openFile() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_AUDIO
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            filePicker.launch("audio/*");
        } else {
            permissionRequest.launch(permission);
        }
    }

    private void loadAudio(Uri uri) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(this, uri);
            mediaPlayer.prepare();

            String fileName = uri.getLastPathSegment();
            tvFileName.setText(fileName != null ? fileName : uri.toString());

            btnPlay.setEnabled(true);
            btnPause.setEnabled(true);
            btnStop.setEnabled(true);
            btnRestart.setEnabled(true);

            mediaPlayer.setOnCompletionListener(mp -> {
                try {
                    mp.prepare();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Could not load the file", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
