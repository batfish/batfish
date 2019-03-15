// R3.java, created Jul 28, 2004 2:55:30 AM by joewhaley
// Copyright (C) 2004 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package regression;

import junit.framework.Assert;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import bdd.BDDTestCase;

/**
 * unique() and applyUni() bug
 * 
 * @author John Whaley
 * @version $Id: R3.java,v 1.2 2005/04/18 12:00:00 joewhaley Exp $
 */
public class R3 extends BDDTestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(R3.class);
    }
    
    public void testR3() {
        Assert.assertTrue(hasNext());
        while (hasNext()) {
            
            BDDFactory bdd = nextFactory();
            BDD x0,x1,y0,y1,z0,z1,t,or,one;
            bdd.setVarNum(5);
            x0 = bdd.ithVar(0);
            x1 = bdd.ithVar(1);
            one = bdd.one();
            or = x0.or(x1);

            z0 = or.unique(x0);
            t = x1.not();
            Assert.assertTrue(z0.toString(), z0.equals(t));
            t.free();

            z1 = or.unique(x1);
            t = x0.not();
            Assert.assertTrue(z1.toString(), z1.equals(t));
            t.free();

            t = one.unique(x0);
            Assert.assertTrue(t.toString(), t.isZero());
            t.free();

            y0 = x0.applyUni(x1, BDDFactory.or, x0);
            t = x1.not();
            Assert.assertTrue(y0.toString(), y0.equals(t));
            t.free();

            y1 = x0.applyUni(x1, BDDFactory.or, x1);
            t = x0.not();
            Assert.assertTrue(y1.toString(), y1.equals(t));
            t.free();

            x0.free(); x1.free(); y0.free(); y1.free(); z0.free(); z1.free();
            or.free(); one.free();
            
        }
    }
}
