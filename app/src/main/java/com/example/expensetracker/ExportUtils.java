package com.example.expensetracker;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class ExportUtils {

    public static boolean exportDatabase(Context context) {
        try {
            File dbFile = context.getDatabasePath("expenses.db");
            File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            File outFile = new File(exportDir, "expenses_backup.db");
            FileChannel src = new FileInputStream(dbFile).getChannel();
            FileChannel dst = new FileOutputStream(outFile).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
