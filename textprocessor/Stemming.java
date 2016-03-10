package textprocessor;

public class Stemming
{
    @Deprecated
    public static String stem(String string)
    {
        if (string.endsWith("sses"))
        {
            return string.substring(0, string.length() - 4) + "ss";
        }
        else if (string.endsWith("ies"))
        {
            return string.substring(0, string.length() - 3) + "y";     //y or i?
        }
        else if (string.endsWith("ss"))
        {
            return string;
        }
        else if (string.endsWith("s"))
        {
            return string.substring(0, string.length() - 1);
        }
        else
        {
            return string;
        }
    }

}
