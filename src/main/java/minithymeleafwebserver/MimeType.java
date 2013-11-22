/*
 * Mini Thymeleaf Web Server.
 * © G J Barnard 2013 - Attribution-NonCommercial-ShareAlike 3.0 Unported - http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB.
 */
package minithymeleafwebserver;

/**
 * Class to represent a MIME Type.
 * @author G J Barnard
 */
public class MimeType
{
    private final boolean isBinary;
    private final String type;
    public static final String HTML_STR = "text/html";
    public static final String JSON_STR = "application/json";
    public static final String TEXT_STR = "text/plain";

    /**
     * Constructor
     *
     * @param isBinary States if we are a binary type.
     * @param type The type we are.
     */
    public MimeType(boolean isBinary, String type)
    {
        this.isBinary = isBinary;
        this.type = type;
    }

    /**
     * States if we are a binary type.
     *
     * @return true or false.
     */
    public boolean isBinary()
    {
        return isBinary;
    }

    /**
     * States the MIME type.
     *
     * @return The type.
     */
    public String getType()
    {
        return type;
    }
}
