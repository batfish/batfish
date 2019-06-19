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
 * unique() and applyUni() bug
 *
 * @author John Whaley
 * @version $Id: R3.java,v 1.2 2005/04/18 12:00:00 joewhaley Exp $
 */
@RunWith(JUnit38ClassRunner.class)
public class R3 extends BDDTestCase {
  public static void main(String[] args) {
    junit.textui.TestRunner.run(R3.class);
  }

  public void testR3() {
    assertTrue(hasNext());
    while (hasNext()) {

      BDDFactory bdd = next();
      BDD x0, x1, y0, y1, z0, z1, t, or, one;
      bdd.setVarNum(5);
      x0 = bdd.ithVar(0);
      x1 = bdd.ithVar(1);
      one = bdd.one();
      or = x0.or(x1);

      z0 = or.unique(x0);
      t = x1.not();
      assertEquals(z0.toString(), z0, t);
      t.free();

      z1 = or.unique(x1);
      t = x0.not();
      assertEquals(z1.toString(), z1, t);
      t.free();

      t = one.unique(x0);
      assertTrue(t.toString(), t.isZero());
      t.free();

      y0 = x0.applyUni(x1, BDDFactory.or, x0);
      t = x1.not();
      assertEquals(y0.toString(), y0, t);
      t.free();

      y1 = x0.applyUni(x1, BDDFactory.or, x1);
      t = x0.not();
      assertEquals(y1.toString(), y1, t);
      t.free();

      x0.free();
      x1.free();
      y0.free();
      y1.free();
      z0.free();
      z1.free();
      or.free();
      one.free();
    }
  }
}
