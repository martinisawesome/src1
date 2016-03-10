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
import java.util.PriorityQueue;
import java.util.Set;
import links.AuthPair;
import links.AuthorityMap;
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
    private static final boolean GET_TEXT_SNIPPET = false;       // controls whether to pull text snippets
    private static final int QUERIES_TO_SHOW = 10;
    private static final boolean PRINT_WEIGHTS = false;
    private static final int LIMIT = -1;

    private static HashMap<String, LinkedList<TFIDFPair>> gramResults;
    private static HashMap<String, LinkedList<TFIDFPair>> tfidfResults;
    private static HashMap<String, LinkedList<DocPair>> posResults;
    private static DocumentUrlMap URL_DOCUMENT_MAP;
    private static HashMap<String, String> urlToTextSnippetMap;
    private static AuthorityMap AUTH_MAP;

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
            AUTH_MAP = new AuthorityMap();
            URL_DOCUMENT_MAP = new DocumentUrlMap();
            URL_DOCUMENT_MAP.readInFile();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public static String getTextSnippet(String url)
    {
        return urlToTextSnippetMap.get(url);
    }

    //  _____                     _      ___  ___     _   _               _ 
    // /  ___|                   | |     |  \/  |    | | | |             | |
    // \ `--.  ___  __ _ _ __ ___| |__   | .  . | ___| |_| |__   ___   __| |
    //  `--. \/ _ \/ _` | '__/ __| '_ \  | |\/| |/ _ \ __| '_ \ / _ \ / _` |
    // /\__/ /  __/ (_| | | | (__| | | | | |  | |  __/ |_| | | | (_) | (_| |
    // \____/ \___|\__,_|_|  \___|_| |_| \_|  |_/\___|\__|_| |_|\___/ \__,_|
    public static List<String> search(boolean pos, String... searchTerms)
    {
        clear();

        // Find all the query words
        HashMap<String, Double> queryWords = new HashMap<>();
        for (String modWord1 : searchTerms)
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
        
        if (queryWords.isEmpty())
        {
            return new LinkedList<>();
        }

        // first find in index
        findIndex(GET_TEXT_SNIPPET, queryWords);
        
        // start in URL, must never happen before finding index!
       // URL_DOCUMENT_MAP.urlMatches(queryWords);

        // Start Title Map
        TitleMap titleMap = new TitleMap();
        titleMap.processQuery(queryWords);

        // Start anchors
        AnchorTextMap anchorMap = new AnchorTextMap();
        anchorMap.processQuery(queryWords);

        // Store results
        ArrayList<Integer> topDocuments = new ArrayList<>();
        HashMap<Integer, Double> docToWeightMap = new HashMap<>();
        //======================================================================
        // Add gram weights into weight map
        for (LinkedList<TFIDFPair> entry : gramResults.values())
        {
            for (TFIDFPair pair : entry)
            {
                Double weight = docToWeightMap.get(pair.docID);

                if (weight == null)
                {
                    docToWeightMap.put(pair.docID, pair.weight);
                }
                else
                {
                    docToWeightMap.put(pair.docID, weight + pair.weight);
                }
            }
        }

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

            double totalQueryDen = 0;
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
                totalQueryDen += queryWeight * queryWeight;
            }

            // Modify den by query terms and use den to divide
            newList = new LinkedList<>();
            for (Entry<Integer, Double> entry : denMap.entrySet())
            {
                Double denominator = entry.getValue() * totalQueryDen;
                denominator = Math.sqrt(denominator);

                // divide and modify weight
                Integer key = entry.getKey();
                Double weight = docToWeightMap.get(key);
                newList.add(new TFIDFPair(key, weight / denominator));
            }

        }
        
        //======================================================================
        // Wait for other indexers to finish!
        while (titleMap.isAlive() || anchorMap.isAlive() || AUTH_MAP.isAlive() || URL_DOCUMENT_MAP.isAlive())
        {

        }

        // Get mappings of titles
        HashMap<String, LinkedList<Integer>> titleMappings = titleMap.getMapping();
        HashMap<String, LinkedList<Integer>> anchorMappings = anchorMap.getMapping();
        HashMap<String, LinkedList<Integer>> urlMappings = URL_DOCUMENT_MAP.getMatches();

        if (PRINT_WEIGHTS)
        {
            System.out.println("TF-IDF=\n" + PrintHelper.getNice(tfidfResults));

            System.out.println("Title Mappings=\n" + PrintHelper.getNice(titleMappings));

            System.out.println("Anchor Mappings=\n" + PrintHelper.getNice(anchorMappings));

            System.out.println("URL=\n" + PrintHelper.getNice(urlMappings));
        }
        
        //======================================================================
        // find the most authority
        double totalWeight = 0;
        long totalAuth = 0;
        double docCount = 1;
        PriorityQueue<AuthPair> authenticSites = new PriorityQueue<>();
        for (TFIDFPair pair : newList)
        {
            int docId = pair.docID;
            int auth = AUTH_MAP.get(docId);
            totalAuth += auth;
            
            totalWeight += pair.weight;

            // Don't add up low auth sites
            if (auth < 1)
            {
                continue;
            }
            else if (pair.weight > 1)
            {
                docCount++;
            }
            
            authenticSites.add(new AuthPair(docId, auth));
            // Keep only the 5 most authentic sites
            if (authenticSites.size() > QUERIES_TO_SHOW)
            {
                authenticSites.remove();
            }
        }

        // find some measure to increase the high ranked weights
        double averageWeight = totalWeight / docCount;
        double averageAuth =  Math.log(totalAuth / docCount);

        // Increase the weight of high authority sites
        for (TFIDFPair pair : newList)
        {
            AuthPair delete = null;
            for (AuthPair pairA : authenticSites)
            {
                if (pairA.docID == pair.docID)
                {
                    delete = pairA;
                    break;
                }
            }
            
            // don't increase the same weight again
            if (delete != null)
            {
                authenticSites.remove(delete);
                double auth = AUTH_MAP.get(pair.docID);
                double inc = Math.log(auth / averageAuth);
                pair.incWeight(inc);
            }
            
            // no more sites to increase
            if (authenticSites.isEmpty())
            {
                break;
            }
        }
        
        //======================================================================
        // Check title and anchor text
        
        addMoreWeight(averageWeight + averageWeight, docToWeightMap, topDocuments, titleMappings);
        addMoreWeight(averageWeight + averageWeight, docToWeightMap, topDocuments, urlMappings);
        addMoreWeight(averageWeight, docToWeightMap, topDocuments, anchorMappings);

        //======================================================================
        // find the best documents after alligning
        // only find the top n documents for reuslts
        Collections.sort(newList);
        //System.out.println("=========================================");
        while (topDocuments.size() < QUERIES_TO_SHOW * 2 && !newList.isEmpty())
        {
            TFIDFPair pair = newList.removeFirst();
            Integer docId = pair.docID;
            if (!topDocuments.contains(docId))
            {
                topDocuments.add(docId);
            }
        }

        //======================================================================
        // Get the snippets of top documents
        TextHelper t = new TextHelper();
        LinkedList<Integer> processId = new LinkedList<>();     //only process each document once!
        if (GET_TEXT_SNIPPET)
        {
            for (Entry<String, LinkedList<DocPair>> entry : posResults.entrySet())
            {
                for (DocPair docP : entry.getValue())
                {
                    Integer docId = docP.docID;
                    if (topDocuments.contains(docId) && !processId.contains(docId))
                    {
                        t.getSnippet(docId, docP.pos);
                        processId.add(docId);

                    }
                }
            }

            while (t.isAlive())
            {
                // wait for all threads to die
            }
        }
        //======================================================================
        // Return URL list from URL Document Map
        ArrayList<String> results = new ArrayList<>();
        for (Integer i : topDocuments)
        {

            String url = URL_DOCUMENT_MAP.get(i);
            if (url != null)
            {
                results.add(url);
                urlToTextSnippetMap.put(url, t.get(i));
            }
            
            if (results.size() > QUERIES_TO_SHOW)
            {
                break;
            }
        }
        //======================================================================

        // Clear objects!
        titleMap.clear();
        anchorMap.clear();

        return results;
    }

    //  _____          _  ___  ___     _   _               _ 
    // |  ___|        | | |  \/  |    | | | |             | |
    // | |__ _ __   __| | | .  . | ___| |_| |__   ___   __| |
    // |  __| '_ \ / _` | | |\/| |/ _ \ __| '_ \ / _ \ / _` |
    // | |__| | | | (_| | | |  | |  __/ |_| | | | (_) | (_| |
    // \____/_| |_|\__,_| \_|  |_/\___|\__|_| |_|\___/ \__,_|
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
        urlToTextSnippetMap = new HashMap();

        // clear
        if (tfidfResults != null)
        {
            tfidfResults.clear();

        }

        if (posResults != null)
        {
            posResults.clear();
        }
        
        if (gramResults != null)
        {
            gramResults.clear();
        }
        
    }

    /**
     * Iterates a smaller list for the best words to find!
     *
     * @param weightUp
     * @param weightMap
     * @param topDocuments
     * @param map
     */
    public static void addMoreWeight(double weightUp, HashMap<Integer, Double> weightMap, List<Integer> topDocuments, HashMap<String, LinkedList<Integer>> map)
    {
        if (map == null)
        {
            return;
        }

        LinkedList<Integer> bestDocUrls = new LinkedList<>();

        // find if any URL words match
        for (Entry<String, LinkedList<Integer>> entry : map.entrySet())
        {
            for (Integer i : entry.getValue())
            {
                if (weightMap.get(i) == null)
                {
         
                    weightMap.put(i, weightUp);
                }
                else
                {
                    weightMap.put(i, weightMap.get(i) + weightUp);
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
                if (s == null)
                {
                    continue;
                }
                s = s.replaceAll("www.", "");
                if (s.length() < smallestValue && isUrlLegit(i))
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

    public static boolean isUrlLegit(int url)
    {
        return AUTH_MAP.getMap().get(url) > 1;
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
        Set<String> words = queryWords.keySet();

        // Search all words
        for (String word : words)
        {
            t.searchWord(word);
        }

        // find n grams as well
        NGramThreading ng = new NGramThreading(LIMIT);
        if (words.size() > 1 && words.size() < 5)
        {
            List<List<String>> queryLists = QueryHelper.getPermutations(new LinkedList<>(words));
            for (List<String> word : queryLists)
            {
                ng.searchWord(word);
            }
        }

        // Wait until all finished
        while (t.checkAndStartWaitingList() || ng.checkAndStartWaitingList())
        {
            // wait until finished
        }

        gramResults = (HashMap<String, LinkedList<TFIDFPair>>) ng.getWeightResults().clone();
        tfidfResults = (HashMap<String, LinkedList<TFIDFPair>>) t.getWeightResults().clone();

        if (pos)
        {
            posResults = (HashMap<String, LinkedList<DocPair>>) t.getPositionResults().clone();
        }

        // clear
        t.clear();
        ng.clear();
    }
}
