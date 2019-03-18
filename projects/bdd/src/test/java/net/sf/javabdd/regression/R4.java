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

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.bdd.BDDTestCase;
import org.junit.internal.runners.JUnit38ClassRunner;
import org.junit.runner.RunWith;

/**
 * allsat bug
 *
 * @author John Whaley
 * @version $Id: R4.java,v 1.1 2005/07/22 19:37:11 joewhaley Exp $
 */
@RunWith(JUnit38ClassRunner.class)
public class R4 extends BDDTestCase {
  public static void main(String[] args) {
    junit.textui.TestRunner.run(R4.class);
  }

  public void testR4() {
    assertTrue(hasNext());
    while (hasNext()) {
      BDDFactory f = next();
      f.setVarNum(2);
      BDD bdd1 = f.ithVar(0);
      BDD.AllSatIterator i = bdd1.allsat();
      assertTrue(i.hasNext());
      byte[] b = i.next();
      assertTrue(!i.hasNext());
      assertEquals(b.length, 2);
      assertEquals(b[0], 1);
      assertEquals(b[1], -1);
      bdd1.free();
    }
  }
}
