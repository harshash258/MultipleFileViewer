package com.mycompany.multiplefileviewer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final int PERMISSION_CODE = 101;
    public static final String TAG = "";
    DrawerLayout drawer;
    NavigationView navigationView;
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawer = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.navigation);
        toolbar = findViewById(R.id.toolbar);

        requestPermission();

        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_open, R.string.navigation_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame,
                    new EmptyFragment()).commit();
            navigationView.setCheckedItem(R.id.rateUs);
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Please Grant Permission to Continue", Toast.LENGTH_SHORT).show();
            requestPermission();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.newFile:
                createNewFile();
                break;
            case R.id.openFile:
                chooseFile();
                break;
            case R.id.suggest:
                Intent intent = new Intent(this, SuggestFeatures.class);
                startActivity(intent);
                break;
            case R.id.rateUs:
                break;
        }
        return false;
    }

    private void createNewFile() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.create_new_file, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();
        TextInputLayout inputEditText = view.findViewById(R.id.fileName);
        Button button = view.findViewById(R.id.createFile);

        button.setOnClickListener(v -> {
            String sFileName = inputEditText.getEditText().getText().toString();
            String fileName = "";
            try {
                File root = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));

                if (!root.exists()) {
                    root.mkdirs();
                }
                File gpxfile = new File(root, sFileName + ".txt");
                fileName = sFileName + ".txt";
                if (!gpxfile.exists()) {
                    gpxfile.createNewFile();
                    Toast.makeText(this, "File Created", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "createNewFile: " + fileName);
                } else {
                    Toast.makeText(this, "File Already Exist.", Toast.LENGTH_SHORT).show();
                    createNewFile();
                }
            } catch (Exception exception) {
                Toast.makeText(this, "Error in Creating File" + exception.getMessage(), Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();

            FileViewFragment fragment = new FileViewFragment();
            Bundle bundle = new Bundle();
            bundle.putString("fileName", fileName);
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.frame,
                    fragment).commit();
            navigationView.setCheckedItem(R.id.openFile);
        });
    }

    private void chooseFile() {
        String[] mimeTypes =
                {
                        "text/plain",
                };

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(Intent.createChooser(intent, "Choose File"), 101);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            String uriString = uri.toString();
            File myFile = new File(uriString);
            String path = myFile.getAbsolutePath();
            String displayName = null;

            if (uriString.startsWith("content://")) {
                Cursor cursor = null;
                try {
                    cursor = getContentResolver().query(uri, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } finally {
                    cursor.close();
                }
            } else if (uriString.startsWith("file://")) {
                displayName = myFile.getName();
            }
            FileViewFragment fragment = new FileViewFragment();
            Bundle bundle = new Bundle();
            bundle.putString("fileName", displayName);
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.frame,
                    fragment).commit();
            navigationView.setCheckedItem(R.id.openFile);

        }
    }
}