
import engine.Engine;
import engine.PrintHelper;
import java.io.IOException;
import java.util.List;
import maps.DocumentSize;
import tfidf.*;

/**
 *
 */
public class Main
{
    public static void main(String[] args) throws Exception
    {
        long startTime = System.nanoTime();

        getPrint(" mondego");
        getPrint("machine learning");
        getPrint(" software engineering");
        getPrint(" security");
        getPrint(" student affairs");
        getPrint(" graduate courses");
        getPrint(" Crista Lopes");
        getPrint(" REST");
        getPrint(" computer games");
        getPrint(" information retrieval");

        long completionTime = System.nanoTime() - startTime;
        double time = completionTime / 1000;
        time = time / 1000;
        System.out.println(
                String.format("Time to completion: %.03fms", time));
    }

    private static void getPrint(String words)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(words);
        List<String> urls = Engine.search(words);
        
        for (String url : urls)
        {
            sb.append("\n  ");
            sb.append(url);                         // TODO this is the URL!!
            sb.append("\n  ");
            sb.append(Engine.getTextSnippet(url));  // TODO this is the test snippet!!
        }
        System.out.println(sb.toString());
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
