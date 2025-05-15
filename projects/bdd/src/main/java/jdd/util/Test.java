package jdd.util;

/** This used to be out own testing framework but now it is just used for runtime assertion */
public class Test {

  private static void fail() {
    Thread.dumpStack();
    System.exit(20);
  }

  public static void check(boolean c) {
    check(c, null);
  }

  public static void check(boolean c, String s) {
    if (!c) {
      if (s != null) System.err.println("ASSERTION FAILED: " + s + "     ");
      fail();
    }
  }

  public static void checkEquality(int a, int b, String s) {

    if (a != b) {
      System.err.print("ASSERTION FAILED: ");
      if (s != null) System.err.print(s + " ");
      System.err.println("" + a + " != " + b + "    ");
      fail();
    }
  }

  public static void checkInequality(int a, int b, String s) {

    if (a == b) {
      System.err.print("ASSERTION FAILED: ");
      if (s != null) System.err.print(s + " ");
      System.err.println("" + a + " == " + b + "    ");
      fail();
    }
  }
}
