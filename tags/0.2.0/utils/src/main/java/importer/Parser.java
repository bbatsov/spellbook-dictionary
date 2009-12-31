package importer;

import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Arrays;

/**
 * User: bozhidar
 * Date: Sep 2, 2009
 * Time: 12:17:52 AM
 */
public class Parser {
    private static final String DICT_FILE = "/home/bozhidar/downloads/bg-en_dual/data/en-bg.dat";
    private static final String OUT_FILE = "output_en_bg.txt";

    public static void main(String[] args) throws IOException {
        RandomAccessFile file = new RandomAccessFile(DICT_FILE, "r");
        File out = new File(OUT_FILE);
        FileWriter writer = new FileWriter(out);
        BufferedWriter bufferedWriter = new BufferedWriter(writer);

        //first byte in the data file is '\0'
        byte nullByte = file.readByte();

        while (true) {
            try {
                byte[] record = new byte[20000];

                int i = 0;

                while (true) {
                    byte byteRead = file.readByte();

                    if (byteRead == nullByte) {
                        break;
                    }

                    record[i++] = byteRead;
                }

                byte[] copy = Arrays.copyOf(record, i);

                Charset charset = Charset.forName("CP1251");
                CharsetDecoder decoder = charset.newDecoder();

                CharBuffer charBuffer = decoder.decode(ByteBuffer.wrap(copy));

                System.out.println(charBuffer.toString());
                bufferedWriter.append(charBuffer.toString() + "\n");
                bufferedWriter.append("<========>\n");
            } catch (EOFException e) {
                bufferedWriter.flush();
                break;
            }
        }
    }
}
