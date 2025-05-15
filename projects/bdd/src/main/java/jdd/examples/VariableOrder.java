package jdd.examples;

import jdd.bdd.BDD;
import jdd.bdd.BDDPrinter;
import jdd.util.JDDConsole;
import jdd.util.math.Digits;

/**
 * This example illustrates the effect of variable ordering.
 *
 * <p>Given to sets A = { x0, x1, ..., xn } and B = { x0, x1, ..., xn } we build the equality
 * relation R: A x B.
 *
 * <p>A and B are encoded using two sets of BDD variables (a's and b's). We consider two different
 * variable orderings: disjoint (a0, a1, ... b0, b1, ...) and interleaved (a0, b0, a1, b1, ...) and
 * take a look of the resulting BDDs.
 */
public class VariableOrder {
  static final int COUNT = 32;

  // encode a number as a BDD over a set of variables
  private static int numToBDD(BDD bdd, int[] vars, int n) {
    int ret = 1;
    for (int i = 0; i < vars.length; i++) {
      int v = vars[i];
      if ((n & (1 << i)) == 0) {
        v = bdd.ref(bdd.not(v));
      }
      int old = ret;
      ret = bdd.ref(bdd.and(ret, v));
      bdd.deref(v);
      bdd.deref(old);
    }
    return ret;
  }

  // create R: A -> B given the BDD variables in A and B
  private static int createR(BDD bdd, int[] a, int[] b) {
    final int N = a.length;
    int ret = 0;

    for (int i = 0; i < COUNT; i++) {
      int left = bdd.ref(numToBDD(bdd, a, i));
      int right = bdd.ref(numToBDD(bdd, b, i));

      int entry = bdd.ref(bdd.and(left, right));
      bdd.deref(left);
      bdd.deref(right);

      int old = ret;
      ret = bdd.ref(bdd.or(ret, entry));
      bdd.deref(old);
      bdd.deref(entry);
    }
    return ret;
  }

  // since the example is so simple, we will put everything in the main function
  public static void main(String[] args) {
    // how many bits to represent 0 to COUNT-1
    final int bits = Digits.log2_ceil(COUNT);
    JDDConsole.out.printf("Note: A and B have %d elements, encoded with %d bits\n", COUNT, bits);

    // disjoint variables
    BDD bdd1 = new BDD(1000);
    int[] bdd1_a = bdd1.createVars(bits);
    int[] bdd1_b = bdd1.createVars(bits);
    int bdd1_r = createR(bdd1, bdd1_a, bdd1_b);

    JDDConsole.out.printf(
        "DISJOINT ordering: satcount=%.0f BDD-size=%d\n",
        bdd1.satCount(bdd1_r), bdd1.nodeCount(bdd1_r));
    BDDPrinter.printDot("order_disjoint", bdd1_r, bdd1, null);

    // interleaved variables
    BDD bdd2 = new BDD(1000);
    int[] bdd2_ab = bdd2.createVars(bits * 2);
    int[] bdd2_a = new int[bits];
    int[] bdd2_b = new int[bits];
    for (int i = 0; i < bits; i++) {
      bdd2_a[i] = bdd2_ab[i * 2 + 0];
      bdd2_b[i] = bdd2_ab[i * 2 + 1];
    }
    int bdd2_r = createR(bdd2, bdd2_a, bdd2_b);

    JDDConsole.out.printf(
        "INTERLEAVED ordering: satcount=%.0f BDD-size=%d\n",
        bdd2.satCount(bdd2_r), bdd2.nodeCount(bdd2_r));
    BDDPrinter.printDot("order_interleaved", bdd2_r, bdd2, null);
  }
}
