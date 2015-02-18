/*
    Copyright (c) 2005 Ariba, Inc.
    All rights reserved. Patents pending.

    $Id: //ariba/ond/platform/R1_rel/test/test-framework/test/ariba/framework/MessageCustomizable.java#1 $

    Responsible: dlee
*/

package ariba.ai.testing.selenium.rc;


/**
 *  The methods of this interface are used by {@link XMLFormatter} to 
 *  determine if any customization on message displayed in the html 
 *  report needs to be done.    
 */
public interface MessageCustomizable
{
    /**
     * 
     * @return the summary hyperlink content displayed 
     * in the html report.  Return empty string or null if there 
     * is no customization needs to be done.
     * The short description of the
     * <code>Throwable</code> will be used as the default.
     * 
     * @aribaapi public
     */    
    public String getSummaryHyperLinkContent ();
    
    /**
     * 
     * @return the message displayed in the html report.
     * Return empty string or null if there is no customization.
     * needs to be done.  The stacktrace of the 
     * <code>Throwable</code> will be used as the default.
     * 
     * @aribaapi public
     */
    public String getMessage ();
    
    /**
     * 
     * @return the destination anchor for the detail 
     * hyperlink displayed in the html report.  Return
     * empty string or null if there is no log.
     * 
     * @aribaapi public
     */
    public String getDetailDestinationAnchor ();
}
