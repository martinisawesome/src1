package engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import tfidf.IndexParser;
import tfidf.TFIDFPair;

/**
 * For each query term, we should start a new thread
 */
public class NGramThreading
{
    // Tracks which files are being waited on <filename, thread>
    private final HashMap<String, Thread> inProgressMap;
    private final HashMap<String, LinkedList<Thread>> waitingMap;

    // Gets the results of the queries <word, result list>
    private final HashMap<String, LinkedList<TFIDFPair>> weightResults;

    private final int searchLimit;
    private boolean first;

    public NGramThreading(int searchLimit)
    {
        first = true;
        inProgressMap = new HashMap<>();
        waitingMap = new HashMap<>();
        weightResults = new HashMap<>();
        this.searchLimit = searchLimit;

    }

    //  _____      _   _                
    // |  __ \    | | | |               
    // | |  \/ ___| |_| |_ ___ _ __ ___ 
    // | | __ / _ \ __| __/ _ \ '__/ __|
    // | |_\ \  __/ |_| ||  __/ |  \__ \
    //  \____/\___|\__|\__\___|_|  |___/
    public HashMap<String, LinkedList<TFIDFPair>> getWeightResults()
    {
        return weightResults;
    }

    public void clear()
    {
        inProgressMap.clear();
        waitingMap.clear();
        weightResults.clear();
    }

    // ______      _     _ _       ___  ___     _   _               _     
    // | ___ \    | |   | (_)      |  \/  |    | | | |             | |    
    // | |_/ /   _| |__ | |_  ___  | .  . | ___| |_| |__   ___   __| |___ 
    // |  __/ | | | '_ \| | |/ __| | |\/| |/ _ \ __| '_ \ / _ \ / _` / __|
    // | |  | |_| | |_) | | | (__  | |  | |  __/ |_| | | | (_) | (_| \__ \
    // \_|   \__,_|_.__/|_|_|\___| \_|  |_/\___|\__|_| |_|\___/ \__,_|___/
    public boolean checkAndStartWaitingList()
    {
        Set<Entry<String, LinkedList<Thread>>> set = waitingMap.entrySet();
        boolean remain = false;
        for (Entry<String, LinkedList<Thread>> entry : set)
        {
            LinkedList<Thread> threads = entry.getValue();
            if (!threads.isEmpty())
            {
                String fileName = entry.getKey();
                if (isDeadThread(fileName))
                {
                    startThread(fileName, threads.removeFirst());
                }
                remain = true;
            }
        }

        // If nothing waiting, check what's still in progress
        if (!remain)
        {
            for (Thread t : inProgressMap.values())
            {
                if (t.isAlive())
                {
                    return true;
                }
            }
        }

        return remain;
    }

    public void searchWord(List<String> wordList)
    {
        int grams = wordList.size();
        String word = wordList.get(0);
        for (int i = 1; i < grams; i++)
        {
            word += ", " + wordList.get(i);
        }

        String weiFileName = IndexParser.getGramFile(word, grams);
        Thread newWei = getWeightThread(first, word, weiFileName);

        if (isDeadThread(weiFileName))
        {
            startThread(weiFileName, newWei);

        }
        else
        {
            addToWaiting(weiFileName, newWei);
        }

        first = false;
    }

    // ______     _            _         _   _      _                 
    // | ___ \   (_)          | |       | | | |    | |                
    // | |_/ / __ ___   ____ _| |_ ___  | |_| | ___| |_ __   ___ _ __ 
    // |  __/ '__| \ \ / / _` | __/ _ \ |  _  |/ _ \ | '_ \ / _ \ '__|
    // | |  | |  | |\ V / (_| | ||  __/ | | | |  __/ | |_) |  __/ |   
    // \_|  |_|  |_| \_/ \__,_|\__\___| \_| |_/\___|_| .__/ \___|_|            
    //                                               |_|   
    private void addToWaiting(String fileName, Thread t)
    {
        LinkedList<Thread> list = waitingMap.get(fileName);
        if (list == null)
        {
            list = new LinkedList<>();
        }

        list.add(t);
        waitingMap.put(fileName, list);
    }

    private boolean isDeadThread(String fileName)
    {
        Thread currPos = inProgressMap.get(fileName);
        return (currPos == null || !currPos.isAlive());
    }

    private void startThread(String posFileName, Thread newPos)
    {
        newPos.start();
        inProgressMap.put(posFileName, newPos);
    }

    private Thread getWeightThread(final boolean first, final String word, final String fileName)
    {
        Thread t1 = new Thread()
        {

            @Override
            public void run()
            {
                try
                {

                    LinkedList<TFIDFPair> pairs = IndexParser.getWordWeight(word, fileName, searchLimit);

                    // Add more weight to first!
                    if (first)
                    {
                        for (TFIDFPair pair : pairs)
                        {
                            pair.incWeight(10);
                        }
                    }

                    weightResults.put(word, pairs);
                }
                catch (IOException ex)
                {
                    System.err.println(ex.getMessage());
                    throw new RuntimeException(ex);
                }
            }
        };
        return t1;
    }

}
