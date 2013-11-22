/*
 *  Mini Thymeleaf Web Server.
 *  © G J Barnard 2013 - Attribution-NonCommercial-ShareAlike 3.0 Unported - http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB.
 */
package minithymeleafwebserver;

/**
 * Factory class to deal with MIME Types.
 * @author G J Barnard
 */
public class MimeTypeFactory
{
    public MimeType HTML = new MimeType(false, MimeType.HTML_STR);
    public MimeType JSON = new MimeType(false, MimeType.JSON_STR);
    public MimeType TEXT = new MimeType(false, MimeType.TEXT_STR);

    /**
     * Try to guess the content type from the filename.
     *
     * @param name Name of the file to guess.
     * @return Guessed MIME type text.
     */
    public MimeType guessContentTypeFromName(String name)
    {
        if (name.endsWith(".html") || name.endsWith(".htm")) {
            return new MimeType(false, MimeType.HTML_STR);
        }
        else if (name.endsWith(".txt") || name.endsWith(".java")) {
            return new MimeType(false, "text/plain");
        }
        else if (name.endsWith(".gif")) {
            return new MimeType(true, "image/gif");
        }
        else if (name.endsWith(".class")) {
            return new MimeType(true, "application/octet-stream");
        }
        else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            return new MimeType(true, "image/jpeg");
        }
        else if (name.endsWith(".js")) {
            return new MimeType(false, "text/javascript; charset=UTF-8");
        }
        else if (name.endsWith(".css")) {
            return new MimeType(false, "text/css");
        }
        else if (name.endsWith(".png")) {
            return new MimeType(true, "image/png");
        }
        else if (name.endsWith(".otf")) {
            //return new MimeType(true, "vnd.oasis.opendocument.formula-template"); // http://www.iana.org/assignments/media-types/application/vnd.oasis.opendocument.formula-template
            return new MimeType(true, "font/opentype");  // Chrome no winge with this.
        }
        else if (name.endsWith(".ico")) {
            return new MimeType(true, "image/vnd.microsoft.icon");
        }
        else if (name.endsWith(".swf")) {
            return new MimeType(true, "application/x-shockwave-flash");
        }
        else {
            return new MimeType(false, MimeType.HTML_STR);
        }
    }

}
