
package tfidf;

/**
 *
 */
public class TFIDFPair  implements Comparable<TFIDFPair>
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

        return this.docID - p2.docID;

    }
}
