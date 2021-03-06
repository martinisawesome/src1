package storage;

import maps.DocumentUrlMap;
import def.StopWords;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import textprocessor.TextProcessor;

/**
 * Used for post-processing all the file locations
 */
public class FileSystem
{

    public static final String TOKEN = "Token";
    public static final String TEXT = "Text";
    public static final String CRAWLER_DIRECTORY = "E:\\Crawl\\";
    public static final String MISC_PARTITION_DIRECTORY = CRAWLER_DIRECTORY + "Misc\\";
    public static final String TFDF_PARTITION_DIRECTORY = CRAWLER_DIRECTORY + "TFDF\\";
    public static final String CONTENT_PARTITION_DIRECTORY = CRAWLER_DIRECTORY + "ContentPart\\";
    public static final String RAW_DIRECTORY = CRAWLER_DIRECTORY + "RAW\\";
    public static final String TOKEN_DIRECTORY = CRAWLER_DIRECTORY + "Token\\";
    public static final String POSITION_DIRECTORY = CRAWLER_DIRECTORY + "Position\\";
    public static final String TEXT_DIRECTORY = TOKEN_DIRECTORY;
    public static final String CRAWLING_HEADER_DIRECTORY = CRAWLER_DIRECTORY + "Head\\";
    public static final String ANCHOR_NAME = "Anchor";
    public static final String DOCUMENT_MAP_NAME = "Doc_Map";
    public static final String DOCUMENT_TITLE_NAME = "Doc_Title";
    public static final String DOCUMENT_SIZE_NAME = "Doc_Size";
    public static final String FREQ_FILE = "IndexFreq";
    public static final String INDEX_FILE = "Index";
    public static final String FOUR_GRAM = "Index4Gram";
    public static final String THREE_GRAM = "Index3Gram";
    public static final String TWO_GRAM = "Index2Gram";

    private final DocumentUrlMap documentMap;

    // Store Header Logs
    // Store Content Logs
    // Store Document ID logs
    // Store Indexer Logs
    public FileSystem() throws IOException
    {
        this.documentMap = new DocumentUrlMap();
    }

    public DocumentUrlMap getDocumentMap()
    {
        return documentMap;
    }

