package links;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Pattern;
import maps.DocumentUrlMap;
import storage.FileSystem;

/**
 *
 * Will go through all RAW files and rips links
 */
public class LinksProcess
{
    public static final String DIRECTORY = FileSystem.CRAWLER_DIRECTORY + "Link\\";
    private static final Pattern BAD_EXTENSIONS = Pattern.compile(".*\\.(css|js|mp[2-4]|zip|gz|bmp|gif|mpeg"
                                                                  + "|xls|xlsx|jpg|png|pdf|ico|tiff|mid|names"
                                                                  + "|ppt|pptx|bin|7z|rar|dmg|iso|mov|jar|lzip|tar|tgz)$");
    private static final Pattern CODE_EXTENSIONS = Pattern.compile(".*\\.(java|javac|py|h|cpp|cc|pyc|cs)$");

    /**
     * Parses all files to retrieve content from all files, and creates new files with content only.
     * We will also pull the subdomain name from first line of file
     *
     * @return
     * @throws java.io.IOException
     */
    public static LinkedList<File> processAllContentLinkFiles() throws IOException
    {
        File[] deletes = new File(DIRECTORY).listFiles();
        if (deletes != null)
        {
            for (File f : deletes)
            {

                f.delete();
            }

        }

        Pattern numberic = Pattern.compile("[0-9]*$");

        LinkedList<File> domains = new LinkedList<>();
        File directory = new File(FileSystem.RAW_DIRECTORY);
        File[] files = directory.listFiles();
        DocumentUrlMap documentMap = new DocumentUrlMap();
        documentMap.readInFile();
        if (files != null)
        {
            for (File f : files)
            {
                if (f.isDirectory())
                {

                }
                else if (numberic.matcher(f.getName()).matches() && !f.getName().contains("Crawler"))
                {
                    File file = parseContentFileForLink(documentMap, f);
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
    private static File parseContentFileForLink(DocumentUrlMap documentMap, File file)
    {
        File contentFile = new File(DIRECTORY + file.getName());

      
        try
        {
            contentFile.delete();
            contentFile.createNewFile();
            StringBuilder sb = new StringBuilder();

            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String curr;
            boolean found = false;

            //Scan for the Sub-Domains text line
            while ((curr = br.readLine()) != null)
            {
                if ("#Links#".equals(curr))
                {
                    found = true;
                    continue;
                }

                // search only until we find the links marker
                if (found)
                {
                    if (curr.equals(FileSystem.SPACER))
                    {
                        break;
                    }
                    else if (found && !curr.isEmpty() && !curr.contains("#Links#"))
                    {
                        // find only the URLS that match our specifics
                        String[] urls = curr.replace("[", "").replace("]", "").split(",");
                        for (String url : urls)
                        {
                            url = url.trim();
                            if (url.contains("?"))
                            {
                                continue;
                            }
                            if (BAD_EXTENSIONS.matcher(url).matches() || CODE_EXTENSIONS.matcher(url).matches())
                            {
                                continue;
                            }
                            if (url.isEmpty() || !url.contains("ics.uci.edu"))
                            {
                                continue;
                            }

                            // Ignore ID's we did not map
                            Integer docId = documentMap.urlHas(url);
                            if (docId == null)
                            {
                                //System.out.println(contentFile.getName() + " " + url);
                                continue;
                            }

                            sb.append(docId);
                            sb.append(' ');

                        }

                    }
                }

            }
            String strings = sb.toString().trim();

            if (!strings.isEmpty())
            {
                FileWriter wr = new FileWriter(contentFile, false);

                wr.write(strings);

                wr.close();
            }
            fr.close();
        }

        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }


        return contentFile;
    }

}
