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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import junit.framework.TestCase;
import net.sf.javabdd.BDDFactory;

/**
 * BDDTestCase
 *
 * @author John Whaley
 * @version $Id: BDDTestCase.java,v 1.8 2005/06/29 08:01:54 joewhaley Exp $
 */
public abstract class BDDTestCase extends TestCase implements Iterator<BDDFactory> {

  public static final String[] factoryNames = {
    "net.sf.javabdd.JFactory",
  };

  private static Collection<BDDFactory> factories;
  private Iterator<BDDFactory> i;
  private int nodenum, cachesize;

  private void initFactories() {
    if (factories != null) {
      return;
    }
    Collection<BDDFactory> f = new LinkedList<>();
    for (String bddpackage : factoryNames) {
      try {
        Class<?> c = Class.forName(bddpackage);
        Method m = c.getMethod("init", int.class, int.class);
        BDDFactory b = (BDDFactory) m.invoke(null, new Object[] {nodenum, cachesize});
        f.add(b);
      } catch (Throwable e) {
        if (e instanceof InvocationTargetException) {
          e = ((InvocationTargetException) e).getTargetException();
        }
        System.out.println("Failed initializing " + bddpackage + ": " + e);
      }
    }
    factories = f;
  }

  protected void destroyFactories() {
    if (factories == null) {
      return;
    }
    for (Object factory : factories) {
      BDDFactory f = (BDDFactory) factory;
      f.done();
    }
    factories = null;
  }

  protected BDDTestCase(int nodenum, int cachesize) {
    this.nodenum = nodenum;
    this.cachesize = cachesize;
  }

  public BDDTestCase() {
    this(1000, 1000);
  }

  @Override
  protected void setUp() {
    // System.out.println("Doing setUp()");
    initFactories();
    reset();
  }

  @Override
  public BDDFactory next() {
    BDDFactory f = i.next();
    f.reset();
    return f;
  }

  @Override
  public boolean hasNext() {
    return i.hasNext();
  }

  @Override
  public void remove() {
    i.remove();
  }

  protected void reset() {
    i = factories.iterator();
  }

  @Override
  protected void tearDown() {
    // System.out.println("Doing tearDown()");
    // destroyFactories();
  }
}
