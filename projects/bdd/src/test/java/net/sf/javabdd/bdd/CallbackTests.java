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
package net.sf.javabdd.bdd;

import java.lang.reflect.Method;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.junit.internal.runners.JUnit38ClassRunner;
import org.junit.runner.RunWith;

/**
 * CallbackTests
 *
 * @author jwhaley
 * @version $Id: CallbackTests.java,v 1.3 2005/05/09 09:58:02 joewhaley Exp $
 */
@RunWith(JUnit38ClassRunner.class)
public class CallbackTests extends BDDTestCase {

  public static void main(String[] args) {
    junit.textui.TestRunner.run(CallbackTests.class);
  }

  volatile int gc_called;

  public void my_gc_callback(int x, BDDFactory.GCStats stats) {
    if (x == 0) {
      System.out.println(stats);
      gc_called++;
    }
  }

  public void testGCCallback() {
    reset();
    Method m;
    try {
      m =
          CallbackTests.class.getDeclaredMethod(
              "my_gc_callback", int.class, BDDFactory.GCStats.class);
    } catch (SecurityException | NoSuchMethodException e) {
      fail(e.toString());
      return;
    }
    assertTrue(hasNext());
    while (hasNext()) {
      BDDFactory bdd = next();
      bdd.registerGCCallback(this, m);
      gc_called = 0;
      final int numBits = 20;
      final int max = (1 << numBits) - 1;
      if (bdd.varNum() < numBits) {
        bdd.setVarNum(numBits);
      }
      int[] vars = new int[numBits];
      for (int i = 0; i < numBits; ++i) {
        vars[i] = i;
      }
      for (int i = 0; i < max; ++i) {
        if (gc_called > 1) {
          break;
        }
        BDD v = bdd.buildCube(i, vars);
        v.free();
      }
      assertTrue(gc_called > 0);
      bdd.unregisterGCCallback(this, m);
    }
  }

  volatile boolean reorder_called;

  public void my_reorder_callback(boolean k, BDDFactory.ReorderStats stats) {
    if (!k) {
      System.out.println(stats);
      reorder_called = true;
    }
  }

  public void testReorderCallback() {
    reset();
    Method m;
    try {
      m =
          CallbackTests.class.getDeclaredMethod(
              "my_reorder_callback", boolean.class, BDDFactory.ReorderStats.class);
    } catch (SecurityException | NoSuchMethodException e) {
      fail(e.toString());
      return;
    }
    assertTrue(hasNext());
    while (hasNext()) {
      BDDFactory bdd = next();
      bdd.registerReorderCallback(this, m);
      reorder_called = false;
      if (bdd.varNum() < 5) {
        bdd.setVarNum(5);
      }
      // bdd.varBlockAll();
      BDD x = bdd.ithVar(0);
      x.andWith(bdd.ithVar(1));
      x.andWith(bdd.ithVar(2));
      x.andWith(bdd.ithVar(3));
      bdd.setVarOrder(new int[] {4, 3, 2, 1, 0});
      assertTrue(reorder_called);
      x.free();
      bdd.unregisterReorderCallback(this, m);
    }
  }

  volatile boolean resize_called;

  public void my_resize_callback(int oldsize, int newsize) {
    System.out.println("old size = " + oldsize + ", new size = " + newsize);
    resize_called = true;
  }

  public void testResizeCallback() {
    reset();
    Method m;
    try {
      m = CallbackTests.class.getDeclaredMethod("my_resize_callback", int.class, int.class);
    } catch (SecurityException | NoSuchMethodException e) {
      fail(e.toString());
      return;
    }
    assertTrue(hasNext());
    while (hasNext()) {
      BDDFactory bdd = next();
      bdd.registerResizeCallback(this, m);
      resize_called = false;
      int newSize = bdd.getNodeTableSize() * 2;
      bdd.setNodeTableSize(newSize);
      assertTrue(resize_called);
      bdd.unregisterResizeCallback(this, m);
    }
  }
}
