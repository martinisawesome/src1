package storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Tracks which document ID is associated with what URL
 */
public class DocumentMap
{
    private final HashMap<Integer, String> map;

    public DocumentMap()
    {
        map = new HashMap<>();

    }

    public void readInFile() throws IOException
    {
        File file = new File(FileSystem.CRAWLER_DIRECTORY + FileSystem.DOCUMENT_MAP_NAME);
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String curr;
        String[] parts;
        Integer docId;
        String url;

        // fina all the words in this line
        while ((curr = br.readLine()) != null)
        {
            parts = curr.split(" ");
            docId = Integer.parseInt(parts[0]);
            url = parts[1];
            map.put(docId, url);
        }

        fr.close();
    }

    public void writeToFile() throws IOException
    {
        FileWriter wr = new FileWriter(FileSystem.CRAWLER_DIRECTORY + FileSystem.DOCUMENT_MAP_NAME, false);
        for (Map.Entry<Integer, String> entry : map.entrySet())
        {
            wr.write("" + entry.getKey());
            wr.write(" ");
            wr.write(entry.getValue());
            wr.write("\n");
        }
        wr.close();
    }

    public void add(Integer id, String url)
    {
        map.put(id, url);
    }

    public Set<Integer> getAllIds()
    {
        return map.keySet();
    }

}
