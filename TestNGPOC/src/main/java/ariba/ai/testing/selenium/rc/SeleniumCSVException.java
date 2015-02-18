/*
    Copyright (c) 1996-2008 Ariba, Inc.
    All rights reserved. Patents pending.

    $Id: //ariba/ond/platform/R1_rel/test/test-framework/test/ariba/framework/SeleniumCSVException.java#1 $

    Responsible: sihde
 */

package ariba.ai.testing.selenium.rc;

public class SeleniumCSVException extends RuntimeException
{
    public SeleniumCSVException (String message)
    {
        super(message);
    }

    public SeleniumCSVException (String message, Throwable cause)
    {
        super(message, cause);
    }

}
