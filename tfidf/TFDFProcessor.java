package tfidf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import maps.DocumentSize;
import storage.FilePartioning;
import storage.FileSystem;

/**
 * Looks through my files to find the TFDF
 */
public class TFDFProcessor
{
    private final int limit;
    private final DocumentSize docSizer;
    // No file header in name

    public TFDFProcessor() throws IOException
    {
        this(-1);

    }

    public TFDFProcessor(int limit) throws IOException
    {
        docSizer = new DocumentSize();
        this.limit = limit;

    }

    public void clear()
    {
        docSizer.clear();
    }

    public void writeTfDfFiles() throws IOException
    {
        docSizer.readInFile();

        // Get all files that record term frequencies
        LinkedList<File> files = FileSystem.getAllTermFrequencyFiles();

        for (File file : files)
        {
            String curr;
            String prevWord = null;
            String fileName = file.getName();
            int filelength = fileName.length();

            // File Write
            String fileEnder = fileName.substring(filelength - 2, filelength);
            File writeFile = new File(FileSystem.TFDF_PARTITION_DIRECTORY + fileEnder);
            writeFile.delete();
            writeFile.createNewFile();
            FileWriter fw = new FileWriter(writeFile, false);

            // file Read
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);

            // Store number of documents containing a term
            ArrayList<DocPair> docIdList = new ArrayList<>();

            // Look through all files to find TF and DF
            while ((curr = br.readLine()) != null)
            {
                String[] splits = curr.split(" ");
                String word = splits[0].replace(":", "");
                Integer docID = Integer.parseInt(splits[1]);
                Integer freqCount = Integer.parseInt(splits[2]);

                if (limit > 0 && docID > limit)
                {
                    continue;
                }

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
                        StringBuilder sb = new StringBuilder();
                        double docFreq = docIdList.size();
                        for (int i = 0; i < docFreq; i++)
                        {
                            DocPair pair = docIdList.get(i);
                            double weight = computeWeight(pair, docFreq);
                            String string = String.format("%s:%d:%.8f%n", prevWord, pair.docID, weight);
                            sb.append(string);
                        }
                        fw.write(sb.toString());
                    }

                    docIdList.clear();
                    prevWord = word;
                    docIdList.add(new DocPair(docID, freqCount));
                }
            }   //eWhile end of file

            // Write the final word
            Collections.sort(docIdList);
            StringBuilder sb = new StringBuilder();
            int docFreq = docIdList.size();
            for (int i = 0; i < docFreq; i++)
            {
                DocPair pair = docIdList.get(i);
                double weight = computeWeight(pair, docFreq);
                String string = String.format("%s:%d:%.8f%n", prevWord, pair.docID, weight);
                sb.append(string);

            }

            fw.write(sb.toString());

            // Close the files
            fw.close();
            fr.close();

        } //eFor end of directory

        docSizer.clear();
    }

    private double computeWeight(DocPair pair, double docFreq)
    {
        int oldFreq = pair.pos;

        // Compute TF
        double logOldFreq = Math.log10(1 + oldFreq);

        // Compute IDF
        double n = limit > 0 ? limit : docSizer.getSize();
        double logOldDoc = n / docFreq;
        logOldDoc = Math.log10(logOldDoc);

        //Compute Weight
        double weight = logOldFreq * logOldDoc;
        return weight;
    }

    /**
     * Gets the term frequency list for a word
     *
     * @param word
     * @return
     * @throws IOException
     */
    public static LinkedList<DocPair> getTermFrequencyList(String word) throws IOException
    {
        String modWord = word + ":";
        LinkedList<DocPair> positions = new LinkedList<>();
        String fileName = FilePartioning.getPartitionFileName(FileSystem.CONTENT_PARTITION_DIRECTORY, word, FileSystem.FREQ_FILE);
        File file = new File(fileName);
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String curr;
        DocPair p;
        boolean found = false;

        while ((curr = br.readLine()) != null)
        {
            String[] splits = curr.split(" ");
            if (splits[0].equals(modWord))
            {
                found = true;
                p = new DocPair(Integer.parseInt(splits[1]), Integer.parseInt(splits[2]));
                positions.add(p);
            }
            // Otherwise, stop searching if we found the word before
            else if (found)
            {
                break;
            }
        }

        fr.close();

        return positions;
    }
}
