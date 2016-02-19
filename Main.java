
import engine.Engine;
import java.io.IOException;
import maps.DocumentSize;
import tfidf.*;

/**
 *
 */
public class Main
{
    public static void main(String[] args) throws Exception
    {
        //demo();
    }

    /**
     * Does a demo of just 100 files
     *
     * @throws Exception
     */
    private static void demo() throws Exception
    {
        // TODO move all position and TFDF files before running this

        // Get all positions
        System.out.println("Finding positions of all tokens in documents");
        PositionProcessor p = new PositionProcessor(100);
        p.positionAllFiles();

        // Create TFDF file partitions
        TFDFProcessor tf = new TFDFProcessor(100);
        tf.writeTfDfFiles(false);

        System.out.println("Generating index per document");
    }

    public static void performEverything() throws IOException
    {

        PositionProcessor p = new PositionProcessor();
        p.positionAllFiles();

        // TFIDF postings
        TFDFProcessor tf = new TFDFProcessor();
        tf.writeTfDfFiles(true);
        tf.writeTfDfFiles(false);
        //  TF postings
        System.out.println("Number of Unique Tokens: " + PostingProcessor.createPostingsFiles());
        DocumentSize s = new DocumentSize();
        s.readInFile();
        System.out.println("Number of documents: " + s.getSize());
        s.clear();

    }
}
