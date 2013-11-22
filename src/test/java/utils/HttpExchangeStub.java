/*
 * Mini Thymeleaf Web Server.
 * © G J Barnard 2013 - Attribution-NonCommercial-ShareAlike 3.0 Unported - http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB.
 */
package utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpPrincipal;
import java.util.LinkedList;

/**
 * Test stub for a HttpExchange
 *
 * @author G J Barnard
 */
public class HttpExchangeStub extends com.sun.net.httpserver.HttpExchange
{

    private Headers responseHeaders = null;
    private Headers requestHeaders = null;
    private ByteArrayOutputStream responseBody = null;
    private URI requestURI = null;

    public HttpExchangeStub(String requestURI) throws URISyntaxException
    {
        this.requestURI = new URI(requestURI);
    }

    @Override
    public void close()
    {
        System.out.println("HttpExchangeStub - close()");
        if (responseBody != null)
        {
            try
            {
                responseBody.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Object getAttribute(String arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HttpContext getHttpContext()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InetSocketAddress getLocalAddress()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HttpPrincipal getPrincipal()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getProtocol()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InetSocketAddress getRemoteAddress()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream getRequestBody()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Headers getRequestHeaders()
    {
        if (requestHeaders == null)
        {
            requestHeaders = new Headers();
            LinkedList<String> hostvars = new LinkedList<>();
            hostvars.add("http://localhost");
            requestHeaders.put("Host", hostvars);
        }
        return requestHeaders;
    }

    @Override
    public String getRequestMethod()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public URI getRequestURI()
    {
        return requestURI;
    }

    @Override
    public OutputStream getResponseBody()
    {
        if (responseBody == null)
        {
            responseBody = new ByteArrayOutputStream();
        }
        return responseBody;
    }

    @Override
    public int getResponseCode()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Headers getResponseHeaders()
    {
        if (responseHeaders == null)
        {
            responseHeaders = new Headers();
        }
        return responseHeaders;
    }

    @Override
    public void sendResponseHeaders(int arg0, long arg1) throws IOException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setAttribute(String arg0, Object arg1)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setStreams(InputStream arg0, OutputStream arg1)
    {
        // TODO Auto-generated method stub

    }

    public String responseHeadersToString()
    {
        String retr = "No headers";

        if (responseHeaders != null)
        {
            Set<Map.Entry<String, List<String>>> entrySet = responseHeaders.entrySet();

            Iterator<Map.Entry<String, List<String>>> its = entrySet.iterator();
            Map.Entry<String, List<String>> currentEntry;

            if (its.hasNext())
            {
                retr = "";
            }

            while (its.hasNext())
            {
                currentEntry = its.next();
                String currentKey = currentEntry.getKey();
                Iterator<String> itl = currentEntry.getValue().iterator();
                while (itl.hasNext())
                {
                    retr += "Key: " + currentKey + " Value: " + itl.next() + "\n";
                }
            }
        }

        return retr;
    }
}
