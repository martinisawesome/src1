package tfidf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import storage.FilePartioning;
import storage.FileSystem;
import textprocessor.Stemming;

/**
 * Takes the indexPos files and partitions them out
 */
public class IndexParser
{
    public static final String IGNORE = "N";
    public static final String KEEP = "K";

    public static String getPositionFile(String word)
    {
        String fileName;
        File file;
        if (!word.matches(".*[0-9].*"))
        {
            fileName = FilePartioning.getPartitionFileName(PositionProcessor.DIRECTORY, PositionProcessor.FILE_HEADER1, word);
            file = new File(fileName);
            if (!file.exists())
            {
                fileName = FilePartioning.getPartitionFileName(PositionProcessor.DIRECTORY, PositionProcessor.FILE_HEADER, word);

            }
        }
        else
        {
            fileName = FilePartioning.getPartitionFileName(PositionProcessor.DIRECTORY, PositionProcessor.FILE_HEADER, word);

        }

        return fileName;
    }

    public static String getFile(String directory, String header, String word)
    {
        String fileName;
        File file;
        if (!word.matches(".*[0-9].*"))
        {
            fileName = FilePartioning.getPartitionFileName(directory, header + IGNORE, word);
            file = new File(fileName);
            if (!file.exists())
            {
                fileName = FilePartioning.getPartitionFileName(directory, header, word);

            }
        }
        else
        {
            fileName = FilePartioning.getPartitionFileName(directory, header, word);

        }
        return fileName;
    }

    public static String getGramFile(String word, int gram)
    {

        String header = gram + "Gram";
        return getFile(FileSystem.TFDF_PARTITION_DIRECTORY, header, word);
    }

    public static String getWeightFile(String word)
    {

        return getFile(FileSystem.TFDF_PARTITION_DIRECTORY, "", word);

    }

    /**
     * Find the document-position pair for a word
     *
     * @param word
     * @param fileName
     * @return
     * @throws IOException
     */
    public static LinkedList<DocPair> getWordPosition(String word, String fileName) throws IOException
    {
        LinkedList<DocPair> positions = new LinkedList<>();
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

                String[] tokens = splits[1].split(";");
                for (String split : tokens)
                {
                    String[] pair = split.split(",");
                    p = new DocPair(Integer.parseInt(pair[0]), Integer.parseInt(pair[1]));
                    positions.add(p);
                }
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

    public static LinkedList<DocPair> getWordPostings(String word) throws IOException
    {
        LinkedList<DocPair> positions = new LinkedList<>();
        String fileName;
        File file;

        // find the correct file to open
        if (!word.matches(".*[0-9].*"))
        {
            fileName = FilePartioning.getPartitionFileName(PostingProcessor.DIRECTORY, IGNORE, word);
            file = new File(fileName);
            if (!file.exists())
            {
                fileName = FilePartioning.getPartitionFileName(PostingProcessor.DIRECTORY, "", word);
                file = new File(fileName);
            }
        }
        else
        {
            fileName = FilePartioning.getPartitionFileName(PostingProcessor.DIRECTORY, "", word);
            file = new File(fileName);
        }

        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String curr;
        DocPair p;
        boolean found = false;
        while ((curr = br.readLine()) != null)
        {
            int index = curr.indexOf(':');
            String token = curr.substring(0, index);
            if (token.equals(word))
            {
                curr = curr.substring(index + 1, curr.length());
                String[] splits = curr.split(";");
                for (String split : splits)
                {
                    String[] pair = split.split(",");
                    p = new DocPair(Integer.parseInt(pair[0]), Integer.parseInt(pair[1]));
                    positions.add(p);
                }
                break;
            }
            else if (token.compareTo(word) > 0)
            {
                // This word does not exist
                break;
            }
            else if (found)
            {
                // This word does not exist
                break;
            }
        }

        fr.close();
        return positions;
    }

    public static LinkedList<TFIDFPair> getWordWeight(String word, String fileName, int limit) throws IOException
    {
        LinkedList<TFIDFPair> positions = new LinkedList<>();

        if (word.length() > 3)
        {
            word = Stemming.stem(word);
        }

        File file = new File(fileName);
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String curr;
        TFIDFPair p;
        boolean found = false;
        while ((curr = br.readLine()) != null)
        {
            int index = curr.indexOf(':');
            String token = Stemming.stem(curr.substring(0, index));

            if (token.equals(word))
            {
                curr = curr.substring(index + 1, curr.length());
                String[] splits = curr.split(";");
                int count = 0;
                for (String split : splits)
                {
                    String[] pair = split.split(",");
                    p = new TFIDFPair(Integer.parseInt(pair[0]), Double.parseDouble(pair[1]));
                    positions.add(p);

                    count++;
                    if (limit != -1 && count >= limit)
                    {
                        break;
                    }
                }
                break;
            }
            else if (token.compareTo(word) > 0)
            {
                // This word does not exist
                break;
            }
            else if (found)
            {
                // This word does not exist
                break;
            }
        }

        fr.close();
        return positions;
    }
}
