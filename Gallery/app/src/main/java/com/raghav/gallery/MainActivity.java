package com.raghav.gallery;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_CAMERA_PERMISSION = 1;
    private static final int REQ_CAPTURE = 2;
    private static final int REQ_DETAIL = 3;

    private static final String PREFS_NAME = "gallery_prefs";
    private static final String PREF_FOLDER_URI = "folder_uri";

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ImageAdapter imageAdapter;
    private final List<DocumentFile> imageList = new ArrayList<>();
    private File tempPhotoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);

        imageAdapter = new ImageAdapter(this, imageList, position -> {
            DocumentFile clicked = imageList.get(position);
            Intent intent = new Intent(this, ImageDetailActivity.class);
            intent.putExtra("IMAGE_URI", clicked.getUri().toString());
            intent.putExtra("IMAGE_NAME", clicked.getName());
            intent.putExtra("IMAGE_SIZE", clicked.length());
            intent.putExtra("IMAGE_DATE", clicked.lastModified());
            startActivityForResult(intent, REQ_DETAIL);
        });

        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(imageAdapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> onFabClicked());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadImages();
    }

    private void onFabClicked() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (prefs.getString(PREF_FOLDER_URI, null) == null) {
            Toast.makeText(this, R.string.set_folder_first, Toast.LENGTH_SHORT).show();
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQ_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CAMERA_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            Toast.makeText(this, R.string.permission_required, Toast.LENGTH_SHORT).show();
        }
    }

    private void launchCamera() {
        tempPhotoFile = new File(getCacheDir(), "temp_capture.jpg");
        Uri photoUri = FileProvider.getUriForFile(this, "com.raghav.gallery.fileprovider", tempPhotoFile);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(intent, REQ_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CAPTURE && resultCode == RESULT_OK) {
            savePhotoToFolder();
        }
        if (requestCode == REQ_DETAIL && resultCode == RESULT_OK) {
            loadImages();
        }
    }

    private void savePhotoToFolder() {
        String folderUriString = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(PREF_FOLDER_URI, null);
        if (tempPhotoFile == null || !tempPhotoFile.exists() || folderUriString == null) {
            Toast.makeText(this, R.string.photo_save_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        DocumentFile folder = DocumentFile.fromTreeUri(this, Uri.parse(folderUriString));
        if (folder == null || !folder.canWrite()) {
            Toast.makeText(this, R.string.photo_save_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
        DocumentFile newFile = folder.createFile("image/jpeg", fileName);
        if (newFile == null) {
            Toast.makeText(this, R.string.photo_save_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        try (OutputStream out = getContentResolver().openOutputStream(newFile.getUri());
             FileInputStream in = new FileInputStream(tempPhotoFile)) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            tempPhotoFile.delete();
            Toast.makeText(this, R.string.photo_saved, Toast.LENGTH_SHORT).show();
            loadImages();
        } catch (IOException e) {
            Toast.makeText(this, R.string.photo_save_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadImages() {
        String folderUriString = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(PREF_FOLDER_URI, null);
        imageList.clear();
        if (folderUriString == null) {
            imageAdapter.notifyDataSetChanged();
            tvEmpty.setText(R.string.no_folder_set);
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }
        DocumentFile folder = DocumentFile.fromTreeUri(this, Uri.parse(folderUriString));
        if (folder != null) {
            for (DocumentFile file : folder.listFiles()) {
                String mimeType = file.getType();
                if (mimeType != null && mimeType.startsWith("image/")) {
                    imageList.add(file);
                }
            }
        }
        imageAdapter.notifyDataSetChanged();
        tvEmpty.setText(R.string.no_images);
        tvEmpty.setVisibility(imageList.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(imageList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
        private final Context context;
        private final List<DocumentFile> items;
        private final OnItemClickListener listener;

        interface OnItemClickListener {
            void onItemClick(int position);
        }

        ImageAdapter(Context context, List<DocumentFile> items, OnItemClickListener listener) {
            this.context = context;
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Glide.with(context)
                    .load(items.get(position).getUri())
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.imageView);
            holder.itemView.setOnClickListener(v -> {
                int pos = holder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_ID) listener.onItemClick(pos);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView imageView;
            ViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView);
            }
        }
    }
}
