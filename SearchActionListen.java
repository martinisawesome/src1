import engine.Engine;
import javax.swing.*;
import java.lang.Object;
import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by diann on 2/27/2016.
 */
public class SearchActionListen {
    public static String getPrint(String words)
    {
        StringBuilder sb = new StringBuilder();
      //  sb.append(words);
        List<String> urls = Engine.search(words);
        String results ;


        for (String url : urls)
        {
         //   sb.append("\n  ");
            sb.append(url);                         // TODO this is the URL!!
            sb.append("\n  ");
            sb.append(Engine.getTextSnippet(url));  // TODO this is the test snippet!!
            sb.append(",");
//            results.add(url);
//            results.add("\n");
//            results.add(Engine.getTextSnippet(url));
//            results.add("\n");

        }
        results = sb.toString();
        System.out.println(results);
        return results;
    }
}
