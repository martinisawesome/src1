package tfidf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import storage.FileSystem;

/**
 * Generates postings for each term
 *
 * @author mao_ma
 */
public class PostingProcessor
{
    public static final String DIRECTORY = FileSystem.CRAWLER_DIRECTORY + "Posting\\";
    public static final String MEGA_FILE = DIRECTORY + "MegaFile";

    public static int createPostingsFiles() throws IOException
    {
        StringBuilder sb;
        String curr;
        String head;
        // Create megafile with all unique files
        int count = 0;
        File f = new File(MEGA_FILE);
        f.delete();
        f.createNewFile();
        FileWriter megaWriter = new FileWriter(f);

        // Get all files that record term frequencies
        LinkedList<File> files = FileSystem.getAllTermFrequencyFiles();
        for (File file : files)
        {
            String prevWord = null;
            String fileName = file.getName();
            int filelength = fileName.length();

            // File Write
            String fileEnder = fileName.substring(filelength - 1, filelength);
            File writeFile = new File(DIRECTORY + fileEnder);
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
                        count++;
                        megaWriter.write(prevWord + ((count + 1) % 20 == 0 ? "\n" : " "));

                        Collections.sort(docIdList);
                        sb = new StringBuilder();
                        head = String.format("%s:", prevWord);
                        sb.append(head);
                        double docFreq = docIdList.size();
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
            }   //eWhile end of file

            //add to mega file
            count++;
            megaWriter.write(prevWord + ((count + 1) % 20 == 0 ? "\n" : " "));

            // Write the final word
            Collections.sort(docIdList);
            sb = new StringBuilder();
            head = String.format("%s:", prevWord);
            sb.append(head);
            double docFreq = docIdList.size();
            for (int i = 0; i < docFreq; i++)
            {
                DocPair pair = docIdList.get(i);
                String string = String.format("%d,%d%s", pair.docID, pair.pos,
                                              i == docFreq - 1 ? "" : ";");
                sb.append(string);

            }

            fw.write(sb.toString());

            // Close the files
            fw.close();
            fr.close();

        } //eFor end of directory

        megaWriter.close();
        return count;
    }

}
