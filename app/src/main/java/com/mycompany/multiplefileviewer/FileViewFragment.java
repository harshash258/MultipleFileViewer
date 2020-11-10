package com.mycompany.multiplefileviewer;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;

public class FileViewFragment extends Fragment {

    TextView lineNumber, lineNumber2;
    EditText fileText, fileText2;
    String fileName = "", path;
    RelativeLayout relativeLayout, relativeLayout2;
    int height, width;
    boolean isShown = false;
    ImageButton save, openSecondFile, save2;
    TextView textView, textView2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_file_viewer, container, false);

        intialiseVariables(view);
        checkForSecondFile();

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            fileName = bundle.getString("fileName");
        }

        openFile(fileName, fileText);
        textView.setText(fileName);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;


        fileText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int lines = fileText.getLineCount();
                String linesNumber = "";
                for (int i = 1; i <= lines; i++) {
                    linesNumber = linesNumber + i + "\n";
                }
                lineNumber.setText(linesNumber);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        fileText2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int lines = fileText2.getLineCount();
                String linesNumber = "";
                for (int i = 1; i <= lines; i++) {
                    linesNumber = linesNumber + i + "\n";
                }
                lineNumber2.setText(linesNumber);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        openSecondFile.setOnClickListener(v -> {
            chooseFile();
        });

        save.setOnClickListener(v -> {
            saveChanges();
        });


        return view;
    }


    private void intialiseVariables(View view) {
        lineNumber = view.findViewById(R.id.lineNumber);
        lineNumber2 = view.findViewById(R.id.lineNumber2);

        fileText = view.findViewById(R.id.fileText);
        fileText2 = view.findViewById(R.id.fileText2);

        relativeLayout = view.findViewById(R.id.relativeLayout);
        relativeLayout2 = view.findViewById(R.id.relativeLayout2);

        save = view.findViewById(R.id.save);
        save2 = view.findViewById(R.id.save2);

        openSecondFile = view.findViewById(R.id.openAnotherFile);

        textView = view.findViewById(R.id.fileName);
        textView2 = view.findViewById(R.id.fileName2);


    }

    private void openFile(String fileName, EditText editText) {

        File root = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));
        File file = new File(root, fileName);
        try {
            FileInputStream fis = new FileInputStream(file);
            int c;
            StringBuilder temp = new StringBuilder();

            while ((c = fis.read()) != -1) {
                temp.append((char) c);

            }
            editText.setText(temp);
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error Occurred in opening File", Toast.LENGTH_SHORT).show();
        }
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

    private void saveChanges() {
        File root = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));
        File file = new File(root, fileName);
        String temp = fileText.getText().toString();
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(temp);
            writer.flush();
            writer.close();
            Toast.makeText(getContext(), "Changes Saved.", Toast.LENGTH_SHORT).show();

        } catch (FileNotFoundException e) {
            Toast.makeText(getContext(), "File Not Found!!!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error Occurred while saving File. Please try Again.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void checkForSecondFile() {
        if (isShown) {
            openSecondFile.setEnabled(false);
            openSecondFile.setImageAlpha(133);
        } else {
            openSecondFile.setEnabled(true);
            openSecondFile.setImageAlpha(255);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            String uriString = uri.toString();
            File myFile = new File(uriString);
            String path = myFile.getAbsolutePath();
            String displayName = null;

            if (uriString.startsWith("content://")) {
                Cursor cursor = null;
                try {
                    cursor = getContext().getContentResolver().query(uri, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } finally {
                    cursor.close();
                }
            } else if (uriString.startsWith("file://")) {
                displayName = myFile.getName();
            }

            openFile(displayName, fileText2);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) relativeLayout.getLayoutParams();
            params.height = height / 2;
            params.width = width / 2;
            relativeLayout2.setVisibility(View.VISIBLE);
            isShown = true;
            checkForSecondFile();

        }
    }
}

