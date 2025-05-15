package jdd.util.math;

import jdd.util.Array;

/**
 * \chi^2 random distribution test.
 *
 * <p> This class is used to test the distribution of series
 * of numbers such hashes and (pseudo) random numbers.
 *
 *
 * <p>
 * assume that you want to test a function f() which returns a number between 0 and N-1:
 * <pre>
 * Chi2Test  c2t = new Chi2Test(N);
 *
 * while(c2t.more()) c2t.add( f() );
 *
 * if(c2t.isChi2Acceptable())
 *    System.out.println("The distribution of f() is random enough for me!");
 * <pre>
 */
public class Chi2Test {

  private int n, samples_needed, samples_have;
  private int[] distibution;
  private boolean has_chi2; // have we computed the values?
  private double the_chi2, the_stddev; // when computed, the values are stored here

  /**
   * start a chi^2 for the input numbers 0..n-1
   *
   * <p><tt>n</tt> must be larger than 20. don't make it too large unless you have enough memory for
   * it.
   *
   * <p>Also, if <tt>n</tt> is too small (say bellow 1000), then you might get many false answers so
   * instead consider the majority of multiple runs.
   */
  public Chi2Test(int n) {
    // Test.check(n > 20, "n to small");
    this.n = n;
    this.samples_needed = 25 * n + 3; // Knuth said something about 5*n, but what does he knows?
    this.distibution = new int[n];

    reset();
  }

  /** reset the chi^2, start all over */
  public void reset() {
    samples_have = 0;
    Array.set(distibution, 0);
    has_chi2 = false;
  }

  /**
   * returns true if it has enough samples to give an accurate answer.
   *
   * <p>NOTE: most other functions here cannot be called before this functions starts returning
   * <tt>false</tt>es!
   */
  public boolean more() {
    return samples_have < samples_needed;
  }

  /**
   * add a new number. you now you have fed it enough numbers when more() returns false.
   *
   * @see #more
   */
  public void add(int x) {
    distibution[x]++;
    samples_have++;
    has_chi2 = false; // the old chi^2 is no longer valid
  }

  /** get the chi2 value. do not call before more() has returned true! */
  public double getChi2() {
    if (!has_chi2) computeChi2();
    return the_chi2;
  }

  /** get the standard deviation. do not call before more() has returned true! */
  public double getStandardDeviation() {
    if (!has_chi2) computeChi2(); // std-dev is computed in the same function as chi^2
    return the_stddev;
  }

  /** chi^2 is computed here */
  private void computeChi2() {
    has_chi2 = true;

    /*
    // get the repeat distribution
    int b_i [] = new int[n];
    Array.set(b_i, 0);
    for(int i = 0; i < n; i++) b_i[ distibution[i] ]++;

    double p = (double) samples_have / n;

    // compute chi2:
    the_chi2 = 0;
    for(int i = 0; i < n; i++) the_chi2 += (double)(b_i[i] * (i-p) * (i-p));
    the_chi2 /= p;

    the_stddev = (the_chi2 - n) / Math.sqrt(n);
    */

    double p = (double) samples_have / n;

    // compute chi2:
    the_chi2 = 0;
    for (int i = 0; i < n; i++) {
      double t = distibution[i] - p;
      the_chi2 += t * t;
    }

    the_chi2 /= p;
    the_stddev = (the_chi2 - n) / Math.sqrt(n);
  }

  // ------------------------------------------------------------------
  /**
   * "Acceptable" does not mean good. For example, for hash functions, acceptable means very good.
   * So don't take it as a hard limit, it sometimes fails so do multiple runs,
   *
   * @see #isStandardDeviationAcceptable
   * @see #getChi2
   * @return true if the current chi2 value is acceptable
   */
  public boolean isChi2Acceptable() {
    double c2 = getChi2();
    return Math.abs(c2 - n) < (3.5 * Math.sqrt(n)); // should really be 3.0
  }

  /**
   * "Acceptable" does not mean good. For example, for hash functions, acceptable means very good.
   * So don't take it as a hard limit, it sometimes fails so do multiple runs,
   *
   * @see #isChi2Acceptable
   * @see #getStandardDeviation
   * @return true if the current standard deviation is acceptable.
   */
  public boolean isStandardDeviationAcceptable() {
    double stddev = getStandardDeviation();
    return Math.abs(stddev) < 3.5; // should actually be 3.0
  }

  /** Get the distribution vector */
  public int[] getDistibution() {
    return distibution;
  }

  // ------------------------------------------------------------------

  public static void main(String[] args) {
    // THIS IS NOT A TEST!
    // we _should_ have had a testbed here, but the developer decided to distribute her time
    // very non-uniformly on other things like watching TV :)

    // this is just to see how good the Java random number generators are.
    // you will soon see that they truly suck :(

    int max = Prime.nextPrime(1000);

    // check Math.random()
    Chi2Test c2t = new Chi2Test(max);
    while (c2t.more()) c2t.add((int) (Math.random() * max));
    System.out.println("testing Math.random() * " + max);
    System.out.println("chi2 ==> " + c2t.getChi2());
    System.out.println("stddev==> " + c2t.getStandardDeviation());

    // check java.util.Random.nextInt()
    java.util.Random rnd = new java.util.Random();
    c2t.reset();
    while (c2t.more()) c2t.add(rnd.nextInt(max));
    System.out.println("\nesting java.util.Random.nextInt(" + max + ")");
    System.out.println("chi2 ==> " + c2t.getChi2());
    System.out.println("stddev==> " + c2t.getStandardDeviation());
  }
}
