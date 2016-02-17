package tfidf;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import storage.FileSystem;
import textprocessor.FreqIndex;
import textprocessor.TextProcessor;

/**
 * Created IndexPos files that have token: docID position; listings
 * These files have to be parsed to find out where a file should go
 */
public class DocumentPositionProcessor
{
    public static final String FILE_HEADER = FileSystem.INDEX_FILE + FileSystem.POS;
    public static final String DIRECTORY = FileSystem.POSITION_DIRECTORY;
    private final List<FreqIndex<String>> wordCount;

    private int freqFileCount;
    private final int limit;

    public DocumentPositionProcessor()
    {
        this(-1);
    }
    
        public DocumentPositionProcessor(int limit)
    {
        wordCount = new LinkedList<>();
        freqFileCount = 0;
        this.limit = limit;
    }

    public void positionAllFiles() throws IOException
    {
        // Create positions for each token file
        LinkedList<File> files = FileSystem.getAllTokenTextFiles();
        for (File f : files)
        {
            createPositionFile(f);
        }

        // Create joined index files for each position file
        writeIndexToFile();
    }

    public void createPositionFile(File f) throws IOException
    {
        int docId = Integer.parseInt(f.getName().replace(FileSystem.TEXT, "").replace(FileSystem.TOKEN, ""));
        
        // Check if we want this file
        if (limit > 0 && docId > limit)
        {
            return;
        }
        
        
        String name = FileSystem.POS + docId;
        ArrayList<String> tokenList = TextProcessor.tokenizeFile(f);
        File file = new File(FileSystem.POSITION_DIRECTORY + name);
        FreqIndex id;

        // delete and make new file
        file.delete();
        file.createNewFile();

        //write this file out
        FileWriter orderFw = new FileWriter(file);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokenList.size(); i++)
        {
            id = new FreqIndex(tokenList.get(i), docId, i);

            wordCount.add(id);

            if (wordCount.size() > 160000)
            {
                writeIndexToFile();
            }

            //Write to Pos File
            sb.append(tokenList.get(i));
            sb.append(":");
            sb.append(i);
            sb.append(";");
            if (i % 160000 == 0)
            {
                orderFw.write(sb.toString());
                sb = new StringBuilder();
            }

        }

        orderFw.write(sb.toString());
        orderFw.close();
    }

    /**
     * Writes the current position counts stored in memory to file.
     *
     * @param wordCountMap
     * @param wordCount
     * @throws IOException
     */
    private void writeIndexToFile() throws IOException
    {
        Collections.sort(wordCount);
        FileWriter wr = new FileWriter(FileSystem.POSITION_DIRECTORY + FileSystem.INDEX_FILE + FileSystem.POS + (freqFileCount++), false);
        StringBuilder sb = new StringBuilder();
        for (FreqIndex f : wordCount)
        {
            sb.append(f.toSmartString());
            sb.append("\n");
        }
        wr.write(sb.toString());
        wr.close();

        wordCount.clear();
    }
}
