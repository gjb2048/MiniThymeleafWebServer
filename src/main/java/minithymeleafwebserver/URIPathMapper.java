/*
 * Mini Thymeleaf Web Server.
 * © G J Barnard 2013 - Attribution-NonCommercial-ShareAlike 3.0 Unported - http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB.
 */

package minithymeleafwebserver;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to encapsulate the mapping of a URI to its variables.
 * @author G J Barnard
 */
public class URIPathMapper
{
    private final HashMap<String, ViewData> mappings = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(URIPathMapper.class);
    
    /**
     * Constructor
     */
    public URIPathMapper()
    {        
    }
    
    /**
     * Associates a URI with its view data.
     * @param uri The URI.
     * @param template The view data for the template.
     */
    public void addMapping(String uri, ViewData template)
    {
        mappings.put(uri, template);
        log.info("Now serving: " + uri);
    }
    
    /**
     * Gets the template view data for the given URI mapping.
     * @param uri
     * @return The template view data.
     */
    public ViewData getMapping(String uri)
    {
        return mappings.get(uri);
    }
    
    /**
     * Get all of the mapping URI's.
     * @return 
     */
    public Set<String> getMappings()
    {
        return new TreeSet(mappings.keySet());
    }
    
    /**
     * States if we hold a mapping for the given URI.
     * @param uri The URI to check.
     * @return Yes (true) or No (false).
     */
    public boolean hasMapping(String uri)
    {
        return mappings.containsKey(uri);
    }
}
