package textprocessor;

import def.StopWords;
import storage.FileSystem;
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
import java.util.List;
import java.util.Map;

public class TextProcessor<E>
{

    private final List<FreqIndex<String>> wordCount;
    private final Map<String, Integer> wordCountMap;

    private final List<FreqIndex<String>> NGramCount;
    private final Map<String, Integer> NGramMap;
    private int freqFileCount;
    private int threeGCount;

    public TextProcessor()
    {
        wordCount = new LinkedList<>();
        NGramCount = new LinkedList<>();
        wordCountMap = new HashMap<>();
        NGramMap = new HashMap<>();
        freqFileCount = 0;
        threeGCount = 0;
    }

    // =========================================================================
    //  _   _ _   _ _ _ _   _           
    // | | | | | (_) (_) | (_)          
    // | | | | |_ _| |_| |_ _  ___  ___ 
    // | | | | __| | | | __| |/ _ \/ __|
    // | |_| | |_| | | | |_| |  __/\__ \
    // \___/ \__|_|_|_|\__|_|\___||___/
    public static ArrayList<String> tokenizeFile(File textFile) throws IOException
    {
        // Init method
        ArrayList<String> tokenList = new ArrayList<>();
        FileReader fr = new FileReader(textFile);
        BufferedReader br = new BufferedReader(fr);
        String curr;
        String token;

        while ((curr = br.readLine()) != null)
        {
            //curr = scanner.next();
            curr = curr.toLowerCase();

            // Hyphenated words treated as different words for now
            curr = curr.replace("'", "");                   // Handle apostrophe differently
            curr = curr.replaceAll("[^a-zA-Z0-9]", " ");    // Remove all other non-alphanumeric chars

            // get the string list after regex
            String[] strings = curr.split(" ");
            for (String string : strings)
            {
                // Handle all empty strings
                token = string.trim();
                if (!token.isEmpty())
                {
                    tokenList.add(token);
                }
            }

        }   //eWhile

        // End Method
        br.close();
        return tokenList;

    }

    public static <E> void print(List<E> tokenList)
    {
        StringBuilder sb = new StringBuilder();
        for (E token : tokenList)
        {
            // Print
            sb.append("\"");
            sb.append(token);
            sb.append("\",");
            sb.append("\n");

        }
        // New line
        System.out.println(sb.toString());
    }

    public static void removeStopWords(List<String> tokens)
    {
        for (String word : StopWords.WORDS)
        {
            while (tokens.remove(word))
            {
                //keep removing the same word
            }
        }
    }

    //  _    _               _  ______                                    _           
    // | |  | |             | | |  ___|                                  (_)          
    // | |  | | ___  _ __ __| | | |_ _ __ ___  __ _ _   _  ___ _ __   ___ _  ___  ___ 
    // | |/\| |/ _ \| '__/ _` | |  _| '__/ _ \/ _` | | | |/ _ \ '_ \ / __| |/ _ \/ __|
    // \  /\  / (_) | | | (_| | | | | | |  __/ (_| | |_| |  __/ | | | (__| |  __/\__ \
    //  \/  \/ \___/|_|  \__,_| \_| |_|  \___|\__, |\__,_|\___|_| |_|\___|_|\___||___/
    //                                           |_|  
    public void computeWordFrequencies(int docId, List<String> tokenList) throws IOException
    {

        for (String token : tokenList)
        {
            // Check if token already exists
            Integer index = wordCountMap.get(token);

            // Token already added
            if (index != null)
            {
                wordCount.get(index).incCount();
            }
            // Otherwise, is a new token
            else
            {
                index = wordCount.size();
                wordCount.add(new FreqIndex<>(token, docId));
                wordCountMap.put(token, index);
            }

            if (wordCount.size() > 160000)
            {
                writeToFile();
            }
        }

    }

