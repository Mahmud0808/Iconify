package com.drdisagree.iconify.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.common.Resources;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {

    public static final String DATA_DIR = Iconify.getAppContext().getFilesDir().toString();

    public static void copyAssets(String assetFolder) throws IOException {
        cleanDir(assetFolder);
        createDir(assetFolder);
        copyFileOrDirectory(Iconify.getAppContext(), assetFolder, DATA_DIR + "/" + assetFolder);
    }

    public static void cleanDir(String dirName) {
        Shell.cmd("rm -rf " + DATA_DIR + "/" + dirName).exec();
    }

    private static void createDir(String dirName) {
        File new_folder = new File(DATA_DIR + "/" + dirName + "/");
        new_folder.mkdirs();
    }

    private static void copyFileOrDirectory(Context context, String dirName, String outPath) throws IOException {
        String[] srcFiles = context.getAssets().list(dirName);
        for (String srcFileName : srcFiles) {
            String outFileName = outPath + File.separator + srcFileName;
            String inFileName = dirName + File.separator + srcFileName;
            if (dirName.equals("")) {
                inFileName = srcFileName;
            }
            try {
                InputStream inputStream = context.getAssets().open(inFileName);
                copyAndClose(inputStream, new FileOutputStream(outFileName));
            } catch (IOException e) {
                new File(outFileName).mkdir();
                copyFileOrDirectory(context, inFileName, outFileName);
            }
        }
    }

    public static void closeQuietly(AutoCloseable autoCloseable) {
        try {
            if (autoCloseable != null) {
                autoCloseable.close();
            }
        } catch (Exception ignored) {
        }
    }

    public static void copyAndClose(InputStream input, OutputStream output) throws IOException {
        copy(input, output);
        closeQuietly(input);
        closeQuietly(output);
    }

    public static void copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
    }

    public static void copyToIconifyHiddenDir(Context context, int requestCode, int resultCode, Intent data, int PICKFILE_RESULT_CODE, String fileName, Button enableButton) {
        if (data == null) return;

        if (requestCode == PICKFILE_RESULT_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String source = getRealPathFromURI(uri);
            if (source == null) {
                Toast.makeText(Iconify.getAppContext(), context.getResources().getString(R.string.toast_rename_file), Toast.LENGTH_SHORT).show();
                return;
            }

            String destination = Resources.XPOSED_RESOURCE_TEMP_DIR + "/" + fileName;

            Shell.cmd("mkdir -p " + Resources.XPOSED_RESOURCE_TEMP_DIR).exec();

            if (Shell.cmd("cp \"" + source + "\" \"" + destination + "\"").exec().isSuccess())
                enableButton.setVisibility(View.VISIBLE);
            else
                Toast.makeText(Iconify.getAppContext(), context.getResources().getString(R.string.toast_rename_file), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(Iconify.getAppContext(), context.getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
        }
    }

    private static String getRealPathFromURI(Uri uri) {
        File file = null;
        try {
            @SuppressLint("Recycle") Cursor returnCursor = Iconify.getAppContext().getContentResolver().query(uri, null, null, null, null);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            String name = returnCursor.getString(nameIndex);
            file = new File(Iconify.getAppContext().getFilesDir(), name);
            @SuppressLint("Recycle") InputStream inputStream = Iconify.getAppContext().getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1024 * 1024;
            int bytesAvailable = inputStream.available();
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return file.getPath();
    }
}
