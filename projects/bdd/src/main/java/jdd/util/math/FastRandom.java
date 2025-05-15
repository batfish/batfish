package jdd.util.math;

/**
 * A class for fast random number generation.
 *
 * <p>Currently, we only have an implementation of the Mersenne Twister PRNG.
 */
public class FastRandom {

  static {
    mt_mt = new int[624]; // thats MT_N, not initialized yet!
    mt_mag01 = new int[2];
    mtseed((int) (1 + 0x7ffffffe * Math.random()));
  }

  // ---- [ The Mersenne Twister random number generator ] --------------------------------
  // MT constancts:
  private static final int MT_N = 624;
  private static final int MT_M = 397;
  private static final int MT_A = 0x9908bd0f;

  /** Mersenne Twister matrix A */
  private static final int MT_B = 0x9d2c5680;

  /** Mersenne Twister tampering mask B */
  private static final int MT_C = 0xefc60000;

  /** Mersenne Twister tampering mask C */
  private static final int MT_MAKS_UPPER = 0x80000000;

  private static final int MT_MASK_LOWER = 0x7fffffff;

  private static final int MT_SHIFT_U(int y) {
    return y >>> 11;
  }

  private static final int MT_SHIFT_S(int y) {
    return y << 7;
  }

  private static final int MT_SHIFT_T(int y) {
    return y << 15;
  }

  private static final int MT_SHIFT_L(int y) {
    return y >>> 18;
  }

  private static int[] mt_mt;
  private static int[] mt_mag01;

  private static int mt_mti;

  /** seed the Mersenne Twister PRNG */
  public static final void mtseed(int n) {
    mt_mt[0] = n;
    for (mt_mti = 1; mt_mti < MT_N; mt_mti++)
      // WAS: mt_mt[mt_mti] = (69069 * mt_mt[mt_mti-1]);
      mt_mt[mt_mti] = (1812433253 * (mt_mt[mt_mti - 1] ^ (mt_mt[mt_mti - 1] >>> 30)) + mt_mti);
  }

  /**
   * The Mersenne Twister PRNG, based on the following paper:
   *
   * <p>Makato Matsumoto and Takuji Nishimura, "Mersenne Twister: A 623-Dimensionally
   * Equidistributed Uniform Pseudo-Random Number Generator", in ACM Transactions on Modeling and
   * Computer Simulation
   */
  public static final int mtrand() {
    int y;

    if (mt_mti >= MT_N) {
      int kk = 0;
      mt_mag01[0] = 0;
      mt_mag01[1] = MT_A;

      for (; kk < MT_N - MT_M; kk++) {
        y = (mt_mt[kk] & MT_MAKS_UPPER) | (mt_mt[kk + 1] & MT_MASK_LOWER);
        mt_mt[kk] = mt_mt[kk + MT_M] ^ (y >>> 1) ^ mt_mag01[y & 1];
      }

      for (; kk < MT_N - 1; kk++) {
        y = (mt_mt[kk] & MT_MAKS_UPPER) | (mt_mt[kk + 1] & MT_MASK_LOWER);
        mt_mt[kk] = mt_mt[kk + (MT_M - MT_N)] ^ (y >>> 1) ^ mt_mag01[y & 1];
      }

      y = (mt_mt[MT_N - 1] & MT_MAKS_UPPER) | (mt_mt[0] & MT_MASK_LOWER);
      mt_mt[MT_N - 1] = mt_mt[MT_M - 1] ^ (y >>> 1) ^ mt_mag01[y & 1];

      mt_mti = 0;
    }

    y = mt_mt[mt_mti++];
    y ^= MT_SHIFT_U(y);
    y ^= MT_SHIFT_S(y) & MT_B;
    y ^= MT_SHIFT_T(y) & MT_C;
    y ^= MT_SHIFT_L(y);

    // XXX: this is possibly a problem, but we dont want out PRNG to return -3, do we?
    return y & 0x7FFFFFFF;
  }
}
