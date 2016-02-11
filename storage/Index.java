package storage;

public class Index
{
    public final int docId;
    public final int wordFrequency;

    public Index(int docId, int wordFrequency)
    {
        this.docId = docId;
        this.wordFrequency = wordFrequency;
    }
}
