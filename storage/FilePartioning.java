package storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Partitions files based on alphabetic. Helps to tell which file a word belongs to
 *
 */
public class FilePartioning
{
    private static final char D0 = '0';
    private static final char D1 = '1';
    private static final char D2 = '2';
    private static final char DT = '3';
    private static final char D3 = '5';
    private static final char D4 = '7';
    private static final char A = 'a';
    private static final char B = 'b';
    private static final char D = 'd';
    private static final char G = 'g';
    private static final char K = 'k';
    private static final char N = 'n';
    private static final char Q = 'q';
    private static final char S = 's';
    private static final char U = 'u';

    private static final String ZERO = "00";
    private static final String ONE = "11";
    private static final String TWO = "22";
    private static final String THREE = "34";
    private static final String FIVE = "56";
    private static final String SEVEN = "79";
    private static final String AA = "AA";
    private static final String BC = "BC";
    private static final String DF = "DF";
    private static final String GJ = "GJ";
    private static final String KM = "KM";
    private static final String NP = "NP";
    private static final String QR = "QR";
    private static final String ST = "ST";
    private static final String UZ = "UZ";

    /**
     * Returns the location of a file
     * Ex) FileSystem.THREE_GRAM, apple
     *
     * @param directory
     * @param header
     * @param word
     * @return
     */
    public static String getPartitionFileName(String directory, String header, String word)
    {
        String end;
        char car = word.charAt(0);
        if (car >= D0 && car < D1)
        {
            end = ZERO;
        }
        else if (car >= D1 && car < D2)
        {
            end = ONE;
        }
        else if (car >= D2 && car < DT)
        {
            end = TWO;
        }
        else if (car >= DT && car < D3)
        {
            end = THREE;
        }
        else if (car >= D3 && car < D4)
        {
            end = FIVE;
        }
        else if (car >= D4 && car <= '9')
        {
            end = SEVEN;
        }
        else if (car >= A && car < B)
        {
            end = AA;
        }
        else if (car >= B && car < D)
        {
            end = BC;
        }
        else if (car >= D && car < G)
        {
            end = DF;
        }
        else if (car >= G && car < K)
        {
            end = GJ;
        }
        else if (car >= K && car < N)
        {
            end = KM;
        }
        else if (car >= N && car < Q)
        {
            end = NP;
        }
        else if (car >= Q && car < S)
        {
            end = QR;
        }
        else if (car >= S && car < U)
        {
            end = ST;
        }
        else if (car >= U && car <= 'z')
        {
            end = UZ;
        }
        else
        {
            throw new IllegalArgumentException("Cannot partiton for: " + word);
        }
        return directory + header + end;
    }

    /**
     * Takes one file and partitions it out by naming
     * @param header
     * @param directory
     * @param fileName
     * @throws IOException 
     */
    public static void partitionOutFile(String header, String directory, String fileName) throws IOException
    {
        File file = new File(directory + fileName);
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String curr;

        FileWriter fw = null;
        String oldFileName = null;

        while ((curr = br.readLine()) != null)
        {
            //find if we need to open a new file
            curr = curr.replace("[", "").replace("]", "");
            String newFileName = getPartitionFileName(directory, header, curr);
            if (oldFileName == null ? newFileName != null : !oldFileName.equals(newFileName))
            {
                if (fw != null)
                {
                    fw.close();
                }

                oldFileName = newFileName;
                File out = new File(newFileName);
                out.delete();
                out.createNewFile();
                fw = new FileWriter(out);
            }

            fw.write(curr);
            fw.write("\n");
        }

        fw.close();
        fr.close();
    }

    //TODO do not ever call this again!
    @Deprecated
    public static void partitionAll() throws IOException
    {
        partitionOutFile(FileSystem.FREQ_FILE, FileSystem.CONTENT_PARTITION_DIRECTORY, "IndexFreqComplete");
        partitionOutFile(FileSystem.TWO_GRAM, FileSystem.CONTENT_PARTITION_DIRECTORY, "Index2GramComplete");
        partitionOutFile(FileSystem.THREE_GRAM, FileSystem.CONTENT_PARTITION_DIRECTORY, "Index3GramComplete");
    }
}
