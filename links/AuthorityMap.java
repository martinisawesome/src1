package links;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author mao_ma
 */
public class AuthorityMap
{
    private final HashMap<Integer, Integer> authorityMap;
    private final Thread t;

    public AuthorityMap()
    {
        this.authorityMap = new HashMap<>();
        t = getThread();
        t.start();
    }

    public boolean isAlive()
    {
        return t.isAlive();
    }

    public HashMap<Integer, Integer> getMap()
    {
        return authorityMap;
    }

    public int get(int docId)
    {
        Integer auth = authorityMap.get(docId);
        if (auth == null)
        {
            return 0;
        }
        return auth;
    }

    private Thread getThread()
    {
        Thread t1 = new Thread()
        {

            @Override
            public void run()
            {
                try
                {
                    int docID;
                    int count;
                    File f = new File(AuthProcess.SIZE_DOC);
                    String curr;
                    FileReader fr = new FileReader(f);
                    BufferedReader br = new BufferedReader(fr);
                    while ((curr = br.readLine()) != null)
                    {
                        String[] nodes = curr.split(";");
                        for (String s : nodes)
                        {
                            String[] pair = s.split(":");
                            docID = Integer.parseInt(pair[0]);
                            count = Integer.parseInt(pair[1]);
                            authorityMap.put(docID, count);
                        }

                    }
                    fr.close();
                }
                catch (IOException ex)
                {
                    System.err.println(ex.getMessage());
                    throw new RuntimeException(ex);
                }
            }
        };
        return t1;
    }

}
