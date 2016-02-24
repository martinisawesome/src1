package maps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import storage.FileSystem;

/**
 * Tracks which document ID is associated with what URL
 */
public final class DocumentUrlMap
{
    private final HashMap<Integer, String> map;

    public DocumentUrlMap() throws IOException
    {
        map = new HashMap<>();
        readInFile();

    }

    public HashMap<String, LinkedList<Integer>> urlMatches(HashMap<String, Double> word)
    {
        HashMap<String, LinkedList<Integer>> urlMap = new HashMap<>();
        for (String s : word.keySet())
        {
            urlMap.put(s, urlMatches(s));
        }
        return urlMap;
    }

    /**
     * Call this method to check if the URL is relevant!
     *
     * @param word
     * @return
     */
    public LinkedList<Integer> urlMatches(String word)
    {
        LinkedList<Integer> docId = new LinkedList<>();
        String shortened = shortUrl(word);
        for (Map.Entry<Integer, String> entry : map.entrySet())
        {
            // check if it's exact match
            String compare = shortUrl(entry.getValue());
            if (shortened.equals(compare))
            {
                docId.clear();
                docId.add(entry.getKey());
                return docId;
            }
            // Otherwise, check if this URL has the word
            else if (compare.contains(shortened))
            {
                docId.add(entry.getKey());
            }
        }

        return docId;
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

    private String shortUrl(String word)
    {
        String shortened = word.replace("https://", "").replace("http://", "");
        return shortened;
    }

    public void add(Integer id, String url)
    {
        map.put(id, url);
    }

    public Set<Integer> getAllIds()
    {
        return map.keySet();
    }

    public String get(Integer docId)
    {
        return map.get(docId);
    }

}
