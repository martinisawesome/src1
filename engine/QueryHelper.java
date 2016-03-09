package engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Martin
 */
public class QueryHelper
{

    public static List<List<String>> getPermutations(List<String> query)
    {
        List<List<String>> first = getPermutations(query, query.size());
        
        // reverse order so first is back to first in list
        Collections.reverse(first);
        
        // get 2 grams
        first.addAll(getSmallPermutations(first));
        return first;
    }

    /**
     * DISCLAIMER!!! This code was taken from Stack Overflow!
     * Gets different permutations of a string to help
     *
     * @param query
     * @param size
     * @return
     */
    public static List<List<String>> getPermutations(List<String> query, int size)
    {
        if (query.isEmpty())
        {
            List<List<String>> result = new ArrayList<>();
            result.add(new ArrayList<String>());
            return result;
        }
        // don't permute for query size > 3
        else if (query.size() > 3)
        {
            List<List<String>> result = new ArrayList<>();
            result.add(query);
            return result;
        }

        String first = query.remove(0);
        List<List<String>> returnValue = new ArrayList<>();
        List<List<String>> permutations = getPermutations(query, size);
        for (List<String> permute : permutations)
        {
            for (int index = 0; index <= permute.size(); index++)
            {
                ArrayList<String> temp = new ArrayList<>(permute);
                temp.add(index, first);
                returnValue.add(temp);
            }
        }
        return returnValue;
    }

    public static List<List<String>> getSmallPermutations(List<List<String>> all)
    {
        List<List<String>> newList = new LinkedList<>();
        for (List<String> lists : all)
        {
            if (lists.size() <= 2)
            {
                continue;
            }
            List<String> newIndex = lists.subList(0, 2);
            if (!newList.contains(newIndex))
            {
                newList.add(newIndex);
            }
        }

        return newList;
    }

}