    public void computeNGramFrequencies(int docId, ArrayList<String> tokenList, int n) throws IOException
    {
        if (n == 1)
        {
            computeWordFrequencies( docId,  tokenList);
            return;
        }
        
        String[] pos = new String[n];

        if (tokenList.size() < n)
        {
            return;
        }

        // Initialize the first three gram
        for (int i = 0; i < n; i++)
        {
            pos[i] = tokenList.get(i);
        }

        String string = Arrays.toString(pos);
        NGramCount.add(new FreqIndex<>(string, docId));
        NGramMap.put(string, 0);   //goes to index 0, starting

        if (tokenList.size() == n)
        {
            return;
        }

        for (int i = n; i < tokenList.size(); i++)
        {
            // Initialize current three-gram
            String token = tokenList.get(i);
            for (int j = 0; j < n - 1; j++)
            {
                pos[j] = pos[j + 1];
            }
            pos[n - 1] = token;
            String gram = Arrays.toString(pos);

            // Check if N-gram already exists
            Integer index = NGramMap.get(gram);

            // 3-gram already added
            if (index != null)
            {
                NGramCount.get(index).incCount();
            }
            // Otherwise, is a new 3-gram
            else
            {
                index = NGramCount.size();
                NGramCount.add(new FreqIndex<>(gram, docId));
                NGramMap.put(gram, index);
            }

            if (NGramCount.size() > 80000)
            {
                writeToFile3();
            }
        }
    }

    public void flush() throws IOException
    {
        //writeToFile();
        writeToFile3();
    }

    /**
     * Writes the current frequency counts stored in memory to file.
     *
     * @param wordCountMap
     * @param wordCount
     * @throws IOException
     */
    private void writeToFile() throws IOException
    {
        Collections.sort(wordCount);
        FileWriter wr = new FileWriter(FileSystem.CONTENT_PARTITION_DIRECTORY + FileSystem.FREQ_FILE + (freqFileCount++), false);
        StringBuilder sb = new StringBuilder();
        for (FreqIndex f : wordCount)
        {
            sb.append(f.toString());
            sb.append("\n");
        }
        wr.write(sb.toString());
        wr.close();

        wordCount.clear();
        wordCountMap.clear();
    }

    /**
     * Writes the current three-grams stored in memory to file
     *
     * @throws IOException
     */
    private void writeToFile3() throws IOException
    {
        Collections.sort(NGramCount);

        FileWriter wr = new FileWriter(FileSystem.CONTENT_PARTITION_DIRECTORY + FileSystem.FOUR_GRAM + (threeGCount++), false);
        StringBuilder sb = new StringBuilder();
        for (FreqIndex f : NGramCount)
        {
            sb.append(f.toString());
            sb.append("\n");
        }
        wr.write(sb.toString());
        wr.close();

        NGramCount.clear();
        NGramMap.clear();
    }

    /**
     * Counts up to limit, the most frequent words in the file. File does not have to be sorted.
     *
     * @param f
     * @param limit
     * @return
     * @throws IOException
     */
    public static ArrayList<FreqIndex<String>> countHighest(File f, int limit) throws IOException
    {
        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);
        String curr;
        int least = 2;
        ArrayList<FreqIndex<String>> list = new ArrayList<>();

        while ((curr = br.readLine()) != null)
        {
            String[] parm0 = curr.split(":");
            int count0 = Integer.parseInt(parm0[1].trim());
            if (count0 == 1)
            {
                continue;
            }

            String name = parm0[0];

            if (list.size() < limit)
            {
                list.add(new FreqIndex(name, count0));
            }
            else if (list.size() == limit && count0 > least)
            {
                Collections.sort(list);
                least = list.get(limit - 1).getCount();

                //if bigger than smallest index, need to remove smallest
                if (count0 > least)
                {
                    list.remove(limit - 1);
                    list.add(new FreqIndex(name, count0));
                }
            }
            else if (list.size() > limit)
            {
                throw new RuntimeException("List size > n!");
            }
        }

        Collections.sort(list);
        fr.close();
        return list;
    }
}
