/*
 * Mini Thymeleaf Web Server.
 * © G J Barnard 2013 - Attribution-NonCommercial-ShareAlike 3.0 Unported - http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB.
 */
package minithymeleafwebserver;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import org.thymeleaf.context.Context;

/**
 * Contains the variables associated with one view.
 * @author G J Barnard
 */
public class ViewData
{
    private final String name;
    private final Context context;
    private final HashMap<String, ViewVariable> updateVariables = new HashMap<>();

    /**
     * Construct a template view data instance with the given view name.
     * @param name The name of the view.
     */
    public ViewData(String name)
    {
        this.name = name;
        context = new Context();
    }

    /**
     * Get the name of the view.
     * @return The name of the view.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the given view variable name and its content.
     * @param name The name of the view variable.
     * @param variable The content of the variable.
     */
    public void setVariable(String name, ViewVariable variable)
    {
        context.setVariable(name, variable.getVariable());
        if (variable.updateable())
        {
            updateVariables.put(name, variable);
        }
    }
    
    /**
     * Sets the 'Host' view variable to the content of the given parameter.
     * @param host The value to store in the 'host' view variable.
     */
    public void setHost(String host)
    {
        context.setVariable("host", host);
    }
    
    /**
     * Update the view variables for the view.  To be called for every request.
     */
    public void update()
    {
        Iterator<Entry<String,ViewVariable>> it = updateVariables.entrySet().iterator();
        Entry<String,ViewVariable> current;
        while (it.hasNext())
        {
            current = it.next();
            Object result = current.getValue().update();
            context.setVariable(current.getKey(), result);
        }
    }

    /**
     * Gets the context used by the template engine.
     * @return The context.
     */
    public Context getContext()
    {
        return context;
    }
}
