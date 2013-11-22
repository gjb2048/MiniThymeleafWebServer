/*
 * Mini Thymeleaf Web Server.
 * © G J Barnard 2013 - Attribution-NonCommercial-ShareAlike 3.0 Unported - http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB.
 */
package minithymeleafwebserver;

/**
 * Action that is performed if a view variable needs to be updated upon view
 * render. This is an 'interface' that facilitates the methods to be called
 * generically by the code and yet still have specific implementation for each
 * template view variable.
 *
 * @author G J Barnard
 */
public interface ViewVariableAction
{
    public Object performUpdate();
}
