/*
 * Mini Thymeleaf Web Server.
 * © G J Barnard 2013 - Attribution-NonCommercial-ShareAlike 3.0 Unported - http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB.
 */
package minithymeleafwebserver;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
//import org.thymeleaf.templateresolver.FileTemplateResolver;

/**
 * @author G J Barnard
 */
public class MiniThymeleafWebServer implements HttpHandler
{
    private static MiniThymeleafWebServer us = null;
    private final MimeTypeFactory mimeTypeFactory = new MimeTypeFactory();
    private static final int BUFFER_MAX = 65536 - 40;
    private int httpPort = 8084;
    private String userdir = null;
    private static final String MTWS_TITLE = "MiniThymeWebServer";
    private final TemplateEngine engine = new TemplateEngine();
    //private final FileTemplateResolver resolver = new FileTemplateResolver();  // To experiment with the templates not in the JAR file.
    private final ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
    private final URIPathMapper mapper = new URIPathMapper();
    private static final Logger log = LoggerFactory.getLogger(MiniThymeleafWebServer.class);

    /**
     * Main.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        Thread.setDefaultUncaughtExceptionHandler(new MiniThymeleafWebServer.MiniThymeleafWebServerExceptionHandler());
        Integer port = null;
        if ((args.length > 0) && (args[0] != null))
        {
            port = Integer.parseInt(args[0]);
            // http://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.txt
            if (!((port.intValue() == 80) || (port.intValue() == 8080) || (port.intValue() >= 8084)))
            {
                log.info("Please use a valid port number, 80, 8080 or >= 8084.  Using default.");
                port = null;
            }
        }
        us = MiniThymeleafWebServer.getInstance(port);
    }

    /**
     * Private constructor to enforce singleton pattern.
     *
     * @param port
     */
    private MiniThymeleafWebServer(Integer port)
    {
        if (port != null)
        {
            httpPort = port.intValue();
        }
        createMappings();
        createThymeleaf();
        constructServer();
    }

    /**
     * Singleton pattern.
     *
     * @param port The HTTP Port to use or null for default.
     * @return The single MiniThymeleafWebServer instance.
     */
    public static MiniThymeleafWebServer getInstance(Integer port)
    {
        if (MiniThymeleafWebServer.us == null)
        {
            us = new MiniThymeleafWebServer(port);
        }
        return us;
    }

    /**
     * Construct the server.
     */
    private void constructServer()
    {
        log.info("Mini Thymeleaf Web Server");
        log.info("Port is: " + httpPort + " - to change append the port number after the '.jar', i.e. java -jar ./target/MiniThymeleafWebServer-1.0-SNAPSHOT.jar 80");
        userdir = System.getProperty("user.dir") + System.getProperty("file.separator") + "web";
        log.info("Served directory is: " + userdir);
        log.debug("MiniThymeleafWebServer:constructServer() - Class path is: " + System.getProperty("java.class.path"));
        createServer();
    }

    /**
     * Destructor.
     *
     * @param exitCode The exit code.
     */
    public static void destruct(int exitCode)
    {
        System.exit(exitCode);
    }

    /**
     * Should execute in response to a Ctrl-C etc.
     */
    protected static class MiniThymeleafWebServerExceptionHandler implements Thread.UncaughtExceptionHandler
    {
        @Override
        public void uncaughtException(Thread t,
                Throwable e)
        {
            log.info("Mini Thymeleaf Web Server Java VM Exception: " + e.getMessage() + " from " + t.getName());
            log.info("Goodbye.");

            destruct(-1);
        }
    }

    /**
     * Create the server.
     */
    private void createServer()
    {
        InetSocketAddress address = new InetSocketAddress(httpPort);
        try
        {
            HttpServer theServer = HttpServer.create(address, 0);

            theServer.createContext("/", this);
            createThreads(theServer);  // Try without to see the difference of not having multiple threads to serve the requests.
            theServer.start();
            log.info("Accepting requests on " + httpPort);
        }
        catch (IOException ex)
        {
            log.error("MiniThymeleafWebServer:createServer()", ex);
        }
    }

