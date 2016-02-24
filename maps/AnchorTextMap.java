package maps;

import def.StopWords;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import storage.FileSystem;

/**
 *
 * @author mao_ma
 */
public class AnchorTextMap
{
    public static final String FULL_ANCHOR_FILE = FileSystem.MISC_PARTITION_DIRECTORY + "FULL_ANCHOR";
    public static final String TOKEN_ANCHOR_FILE = FileSystem.MISC_PARTITION_DIRECTORY + "TOKEN_ANCHOR";
    private final HashMap<Integer, String[]> map;
    private final HashMap<String, LinkedList<Integer>> mapping;
    private Thread t1;

    public AnchorTextMap()
    {
        map = new HashMap<>();
        mapping = new HashMap<>();
    }

    public boolean isAlive()
    {
        return t1.isAlive();
    }

    public void clear()
    {
        mapping.clear();
        map.clear();
    }

    public HashMap<String, LinkedList<Integer>> getMapping()
    {
        return mapping;
    }

    public void processQuery(final HashMap<String, Double> words)
    {
        t1 = new Thread()
        {

            @Override
            public void run()
            {
                try
                {
                    mapping.clear();
                    LinkedList<String> list = new LinkedList<>();
                    LinkedList<Integer> numbers;
                    list.addAll(words.keySet());

                    Collections.sort(list);

                    File tokenFile = new File(TOKEN_ANCHOR_FILE);

                    FileReader fr = new FileReader(tokenFile);
                    BufferedReader br = new BufferedReader(fr);
                    String curr;
                    while ((curr = br.readLine()) != null)
                    {
                        String[] parts = curr.split(":");
                        String word = parts[0];
                        int compare = word.compareTo(list.peek());
                        if (compare == 0)
                        {
                            String[] docs = parts[1].split(",");
                            numbers = new LinkedList<>();
                            for (String doc : docs)
                            {
                                numbers.add(Integer.parseInt(doc));
                            }

                            mapping.put(list.removeFirst(), numbers);
                        }
                        else if (compare > 0)
                        {
                            list.removeFirst();
                        }

                        if (list.isEmpty())
                        {
                            break;
                        }
                    }

                    fr.close();

                }
                catch (IOException ex)
                {
                    System.err.println(ex.getMessage());
                    throw new RuntimeException(ex);
                }
            }
        };

        t1.start();
    }

    @Deprecated
    public void reorder() throws IOException
    {
        File f0 = new File(FULL_ANCHOR_FILE);
        File f1 = new File(TOKEN_ANCHOR_FILE);
        f0.delete();
        f1.delete();
        f0.createNewFile();
        f1.createNewFile();

        File targetFile = getThisFile();
        FileReader fr = new FileReader(targetFile);
        BufferedReader br = new BufferedReader(fr);
        String curr;
        HashMap<String, ArrayList<Integer>> titles = new HashMap<>();
        HashMap<String, ArrayList<Integer>> dictionary = new HashMap<>();
        while ((curr = br.readLine()) != null)
        {
            String[] splits = curr.split(":");
            Integer docID = Integer.parseInt(splits[0]);
            String title = splits[1].trim();

            if (title.isEmpty())
            {
                continue;
            }

            // add in the whole title
            ArrayList<Integer> titlesList = titles.get(title);
            if (titlesList == null)
            {
                titlesList = new ArrayList<>();
            }

            titlesList.add(docID);
            titles.put(title, titlesList);

            String data = title.replaceAll("[^a-zA-Z0-9]", " ");

            // add in parts of title
            for (String string : data.split(" "))
            {
                string = string.trim().toLowerCase();
                if (string.isEmpty())
                {
                    continue;
                }
                else if (StopWords.isStop(string))
                {
                    continue;
                }
                ArrayList<Integer> list = dictionary.get(string);
                if (list == null)
                {
                    list = new ArrayList<>();
                }

                list.add(docID);
                dictionary.put(string, list);
            }
        }
        fr.close();

        // Sort and write full titles!
        Set<String> titlesSet = titles.keySet();
        ArrayList<String> words = new ArrayList<>();
        words.addAll(titlesSet);
        Collections.sort(words);
        FileWriter fullWriter = new FileWriter(f0);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.size(); i++)
        {
            String word = words.get(i);
            sb.append(word).append(":");
            ArrayList<Integer> docs = titles.get(word);
            for (int j = 0; j < docs.size(); j++)
            {
                Integer doc = docs.get(j);
                sb.append(doc);
                if (j < docs.size() - 1)
                {
                    sb.append(",");
                }
            }
            if (i < words.size() - 1)
            {
                sb.append("\n");
            }
        }

