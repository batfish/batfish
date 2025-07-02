package jdd.examples;

import jdd.util.Array;
import jdd.util.JDDConsole;
import jdd.util.Options;
import jdd.util.math.Digits;
import jdd.zdd.ZDD2;

/**
 * N Queen with Z-BDDs. Check out Minatos unate cube paper [ cant remember the name of it :( ] for
 * an explanation...
 */
public class ZDDQueens extends ZDD2 implements Queens {
  private int n, sols;
  private int[] pos_x, pos_xv;

  private int get(int i, int j) {
    return pos_x[i + j * n];
  }

  private int getVar(int i, int j) {
    return pos_xv[i + j * n];
  }

  private boolean[] solvec;
  private long time, memory;

  public ZDDQueens(int n) {
    super(1 + Math.max(1000, (int) (Math.pow(3.5, n - 6)) * 1000), 10000);

    time = System.currentTimeMillis();
    this.n = n;
    pos_x = new int[n * n];
    pos_xv = new int[n * n];
    boolean[] mark = new boolean[n * n];
    for (int i = 0; i < n * n; i++) {
      pos_xv[i] = createVar();
      pos_x[i] = ref(change(1, pos_xv[i]));
    }

    // compute S1
    int S1 = 0;
    for (int i = 0; i < n; i++) S1 = unionWith(S1, get(0, i));

    // compute the rest
    int last_S = S1;
    for (int i = 1; i < n; i++) {
      int new_S = 0;
      for (int j = 0; j < n; j++) {
        int bld = build(i, j, last_S, mark);
        new_S = unionWith(new_S, bld);
        deref(bld);
      }
      deref(last_S);
      last_S = new_S;
    }

    solvec = satOne(last_S, null);

    sols = count(last_S);
    deref(last_S);
    time = System.currentTimeMillis() - time;
    if (Options.verbose) showStats();

    memory = getMemoryUsage();
    cleanup();
  }

  // --- [Queens interface ] ---------------------------------------------
  public int getN() {
    return n;
  }

  public double numberOfSolutions() {
    return sols;
  }

  public long getTime() {
    return time;
  }

  public long getMemory() {
    return memory;
  }

  public boolean[] getOneSolution() {
    return solvec;
  }

  // --- [ internal stuff ] --------------------------------------------------
  private boolean valid(int a, int b) {
    return (a >= 0 && a < n) && (b >= 0 && b < n);
  }

  private int build(int i, int j, int S, boolean[] mark) {
    ref(S); // our copy

    Array.set(mark, false);

    for (int k = 0; k < i; k++) mark[k + n * j] = true;
    for (int k = 1; k <= i; k++) {
      int a = j - k, b = i - k;
      if (valid(b, a)) mark[b + n * a] = true;
      a = j + k;
      if (valid(b, a)) mark[b + n * a] = true;
    }

    for (int k = 0; k < n * n; k++) {
      if (mark[k]) {
        int a = k / n, b = k % n;
        S = offsetWith(S, getVar(b, a));
      }
    }
    int ret = ref(mul(S, get(i, j)));
    deref(S);
    return ret;
  }

  // -------------------------------------------------------------
  private int unionWith(int a, int b) {
    int tmp = ref(union(a, b));
    deref(a);
    return tmp;
  }

  private int offsetWith(int a, int b) {
    int tmp = ref(subset0(a, b));
    deref(a);
    return tmp;
  }

  public static void main(String[] args) {
    for (String str : args) {
      final int n = Integer.parseInt(str);

      ZDDQueens q = new ZDDQueens(n);
      JDDConsole.out.printf(
          "ZDD-Queen\tSolutions=%.0f\tN=%d\tmem=%s\ttime=%d\n",
          q.numberOfSolutions(), n, Digits.prettify1024((long) q.getMemory()), q.getTime());
    }
  }
}
