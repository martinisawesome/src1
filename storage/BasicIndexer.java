package storage;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * TODO we do not need this now, but this will reference each String token to a list of DocID
 */
public class BasicIndexer
{
    /**
     * TODO! DO NOT WORK ON OR USE THIS YET! THIS WILL CHANGE!
     */
    public HashMap<String, LinkedList<Index>> matrix;

    public BasicIndexer()
    {
        matrix = new HashMap<>();
    }

    public BasicIndexer(String file)
    {
        readFromFile(file);
    }

    public final void readFromFile(String file)
    {

    }

    public void writeToFile()
    {
        sort();

        //TODO some writing
    }

    public LinkedList<Index> addToken(String token)
    {
        LinkedList<Index> list = new LinkedList<>();
        matrix.put(token, list);
        return list;
    }

    public void addDocumentToWord(String token, int docId, int frequency)
    {
        LinkedList<Index> list = matrix.get(token);
        if (list == null)
        {
            list = addToken(token);
        }

        list.add(new Index(docId, frequency));
    }

    /**
     * Sorts the map alphabetically
     */
    public void sort()
    {
        Set<String> tokens = matrix.keySet();
        List<String> list = new LinkedList<>(tokens);
        Collections.sort(list);
        HashMap<String, LinkedList<Index>> matrix2 = new LinkedHashMap<>();
        for (String word : list)
        {
            matrix2.put(word, matrix.get(word));
        }

        matrix = matrix2;
    }

}
