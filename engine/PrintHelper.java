package engine;

import java.util.ArrayList;
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
        System.out.println(getNice(list));
    }

    public static <E> String getNice(List<E> list)
    {
        StringBuilder sb = new StringBuilder();

        for (E o : list)
        {
            sb.append(o.toString());

            sb.append("\n");

        }
        return sb.toString();
    }

    public static <E> String getNice(E[] list)
    {
        StringBuilder sb = new StringBuilder();

        for (E o : list)
        {
            sb.append(o.toString());

            sb.append("\n");

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
