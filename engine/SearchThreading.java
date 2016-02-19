package engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import tfidf.DocPair;
import tfidf.IndexParser;
import tfidf.PostingProcessor;
import tfidf.TFIDFPair;

/**
 * For each query term, we should start a new thread
 */
public class SearchThreading
{
    // Tracks which files are being waited on <filename, thread>
    private final HashMap<String, Thread> inProgressMap;
    private final HashMap<String, LinkedList<Thread>> waitingMap;

    // Gets the results of the queries <word, result list>
    private final HashMap<String, LinkedList<DocPair>> positionResults;
    private final HashMap<String, LinkedList<TFIDFPair>> weightResults;

    private final int searchLimit;
    private final boolean searchPositions;

    public SearchThreading(boolean searchPositions, int searchLimit)
    {
        inProgressMap = new HashMap<>();
        waitingMap = new HashMap<>();
        positionResults = new HashMap<>();
        weightResults = new HashMap<>();
        this.searchLimit = searchLimit;
        this.searchPositions = searchPositions;
    }

    //  _____      _   _                
    // |  __ \    | | | |               
    // | |  \/ ___| |_| |_ ___ _ __ ___ 
    // | | __ / _ \ __| __/ _ \ '__/ __|
    // | |_\ \  __/ |_| ||  __/ |  \__ \
    //  \____/\___|\__|\__\___|_|  |___/
    public HashMap<String, LinkedList<DocPair>> getPositionResults()
    {
        return positionResults;
    }

    public HashMap<String, LinkedList<TFIDFPair>> getWeightResults()
    {
        return weightResults;
    }

    public void clear()
    {
        inProgressMap.clear();
        waitingMap.clear();
        positionResults.clear();
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

    public void searchWord(String word)
    {
        if (searchPositions)
        {
            String posFileName = IndexParser.getPositionFile(word);
            Thread newPos = getPositionsThread(word, posFileName);
            // Check if okay to start current threads, else put them in waiting!
            if (isDeadThread(posFileName))
            {
                startThread(posFileName, newPos);
            }
            else
            {
                addToWaiting(posFileName, newPos);
            }
        }

        String weiFileName = IndexParser.getWeightFile(word);
        Thread newWei = getWeightThread(word, weiFileName);

        if (isDeadThread(weiFileName))
        {
            startThread(weiFileName, newWei);

        }
        else
        {
            addToWaiting(weiFileName, newWei);
        }
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

    private Thread getPositionsThread(final String word, final String posFileName)
    {
        Thread t1 = new Thread()
        {

            @Override
            public void run()
            {
                try
                {

                    LinkedList<DocPair> pairs = IndexParser.getWordPosition(word, posFileName);
                    positionResults.put(word, pairs);
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

    private Thread getWeightThread(final String word, final String fileName)
    {
        Thread t1 = new Thread()
        {

            @Override
            public void run()
            {
                try
                {
                    LinkedList<TFIDFPair> pairs = IndexParser.getWordWeight(word, fileName, searchLimit);

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
    //  _____                             _____         _     _____
    // |_   _|                           |_   _|       | |   /  __ \
    //   | |  __ _ _ __   ___  _ __ ___    | | ___  ___| |_  | /  \/ __ _ ___  ___  ___
    //   | | / _` | '_ \ / _ \| '__/ _ \   | |/ _ \/ __| __| | |    / _` / __|/ _ \/ __|
    //  _| || (_| | | | | (_) | | |  __/   | |  __/\__ \ |_  | \__/\ (_| \__ \  __/\__ \
    //  \___/\__, |_| |_|\___/|_|  \___|   \_/\___||___/\__|  \____/\__,_|___/\___||___/
    //       |___/

    /**
     * Check if the files are okay!
     *
     * @return
     * @throws IOException
     */
    public List<String> getUniqueTokensEntireCorpus() throws IOException
    {
        LinkedList<String> tokens = new LinkedList<>();
        File file = new File(PostingProcessor.MEGA_FILE);
        String curr;
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        while ((curr = br.readLine()) != null)
        {
            String[] splits = curr.split(" ");
            for (String word : splits)
            {
                String posFileName = IndexParser.getPositionFile(word);
                String weiFileName = IndexParser.getWeightFile(word);

                // Start Position Thread
                Thread t1 = getPositionsThread(word, posFileName);
                t1.start();

                Thread t2 = new Thread()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            if (IndexParser.getWordPostings(word).isEmpty())
                            {
                                System.out.println("No Posting: " + word);
                            }
                        }
                        catch (IOException ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                };
                t2.start();
                // Start Weight Thread
                Thread t0 = getWeightThread(word, weiFileName);
                t0.start();
                while (t1.isAlive() || t0.isAlive() || t2.isAlive())
                {
                    //do nothing
                }

                HashMap<String, LinkedList<DocPair>> posResults = getPositionResults();
                HashMap<String, LinkedList<TFIDFPair>> tfidfResults = getWeightResults();
                System.out.println(posResults);
                System.out.println(tfidfResults);
                clear();
            }
        }
        fr.close();
        return tokens;
    }
}
