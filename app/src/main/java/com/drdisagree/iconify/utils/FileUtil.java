package com.drdisagree.iconify.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.activity.result.ActivityResultLauncher;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.common.Resources;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

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
        if (srcFiles == null) return;

        for (String srcFileName : srcFiles) {
            String outFileName = outPath + File.separator + srcFileName;
            String inFileName = dirName + File.separator + srcFileName;
            if (dirName.equals("")) {
                inFileName = srcFileName;
            }
            try {
                InputStream inputStream = context.getAssets().open(inFileName);
                copyAndClose(inputStream, Files.newOutputStream(Paths.get(outFileName)));
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

    public static String getRealPath(Object obj) {
        if (obj instanceof Intent) {
            return getRealPathFromURI(((Intent) obj).getData());
        } else if (obj instanceof Uri) {
            return getRealPathFromURI((Uri) obj);
        } else {
            throw new IllegalArgumentException("Object must be an Intent or Uri");
        }
    }

    private static String getRealPathFromURI(Uri uri) {
        File file;
        try {
            @SuppressLint("Recycle") Cursor returnCursor = Iconify.getAppContext().getContentResolver().query(uri, null, null, null, null);

            if (returnCursor == null) return null;

            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            String name = returnCursor.getString(nameIndex);
            file = new File(Iconify.getAppContext().getFilesDir(), name);
            @SuppressLint("Recycle") InputStream inputStream = Iconify.getAppContext().getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read;
            int maxBufferSize = 1024 * 1024;

            if (inputStream == null) return null;

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

    public static boolean moveToIconifyHiddenDir(String source, String destination) {
        return Shell.cmd("mkdir -p " + Resources.XPOSED_RESOURCE_TEMP_DIR, "rm -f \"" + destination + "\"", "mv -f \"" + source + "\" \"" + destination + "\"").exec().isSuccess();
    }

    public static void launchFilePicker(ActivityResultLauncher<Intent> launcher, String type) {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType(type);
        launcher.launch(chooseFile);
    }
}
