package tfidf;

import def.StopWords;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import storage.FilePartioning;
import storage.FileSystem;
import textprocessor.FreqIndex;
import textprocessor.TextProcessor;

/**
 * Created IndexPos files that have token: docID position; listings
 * These files have to be parsed to find out where a file should go
 */
public class PositionProcessor
{
    public static final String POS = "Pos";
    public static final String LOC = "Loc";
    private static final String TEMP_HEADER = POS + IndexParser.KEEP;
    private static final String TEMP_HEADER1 = POS + IndexParser.IGNORE;
    public static final String FILE_HEADER = LOC + IndexParser.KEEP;
    public static final String FILE_HEADER1 = LOC + IndexParser.IGNORE;
    public static final String DIRECTORY = FileSystem.POSITION_DIRECTORY;
    private final List<FreqIndex<String>> wordCount;

    private int freqFileCount;
    private final int limit;

    public PositionProcessor()
    {
        this(-1);
    }

    public PositionProcessor(int limit)
    {
        wordCount = new LinkedList<>();
        freqFileCount = 0;
        this.limit = limit;
    }

    public void deleteIndexFiles()
    {

        File directory = new File(DIRECTORY);
        File[] files = directory.listFiles();
        if (files != null)
        {
            for (File f : files)
            {
                if (f.isDirectory())
                {

                }
                else
                {
                    f.delete();
                }
            }
        }
    }

    public void positionAllFiles() throws IOException
    {
        deleteIndexFiles();

        // Create positions for each token file
        LinkedList<File> files = FileSystem.getAllTokenTextFiles();
        for (File f : files)
        {
            createPositionFile(f);
        }

        // Create joined index files for each position file
        writeIndexToFile();

        File f = FileSystem.binaryMergeByAlphabetic(PositionProcessor.DIRECTORY, TEMP_HEADER, 0);
        FilePartioning.partitionOutFile(TEMP_HEADER, PositionProcessor.DIRECTORY, f.getName());

        f = FileSystem.binaryMergeByAlphabetic(PositionProcessor.DIRECTORY, TEMP_HEADER1, 0);
        FilePartioning.partitionOutFile(TEMP_HEADER1, PositionProcessor.DIRECTORY, f.getName());

        //shortenFile();
    }

    /**
     * Decreases the length of each position file
     *
     * @throws IOException
     */
    public void shortenFile() throws IOException
    {
        String head;
        String curr;
        StringBuilder sb;
        // Shorten the file!
        File[] list = new File(DIRECTORY).listFiles();
        for (File file : list)
        {
            if (file.getName().startsWith(TEMP_HEADER) || file.getName().startsWith(TEMP_HEADER1))
            {
                String fileName = file.getName().replace(POS, LOC);
                FileWriter fw = new FileWriter(DIRECTORY + fileName);
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);

                String prevWord = null;

                // Store number of documents containing a term
                ArrayList<DocPair> docIdList = new ArrayList<>();

                while ((curr = br.readLine()) != null)
                {
                    String[] splits = curr.split(":");

                    String word = splits[0];

                    Integer docID = Integer.parseInt(splits[1]);
                    Integer freqCount = Integer.parseInt(splits[2]);

                    if (word.equals(prevWord))
                    {
                        docIdList.add(new DocPair(docID, freqCount));

                    }
                    // Otherwise, start a different word
                    else
                    {

                        // write prevWord
                        if (prevWord != null)
                        {
                            Collections.sort(docIdList);
                            sb = new StringBuilder();
                            head = String.format("%s:", prevWord);
                            sb.append(head);
                            int docFreq = docIdList.size();
                            for (int i = 0; i < docFreq; i++)
                            {
                                DocPair pair = docIdList.get(i);
                                String string = String.format("%d,%d%s", pair.docID, pair.pos,
                                                              i == docFreq - 1 ? "" : ";");
                                sb.append(string);

                            }
                            sb.append("\n");
                            fw.write(sb.toString());
                        }

                        docIdList.clear();
                        prevWord = word;
                        docIdList.add(new DocPair(docID, freqCount));
                    }

                } //end of File

                Collections.sort(docIdList);
                sb = new StringBuilder();
                head = String.format("%s:", prevWord);
                sb.append(head);
                int docFreq = docIdList.size();
                for (int i = 0; i < docFreq; i++)
                {
                    DocPair pair = docIdList.get(i);
                    String string = String.format("%d,%d%s", pair.docID, pair.pos,
                                                  i == docFreq - 1 ? "" : ";");
                    sb.append(string);

                }
                fw.write(sb.toString());

                fw.close();
                fr.close();
            }

        }
    }

    public void createPositionFile(File f) throws IOException
    {
        int docId = Integer.parseInt(f.getName().replaceAll("[^0-9]", ""));

        // Check if we want this file
        if (limit > 0 && docId > limit)
        {
            return;
        }

        String name = POS + docId;
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
        FileWriter wr = new FileWriter(FileSystem.POSITION_DIRECTORY + TEMP_HEADER + (freqFileCount++), false);
        FileWriter wr1 = new FileWriter(FileSystem.POSITION_DIRECTORY + TEMP_HEADER1 + (freqFileCount++), false);
        StringBuilder sb = new StringBuilder();
        StringBuilder sb1 = new StringBuilder();
        for (FreqIndex f : wordCount)
        {
            if (!f.token.toString().matches(".*[0-9].*"))
            {
                sb1.append(f.toSmartString());
                sb1.append("\n");
            }
            sb.append(f.toSmartString());
            sb.append("\n");
        }
        wr1.write(sb1.toString());
        wr.write(sb.toString());
        wr.close();
        wr1.close();

        wordCount.clear();
    }

    @Deprecated
    public static void listAll(char base) throws IOException
    {
        File directory = new File(DIRECTORY);
        File[] files = directory.listFiles();
        if (files != null)
        {
            for (File f : files)
            {
                if (f.isDirectory())
                {

                }
                else if (f.getName().contains(FILE_HEADER1 + base))
                {
                    FileReader fr = new FileReader(f);
                    BufferedReader br = new BufferedReader(fr);
                    String curr;

                    while ((curr = br.readLine()) != null)
                    {

                        String[] split = curr.split(":");
                        String word = split[0];
                        if (StopWords.isStop(word))
                        {
                            continue;
                        }
                        int length = split[1].split(";").length;
                        if (length > 10 || length == 1 || word.length() > 20)
                        {
                            continue;
                        }
                        System.out.println(curr);
                        String weiFileName = IndexParser.getWeightFile(word);
                        System.out.println(IndexParser.getWordWeight(word, weiFileName, -1));

                    }
                    fr.close();
                }
            }
        }
    }
}
