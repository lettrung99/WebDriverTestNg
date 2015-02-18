/*
    Copyright (c) 1996-2007 Ariba, Inc.
    All rights reserved. Patents pending.

    $Id: //ariba/ond/platform/R1_rel/ui/testautomation/ariba/ui/testautomation/html/TestExceptionInfo.java#1 $

    Responsible: achung
*/

package ariba.ai.testing.selenium.rc;

import ariba.ui.aribaweb.core.AWBindingNames;
import ariba.ui.aribaweb.core.AWComponent;
import ariba.ui.aribaweb.core.AWRequestContext;
import ariba.util.core.ListUtil;
import ariba.util.core.MapUtil;

import java.util.List;
import java.util.Map;


public class TestExceptionInfo extends AWComponent
{
    public static final String AWNAME_LOCATOR            = "_TEST_EXCEPTION_INFO_";

    public static final int    TABLE_ROWS                = 5;
    public static final String CLASS_NAME_KEY            = "CLASS_NAME";
    public static final String MESSAGE_KEY               = "MESSAGE";
    public static final String ROOT_CAUSE_CLASS_NAME_KEY = "ROOT_CAUSE_CLASS_NAME";
    public static final String ROOT_CAUSE_MESSAGE_KEY    = "ROOT_CAUSE_MESSAGE";
    public static final String STACK_TRACE_KEY           = "STACK_TRACE";

    private Throwable _exception;
    private String _stackTrace;
    public Map<String, String> _item;


    public void sleep ()
    {
        _exception = null;
        _stackTrace = null;
        _item = null;
    }

    public void renderResponse (AWRequestContext requestContext, AWComponent component)
    {
        _exception = (Throwable) valueForBinding(AWBindingNames.exception);
        _stackTrace = (String) valueForBinding("stackTrace");

        super.renderResponse(requestContext, component);
    }

    public boolean showInfo ()
    {
        return (_exception != null && requestContext()._debugIsInPlaybackMode());
    }

    public List<Map<String, String>> infoList ()
    {
        List<Map<String, String>> list = ListUtil.list();

        // exception itself
        Map<String, String> item = MapUtil.map();
        item.put(AWBindingNames.key, CLASS_NAME_KEY);
        item.put(AWBindingNames.value, _exception.getClass().getName());
        list.add(item);
        item = MapUtil.map();
        item.put(AWBindingNames.key, MESSAGE_KEY);
        item.put(AWBindingNames.value, _exception.getMessage());
        list.add(item);

        // root cause
        Throwable rootCause = _exception;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        item = MapUtil.map();
        item.put(AWBindingNames.key, ROOT_CAUSE_CLASS_NAME_KEY);
        item.put(AWBindingNames.value, rootCause.getClass().getName());
        list.add(item);
        item = MapUtil.map();
        item.put(AWBindingNames.key, ROOT_CAUSE_MESSAGE_KEY);
        item.put(AWBindingNames.value, rootCause.getMessage());
        list.add(item);

        // stack trace
        item = MapUtil.map();
        item.put(AWBindingNames.key, STACK_TRACE_KEY);
        item.put(AWBindingNames.value, _stackTrace);
        list.add(item);

        return list;
    }
}