/*
    Copyright (c) 2009 Ariba, Inc.
    All rights reserved. Patents pending.

    $Id: //ariba/ond/platform/R1_rel/test/test-framework/test/ariba/framework/util/FileUtil.java#1 $

    Responsible: awysocki
 */

package ariba.ai.testing.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ariba.util.core.ListUtil;
import ariba.util.core.StringUtil;
import ariba.util.io.FilenameExtensionFilter;

public class FileUtil
{
    private static final String DirectoryDelimiter = "/";

    public static String strcatPath (String path1, String path2)
    {
            if (path1.endsWith(DirectoryDelimiter) &&
                        path2.startsWith(DirectoryDelimiter)) {
            return StringUtil.strcat(path1, path2.substring(1, path2.length()));
        }
        else if (path1.endsWith(DirectoryDelimiter) ||
                path2.startsWith(DirectoryDelimiter)) {
            return StringUtil.strcat(path1, path2);
        }
        else {
            return StringUtil.strcat(path1, DirectoryDelimiter, path2);
        }
    }
    
    
    /**
     * Recursively delete files and folders for a given directory
     * 
     * @param f the root directory for the tree structure that its contents to be deleted.
     * @throws IOException
     */
    public static void delete(File f) throws IOException 
    {
        if (f.isDirectory()) 
        {
            for (File c : f.listFiles())
                delete(c);
        }
        if (!f.delete()) 
        {
            throw new IOException("Failed to delete file: " + f);
        }
    }
    
    
    /**
     * 
     *
     *   Recursively get the list of the files in a given dir and add it to the list passed
     *   in the second argument.
     * 
     * @param dir           the root directory for the tree structure that its contents are to be listed.
     * @param filesList     is the list that will contain the list of Files under the dir.
     *                       The list of files will be added to this list recursively.
     * @param filter        Filter to filter out unwanted file extensions.. Filter is optional and can be null.
     *                      If filter is provided, then files will be added based on the filter accept
     * @return
     */
    
    public static List<File> listFiles(File dir, List<File> filesList, FilenameExtensionFilter filter)
    {
        if (filesList == null) {
            filesList = new LinkedList<File>();
        }

        if (!dir.isDirectory())
        {
            if (filter == null || filter.accept(dir, dir.getName()) ) 
            {
                filesList.add(dir);
            } 
            
            return filesList;
        }
       
        for (File file : dir.listFiles())
        {
            listFiles(file, filesList, filter);
        }
        return filesList;
    }
    
}
