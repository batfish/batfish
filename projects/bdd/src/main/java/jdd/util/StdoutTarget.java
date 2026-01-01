package jdd.util;

/** this target send everything to stdout (System.out) */
public class StdoutTarget implements PrintTarget {
  public void printf(String format, Object... args) {
    System.out.printf(format, args);
  }

  // XXX: this will be removed
  public void println(String s) {
    System.out.println(s);
  }

  // XXX: this will be removed
  public void print(String s) {
    System.out.print(s);
  }

  public void print(char c) {
    System.out.print(c);
  }

  public void flush() {
    System.out.flush();
  }
}
