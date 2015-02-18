/*
    Copyright (c) 1996-2008 Ariba, Inc.
    All rights reserved. Patents pending.

    $Id: //ariba/ond/platform/R1_rel/test/test-framework/test/ariba/framework/SeleniumTestError.java#1 $

    Responsible: jimh
 */
package ariba.ai.testing.selenium.rc;

public class SeleniumTestError extends AssertionError implements MessageCustomizable {
    private String _summary;
    private String _detail;

    public SeleniumTestError (String summary, String detail)
    {
        super(summary);
        _summary = summary;
        _detail = detail;
    }

    public String getSummaryHyperLinkContent ()
    {
        return _summary;
    }

    public String getMessage ()
    {
        return _detail;
    }

    public String getDetailDestinationAnchor ()
    {
        return null;
    }
}
