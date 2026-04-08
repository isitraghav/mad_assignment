package com.raghav.gallery;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.documentfile.provider.DocumentFile;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageDetailActivity extends AppCompatActivity {

    private String imageUriString;
    private String imageName;
    private long imageSize;
    private long imageDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_image_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detail_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        imageUriString = getIntent().getStringExtra("IMAGE_URI");
        imageName = getIntent().getStringExtra("IMAGE_NAME");
        imageSize = getIntent().getLongExtra("IMAGE_SIZE", 0);
        imageDate = getIntent().getLongExtra("IMAGE_DATE", 0);

        ImageView ivDetail = findViewById(R.id.ivDetail);
        TextView tvName = findViewById(R.id.tvName);
        TextView tvPath = findViewById(R.id.tvPath);
        TextView tvSize = findViewById(R.id.tvSize);
        TextView tvDate = findViewById(R.id.tvDate);
        MaterialButton btnDelete = findViewById(R.id.btnDelete);

        Glide.with(this).load(Uri.parse(imageUriString)).into(ivDetail);

        tvName.setText(getString(R.string.image_name, imageName));
        tvPath.setText(getString(R.string.image_path, imageUriString));
        tvSize.setText(getString(R.string.image_size, imageSize / 1024));
        tvDate.setText(getString(R.string.image_date,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(imageDate))));

        btnDelete.setOnClickListener(v -> showDeleteDialog());
    }

    private void showDeleteDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteImage())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteImage() {
        Uri uri = Uri.parse(imageUriString);
        DocumentFile file = DocumentFile.fromSingleUri(this, uri);
        if (file != null && file.delete()) {
            Toast.makeText(this, R.string.image_deleted, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, R.string.delete_failed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
