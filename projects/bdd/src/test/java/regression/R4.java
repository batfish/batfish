// R4.java, created Jul 22, 2005 11:36:30 AM by joewhaley
// Copyright (C) 2005 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package regression;

import junit.framework.Assert;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import bdd.BDDTestCase;

/**
 * allsat bug
 * 
 * @author John Whaley
 * @version $Id: R4.java,v 1.1 2005/07/22 19:37:11 joewhaley Exp $
 */
public class R4 extends BDDTestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(R4.class);
    }
    
    public void testR4() {
        Assert.assertTrue(hasNext());
        while (hasNext()) {
            BDDFactory f = nextFactory();
            f.setVarNum(2);
            BDD bdd1 = f.ithVar(0);
            BDD.AllSatIterator i = bdd1.allsat();
            Assert.assertTrue(i.hasNext());
            byte[] b = (byte[]) i.next();
            Assert.assertTrue(!i.hasNext());
            Assert.assertEquals(b.length, 2);
            Assert.assertEquals(b[0], 1);
            Assert.assertEquals(b[1], -1);
            bdd1.free();
        }
    }
}
