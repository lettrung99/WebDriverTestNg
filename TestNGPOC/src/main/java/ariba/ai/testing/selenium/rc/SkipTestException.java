/*
    Copyright (c) 1996-2004 Ariba, Inc.
    All rights reserved. Patents pending.

    $Id: //ariba/ond/platform/R1_rel/test/test-framework/test/ariba/framework/SkipTestException.java#1 $

    Responsible: dlee
*/

package ariba.ai.testing.selenium.rc;



/**
    This runtime exception is used to let the TestFramework know that
    we want to "skip" this test. A skipped test is a test that
    did not PASS or FAIL, just a test that we did not run.
    The reason why we don't run this test should be set in the
    message passed to the contructor
*/

public class SkipTestException extends java.lang.RuntimeException
{

    /**
        @aribaapi private
    */
    public static final String ClassName = "test.ariba.framework.SkipTestException";

    /**
        @aribaapi private
        @param message The reason why we skip this test. Could be a a defect number,
        a short description which configuration is needed to run this test
    */
    public SkipTestException (String message)
    {
        super(message);
    }
}
