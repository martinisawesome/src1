package maps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import storage.FileSystem;

/**
 * TODO me
 *
 * @author mao_ma
 */
public class AnchorTextMap
{
    private final HashMap<Integer, String[]> map;
    
    public AnchorTextMap()
    {
        map = new HashMap<>();
        // readFromFile() TODO
    }
    
    public void readFromFile() throws IOException
    {
        File targetFile = getThisFile();
        
        FileReader fr = new FileReader(targetFile);
        BufferedReader br = new BufferedReader(fr);
        String curr;
        while ((curr = br.readLine()) != null)
        {
            //TODO remove stop words and other chars?
            String[] parts = curr.split(":");
            
            int id = Integer.parseInt(parts[0]);
            map.put(id, parts[1].split(" "));
            
        }
        
        fr.close();
    }
    
    public static void ripFromRaw() throws IOException
    {
        HashMap<Integer, String> map = new HashMap<>();
        File targetFile = getThisFile();
        targetFile.delete();
        targetFile.createNewFile();
        File directory = new File(FileSystem.CRAWLING_HEADER_DIRECTORY);
        File[] files = directory.listFiles();
        FileWriter fw = new FileWriter(targetFile);
        String curr;
        int id;
        if (files != null)
        {
            for (File f : files)
            {
                if (f.isDirectory())
                {
                    
                }
                else if (f.getName().contains("Header"))
                {
                    
                    FileReader fr = new FileReader(f);
                    BufferedReader br = new BufferedReader(fr);
                    boolean finished = false;
                    
                    while (!finished)
                    {
                        do
                        {
                            curr = br.readLine();
                            if (curr == null)
                            {
                                finished = true;
                                break;
                            }
                        } while (!curr.contains("DocId"));

                        // end searching this file
                        if (finished)
                        {
                            break;
                        }
                        
                        String[] split = curr.split(" ");
                        id = Integer.parseInt(split[1]);
                        
                        while (!(curr = br.readLine()).contains("Anchor Text"))
                        {
                            
                        }
                        
                        int index = curr.indexOf("Anchor Text");
                        curr = curr.substring(index);
                        curr = curr.replace("Anchor Text: ", "");
                        curr = curr.replaceAll("[^a-zA-Z0-9 ]", "");
                        curr = curr.trim();
                        curr = curr.replaceAll("  ", " ");
                        
                        if (!"null".equals(curr) && !curr.isEmpty())
                        {
                            map.put(id, curr);
                        }
                        
                    }

                    // end of reading this file!
                    fr.close();
                    
                }
            }
        }   //end of looking all files

        LinkedList<Integer> list = new LinkedList<>();
        list.addAll(map.keySet());
        Collections.sort(list);
        StringBuilder sb = new StringBuilder();
        for (Integer i : list)
        {
            curr = map.get(i);
            sb.append(i).append(":").append(curr);
            sb.append("\n");
            
        }
        fw.write(sb.toString());
        fw.close();
    }
    
    private static File getThisFile()
    {
        File targetFile = new File(FileSystem.CRAWLER_DIRECTORY + FileSystem.ANCHOR_NAME);
        return targetFile;
    }
}
