package tfidf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import storage.FilePartioning;

/**
 * Takes the indexPos files and partitions them out
 */
public class PositionParser
{
    /**
     * Find the document-position pair for a word
     *
     * @param word
     * @return
     * @throws IOException
     */
    public static LinkedList<DocPair> getWordPosition(String word) throws IOException
    {
        LinkedList<DocPair> positions = new LinkedList<>();
        String fileName = FilePartioning.getPartitionFileName(DocumentPositionProcessor.DIRECTORY, DocumentPositionProcessor.FILE_HEADER, word);

        File file = new File(fileName);
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String curr;
        DocPair p;
        boolean found = false;
        while ((curr = br.readLine()) != null)
        {
            String[] splits = curr.split(":");
            if (splits[0].equals(word))
            {
                found = true;
                p = new DocPair(Integer.parseInt(splits[1]), Integer.parseInt(splits[2]));
                positions.add(p);
            }
            // Otherwise, stop searching if we found the word before
            else if (found)
            {
                break;
            }
            else if (splits[0].compareTo(word) > 0)
            {
                System.out.println(splits[0]);
                // This word does not exist
                break;
            }
        }

        fr.close();
        return positions;
    }

}
