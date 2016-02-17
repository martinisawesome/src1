package maps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import storage.FileSystem;

public class TitleMap
{
    private final HashMap<Integer, String> titleMap;

    public TitleMap()
    {
        titleMap = new HashMap<>();
    }

    public void readFromFile() throws IOException
    {
        File targetFile = getThisFile();
        FileReader fr = new FileReader(targetFile);
        BufferedReader br = new BufferedReader(fr);
        String curr;
        while ((curr = br.readLine()) != null)
        {
            String[] parts = curr.split(":");
            if (parts[1].isEmpty() || "null".equals(parts[1]))
            {
                //do nothing
            }
            else    //TODO remove stop words and other chars?
            {
                int id = Integer.parseInt(parts[0]);
                titleMap.put(id, parts[1]);
            }
        }

        fr.close();
    }

    public static void ripTitlesFromRaw() throws IOException
    {
        File targetFile = getThisFile();
        File directory = new File(FileSystem.RAW_DIRECTORY);
        File[] files = directory.listFiles();
        FileWriter fw = new FileWriter(targetFile);
        String curr;
        if (files != null)
        {
            for (File f : files)
            {
                if (f.isDirectory())
                {

                }
                else
                {
                    StringBuilder sb = new StringBuilder();
                    Integer docID = Integer.parseInt(f.getName());
                    sb.append(docID);
                    sb.append(":");

                    FileReader fr = new FileReader(f);
                    BufferedReader br = new BufferedReader(fr);
                    br.readLine();  //skip URL
                    curr = br.readLine();
                    if ("#Title#".equals(curr))
                    {
                        while (!"#Text#".equals(curr = br.readLine()))
                        {
                            sb.append(curr).append(" ");
                        }
                    }
                    //Ignore text files
                    else if ("#Text#".equals(curr))
                    {
                        fr.close();
                        continue;
                    }
                    else
                    {
                        throw new IOException("This file does not have title in place: " + f.getName());
                    }

                    sb.append("\n");
                    fw.write(sb.toString());

                    fr.close();
                }
            }
        }

        fw.close();
    }

    private static File getThisFile()
    {
        File targetFile = new File(FileSystem.CRAWLER_DIRECTORY + FileSystem.DOCUMENT_TITLE_NAME);
        return targetFile;
    }

}
