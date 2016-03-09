

package links;

/**
 *
 * @author mao_ma
 */
public class AuthPair implements Comparable<AuthPair>
{
    public final int docID;
    public final int pos;

    public AuthPair(int docID, int pos)
    {
        this.docID = docID;
        this.pos = pos;
    }

    @Override
    public String toString()
    {
        return String.format("Doc: %d Auth: %d", docID, pos);
    }

    @Override
    public int compareTo(AuthPair p2)
    {

        return this.pos - p2.pos;

    }
}