    // DISCLAIMER!!! This method was taken from stackoverflow.com
    public static void deleteFolder(File folder)
    {
        File[] files = folder.listFiles();
        if (files != null)
        {
            for (File f : files)
            {
                if (f.isDirectory())
                {
                    deleteFolder(f);
                }
                else
                {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    public static int processTokenPages()
    {
        // Clear all text data before making new
        FileSystem.clearTextData();
        LinkedList<String> pages = new LinkedList<>();

        for (File page : getAllContentTextFiles())   //turns all files into text only
        {

            try
            {
                if (page == null)
                {
                    continue;
                }

                if (!pages.contains(page.getName()))
                {
                    List<String> tokenList = TextProcessor.tokenizeFile(page);

                    //don't process empty files...
                    if (tokenList.isEmpty())
                    {
                        continue;
                    }

                    File content = new File(FileSystem.TOKEN_DIRECTORY + page.getName() + FileSystem.TOKEN);
                    FileWriter wr = new FileWriter(content, false);
                    StringBuilder sb = new StringBuilder();
                    int counter = 0;
                    for (String s : tokenList)
                    {
                        sb.append(s);
                        counter++;

                        if (counter % 100 == 0)
                        {
                            sb.append("\n");
                        }
                        else
                        {
                            sb.append(" ");
                        }

                        if (counter > 5000)
                        {
                            counter = 0;
                            wr.write(sb.toString());
                            sb = new StringBuilder();
                        }
                    }
                    wr.write(sb.toString());
                    wr.close();

                    //==================================================================
                    pages.add(page.getName());
                }
                else
                {
                    //System.out.println("Duplicate Page: " + page.getName());
                }
            }
            catch (IOException e)
            {
                System.out.println("Failed to open and tokenize a file: " + page.getName());
                e.printStackTrace();
            }
        }

        return pages.size();
    }

    /**
     * Clears all files with content only
     */
    public static void clearTextData()
    {
        File[] files = new File(TOKEN_DIRECTORY).listFiles();
        if (files != null)
        {
            for (File f : files)
            {
                if (f.getName().contains(TEXT))
                {

                    f.delete();
                }
            }
        }
    }

    public static void clearContentData()
    {
        File[] files = new File(CONTENT_PARTITION_DIRECTORY).listFiles();
        if (files != null)
        {
            for (File f : files)
            {

                f.delete();

            }
        }
    }

    /**
     * Looks for all files with Token in it
     *
     * @return
     */
    public static LinkedList<File> getAllGramFiles()
    {

        LinkedList<File> domains = new LinkedList<>();

        File directory = new File(CONTENT_PARTITION_DIRECTORY);
        File[] files = directory.listFiles();
        if (files != null)
        {
            for (File f : files)
            {
                if (f.isDirectory())
                {

                }
                else if (f.getName().contains("Gram")
                         && !f.getName().contains("Complete"))
                {
                    domains.add(f);
                }
            }
        }
        return domains;
    }

    /**
     * Looks for all files with Token in it
     *
     * @return
     */
    public static LinkedList<File> getAllTermFrequencyFiles()
    {

        LinkedList<File> domains = new LinkedList<>();

        File directory = new File(CONTENT_PARTITION_DIRECTORY);
        File[] files = directory.listFiles();
        if (files != null)
        {
            for (File f : files)
            {
                if (f.isDirectory())
                {

                }
                else if (f.getName().contains(FREQ_FILE)
                         && !f.getName().contains("Complete"))
                {
                    domains.add(f);
                }
            }
        }
        return domains;
    }

    public static LinkedList<File> getAllTokenTextFiles()
    {

        LinkedList<File> domains = new LinkedList<>();
        File directory = new File(TOKEN_DIRECTORY);
        File[] files = directory.listFiles();
        if (files != null)
        {
            for (File f : files)
            {
                if (f.isDirectory())
                {

                }
                else if (f.getName().contains(TOKEN))
                {
                    domains.add(f);
                }
            }
        }
        return domains;
    }

    public static void computeFrequencies(int n) throws IOException
    {
        LinkedList<File> files = getAllTokenTextFiles();
        TextProcessor p = new TextProcessor();
        for (File f : files)
        {

            int id = Integer.parseInt(f.getName().replaceAll("[^0-9]", ""));
           
            
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            String curr;
            ArrayList<String> tokenList = new ArrayList<>();

            // fina all the words in this line
            while ((curr = br.readLine()) != null)
            {
                for (String word : curr.split(" "))
                {
                    //Do not use stop words
                    boolean isStop = false;
                    for (String stopWords : StopWords.WORDS)
                    {
                        if (stopWords.equals(word))
                        {
                            isStop = true;
                            break;
                        }
                    }

                    if (!isStop)
                    {
                        // Find file where this belongs
                        tokenList.add(word);
                    }
                }
            }

            br.close();
            fr.close();

            p.computeNGramFrequencies(id, tokenList, n);
        }

        p.flush(n);

    }

    /**
     * Binary merges all files
     *
     * @param directoryName
     * @param nameHas(either 3Gram or Freq)
     * @param index
     * @return
     * @throws IOException
     */
    public static File binaryMergeByAlphabetic(String directoryName, String nameHas, int index) throws IOException
    {
        File directory = new File(directoryName);
        File[] files = directory.listFiles();
        ArrayList<File> targetFiles = new ArrayList<>();
        if (files == null)
        {
            return null;
        }

        // First add all candidate files
        for (File f : files)
        {
            if (f.isDirectory())
            {

            }
            else if (f.getName().contains(nameHas))
            {
                targetFiles.add(f);
            }
        }

        if (targetFiles.size() == 1)
        {
            return targetFiles.get(0);
        }

        File exceptionalFile = (targetFiles.size() % 2 == 0)
                               ? null : targetFiles.get(targetFiles.size() - 1);

        for (int i = 0; i + 1 < targetFiles.size(); i += 2)
        {
            File first = targetFiles.get(i);
            File second = targetFiles.get(i + 1);

            //     System.out.println(first + " Open");
            //      System.out.println(second + " Open");
            FileReader fr = new FileReader(first);
            BufferedReader br0 = new BufferedReader(fr);
            FileReader fr1 = new FileReader(second);
            BufferedReader br1 = new BufferedReader(fr1);
            FileWriter fw = new FileWriter(directoryName + nameHas + "0" + (index++));
            StringBuilder sb = new StringBuilder();
            boolean f0Has = true;
            boolean f1Has = true;

            String curr1 = null;
            String curr0 = null;

            boolean curr1Clear = true;
            boolean curr0Clear = true;

            //loop binary
            for (int writeIndex = 0;; writeIndex++)
            {
                // Flush to file if sb too large
                if (writeIndex > 10000)
                {
                    writeIndex = 0;
                    fw.write(sb.toString());
                    sb = new StringBuilder();
                }

                // both files are empty
                if (!f0Has && !f1Has)
                {
                    break;
                }

                // does file 0 need to update?
                if (f0Has && curr0Clear)
                {
                    curr0 = br0.readLine();
                    if (curr0 == null || curr0.isEmpty())
                    {
                        f0Has = false;
                    }
                    curr0Clear = false;

                }
                // does file 1 need to update?
                if (f1Has && curr1Clear)
                {
                    curr1 = br1.readLine();
                    if (curr1 == null || curr1.isEmpty())
                    {
                        f1Has = false;
                    }
                    curr1Clear = false;
                }

                // need to compare both files
                if (f0Has && f1Has)
                {
                    String[] parm0 = curr0.split(":");
                    String[] parm1 = curr1.split(":");

                    int compares = parm0[0].compareTo(parm1[0]);

                    if (compares == 0)
                    {
                        if (nameHas.contains(INDEX_FILE))
                        {
                            sb.append(curr0);
                            sb.append("\n");
                            curr0Clear = true;
                        }
                        else
                        {
                            int doc0 = Integer.parseInt(parm0[1]);
                            int doc1 = Integer.parseInt(parm1[1]);
                            if (doc0 < doc1)
                            {
                                sb.append(curr0);
                                sb.append("\n");
                                curr0Clear = true;
                            }
                            else if (doc0 > doc1)
                            {
                                sb.append(curr1);
                                sb.append("\n");
                                curr1Clear = true;
                            }
                            else
                            {

                                sb.append(curr0);
                                sb.append("\n");
                                curr0Clear = true;
                            }
                        }
                    }
                    else if (compares < 0)
                    {
                        sb.append(curr0);
                        sb.append("\n");
                        curr0Clear = true;
                    }
                    else
                    {
                        sb.append(curr1);
                        sb.append("\n");
                        curr1Clear = true;
                    }

                }
                else if (f0Has)
                {
                    sb.append(curr0);
                    sb.append("\n");
                    curr0Clear = true;
                }
                else if (f1Has)
                {
                    sb.append(curr1);
                    sb.append("\n");
                    curr1Clear = true;
                }

            }

            fw.write(sb.toString());
            fr1.close();
            fw.close();
            fr.close();

            //    System.out.println("  Done!");
        }   // end of each file binary

        for (File f : targetFiles)
        {
            //don't delete the odd file out
            if (!f.equals(exceptionalFile))
            {
                f.delete();
            }
        }

        // Continually merge until 1 file is left
        return binaryMergeByAlphabetic(directoryName, nameHas, index);
    }

    /**
     * Parses all files to retrieve content from all files, and creates new files with content only.
     * We will also pull the subdomain name from first line of file
     *
     * @return
     */
    public static LinkedList<File> getAllContentTextFiles()
    {
        LinkedList<Integer> hashedContents = new LinkedList<>();
        Pattern numberic = Pattern.compile("[0-9]*$");

        LinkedList<File> domains = new LinkedList<>();
        File directory = new File(RAW_DIRECTORY);
        File[] files = directory.listFiles();
        if (files != null)
        {
            for (File f : files)
            {
                if (f.isDirectory())
                {

                }
                else if (numberic.matcher(f.getName()).matches() && !f.getName().contains("Crawler"))
                {
                    File file = parseContentFileForText(hashedContents, f);
                    if (file != null)
                    {
                        domains.add(file);
                    }
                }
            }
        }
        return domains;
    }

    /**
     * Parses a content file and returns a new file with no HTML markup
     *
     * @param hashedContents
     * @param titles
     * @param file
     * @return
     */
    private static File parseContentFileForText(LinkedList<Integer> hashedContents, File file)
    {
        File contentFile = new File(TOKEN_DIRECTORY + TEXT + file.getName());
        boolean badFile = false;
        try
        {
            StringBuilder sb = new StringBuilder();
            FileWriter wr = new FileWriter(contentFile, false);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String curr;
            boolean found = false;
            String url = br.readLine();
            //  documentMap.add(Integer.parseInt(file.getName()), url);

            //Scan for the Sub-Domains text line
            while ((curr = br.readLine()) != null)
            {
                if (!found)
                {
                    if ("#Title#".equals(curr))
                    {
                        //Skip Title of the text
                        while (!"#Text#".equals(curr = br.readLine()))
                        {
                            // keep skipping title
                        }
                        found = true;
                    }
                    else if ("#Text#".equals(curr))
                    {
                        found = true;

                    }
                    else
                    {
                        throw new IllegalArgumentException("No text after header!");
                    }
                }   //end if found

                //Do not include HTML markup
                if (curr.equals(SPACER))
                {
                    break;
                }
                else if (found && !curr.isEmpty() && !curr.contains("#Text#"))
                {
                    sb.append(curr);
                    sb.append(' ');
                }

            }

            String strings = sb.toString();
            int hash = strings.hashCode();
            if (hashedContents.contains(hash))
            {
                //System.out.println("Duplicate hash for file: " + file.getName() + " with hash: " + hash);
                badFile = true;
            }
            else
            {
                //System.out.println("  Hasing file: " + file.getName() + " with hash " + hash);
                hashedContents.add(hash);
            }

            wr.write(strings);
            fr.close();
            wr.close();
        }

        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }

        // Do not return duplicate hashed files
        if (badFile)
        {
            return null;
        }

        return contentFile;
    }
    public static final String SPACER = "============================================================";

}
