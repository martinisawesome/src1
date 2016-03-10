
import engine.Engine;
import java.io.File;
import java.io.IOException;
import java.util.List;
import links.AuthProcess;
import links.LinksProcess;
import storage.FilePartioning;
import storage.FileSystem;
import tfidf.*;

/**
 *
 */
public class Main
{
    public static void main(String[] args) throws Exception
    {
        long startTime = System.nanoTime();
        getPrint("");
        getPrint("  ");
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
            sb.append(url);
            sb.append("\n    ");
            sb.append(Engine.getTextSnippet(url));
        }
        System.out.println(sb.toString());
    }

    public static void getGramFiles() throws IOException
    {
        //FileSystem.processTokenPages();
        FileSystem.clearContentData();
        FileSystem.computeFrequencies(1);
        FileSystem.computeFrequencies(2);
        FileSystem.computeFrequencies(3);
        FileSystem.computeFrequencies(4);
        File f = FileSystem.binaryMergeByAlphabetic(FileSystem.CONTENT_PARTITION_DIRECTORY, FileSystem.FREQ_FILE, 0);
        FilePartioning.partitionOutFile(FileSystem.FREQ_FILE, FileSystem.CONTENT_PARTITION_DIRECTORY, f.getName());

        f = FileSystem.binaryMergeByAlphabetic(FileSystem.CONTENT_PARTITION_DIRECTORY, FileSystem.TWO_GRAM, 0);

        FilePartioning.partitionOutFile(FileSystem.TWO_GRAM, FileSystem.CONTENT_PARTITION_DIRECTORY, f.getName());

        f = FileSystem.binaryMergeByAlphabetic(FileSystem.CONTENT_PARTITION_DIRECTORY, FileSystem.THREE_GRAM, 0);
        FilePartioning.partitionOutFile(FileSystem.THREE_GRAM, FileSystem.CONTENT_PARTITION_DIRECTORY, f.getName());
        f = FileSystem.binaryMergeByAlphabetic(FileSystem.CONTENT_PARTITION_DIRECTORY, FileSystem.FOUR_GRAM, 0);
        FilePartioning.partitionOutFile(FileSystem.FOUR_GRAM, FileSystem.CONTENT_PARTITION_DIRECTORY, f.getName());

        TFDFProcessor tf = new TFDFProcessor();
        tf.writeTfDfFiles(true);
        tf.writeTfDfFiles(false);
        tf.writeGramTfDfFiles(true);
        tf.writeGramTfDfFiles(false);
        PositionProcessor p = new PositionProcessor();
        p.positionAllFiles();
    }
}
