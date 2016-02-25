package engine;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Martin
 */
public class QueryHelper
{
    /**
     * DISCLAIMER!!! This code was taken from Stack Overflow!
     * Gets different permutations of a string to help with grams
     *
     * @param query
     * @return
     */
    public List<ArrayList<String>> getPermutations(List<String> query)
    {
        if (query.isEmpty())
        {
            List<ArrayList<String>> result = new ArrayList<>();
            result.add(new ArrayList<String>());
            return result;
        }
        
        String first = query.remove(0);
        List<ArrayList<String>> returnValue = new ArrayList<>();
        List<ArrayList<String>> permutations = getPermutations(query);
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
}
