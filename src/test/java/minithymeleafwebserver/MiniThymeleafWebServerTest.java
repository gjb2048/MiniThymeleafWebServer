/*
 * Mini Thymeleaf Web Server.
 * © G J Barnard 2013 - Attribution-NonCommercial-ShareAlike 3.0 Unported - http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB.
 */
package minithymeleafwebserver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Date;

import utils.HttpExchangeStub;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for MiniThymeleafWebServer.
 * @author G J Barnard
 */
public class MiniThymeleafWebServerTest
        extends TestCase
{
    private MiniThymeleafWebServer us = null;

    @Override
    public void setUp()
    {
        us = MiniThymeleafWebServer.getInstance(null);
    }

    @Override
    public void tearDown()
    {
        us = null;
    }

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public MiniThymeleafWebServerTest(String testName)
    {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static TestSuite suite()
    {
        TestSuite suite = new TestSuite(MiniThymeleafWebServerTest.class);

    	//suite.addTest(new MiniThymeleafWebServerTest("testMiniThymeleafWebServer"));
        return suite;
    }

    /**
     * Test the URI '/'.
     */
    public void testIndexRequest()
    {
        try
        {
            HttpExchangeStub exchange = new HttpExchangeStub("/");
            try
            {
                us.handle(exchange);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                assertTrue(false);
            }
            
            boolean result = true;
            
            OutputStream response = exchange.getResponseBody();
            String responseString = response.toString();
            
            if (responseString.equalsIgnoreCase("The date now"))
            {
                // Failed to transform template.
                result = false;
            }
            
            System.out.println(responseString);
            String responseHeadersString = exchange.responseHeadersToString();
            System.out.println(responseHeadersString);
            assertTrue(responseString + " - " + responseHeadersString, result);
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testMimeTypeFactory()
    {
        MimeTypeFactory mtf = new MimeTypeFactory();
        
        MimeType mt = mtf.guessContentTypeFromName(".html");
        
        assertTrue(mt.getType().equalsIgnoreCase("text/html"));
    }
    
    public void testViewData()
    {
        ViewData viewData = new ViewData("test");
        
        ViewVariableAction vva = mock(ViewVariableAction.class);
        
        String vvat = "performUpdate";
        when(vva.performUpdate()).thenReturn(vvat);
        
        ViewVariable vv = new ViewVariable(new Date(), vva);
        
        viewData.setVariable("thedate", vv);
        
        viewData.update();
        
        boolean result = true;
        
        verify(vva).performUpdate();
        
        if (((String)vv.getVariable()).equalsIgnoreCase(vvat) == false)
        {
            result = false;
        }                
        
        assertTrue(result);
    }
    
    public void testURLPathMapper()
    {
        ViewData viewData = mock(ViewData.class);
        
        URIPathMapper upm = new URIPathMapper();
        
        String uri = "/test";
        
        upm.addMapping(uri, viewData);
        
        assertTrue(upm.hasMapping(uri));
        
        assertTrue(upm.getMapping(uri).equals(viewData));
    }
}
