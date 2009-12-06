package importer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nikolay Dimitrov
 */
public class TranscriptionCorrector {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        FileReader inputFile = null;
        FileWriter outputFile = null;
        int c = -1;

        GregorianCalendar currentTime = new GregorianCalendar();

        String fileArchiveName = "output_en_bg_archive_" + currentTime.get(Calendar.YEAR) + (currentTime.get(Calendar.MONTH) + 1) + currentTime.get(Calendar.DATE) + currentTime.get(Calendar.HOUR) + currentTime.get(Calendar.MINUTE) + currentTime.get(Calendar.SECOND) + ".txt";
        final String outputFilePath = "output_en_bg.txt";
        /*
         *
         * Copy the original file to archive.
         *
         */
        try {
            inputFile = new FileReader(outputFilePath);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TranscriptionCorrector.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            outputFile = new FileWriter(fileArchiveName);
        } catch (IOException ex) {
            Logger.getLogger(TranscriptionCorrector.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            c = inputFile.read();
        } catch (IOException ex) {
            Logger.getLogger(TranscriptionCorrector.class.getName()).log(Level.SEVERE, null, ex);
        }

        while (c != -1) {
            try {
                outputFile.write(c);
                c = inputFile.read();
            } catch (IOException ex) {
                Logger.getLogger(TranscriptionCorrector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        System.out.println(c);

        try {
            inputFile.close();
            outputFile.close();
        } catch (IOException ex) {
            Logger.getLogger(TranscriptionCorrector.class.getName()).log(Level.SEVERE, null, ex);
        }

        /*
         *
         * Copy and correction the archive to the new original file.
         *
         */

        try {
            inputFile = new FileReader(fileArchiveName);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TranscriptionCorrector.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            outputFile = new FileWriter(outputFilePath);
        } catch (IOException ex) {
            Logger.getLogger(TranscriptionCorrector.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            c = inputFile.read();
        } catch (IOException ex) {
            Logger.getLogger(TranscriptionCorrector.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(c);
        while (c != -1) {
            try {
                switch (c) {
                    case 164:
                        c = 331;
                        break;
                    case 166:
                        c = 643;
                        break;
                    case 167:
                        c = 652;
                        break;
                    case 169:
                        c = 658;
                        break;
                    case 1025:
                        c = 603;
                        break;
                    case 1032:
                        c = 7440;
                        break;
                    case 1038:
                        c = 230;
                        break;
                    case 1118:
                        c = 477;
                        break;
                    case 1168:
                        c = 629;
                        break;
                }
                outputFile.write(c);
                c = inputFile.read();
            } catch (IOException ex) {
                Logger.getLogger(TranscriptionCorrector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try {
            inputFile.close();
            outputFile.close();
        } catch (IOException ex) {
            Logger.getLogger(TranscriptionCorrector.class.getName()).log(Level.SEVERE, null, ex);
        }
        /*
         *
         * Print characters.
         *
         */
        try {
            inputFile = new FileReader(outputFilePath);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TranscriptionCorrector.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            c = inputFile.read();
        } catch (IOException ex) {
            Logger.getLogger(TranscriptionCorrector.class.getName()).log(Level.SEVERE, null, ex);
        }

        while (c != -1) {
            try {
                if ((c < 127) || ((c > 1039) && (c < 1104)) || (c == 331) || (c == 643) || (c == 652) || (c == 7440) || (c == 230) || (c == 477) || (c == 658) || (c == 629) || (c == 603)) {
                } else {
                    System.out.print(c + "   ");
                    System.out.println((char) c);
                }
                c = inputFile.read();
            } catch (IOException ex) {
                Logger.getLogger(TranscriptionCorrector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        System.out.println(c);

        try {
            inputFile.close();
        } catch (IOException ex) {
            Logger.getLogger(TranscriptionCorrector.class.getName()).log(Level.SEVERE, null, ex);
        }



    }
}
