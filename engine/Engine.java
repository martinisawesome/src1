package engine;

import def.StopWords;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import maps.AnchorTextMap;
import maps.DocumentUrlMap;
import maps.TitleMap;
import tfidf.DocPair;
import tfidf.TFIDFPair;

/**
 *
 */
public class Engine
{
    private static final int QUERIES_TO_SHOW = 20;
    private static final boolean PRINT_WEIGHTS = false;
    private static final int LIMIT = -1;

    private static HashMap<String, LinkedList<TFIDFPair>> tfidfResults;
    private static HashMap<String, LinkedList<DocPair>> posResults;
    private static DocumentUrlMap URL_DOCUMENT_MAP;

    // ______          __                       _____       _         _____                
    // | ___ \        / _|                     |  _  |     | |       |  _  |               
    // | |_/ /__ _ __| |_ ___  _ __ _ __ ___   | | | |_ __ | |_   _  | | | |_ __   ___ ___ 
    // |  __/ _ \ '__|  _/ _ \| '__| '_ ` _ \  | | | | '_ \| | | | | | | | | '_ \ / __/ _ \
    // | | |  __/ |  | || (_) | |  | | | | | | \ \_/ / | | | | |_| | \ \_/ / | | | (_|  __/
    // \_|  \___|_|  |_| \___/|_|  |_| |_| |_|  \___/|_| |_|_|\__, |  \___/|_| |_|\___\___|                  
    //                                                        |___/
    static
    {
        try
        {
            URL_DOCUMENT_MAP = new DocumentUrlMap();
            URL_DOCUMENT_MAP.readInFile();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    /**
     *
     * @param pos
     * @param modWords
     * @return
     */
    public static List<String> search(boolean pos, String... modWords)
    {
        clear();

        // Find all the query words
        HashMap<String, Double> queryWords = new HashMap<>();
        for (String modWord1 : modWords)
        {
            String modWord = modWord1.toLowerCase();
            modWord = modWord.replaceAll("[^a-zA-Z0-9]", "").trim();    // Remove all non-alphanumeric chars
            if (!modWord.isEmpty() && !StopWords.isStop(modWord))
            {
                Double weight = queryWords.get(modWord);
                if (weight == null)
                {
                    queryWords.put(modWord, 1.0);
                }
                else
                {
                    queryWords.put(modWord, weight + 1.0);
                }
            }
        }

        // Compute TF
        if (queryWords.isEmpty())
        {
            return new LinkedList<>();
        }

        // first find in index
        findIndex(pos, queryWords);

        // Start Title Map
        TitleMap titleMap = new TitleMap();
        titleMap.processQuery(queryWords);

        // Start anchors
        AnchorTextMap anchorMap = new AnchorTextMap();
        anchorMap.processQuery(queryWords);

        // start in URL (skip this, takes too long!)
        //HashMap<String, LinkedList<Integer>> urlMap = URL_DOCUMENT_MAP.urlMatches(queryWords);
        
        
        // Wait for other indexers to finish!
        while (titleMap.isAlive() || anchorMap.isAlive() || anchorMap.isAlive())
        {

        }

        // Get mappings of titles
        HashMap<String, LinkedList<Integer>> titleMappings = titleMap.getMapping();
        HashMap<String, LinkedList<Integer>> anchorMappings = anchorMap.getMapping();

        if (PRINT_WEIGHTS)
        {
            System.out.println("TF-IDF=\n" + PrintHelper.getNice(tfidfResults));

            if (pos)
            {
                System.out.println("Positions=\n" + PrintHelper.getNice(posResults));
            }

            System.out.println("Title Mappings=\n" + PrintHelper.getNice(titleMappings));

            System.out.println("Anchor Mappings=\n" + PrintHelper.getNice(anchorMappings));

            //System.out.println("URL=\n" + PrintHelper.getNice(urlMap));
        }
        // Find results
        ArrayList<Integer> topDocuments = new ArrayList<>();
        HashMap<Integer, Double> docToWeightMap = new HashMap<>();

        //======================================================================
        findBestestWords(docToWeightMap, topDocuments, titleMappings);
        findBestestWords(docToWeightMap, topDocuments, anchorMappings);

        //TODO anchor texts!!!
        //======================================================================
        // Compute the simple values
        LinkedList<TFIDFPair> newList;
        if (queryWords.size() == 1)
        {
            Set<Entry<String, LinkedList<TFIDFPair>>> tfiDfEntries = tfidfResults.entrySet();
            Iterator<Entry<String, LinkedList<TFIDFPair>>> it = tfiDfEntries.iterator();
            newList = it.next().getValue();

        }
        else    //compute cosine stuff
        {
            HashMap<Integer, Double> denMap = new HashMap<>();

            double totalDen = 0;
            for (Entry<String, LinkedList<TFIDFPair>> entry : tfidfResults.entrySet())
            {

                // Add all denominators
                LinkedList<TFIDFPair> tfidfs = entry.getValue();
                String word = entry.getKey();
                Double queryWeight = queryWords.get(word);
                for (TFIDFPair idf : tfidfs)
                {
                    // Calculate numerator

                    Double weight = docToWeightMap.get(idf.docID);
                    if (weight == null)
                    {
                        docToWeightMap.put(idf.docID, queryWeight * idf.weight);
                    }
                    else
                    {
                        docToWeightMap.put(idf.docID, weight + queryWeight * idf.weight);
                    }

                    // Calculate denoinator
                    weight = denMap.get(idf.docID);
                    if (weight == null)
                    {
                        denMap.put(idf.docID, idf.weight * idf.weight);
                    }
                    else
                    {
                        denMap.put(idf.docID, weight + (idf.weight * idf.weight));
                    }
                }
                totalDen += queryWeight * queryWeight;
            }

            // Modify den by query terms
            newList = new LinkedList<>();
            for (Entry<Integer, Double> entry : denMap.entrySet())
            {
                Double denominator = entry.getValue() * totalDen;
                denominator = Math.sqrt(denominator);

                // divide and modify weight
                Integer key = entry.getKey();
                Double weight = docToWeightMap.get(key);
                newList.add(new TFIDFPair(key, weight / denominator));
            }

            Collections.sort(newList);
        }
        //======================================================================

        // find the best documents after alligning
        while (topDocuments.size() < QUERIES_TO_SHOW && !newList.isEmpty())
        {
            Integer docId = newList.removeFirst().docID;
            if (!topDocuments.contains(docId))
            {
                topDocuments.add(docId);
            }
        }

        // Return URL list from URL Document Map
        ArrayList<String> results = new ArrayList<>();
        for (Integer i : topDocuments)
        {
            results.add(URL_DOCUMENT_MAP.get(i));
        }

        // Clear objects!
        titleMap.clear();
        anchorMap.clear();

        return results;
    }

    public static List<String> search(String words)
    {
        if (words.trim().isEmpty())
        {
            return new ArrayList<>();
        }

        String[] queryTerms = words.split(" ");

        return search(queryTerms);
    }

    public static List<String> search(String... words)
    {
        return search(true, words);
    }

    private static void clear()
    {
        // clear
        if (tfidfResults != null)
        {
            tfidfResults.clear();

        }

        if (posResults != null)
        {
            posResults.clear();
        }
    }

    /**
     * Iterates a smaller list for the best words to find!
     *
     * @param weightMap
     * @param topDocuments
     * @param urlMap
     */
    public static void findBestestWords(HashMap<Integer, Double> weightMap, List<Integer> topDocuments, HashMap<String, LinkedList<Integer>> urlMap)
    {

        LinkedList<Integer> bestDocUrls = new LinkedList<>();

        // find if any URL words match
        for (Entry<String, LinkedList<Integer>> entry : urlMap.entrySet())
        {
            for (Integer i : entry.getValue())
            {
                if (weightMap.get(i) == null)
                {
                    weightMap.put(i, 1.5);
                }
                else
                {
                    weightMap.put(i, weightMap.get(i) + 1.5);
                }
            }

            // find the number 1 document just once!
            if (topDocuments.isEmpty())
            {
                if (bestDocUrls.isEmpty())
                {
                    bestDocUrls.addAll(entry.getValue());
                }
                else
                {
                    // Remove everything not common between the two lists
                    LinkedList<Integer> removeList = new LinkedList<>();
                    for (Integer i : entry.getValue())
                    {
                        if (!bestDocUrls.contains(i))
                        {
                            removeList.add(i);
                        }
                    }
                    bestDocUrls.removeAll(removeList);
                }
            }
        }

        // find the number 1 document just once!
        if (topDocuments.isEmpty())
        {
            int smallestValue = Integer.MAX_VALUE;
            int smallestIndex = -1;
            for (Integer i : bestDocUrls)
            {
                String s = URL_DOCUMENT_MAP.get(i);
                if (s.length() < smallestValue)
                {
                    smallestValue = s.length();
                    smallestIndex = i;
                }
            }

            if (smallestIndex != -1)
            {
                if (!topDocuments.contains(smallestIndex))
                {
                    topDocuments.add(smallestIndex);
                }
            }
        }
    }

    /**
     * Find the TFIDF of values in the index
     *
     * @param pos
     * @param queryWords
     */
    public static void findIndex(boolean pos, HashMap<String, Double> queryWords)
    {
        SearchThreading t = new SearchThreading(URL_DOCUMENT_MAP.getAllIds().size(), pos, LIMIT, queryWords);

        // Search all words
        for (String word : queryWords.keySet())
        {
            t.searchWord(word);
        }

        // Wait until all finished
        while (t.checkAndStartWaitingList())
        {
            // wait until finished
        }

        tfidfResults = (HashMap<String, LinkedList<TFIDFPair>>) t.getWeightResults().clone();

        if (pos)
        {
            posResults = (HashMap<String, LinkedList<DocPair>>) t.getPositionResults().clone();
        }

        // clear
        t.clear();
    }
}
