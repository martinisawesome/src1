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
import textprocessor.Stemming;

/**
 * Tracks which document ID is associated with what URL
 */
public final class DocumentUrlMap
{
    private final HashMap<Integer, String> map;
    private LinkedList<Integer> matches;
    private Thread t1;

    public DocumentUrlMap() throws IOException
    {
        map = new HashMap<>();
        readInFile();

    }

    public boolean isAlive()
    {
        return t1 != null && t1.isAlive();
    }

    public LinkedList<Integer> getMatches()
    {
        t1 = null;
        return matches;
    }

    public void urlMatches(final HashMap<String, Double> word)
    {
        t1 = new Thread()
        {

            @Override
            public void run()
            {
                if (matches != null)
                {
                    matches.clear();
                }
                String first = null;
                HashMap<String, LinkedList<Integer>> urlMap = new HashMap<>();
                for (String s : word.keySet())
                {
                    if (first == null)
                    {
                        first = s;
                    }
                    urlMap.put(s, urlMatches(s));
                }

                // find only URL that contains all
                matches = urlMap.remove(first);
                for (LinkedList<Integer> list : urlMap.values())
                {
                    LinkedList<Integer> removes = new LinkedList<>();
                    for (Integer doc : matches)
                    {
                        if (!list.contains(doc))
                        {
                            removes.add(doc);
                        }
                    }

                    matches.removeAll(removes);
                }
            }
        };
        t1.start();
    }

    /**
     * Call this method to check if the URL is relevant!
     *
     * @param word
     * @return
     */
    private LinkedList<Integer> urlMatches(String word)
    {
        LinkedList<Integer> docId = new LinkedList<>();
        String shortened = Stemming.stem(word);
        if (shortened.length() > 3)
        {
            for (Map.Entry<Integer, String> entry : map.entrySet())
            {
                // check if it's exact match
                String urlWord = Stemming.stem(entry.getValue());

                if (urlWord.contains(shortened))
                {
                    docId.add(entry.getKey());
                }

            }
        }

        return docId;
    }

    public Integer urlHas(String word)
    {

        for (Map.Entry<Integer, String> entry : map.entrySet())
        {
            // check if it's exact match
            String compare = entry.getValue();
            if (word.equals(compare)
                || compare.replace(".php", "").equals(word)
                || compare.replace(".html", "").equals(word)
                || compare.replace(".htm", "").equals(word))
            {

                return entry.getKey();
            }

        }

        return null;
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

    public String get(Integer docId)
    {
        String fileName = map.get(docId);
        if (fileName.contains("mailman") || fileName.contains("fano"))
        {
            return null;
        }
        return map.get(docId);
    }

}
