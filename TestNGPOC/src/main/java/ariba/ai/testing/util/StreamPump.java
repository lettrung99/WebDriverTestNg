/*
    Copyright 1996-2011 Ariba, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

    $Id: //ariba/ond/platform/R1_rel/test/test-framework/test/ariba/framework/util/StreamPump.java#1 $
*/
package ariba.ai.testing.util;

//import test.ariba.framework.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamPump extends Thread
{
    private InputStream is;
    private StringBuilder content = new StringBuilder();
    private Throwable exception;

    public StreamPump(InputStream is)
    {
        this.is = is;
    }

    public String getContent ()
    {
        return content.toString();
    }

    public Throwable getException ()
    {
        return exception;
    }

    public void run()
    {
        try
        {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null)
            {
                content.append(line).append("\n");
            }
        } catch (Throwable e)
        {
            exception = e;
        }
    }
}