    /**
     * Setup the thread and queue creation classes.
     *
     * @param theServer The server that will receive with the requests.
     */
    private void createThreads(HttpServer theServer)
    {
        int corePoolSize = 20;
        int maximumPoolSize = 40;
        long keepAliveTime = 10;
        TimeUnit unit = TimeUnit.SECONDS;
        int workQueueCapacity = 40;
        int threadPriority = Thread.NORM_PRIORITY;

        log.info("MiniWebServer:createThreads - Creating thread factory with a thread priority of: " + threadPriority);
        MiniThymeleafWebServerThreadFactory threadFactory = new MiniThymeleafWebServerThreadFactory(threadPriority);

        log.info("MiniWebServer:createThreads - Creating thread pool executor with:");
        log.info("Core Pool Size     : " + corePoolSize);
        log.info("Maximum Pool Size  : " + maximumPoolSize);
        log.info("Keep Alive Time    : " + keepAliveTime + " seconds.");
        log.info("Work Queue Capacity: " + workQueueCapacity);

        MiniThymeleafWebServerThreadPoolExecutor threadExecutor
                = new MiniThymeleafWebServerThreadPoolExecutor(
                        corePoolSize,
                        maximumPoolSize,
                        keepAliveTime,
                        unit,
                        workQueueCapacity,
                        threadFactory);

        theServer.setExecutor(threadExecutor);
    }

    /**
     * Info on:
     * http://blog.zenika.com/index.php?post/2013/01/18/introducing-the-thymeleaf-template-engine
     */
    private void createThymeleaf()
    {
        resolver.setTemplateMode("XHTML");
        //resolver.setPrefix(userdir);  // For FileTemplateResolver
        resolver.setSuffix(".html");
        engine.setTemplateResolver(resolver);

        // TODO: I've not worked out how to have a properties file for the message resolver when using Context and not IContext.
        org.thymeleaf.messageresolver.StandardMessageResolver messageResolver = new org.thymeleaf.messageresolver.StandardMessageResolver();
        messageResolver.addDefaultMessage("minithymeleafwebserver.title", "Mini Thymeleaf Web Server");
        messageResolver.addDefaultMessage("props.property", "Property");
        messageResolver.addDefaultMessage("props.value", "Value");
        messageResolver.addDefaultMessage("serve.uri", "Serving uniform resource identifiers");
        engine.setMessageResolver(messageResolver);
    }

    /**
     * Create the mappings between the URI's and their variables.
     */
    private void createMappings()
    {
        ViewData template = new ViewData("web/index");  // View data for the index view.
        // Create the update action for the 'nowdate' view variable.
        ViewVariableAction action = new ViewVariableAction()
        {
            @Override
            public Object performUpdate()
            {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            }
        };
        // Create the 'nowdate' variable.
        ViewVariable variable = new ViewVariable(action.performUpdate(), action);
        // Add the 'nowdate' variable to the list of variables for the index view.
        template.setVariable("nowdate", variable);
        // Create the 'startdate' view variable and add it to the list of variables for the index view.
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        variable = new ViewVariable(now, null);
        template.setVariable("startdate", variable);

        // Map the index view to it's URI's.
        mapper.addMapping("/", template);
        mapper.addMapping("/index.html", template);

        template = new ViewData("web/props");
        Properties sysProperties = System.getProperties();
        java.util.LinkedList<SysProp> sysProps = new java.util.LinkedList<>();
        SysProp currentProp;
        Iterator<Entry<Object, Object>> it = sysProperties.entrySet().iterator();
        Entry<Object, Object> current;
        while (it.hasNext())
        {
            current = it.next();
            currentProp = new SysProp();
            currentProp.setName((String) current.getKey());
            currentProp.setValue((String) current.getValue());
            sysProps.add(currentProp);
        }
        variable = new ViewVariable(sysProps, null);
        template.setVariable("sysProps", variable);
        mapper.addMapping("/props.html", template);

        // Now all the mappings are done we can set the URI list on the index page.
        // Create the 'startdate' view variable and add it to the list of variables for the index view.        
        variable = new ViewVariable(mapper.getMappings(), null);
        template = mapper.getMapping("/");
        template.setVariable("uris", variable);
    }

