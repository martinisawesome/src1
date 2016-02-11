package def;

public class Strings
{
    public static final String SPACER = "============================================================";
    public static final String SUBDOMAIN_TEXT = "Sub-Domain";
    public static final String EMPTY_FIELD = "{}";

    public static String getBasicHeader(String message)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("#").append(message).append("#");
        return sb.toString();
    }
}
