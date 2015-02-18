/*
    Copyright (c) 1996-2012 Ariba, Inc.
    All rights reserved. Patents pending.
    
    $Id: //ariba/ond/platform/R1_rel/test/test-framework/test/ariba/framework/util/ObjectUtil.java#1 $
    
    Responsible: mshaikh
 */

package ariba.ai.testing.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import ariba.util.core.Base64;
import ariba.util.core.StringUtil;
import ariba.util.core.SystemUtil;

public class ObjectUtil {

    public static String serializeObject (Serializable e) throws IOException
    {
        if (e != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(e);
            oos.close();
            return Base64.encodeToString(baos.toByteArray());
        } else {
            return null;
        }
    }

    public static Object deserializeObject (String expS) throws IOException, ClassNotFoundException
    {
        if (!StringUtil.nullOrEmptyOrBlankString(expS)) {
            byte[] expA = Base64.decodeFromString(expS);
            if(expA == null)
                return null;
            ByteArrayInputStream bais = new ByteArrayInputStream(expA);
            ObjectInputStream ois = new ObjectInputStream(bais);
            Object exp = ois.readObject();
            ois.close();
            return exp;
        } else {
            return null;
        }
    }
    
}
