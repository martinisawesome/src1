package engine;

import java.io.IOException;

import edu.mit.jwi.Dictionary;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author mao_ma
 */
public class DictionaryLookup
{

    public static Dictionary getDictionary() throws MalformedURLException, IOException
    {
        String path = "C:\\Program Files (x86)\\WordNet\\2.1\\dict";
        URL url = new URL("file", null, path);
        Dictionary dictionary = new Dictionary(url);
        return dictionary;
    }
}
