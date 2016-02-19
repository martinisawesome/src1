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
        char car = word.charAt(0);
  
        return directory + header + car;
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

    public static void partitionAll() throws IOException
    {
        partitionOutFile(FileSystem.FREQ_FILE, FileSystem.CONTENT_PARTITION_DIRECTORY, "IndexFreqComplete");
        partitionOutFile(FileSystem.TWO_GRAM, FileSystem.CONTENT_PARTITION_DIRECTORY, "Index2GramComplete");
        partitionOutFile(FileSystem.THREE_GRAM, FileSystem.CONTENT_PARTITION_DIRECTORY, "Index3GramComplete");
        partitionOutFile(FileSystem.THREE_GRAM, FileSystem.CONTENT_PARTITION_DIRECTORY, "Index4GramComplete");
    }
}
