package textprocessor;

public class FreqIndex<E> implements Comparable<FreqIndex>
{
    public final E token;
    private int count;
    public final int docId;

    public FreqIndex(E token, int docId)
    {
        this(token, docId, 1);
    }

    public FreqIndex(E token, int docId, int count)
    {
        this.token = token;
        this.count = count;
        this.docId = docId;
    }

    public int incCount()
    {
        count++;
        return count;
    }

    public int getCount()
    {
        return count;
    }

    @Override
    public String toString()
    {
        return String.format("%s: %d %d", token, docId, count);
    }

    @Override
    public int compareTo(FreqIndex p2)
    {
        int diff = this.token.toString().compareTo(p2.token.toString());
        if (diff == 0)
        {
            return this.docId - p2.docId;
        }
        else
        {
            return diff;
        }
    }

}
