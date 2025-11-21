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
package net.sf.javabdd;

import java.util.Arrays;

/**
 * Encodes a table of variable pairs. This is used for replacing variables in a BDD.
 *
 * @author John Whaley
 * @version $Id: BDDPairing.java,v 1.1 2004/10/16 02:58:57 joewhaley Exp $
 */
public abstract class BDDPairing {

  /**
   * Adds the pair (oldvar, newvar) to this table of pairs. This results in oldvar being substituted
   * with newvar in a call to BDD.replace().
   *
   * <p>Compare to bdd_setpair.
   */
  public abstract void set(int oldvar, int newvar);

  /**
   * Like set(), but with a whole list of pairs.
   *
   * <p>Compare to bdd_setpairs.
   */
  public void set(int[] oldvar, int[] newvar) {
    if (oldvar.length != newvar.length) {
      throw new BDDException();
    }

    for (int n = 0; n < oldvar.length; n++) {
      set(oldvar[n], newvar[n]);
    }
  }

  /**
   * Like set(int[], BDD[]) above but given a BDD[] of BDD variables as the first argument.
   *
   * @param oldvars the variables that form the domain of the pairing
   * @param newBDDs the BDDs that form the range of the pairing
   */
  public void set(BDD[] oldvars, BDD[] newBDDs) {
    if (Arrays.stream(oldvars).anyMatch(v -> !v.isVar())) {
      throw new BDDException(
          "the first argument to BDD::set(BDD[], BDD[]) must be an array of BDD variables");
    }

    set(Arrays.stream(oldvars).mapToInt(BDD::var).toArray(), newBDDs);
  }

  /**
   * Adds the pair (oldvar, newvar) to this table of pairs. This results in oldvar being substituted
   * with newvar in a call to bdd.replace(). The variable oldvar is substituted with the BDD newvar.
   * The possibility to substitute with any BDD as newvar is utilized in BDD.compose(), whereas only
   * the topmost variable in the BDD is used in BDD.replace().
   *
   * <p>Compare to bdd_setbddpair.
   */
  public abstract void set(int oldvar, BDD newvar);

  /**
   * Like set(), but with a whole list of pairs.
   *
   * <p>Compare to bdd_setbddpairs.
   */
  public void set(int[] oldvar, BDD[] newvar) {
    if (oldvar.length != newvar.length) {
      throw new BDDException();
    }

    for (int n = 0; n < newvar.length; n++) {
      set(oldvar[n], newvar[n]);
    }
  }

  /**
   * Defines each variable in the finite domain block p1 to be paired with the corresponding
   * variable in p2.
   *
   * <p>Compare to fdd_setpair.
   */
  public void set(BDDDomain p1, BDDDomain p2) {
    int[] ivar1 = p1.vars();
    int[] ivar2 = p2.vars();
    set(ivar1, ivar2);
  }

  /**
   * Like set(), but with a whole list of pairs.
   *
   * <p>Compare to fdd_setpairs.
   */
  public void set(BDDDomain[] p1, BDDDomain[] p2) {
    if (p1.length != p2.length) {
      throw new BDDException();
    }

    for (int n = 0; n < p1.length; n++) {
      if (p1[n].varNum() != p2[n].varNum()) {
        throw new BDDException();
      }
    }

    for (int n = 0; n < p1.length; n++) {
      set(p1[n], p2[n]);
    }
  }

  /**
   * Resets this table of pairs by setting all substitutions to their default values (that is, no
   * change).
   *
   * <p>Compare to bdd_resetpair.
   */
  public abstract void reset();
}
