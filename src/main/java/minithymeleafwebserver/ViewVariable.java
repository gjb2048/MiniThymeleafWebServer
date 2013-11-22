/*
 * Mini Thymeleaf Web Server.
 * © G J Barnard 2013 - Attribution-NonCommercial-ShareAlike 3.0 Unported - http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB.
 */
package minithymeleafwebserver;

/**
 * Contains one variable for a view with its associated update method if
 * required.
 *
 * @author G J Barnard
 */
public class ViewVariable
{
    private Object variable;
    private ViewVariableAction action = null;

    /**
     * Construct a variable that is a part of a template.
     *
     * @param variable The variable.
     * @param action The action to perform if needed on every request for the
     * template. Can be null for no action.
     */
    public ViewVariable(Object variable, ViewVariableAction action)
    {
        this.variable = variable;
        this.action = action;
    }

    /**
     * States if this variable is update-able.
     * @return Yes (true) or No (false).
     */
    public boolean updateable()
    {
        return (action != null);
    }

    /**
     * Gets the variable.
     * @return The variable.
     */
    public Object getVariable()
    {
        return variable;
    }

    /**
     * Updates the variable using the constructor supplied ViewVariableAction.
     * @return 
     */
    public Object update()
    {
        Object retr = null;
        if (updateable())
        {
            retr = action.performUpdate();
            variable = retr;
        }

        return retr;
    }
}
