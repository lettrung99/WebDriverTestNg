/*
    Copyright (c) 2010 Ariba, Inc.
    All rights reserved. Patents pending.

    $Id: //ariba/ond/platform/R1_rel/test/test-framework/test/ariba/framework/util/HttpGet.java#1 $

    Responsible: awysocki
 */
package ariba.ai.testing.util;

import ariba.util.core.CommandLine;
import ariba.util.core.ArgumentParser;
import ariba.util.core.IOUtil;
import ariba.util.core.Fmt;
import ariba.util.core.SystemUtil;
import ariba.util.log.Log;
import ariba.util.log.Logger;
import ariba.util.log.LogManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Simple Http get client
 */
public class HttpGet implements CommandLine
{
    public static final String OptionUrl = "url";
    public static final String OptionFull = "full";

    private LogManager logManager;
    private String url = null;
    private boolean full = false;
    
    private int readTimeOut = 0;
    private boolean isCommandLineRequest = true;
    
    
    public HttpGet() {
        
    }
    
    public HttpGet(String url, boolean isCommandLineRequest) {
        this.url= url;
        this.isCommandLineRequest = isCommandLineRequest;
    }

    public void setupArguments (ArgumentParser arguments)
    {
        logManager.setupArguments(arguments);
		Logger.setupArguments(arguments);
		arguments.addRequiredString(OptionUrl,
				"<URL to request>");
		arguments.addOptionalBoolean (OptionFull, false,
				"<prints the full response (headers included)>");
    }

    public void processArguments (ArgumentParser arguments)
    {
        url = arguments.getString(OptionUrl);
        full = arguments.getBoolean(OptionFull); 
    }

    public void startup ()
    {
        try {
            doGet();
        } catch (MalformedURLException e) {
            handleException(400, "Bad Request", new RuntimeException(Fmt.S("[HttpGet] Invalid URL: %s.", url), e));
            System.exit(0);
        } catch (IOException e) {
            handleException(503, "Service Unavailable", new RuntimeException("[HttpGet] Problem during the request.", e));
            System.exit(0);
        }
    }
    
    public void setReadTimeOut(int timeout){
        this.readTimeOut = timeout;
    }
    
    public String doGet () throws MalformedURLException, IOException {
        HttpURLConnection hConnection = null;
        InputStream iStream = null;
        StringBuilder output = new StringBuilder();
        
        try {
            
            URL getUrl = new URL(url);
            URLConnection connection = getUrl.openConnection();
            if (!(connection instanceof HttpURLConnection)) {
                handleException(400, "Bad Request", 
                        new RuntimeException(Fmt.S("[HttpGet] Invalid protocol from URL: %s.",
                        url)));
                
            }
            hConnection = (HttpURLConnection)connection;
            hConnection.setDoOutput(true);
            hConnection.setDoInput(true);
            hConnection.setUseCaches(false);
            hConnection.setReadTimeout(readTimeOut);
            hConnection.connect();
    
            if(isCommandLineRequest) {
                StringBuilder sb = new StringBuilder();
                String statusLine = hConnection.getHeaderField(0);
                sb.append(statusLine).append('\n');
                
                String key = null;
                int index = 1;
                while ((key = hConnection.getHeaderFieldKey(index))!= null) {
                    String value = hConnection.getHeaderField(index);
                    sb.append(key).append(": ").append(value).append('\n');
                    index++;
                }
                sb.append('\n');
                System.out.print(sb.toString());
            }
            
            if (hConnection.getResponseCode() < 400) {
                iStream = hConnection.getInputStream();
                
                if(isCommandLineRequest) {
                    IOUtil.inputStreamToOutputStream(iStream,
                            System.out);
                } else {
                    output.append(convertStreamToString(iStream));
                }
            } else if (hConnection.getErrorStream() != null) {
                iStream = hConnection.getErrorStream();
                
                if(isCommandLineRequest) {
                    IOUtil.inputStreamToOutputStream(iStream,
                            System.out);
                } else {
                    output.append(convertStreamToString(iStream));
                }
            }
        } finally {
            if (hConnection != null) {
                hConnection.disconnect();
            }
        }
        
        return output.toString();
    }
    
    public void doPost(String postData) throws MalformedURLException, IOException
    {
        URL                 postUrl = null;
        URLConnection   urlConn = null;
        DataOutputStream    printout = null;
        DataInputStream     input = null;
        
        postUrl = new URL (url);
        urlConn = postUrl.openConnection();
        urlConn.setDoInput (true);
        urlConn.setDoOutput (true);
        urlConn.setUseCaches (false);
        urlConn.setRequestProperty
        ("Content-Type", "application/x-www-form-urlencoded");
        printout = new DataOutputStream (urlConn.getOutputStream ());
        //String content = URLEncoder.encode (postData) ;
        if(postData != null && !postData.trim().equalsIgnoreCase("")) {
            printout.writeBytes (postData);
        }
        printout.flush ();
        printout.close ();
        input = new DataInputStream (urlConn.getInputStream ());
        String str;
        while (null != ((str = input.readLine())))
        {
            Log.util.debug (str);
        }
        input.close ();
    }
    
    private void handleException(int errorCode, String phrase, Throwable t) {
        String body = Fmt.S("%s: %s", t.getMessage(), SystemUtil.stackTrace(t));
        System.out.println(Fmt.S("HTTP/1.1 %s %s\n\rContent-Length: %s\n\r\n\r%s",
                errorCode, phrase, body.getBytes().length, body));
    }
    
    private String convertStreamToString(InputStream is) {
        StringBuilder buffer = new StringBuilder(); 
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine(); 
            while (line != null) {
                buffer.append(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            handleException(400, "IO Error", new RuntimeException("[HttpGet] - convertStreamToString - Problem during the request.", e));
        }
        return buffer.toString();
    }

    public static void main (String[] args)
    {
        ArgumentParser.create(HttpGet.class.getName(), args);
    }
    
}
