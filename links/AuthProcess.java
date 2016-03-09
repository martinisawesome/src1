package links;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import storage.FileSystem;

/**
 *
 * @author mao_ma
 */
public class AuthProcess
{
    public static final String DIRECTORY = FileSystem.CRAWLER_DIRECTORY + "Auth\\";
    public static final String SIZE_DOC = DIRECTORY + "Complete";

    public static void getAllContentLinkFiles() throws IOException
    {
        HashMap<Integer, LinkedList<Integer>> map = new HashMap<>();
        File[] deletes = new File(DIRECTORY).listFiles();
        if (deletes != null)
        {
            for (File f : deletes)
            {

                f.delete();
            }

        }

        Pattern numberic = Pattern.compile("[0-9]*$");

        File directory = new File(LinksProcess.DIRECTORY);
        File[] files = directory.listFiles();

        // Iterate all files and find the auth id
        if (files != null)
        {
            for (File f : files)
            {
                if (f.isDirectory())
                {

                }
                else if (numberic.matcher(f.getName()).matches())
                {
                    parseContentFileForLink(map, f);

                }
            }
        }

        File complete = new File(SIZE_DOC);
        FileWriter fwer = new FileWriter(complete);
        // write all to file
        int j = 0;
        for (Entry<Integer, LinkedList<Integer>> entry : map.entrySet())
        {
            Integer auth = entry.getKey();
            LinkedList<Integer> hubs = entry.getValue();
            Collections.sort(hubs);

            File f = new File(DIRECTORY + auth);
            FileWriter fw = new FileWriter(f);
            fwer.write(auth+"");
            fwer.write(":");
            for (Integer i : hubs)
            {
                fw.write(i + ",");
            }
            fwer.write(hubs.size() + "");
            if (j < 20)
            {
                fwer.write(";");
            }
            else
            {
                j =0;
                fwer.write("\n");
            }
            j++;
            fw.close();
            
        }
fwer.close();
    }

    private static void parseContentFileForLink(HashMap<Integer, LinkedList<Integer>> map, File file)
    {
        int hubId = Integer.parseInt(file.getName());

        try
        {

            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String curr;

            //Scan for the Sub-Domains text line
            while ((curr = br.readLine()) != null)
            {
                String[] ids = curr.split(" ");
                for (String s : ids)
                {
                    Integer i = Integer.parseInt(s.trim());
                    LinkedList<Integer> list = map.get(i);
                    if (list == null)
                    {
                        list = new LinkedList<>();
                    }

                    list.add(hubId);
                    map.put(i, list);
                }

            }
        }

        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }

    }
}
