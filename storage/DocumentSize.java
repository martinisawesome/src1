package storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Measures the size of each document
 */
public class DocumentSize
{
    private final HashMap<Integer, Integer> map;

    public DocumentSize()
    {
        map = new HashMap<>();
    }

    /**
     * Inits the map from a file
     * @throws IOException 
     */
    public void readInFile() throws IOException
    {
        File file = new File(FileSystem.CRAWLER_DIRECTORY + FileSystem.DOCUMENT_SIZE_NAME);
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String curr;
        String[] parts;
        Integer docId;
        Integer url;

        // fina all the words in this line
        while ((curr = br.readLine()) != null)
        {
            parts = curr.split(":");
            docId = Integer.parseInt(parts[0]);
            url = Integer.parseInt(parts[1]);
            map.put(docId, url);
        }

        fr.close();
    }

    
    public void init()
    {
        String curr;
        LinkedList<File> files = FileSystem.getAllTokenTextFiles();
        for (File f : files)
        {

            int docID = Integer.parseInt(f.getName().replaceAll("[^0-9]", ""));
            int count = 0;

            try
            {
                FileReader fr = new FileReader(f);
                BufferedReader br = new BufferedReader(fr);
                while ((curr = br.readLine()) != null)
                {
                    count += curr.trim().split(" ").length;
                }

                map.put(docID, count);

                fr.close();
            }
            catch (IOException e)
            {
                System.err.println("Cannot read file: " + f);
                e.printStackTrace();
            }
        }
    }
    
    public void writeToFile() throws IOException
    {

        FileWriter wr = new FileWriter(FileSystem.CRAWLER_DIRECTORY + FileSystem.DOCUMENT_SIZE_NAME, false);
        for (Map.Entry<Integer, Integer> entry : map.entrySet())
        {
            wr.write("" + entry.getKey());
            wr.write(":");
            wr.write("" + entry.getValue());
            wr.write("\n");
        }
        wr.close();
    }

}
