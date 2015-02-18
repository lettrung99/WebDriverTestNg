/*
    Copyright (c) 2009 Ariba, Inc.
    All rights reserved. Patents pending.

    $Id: //ariba/ond/platform/R1_rel/test/test-framework/test/ariba/framework/selenium/SeleniumInternalException.java#1 $

    Responsible: awysocki
*/

package ariba.ai.testing.selenium.rc;

public class SeleniumInternalException extends RuntimeException
{

    public SeleniumInternalException (String arg0)
    {
        super(arg0);
    }

    public SeleniumInternalException (Throwable cause)
    {
        super(cause);
    }

    public SeleniumInternalException (String message, Throwable cause)
    {
        super(message, cause);
    }

}
