/*
   Copyright (c) 2010 Ariba, Inc.
   All Rights Reserved.

    $Id: //ariba/ond/platform/R1_rel/test/test-framework/test/ariba/framework/util/MessageDigestUtil.java#1 $
*/

package ariba.ai.testing.util;

import ariba.util.core.Base64;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


/**
 * MessageDigest generation utility.
 *
 */
public class MessageDigestUtil 
{

    private static final SecureRandom Random = new SecureRandom();
    private static final char[] alphanumeric;

    static {
        StringBuffer buf=new StringBuffer(128);
        for (int i=48; i<= 57;i++) buf.append((char)i); // 0-9
        for (int i=65; i<= 90;i++) buf.append((char)i); // A-Z
        for (int i=97; i<=122;i++) buf.append((char)i); // a-z
        alphanumeric = buf.toString().toCharArray();
    }

    /**
        @param filename name of the file for which to generate a checksum
        @param algorithm name of the algorithm to use to generate the checksum, see <a href="http://java.sun.com/javase/6/docs/api/java/security/MessageDigest.html#getInstance(java.lang.String)">java.security.MessageDigest#getInstance(java.lang.String)</a>

        @return Base64 encoded checksum for the specified file

        @throws SecurityException
    */
    public static String getChecksumForFile (String filename, String algorithm)
    {
        byte[] b = createChecksumForFile(filename, algorithm);
        return Base64.encodeToString(b);
    }

    /**
        @param bytes byte array for which to generate a MessageDigest
        @param algorithm name of the algorithm to use to generate the MessageDigest, see <a href="http://java.sun.com/javase/6/docs/api/java/security/MessageDigest.html#getInstance(java.lang.String)">java.security.MessageDigest#getInstance(java.lang.String)</a>

        @return Base64 encoded string

        @throws SecurityException
    */
    public static String getMessageDigest (byte[] bytes, String algorithm)
    {
        try 
        {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(bytes);
            byte[] b = md.digest();
            String result = "";
            for (int i=0; i < b.length; i++) {
                result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
            }
            return result;
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new SecurityException(e);
        }
    }

    public static String getRandomId (int len)
    {
        StringBuilder out = new StringBuilder();

        while(out.length() < len)
        {
            int idx = Math.abs((Random.nextInt() % alphanumeric.length ));
            out.append(alphanumeric[idx]);
        }
        return out.toString();
    }
    
    /**
        @param filename name of the file for which to generate a checksum
        @return Base64 encoded message

        @throws SecurityException
    */
    private static byte[] createChecksumForFile (String filename, String algorithm)
    {
        try 
        {
            FileInputStream fis =  new FileInputStream(filename);
            byte[] buffer = new byte[4096];
            MessageDigest complete = MessageDigest.getInstance(algorithm);
            int numRead;
            do
            {
                numRead = fis.read(buffer);
                if (numRead > 0)
                {
                    complete.update(buffer, 0, numRead);
                }
            } while (numRead != -1);
            fis.close();
            return complete.digest();
        }
        catch (IOException e)
        {
            throw new SecurityException(e);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new SecurityException(e);
        }
    }

 
}
