package engine;

import def.StopWords;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import tfidf.DocPair;
import tfidf.TFIDFPair;

/**
 *
 */
public class Engine
{
    private static final int LIMIT = -1;

    public static void search(String... words)
    {
        search(true, words);
    }

    public static void search(boolean pos, String... words)
    {
        SearchThreading t = new SearchThreading(pos, LIMIT);
        LinkedList<String> modifiedQuery = new LinkedList<>();

        // Search all words
        for (String word : words)
        {
            // Modify the query term
            String modWord = word.toLowerCase();
            modWord = modWord.replaceAll("[^a-zA-Z0-9]", "");    // Remove all non-alphanumeric chars
            
            // Ignore stop words
            if (StopWords.isStop(modWord))
            {
                continue;
            }
            
            // Check we didnt search this already
            if (!modifiedQuery.contains(modWord))
            {
                modifiedQuery.add(modWord);
                t.searchWord(modWord);
            }
        }

        // Wait until all finished
        while (t.checkAndStartWaitingList())
        {
            // wait until finished
        }

        //TODO preserve ordering of query?
        HashMap<String, LinkedList<TFIDFPair>> tfidfResults = t.getWeightResults();
        System.out.println(PrintHelper.getNice(tfidfResults));

        if (pos)
        {
            HashMap<String, LinkedList<DocPair>> posResults = t.getPositionResults();
            System.out.println(PrintHelper.getNice(posResults));
        }

        // clear
        t.clear();
    }
}
