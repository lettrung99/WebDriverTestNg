/*
    Copyright (c) 1996-2009 Ariba, Inc.
    All rights reserved. Patents pending.

    $Id: //ariba/ond/platform/R1_rel/test/test-framework/test/ariba/framework/ThrowableWrapper.java#1 $

    Responsible: achung
*/

package ariba.ai.testing.selenium.rc;

import ariba.util.core.ListUtil;
import ariba.util.core.FastStringBuffer;

import java.util.List;
import java.util.Collections;


public class ThrowableWrapper extends Throwable
{
    private List<Throwable> _throwableList = ListUtil.list();


    public ThrowableWrapper()
    {
        super();
    }

    public ThrowableWrapper(Throwable t)
    {
        super();
        addThrowable(t);
    }

    public void addThrowable(Throwable t)
    {
        _throwableList.add(t);
    }

    public List<Throwable> getThrowableList()
    {
        return Collections.unmodifiableList(_throwableList);
    }

    public String getMessage ()
    {
        FastStringBuffer buff = new FastStringBuffer();
        for (Throwable t : _throwableList) {
            buff.append(t.getMessage());
            buff.append("\n");
        }

        return buff.toString();
    }
}