        fullWriter.write(sb.toString());
        sb = new StringBuilder();

        fullWriter.close();
        // write tokens!
        FileWriter tokenWriter = new FileWriter(f1);
        words = new ArrayList<>();
        words.addAll(dictionary.keySet());
        Collections.sort(words);

        for (int i = 0; i < words.size(); i++)
        {
            String word = words.get(i);
            sb.append(word).append(":");
            ArrayList<Integer> docs = dictionary.get(word);
            for (int j = 0; j < docs.size(); j++)
            {
                Integer doc = docs.get(j);
                sb.append(doc);
                if (j < docs.size() - 1)
                {
                    sb.append(",");
                }
            }
            if (i < words.size() - 1)
            {
                sb.append("\n");
            }
        }
        tokenWriter.write(sb.toString());
        tokenWriter.close();
    }

    public void readFromFile() throws IOException
    {
        File targetFile = getThisFile();

        FileReader fr = new FileReader(targetFile);
        BufferedReader br = new BufferedReader(fr);
        String curr;
        while ((curr = br.readLine()) != null)
        {
            //TODO remove stop words and other chars?
            String[] parts = curr.split(":");

            int id = Integer.parseInt(parts[0]);
            map.put(id, parts[1].split(" "));

        }

        fr.close();
    }

    public static void ripFromRaw() throws IOException
    {
        HashMap<Integer, String> map = new HashMap<>();
        File targetFile = getThisFile();
        targetFile.delete();
        targetFile.createNewFile();
        File directory = new File(FileSystem.CRAWLING_HEADER_DIRECTORY);
        File[] files = directory.listFiles();
        FileWriter fw = new FileWriter(targetFile);
        String curr;
        int id;
        if (files != null)
        {
            for (File f : files)
            {
                if (f.isDirectory())
                {

                }
                else if (f.getName().contains("Header"))
                {

                    FileReader fr = new FileReader(f);
                    BufferedReader br = new BufferedReader(fr);
                    boolean finished = false;

                    while (!finished)
                    {
                        do
                        {
                            curr = br.readLine();
                            if (curr == null)
                            {
                                finished = true;
                                break;
                            }
                        } while (!curr.contains("DocId"));

                        // end searching this file
                        if (finished)
                        {
                            break;
                        }

                        String[] split = curr.split(" ");
                        id = Integer.parseInt(split[1]);

                        while (!(curr = br.readLine()).contains("Anchor Text"))
                        {

                        }

                        int index = curr.indexOf("Anchor Text");
                        curr = curr.substring(index);
                        curr = curr.replace("Anchor Text: ", "");
                        curr = curr.replaceAll("[^a-zA-Z0-9 ]", "");
                        curr = curr.trim();
                        curr = curr.replaceAll("  ", " ");

                        if (!"null".equals(curr) && !curr.isEmpty())
                        {
                            map.put(id, curr);
                        }

                    }

                    // end of reading this file!
                    fr.close();

                }
            }
        }   //end of looking all files

        LinkedList<Integer> list = new LinkedList<>();
        list.addAll(map.keySet());
        Collections.sort(list);
        StringBuilder sb = new StringBuilder();
        for (Integer i : list)
        {
            curr = map.get(i);
            sb.append(i).append(":").append(curr);
            sb.append("\n");

        }
        fw.write(sb.toString());
        fw.close();
    }

    private static File getThisFile()
    {
        File targetFile = new File(FileSystem.MISC_PARTITION_DIRECTORY + FileSystem.ANCHOR_NAME);
        return targetFile;
    }
}
