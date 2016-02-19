package tfidf;

/**
 *
 */
public class TFIDFPair implements Comparable<TFIDFPair>
{
    public final int docID;
    public final double weight;

    public TFIDFPair(int docID, double pos)
    {
        this.docID = docID;
        this.weight = pos;
    }

    @Override
    public String toString()
    {
        return String.format("Doc: %d Weight: %f", docID, weight);
    }

    @Override
    public int compareTo(TFIDFPair p2)
    {
        double value = this.weight - p2.weight;
        if (value > 0)
        {
            return -1;
        }
        if (value == 0)
        {
            return 0;
        }
        if (value < 0)
        {
            return 1;
        }
        else
        {
            throw new IllegalArgumentException("Bad weight: " + value);
        }
    }
}
