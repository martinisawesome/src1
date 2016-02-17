package engine;

import java.util.List;

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
}
