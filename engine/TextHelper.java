package engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import storage.FileSystem;

/**
 * Helps to search in text for the positions of the word
 */
public class TextHelper
{
    private final HashMap<Integer, String> textMapper;
    private static final int TEXT_SNIPPET_LENGTH = 20;
    private static final int START_OFFSET = 5;
    private final LinkedList<Thread> threads;

    public TextHelper()
    {
        textMapper = new HashMap<>();
        threads = new LinkedList<>();
    }

    public boolean isAlive()
    {
        for (Thread t : threads)
        {
            if (t.isAlive())
            {
                return true;
            }
        }

        return false;
    }

    public void getSnippet(final int docID, final int position)
    {
        Thread t1 = new Thread()
        {

            @Override
            public void run()
            {
                File f = new File(FileSystem.TOKEN_DIRECTORY + FileSystem.TEXT + docID + FileSystem.TOKEN);
                try
                {

                    String curr;
                    FileReader fr = new FileReader(f);
                    BufferedReader br = new BufferedReader(fr);
                    int start = Math.max(position - START_OFFSET, 0);
                
                    StringBuilder sb = new StringBuilder();
                        if (start > 0)
                    {
                        sb.append("...");
                    }
                    
                    int i = 0;
                    while (  (curr = br.readLine()) != null)
                    {
                        String[] splits = curr.split(" ");
                        
                        // not in range yet
                        if (i + splits.length < start)
                        {
                            i += splits.length;
                            continue;
                        }
                        
                        
                        // get in range
                        for (int j = 0; (j < splits.length) && (i < start + TEXT_SNIPPET_LENGTH); j ++)
                        {
                            
                            if ( i >= start)
                            {
                                sb.append(splits[j]).append(" ");
                            }
                            
                            i++;
                        }
                        
                        // end
                        if (i >= start + TEXT_SNIPPET_LENGTH)
                        {
                            sb.append("...");
                            break;
                        }
                        
                    }

                    fr.close();

                    textMapper.put(docID, sb.toString());
                }
                catch (IOException ex)
                {
                    System.err.println("Failed to read file: " + f.getName());
                    ex.printStackTrace();
                }
            }
        };
        t1.start();

        threads.add(t1);
    }

    public String get(int docID)
    {
        return textMapper.get(docID);
    }

}
