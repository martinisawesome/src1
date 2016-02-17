
import engine.PrintHelper;
import java.io.File;
import java.io.IOException;
import maps.*;
import storage.*;
import tfidf.*;

/**
 *
 */
public class Main
{
    public static void main(String[] args) throws Exception
    {

    }

    public static void performEverything() throws IOException
    {
        DocumentPositionProcessor p = new DocumentPositionProcessor();
        p.positionAllFiles();

        File f = FileSystem.binaryMergeByAlphabetic(DocumentPositionProcessor.DIRECTORY, DocumentPositionProcessor.FILE_HEADER, 0);
        FilePartioning.partitionOutFile(DocumentPositionProcessor.FILE_HEADER, DocumentPositionProcessor.DIRECTORY, f.getName());
        TFDFProcessor tf = new TFDFProcessor();
        tf.writeTfDfFiles();

        System.out.println("Number of Unique Tokens: " + PostingProcessor.createPostingsFiles());

        DocumentSize s = new DocumentSize();
        s.readInFile();
        System.out.println("Number of documents: " + s.getSize());
        s.clear();
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
        DocumentPositionProcessor p = new DocumentPositionProcessor(100);
        p.positionAllFiles();

        // binary merge all files and then create partitions for position files
        System.out.println("Partitioning tokens into ordered files");
        File f = FileSystem.binaryMergeByAlphabetic(DocumentPositionProcessor.DIRECTORY, DocumentPositionProcessor.FILE_HEADER, 0);
        FilePartioning.partitionOutFile(DocumentPositionProcessor.FILE_HEADER, DocumentPositionProcessor.DIRECTORY, f.getName());

        // Create TFDF file partitions
        TFDFProcessor tf = new TFDFProcessor(100);
        tf.writeTfDfFiles();

        System.out.println("Generating index per document");
        PrintHelper.printNice(PositionParser.getWordPosition("ow2"));
        PrintHelper.printNice(TFDFParser.getWordWeight("ow2"));
    }
}