    /**
     * Handle the HTTP request.
     *
     * @param request Request to process.
     * @throws IOException If error.
     */
    @Override
    public void handle(HttpExchange request) throws IOException
    {
        long handleTimeNS = System.nanoTime();
        long handleTimeMS = System.currentTimeMillis();
        processRequest(request);
        handleTimeNS = System.nanoTime() - handleTimeNS;
        handleTimeMS = System.currentTimeMillis() - handleTimeMS;
        log.info(request.getRequestURI().toString() + " took " + handleTimeNS + "ns or " + handleTimeMS + "ms.");
    }

    /**
     * Process the request.
     *
     * @param request The request to process.
     */
    private void processRequest(HttpExchange request)
    {
        // TODO: Process specific URL's, GET and POST requests.  Possibly using Servlet technology.

        URI dUrl = request.getRequestURI();
        String rawPath = dUrl.getRawPath();

        if (mapper.hasMapping(rawPath))
        {
            ViewData mapping = mapper.getMapping(rawPath);
            sendTemplate(request, mapping);
        }
        else if (rawPath.length() > 1)
        {
            sendFile(request, rawPath);
        }
        else if (rawPath.charAt(0) == '/')
        {
            sendIndex(request);
        }
        else
        {
            // Bogus request...
            Headers responseHeaders = request.getResponseHeaders();
            responseHeaders.set("Date", new Date().toString());
            responseHeaders.set("Server", MTWS_TITLE);

            try
            {
                //responseHeaders.set("Content-type", MimeType.HTML.getType());
                request.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
                //OutputStream os = request.getResponseBody();
                //os.write(responseString.getBytes());
            }
            catch (IOException ex)
            {
                log.info("MiniThymeleafWebServer:processRequest - Bad request.");
                log.trace("MiniThymeleafWebServer:processRequest - Bad request.", ex);
            }
            request.close();
        }
    }

    /**
     * Send the index page if not a template version.
     *
     * @param request The request.
     */
    private void sendIndex(HttpExchange request)
    {
        log.info("MiniThymeleafWebServer:sendIndex - " + request.getRequestURI().toString());

        Headers responseHeaders = request.getResponseHeaders();

        responseHeaders.set("Content-type", mimeTypeFactory.HTML.getType());
        responseHeaders.set("Date", new Date().toString());
        responseHeaders.set("Server", MTWS_TITLE);

        try
        {
            String contents = "<html><head><title>Mini Thymeleaf Web Server</title></head><body><h1>The content</h1></body></html>";
            request.sendResponseHeaders(HttpURLConnection.HTTP_OK, contents.length());
            try (OutputStream os = request.getResponseBody())
            {
                os.write(contents.getBytes());
                os.flush();
            }
            request.close();
        }
        catch (IOException ex)
        {
            log.error("sendIndex()", ex);
        }
    }

