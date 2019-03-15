/*
 * Note: We obtained permission from the author of Javabdd, John Whaley, to use
 * the library with Batfish under the MIT license. The email exchange is included
 * in LICENSE.email file.
 *
 * MIT License
 *
 * Copyright (c) 2013-2017 John Whaley
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package net.sf.javabdd.regression;

import junit.framework.Assert;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.bdd.BDDTestCase;

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
