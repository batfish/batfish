package net.sf.javabdd;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.Test;

/** Tests that {@link JFactory#setVarOrder} leaves no stale operator-cache entries behind. */
public class JFactoryReorderTest {
  private static final int VARS = 10;

  private static BDD randomBdd(BDDFactory f, Random r) {
    BDD b = f.zero();
    for (int c = 0; c < 4; c++) {
      BDD cube = f.one();
      for (int v = 0; v < VARS; v++) {
        int k = r.nextInt(3);
        if (k == 0) {
          cube.andWith(f.ithVar(v));
        } else if (k == 1) {
          cube.andWith(f.nithVar(v));
        }
      }
      b.orWith(cube);
    }
    return b;
  }

  /** Applies the same operations before and after a reorder; results must not change. */
  @Test
  public void testOperationsAgreeAcrossReorder() {
    for (int seed = 0; seed < 200; seed++) {
      Random r = new Random(seed);
      BDDFactory f = JFactory.init(500, 100);
      f.setVarNum(VARS);
      List<BDD> inputs = new ArrayList<>();
      for (int i = 0; i < 6; i++) {
        inputs.add(randomBdd(f, r));
      }
      BDD cube = f.ithVar(1).andWith(f.ithVar(4)).andWith(f.ithVar(7));
      List<BDD> before = new ArrayList<>();
      for (int i = 0; i + 1 < inputs.size(); i++) {
        before.add(inputs.get(i).and(inputs.get(i + 1)));
        before.add(inputs.get(i).or(inputs.get(i + 1)));
        before.add(inputs.get(i).relprod(inputs.get(i + 1), cube));
        before.add(inputs.get(i).exist(cube));
      }
      // Free throwaway work so its node slots are reused during the reorder.
      for (int i = 0; i < 20; i++) {
        randomBdd(f, r).andWith(randomBdd(f, r)).free();
      }
      List<Integer> order = new ArrayList<>();
      for (int v = 0; v < VARS; v++) {
        order.add(v);
      }
      Collections.shuffle(order, r);
      f.setVarOrder(order.stream().mapToInt(Integer::intValue).toArray());
      int k = 0;
      for (int i = 0; i + 1 < inputs.size(); i++) {
        assertEquals("seed " + seed, before.get(k++), inputs.get(i).and(inputs.get(i + 1)));
        assertEquals("seed " + seed, before.get(k++), inputs.get(i).or(inputs.get(i + 1)));
        assertEquals(
            "seed " + seed, before.get(k++), inputs.get(i).relprod(inputs.get(i + 1), cube));
        assertEquals("seed " + seed, before.get(k++), inputs.get(i).exist(cube));
      }
    }
  }
}