    /**
     * Process the request as being matched to a template with the template
     * engine.
     *
     * @param request The request.
     * @param mapping The mapping that matches the request.
     */
    private void sendTemplate(HttpExchange request, ViewData mapping)
    {
        log.info("MiniThymeleafWebServer:sendTemplate - " + request.getRequestURI().toString());
        
        // Set the 'host' template variable if it has been set in the request headers.
        Headers requestHeaders = request.getRequestHeaders();
        String host = "";
        if (requestHeaders.containsKey("Host"))
        {
            List<String> values = request.getRequestHeaders().get("Host");
            host = values.get(0);
        }
        mapping.setHost(host);

        Headers responseHeaders = request.getResponseHeaders();

        responseHeaders.set("Content-type", mimeTypeFactory.HTML.getType());
        responseHeaders.set("Date", new Date().toString());
        responseHeaders.set("Server", MTWS_TITLE);

        try
        {
            String contents;
            try
            {
                mapping.update();
                contents = engine.process(mapping.getName(), mapping.getContext());
                request.sendResponseHeaders(HttpURLConnection.HTTP_OK, contents.length());
            }
            catch (IOException ex)
            {
                log.error("MiniThymeleafWebServer:sendTemplate()", ex);
                contents = ex.getMessage();
                request.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, contents.length());
            }

            try (OutputStream os = request.getResponseBody())
            {
                os.write(contents.getBytes());
                os.flush();
            }
            request.close();
        }
        catch (IOException ex)
        {
            log.error("MiniThymeleafWebServer:sendTemplate()", ex);
        }
    }

    /**
     * Send the given file to the web browser with the correct MIME type. Looks
     * in the users directory for the file.
     *
     * @param request The request this is for.
     * @param file The file to send.
     */
    private void sendFile(HttpExchange request, String file)
    {
        String fullname = userdir + file;
        log.info("MiniThymeleafWebServer:sendFile - " + request.getRequestURI().toString() + " - " + fullname);

        File theFile = new File(fullname);

        if (theFile.exists())
        {
            MimeType theType = mimeTypeFactory.guessContentTypeFromName(file);

            byte[] fileContents = getFile(theFile);
            if (fileContents != null)
            {
                Headers responseHeaders = request.getResponseHeaders();
                responseHeaders.set("Date", new Date().toString());
                responseHeaders.set("Server", MTWS_TITLE);

                responseHeaders.set("Content-type", theType.getType());
                if (theType.isBinary())
                {
                    try
                    {
                        // Info on http://elliotth.blogspot.com/2009/03/using-comsunnethttpserver.html
                        responseHeaders.set("Content-Encoding", "gzip");
                        request.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);  // Don't know how large the compressed file will be.
                        try (GZIPOutputStream gos = new GZIPOutputStream(request.getResponseBody(), BUFFER_MAX))
                        {
                            gos.write(fileContents);
                            gos.finish();
                            gos.close();
                        }
                        catch (Exception ex)
                        {
                            log.error("MiniThymeleafWebServer:sendFile - Binary file: " + file);
                            log.error("MiniThymeleafWebServer:sendFile()", ex);
                        }
                    }
                    catch (IOException ex)
                    {
                        log.error("MiniThymeleafWebServer:sendFile - Binary file: " + file);
                        log.error("MiniThymeleafWebServer:sendFile()", ex);
                    }
                }
                else
                {
                    try
                    {
                        request.sendResponseHeaders(HttpURLConnection.HTTP_OK, fileContents.length);
                        try (OutputStream os = request.getResponseBody())
                        {
                            os.write(fileContents);
                            os.flush();
                            os.close();
                        }
                        catch (Exception ex)
                        {
                            log.error("MiniThymeleafWebServer:sendFile - Text file: " + file);
                            log.error("MiniThymeleafWebServer:sendFile()", ex);
                        }
                    }
                    catch (IOException ex)
                    {
                        log.error("MiniThymeleafWebServer:sendFile - Text file: " + file);
                        log.error("MiniThymeleafWebServer:sendFile()", ex);
                    }
                }

                request.close();
            }
            else
            {
                // Code above has not got the file...
                try
                {
                    // File has no content.
                    request.sendResponseHeaders(HttpURLConnection.HTTP_NO_CONTENT, 0);
                    request.close();
                }
                catch (IOException ex)
                {
                    log.error("MiniThymeleafWebServer:sendFile - Sending response headers for file: " + file);
                    log.error("MiniThymeleafWebServer:sendFile()", ex);
                }

                log.warn("MiniThymeleafWebServer:sendFile - File '" + file + "' has no content.");
            }
        }
        else
        {
            // Code above has not got the file...
            try
            {
                // File does not exist.
                request.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0);
                request.close();
            }
            catch (IOException ex)
            {
                log.error("MiniThymeleafWebServer:sendFile - Sending response headers for file: " + file);
                log.error("MiniThymeleafWebServer:sendFile()", ex);
            }

            log.info("File '" + file + "' not sent.");
        }
    }

    /**
     * Gets the given file from the operating system.
     *
     * @param theFile The file to get.
     * @return The file as a byte array.
     */
    private byte[] getFile(File theFile)
    {
        byte[] buffer = null;

        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(theFile);
            FileChannel fc = fis.getChannel();
            long len = theFile.length();
            int byteReadCount = 0;
            ByteBuffer byteBuffer = ByteBuffer.allocate((int) len);

            int bytesRead = 0;
            while ((bytesRead != -1) && (byteReadCount < len))
            {
                bytesRead = fc.read(byteBuffer);
                byteReadCount += bytesRead; // Cope with 0 return when have reached end of text file and cannot get -1 byte as buffer is full.
            }
            buffer = byteBuffer.array();
        }
        catch (FileNotFoundException ex)
        {
            log.error("MiniThymeleafWebServer:getFile()", ex);
        }
        catch (IOException ex)
        {
            log.error("MiniThymeleafWebServer:getFile()", ex);
        }
        finally
        {
            if (fis != null)
            {
                try
                {
                    fis.close();
                }
                catch (IOException ex)
                {
                    log.error("MiniThymeleafWebServer:getFile()", ex);
                }
            }
        }

        return buffer;
    }

    private class MiniThymeleafWebServerThreadFactory implements ThreadFactory
    {
        private final ThreadGroup miniThymeleafWebServerThreadGroup = new ThreadGroup("Mini Thymeleaf Web Server Thread Group");
        private int threadPriority = Thread.NORM_PRIORITY;
        private int threadCount = 0;

        /**
         * Constructor
         *
         * @param threadPriority The priority all threads are created with.
         */
        public MiniThymeleafWebServerThreadFactory(int threadPriority)
        {
            this.threadPriority = threadPriority;
        }

        /**
         * Creates a new thread.
         *
         * @param r The runnable task the thread is created for.
         * @return A new thread.
         */
        @Override
        public Thread newThread(Runnable r)
        {
            Thread theThread = new Thread(miniThymeleafWebServerThreadGroup, r, "MTWS HTTP Server Thread-" + ++threadCount);

            theThread.setPriority(threadPriority);

            return theThread;
        }

        /**
         * Gets the thread group that all leader board threads belong to.
         *
         * @return The thread group.
         */
        public ThreadGroup getThreadGroup()
        {
            return miniThymeleafWebServerThreadGroup;
        }

        /**
         * Gets all the current threads.
         *
         * @return The threads.
         */
        public Thread[] getThreads()
        {
            Thread[] theThreads = new Thread[miniThymeleafWebServerThreadGroup.activeCount()];

            int threads = miniThymeleafWebServerThreadGroup.enumerate(theThreads);

            log.info("MiniThymeleafWebServerThreadFactory:getThreads - " + threads + " active threads returned.");

            return theThreads;
        }

        /**
         * Sets the thread priority for all new threads.
         *
         * @param threadPriority The thread priority.
         */
        public void setThreadPriority(int threadPriority)
        {
            // TODO: Set thread priority of all existing threads too.
            this.threadPriority = threadPriority;
        }
    }

    private class MiniThymeleafWebServerThreadPoolExecutor extends ThreadPoolExecutor
    {
        /**
         * See
         * http://download.oracle.com/javase/6/docs/api/java/util/concurrent/ThreadPoolExecutor.html#ThreadPoolExecutor(int,
         * int, long, java.util.concurrent.TimeUnit,
         * java.util.concurrent.BlockingQueue,
         * java.util.concurrent.ThreadFactory)
         *
         * @param corePoolSize
         * @param maximumPoolSize
         * @param keepAliveTime
         * @param unit
         * @param workQueueCapacity
         * @param threadFactory
         */
        public MiniThymeleafWebServerThreadPoolExecutor(
                int corePoolSize,
                int maximumPoolSize,
                long keepAliveTime,
                TimeUnit unit,
                int workQueueCapacity,
                ThreadFactory threadFactory)
        {
            super(corePoolSize,
                    maximumPoolSize,
                    keepAliveTime,
                    unit,
                    new MiniThymeleafWebServerBlockingQueue<Runnable>(workQueueCapacity),
                    threadFactory);
        }
    }

    @SuppressWarnings("hiding")
    private class MiniThymeleafWebServerBlockingQueue<Runnable>
            extends LinkedBlockingQueue<Runnable>
    {
        /**
         *
         */
        private static final long serialVersionUID = 6944217667960062812L;

        /**
         * Constructor
         *
         * @param capacity Capacity of the queue.
         */
        public MiniThymeleafWebServerBlockingQueue(int capacity)
        {
            super(capacity);
        }
    }
}
