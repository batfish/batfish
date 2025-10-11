package jdd.util.jre;

import java.util.Random;
import jdd.util.JDDConsole;
import jdd.util.math.FastRandom;

/** time some operations and checks if JRE implementation is faster the plain java code */
public class JRETest {
  public static void copy1(int[] x, int[] y) {
    final int len = x.length;
    for (int i = 0; i < len; i++) x[i] = y[i];
  }

  public static void copy2(int[] x, int[] y) {
    System.arraycopy(y, 0, x, 0, x.length);
  }

  public static void copy3(int[] x, int[] y) {
    int o = 0;
    for (int i = x.length / 4; i != 0; i--) {
      x[o + 0] = y[o + 0];
      x[o + 1] = y[o + 1];
      x[o + 2] = y[o + 2];
      x[o + 3] = y[o + 3];
      o += 4;
    }

    for (; o < x.length; o++) {
      x[o] = y[o];
    }
  }

  // -------------------------------------------------------
  public static void main(String args[]) {
    final int SIZE = 10240;
    final int ROUNDS = 9876;
    int[] buffer1 = new int[SIZE];
    int[] buffer2 = new int[SIZE];
    long t1 = 0, t2 = 0, t3 = 0;

    JREInfo.show();

    // TEST COPY CODE
    for (int w = 0; w < 2; w++) { // warmup
      t1 = System.currentTimeMillis();
      for (int i = 0; i < ROUNDS; i++) copy1(buffer1, buffer2);
      t1 = System.currentTimeMillis() - t1;

      t2 = System.currentTimeMillis();
      for (int i = 0; i < ROUNDS; i++) copy2(buffer1, buffer2);
      t2 = System.currentTimeMillis() - t2;

      t3 = System.currentTimeMillis();
      for (int i = 0; i < ROUNDS; i++) copy3(buffer1, buffer2);
      t3 = System.currentTimeMillis() - t3;
    }

    System.out.printf(
        "COPY: Java code is %s than System.arraycopy() [%d vs %d]\n",
        t1 < t2 ? "faster" : "slower", t1, t2);

    // PRNG speed test:
    int y, MAX = 10000; // we just need some number
    Random rnd = new Random();
    for (int w = 0; w < 2; w++) { // warmup
      t1 = System.currentTimeMillis();
      for (int i = 0; i < 1000000; i++) {
        y = FastRandom.mtrand() % MAX;
        y = FastRandom.mtrand() % MAX;
        y = FastRandom.mtrand() % MAX;
        y = FastRandom.mtrand() % MAX;
        y = FastRandom.mtrand() % MAX;
        y = FastRandom.mtrand() % MAX;
      }
      t1 = System.currentTimeMillis() - t1;

      t2 = System.currentTimeMillis();
      for (int i = 0; i < 1000000; i++) {
        y = rnd.nextInt(MAX);
        y = rnd.nextInt(MAX);
        y = rnd.nextInt(MAX);
        y = rnd.nextInt(MAX);
        y = rnd.nextInt(MAX);
        y = rnd.nextInt(MAX);
      }
      t2 = System.currentTimeMillis() - t2;
    }

    JDDConsole.out.printf(
        "LPRNG: FastRandom.mtrand() is %s than Random [%d vs %d]\n",
        t1 < t2 ? "faster" : "slower", t1, t2);
  }
}
