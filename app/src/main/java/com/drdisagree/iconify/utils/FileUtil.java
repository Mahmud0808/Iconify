package com.drdisagree.iconify.utils;

import android.content.Context;

import com.drdisagree.iconify.Iconify;
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
            ;
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
}
