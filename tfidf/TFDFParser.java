package tfidf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import storage.FilePartioning;
import storage.FileSystem;

/**
 * Gets the count for TF IDF of word
 */
public class TFDFParser
{
    public static LinkedList<TFIDFPair> getWordWeight(String word) throws IOException
    {
        LinkedList<TFIDFPair> positions = new LinkedList<>();
        String fileName = FilePartioning.getPartitionFileName(FileSystem.TFDF_PARTITION_DIRECTORY, "", word);

        File file = new File(fileName);
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String curr;
        TFIDFPair p;
        boolean found = false;
        while ((curr = br.readLine()) != null)
        {
            String[] splits = curr.split(":");
            if (splits[0].equals(word))
            {
                found = true;
                p = new TFIDFPair(Integer.parseInt(splits[1]), Double.parseDouble(splits[2]));
                positions.add(p);
            }
            // Otherwise, stop searching if we found the word before
            else if (found)
            {
                break;
            }
            else if (splits[0].compareTo(word) > 0)
            {
                // This word does not exist
                break;
            }
        }

        fr.close();
        return positions;
    }
}
