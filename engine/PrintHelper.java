package engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @param <E>
 */
public class PrintHelper<E>
{
    public static <E> void printNice(List<E> list)
    {
        System.out.println(getNice( list));
    }
    public static <E> String getNice(List<E> list)
    {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (E o : list)
        {
            sb.append(o.toString());
            count++;
            if (count >= 20)
            {
                count = 0;
                sb.append("\n");
            }
            else
            {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public static <E> String getNice(E[] list)
    {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (E o : list)
        {
            sb.append(o.toString());
            count++;
            if (count >= 20)
            {
                count = 0;
                sb.append("\n");
            }
            else
            {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
    
    public static <E> String getNice(HashMap<String, LinkedList<E>> map)
    {
         StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, LinkedList<E>> entry : map.entrySet())
        {
             LinkedList<E> value = entry.getValue();
             String key = entry.getKey();
             sb.append(key);
             sb.append(": ");
             sb.append(value);
             sb.append("\n");
        }
        
        
        return sb.toString();
    }
}
