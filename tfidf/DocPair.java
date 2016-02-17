package tfidf;

/**
 *
 */
public class DocPair implements Comparable<DocPair>
{
    public final int docID;
    public final int pos;

    public DocPair(int docID, int pos)
    {
        this.docID = docID;
        this.pos = pos;
    }

    @Override
    public String toString()
    {
        return String.format("Doc: %d Pos: %d", docID, pos);
    }

    @Override
    public int compareTo(DocPair p2)
    {

        return this.docID - p2.docID;

    }
}
