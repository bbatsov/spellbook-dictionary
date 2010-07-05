package com.drowltd.spellbook.util;

import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A simple util class for dealing with archives.
 *
 * @author Miroslava Stancheva
 * @author Bozhidar Batsov
 * @since 0.3
 */
public class ArchiveUtils {
    private static final String ARCHIVED_DB_NAME = "spellbook-db.tar";
    private static final String DB_FILE_NAME = "spellbook.h2.db";

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveUtils.class);

    /**
     * Extract a tar.bz2 archive.
     *
     * @param pathToArchive the full path to the file to be extracted
     * @return the full path to the extracted file
     */
    public static String extractDbFromArchive(String pathToArchive) {
        // Get the current path, where the database will be extracted
        String currentPath = System.getProperty("user.home") + File.separator + ".spellbook" + File.separator;
        LOGGER.info("Current path: " + currentPath);

        try {
            //Open the archive
            FileInputStream archiveFileStream = new FileInputStream(pathToArchive);
            // Read two bytes from the stream before it used by CBZip2InputStream

            for (int i = 0; i < 2; i++) {
                archiveFileStream.read();
            }

            // Open the gzip file and open the output file
            CBZip2InputStream bz2 = new CBZip2InputStream(archiveFileStream);
            FileOutputStream out = new FileOutputStream(ARCHIVED_DB_NAME);

            LOGGER.info("Extracting the tar file...");
            // Transfer bytes from the compressed file to the output file
            byte[] buffer = new byte[1024];
            int len;
            while ((len = bz2.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

            // Close the file and stream
            bz2.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try {
            TarInputStream tarInputStream = null;
            TarEntry tarEntry;
            tarInputStream = new TarInputStream(new FileInputStream(ARCHIVED_DB_NAME));

            tarEntry = tarInputStream.getNextEntry();

            byte[] buf1 = new byte[1024];

            while (tarEntry != null) {
                //For each entry to be extracted
                String entryName = currentPath + tarEntry.getName();
                entryName = entryName.replace('/', File.separatorChar);
                entryName = entryName.replace('\\', File.separatorChar);

                LOGGER.info("Extracting entry: " + entryName);
                FileOutputStream fileOutputStream;
                File newFile = new File(entryName);
                if (tarEntry.isDirectory()) {
                    if (!newFile.mkdirs()) {
                        break;
                    }
                    tarEntry = tarInputStream.getNextEntry();
                    continue;
                }

                fileOutputStream = new FileOutputStream(entryName);
                int n;
                while ((n = tarInputStream.read(buf1, 0, 1024)) > -1) {
                    fileOutputStream.write(buf1, 0, n);
                }

                fileOutputStream.close();
                tarEntry = tarInputStream.getNextEntry();

            }
            tarInputStream.close();
        } catch (Exception e) {
        }

        currentPath += "db" + File.separator + DB_FILE_NAME;

        if (!currentPath.isEmpty()) {
            LOGGER.info("DB placed in : " + currentPath);
        }

        return currentPath;
    }
}
