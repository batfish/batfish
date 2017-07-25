package org.batfish.common;

import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;

public class CompositePrintStream extends PrintStream {

  private final PrintStream _ps1;

  private final PrintStream _ps2;

  public CompositePrintStream(PrintStream ps1, PrintStream ps2) {
    super(ByteStreams.nullOutputStream());
    _ps1 = ps1;
    _ps2 = ps2;
  }

  @Override
  public PrintStream append(char c) {
    _ps1.append(c);
    _ps2.append(c);
    return this;
  }

  @Override
  public PrintStream append(CharSequence arg0) {
    _ps1.append(arg0);
    _ps2.append(arg0);
    return this;
  }

  @Override
  public PrintStream append(CharSequence arg0, int arg1, int arg2) {
    _ps1.append(arg0, arg1, arg2);
    _ps2.append(arg0, arg1, arg2);
    return this;
  }

  @Override
  public void close() {
    _ps1.close();
    _ps2.close();
  }

  @Override
  public void flush() {
    _ps1.flush();
    _ps2.flush();
  }

  @Override
  public PrintStream format(Locale l, String format, Object... args) {
    _ps1.format(l, format, args);
    _ps2.format(l, format, args);
    return this;
  }

  @Override
  public PrintStream format(String format, Object... args) {
    _ps1.format(format, args);
    _ps2.format(format, args);
    return this;
  }

  @Override
  public void print(boolean b) {
    _ps1.print(b);
    _ps2.print(b);
  }

  @Override
  public void print(char c) {
    _ps1.print(c);
    _ps2.print(c);
  }

  @Override
  public void print(char[] s) {
    _ps1.print(s);
    _ps2.print(s);
  }

  @Override
  public void print(double d) {
    _ps1.print(d);
    _ps2.print(d);
  }

  @Override
  public void print(float f) {
    _ps1.print(f);
    _ps2.print(f);
  }

  @Override
  public void print(int i) {
    _ps1.print(i);
    _ps2.print(i);
  }

  @Override
  public void print(long l) {
    _ps1.print(l);
    _ps2.print(l);
  }

  @Override
  public void print(Object obj) {
    _ps1.print(obj);
    _ps2.print(obj);
  }

  @Override
  public void print(String s) {
    _ps1.print(s);
    _ps2.print(s);
  }

  @Override
  public PrintStream printf(Locale l, String format, Object... args) {
    _ps1.printf(l, format, args);
    _ps2.printf(l, format, args);
    return this;
  }

  @Override
  public PrintStream printf(String format, Object... args) {
    _ps1.printf(format, args);
    _ps2.printf(format, args);
    return this;
  }

  @Override
  public void println() {
    _ps1.println();
    _ps2.println();
  }

  @Override
  public void println(boolean x) {
    _ps1.println(x);
    _ps2.println(x);
  }

  @Override
  public void println(char x) {
    _ps1.println(x);
    _ps2.println(x);
  }

  @Override
  public void println(char[] x) {
    _ps1.println(x);
    _ps2.println(x);
  }

  @Override
  public void println(double x) {
    _ps1.println(x);
    _ps2.println(x);
  }

  @Override
  public void println(float x) {
    _ps1.println(x);
    _ps2.println(x);
  }

  @Override
  public void println(int x) {
    _ps1.println(x);
    _ps2.println(x);
  }

  @Override
  public void println(long x) {
    _ps1.println(x);
    _ps2.println(x);
  }

  @Override
  public void println(Object x) {
    _ps1.println(x);
    _ps2.println(x);
  }

  @Override
  public void println(String x) {
    _ps1.println(x);
    _ps2.println(x);
  }

  @Override
  public void write(byte[] b) throws IOException {
    _ps1.write(b);
    _ps2.write(b);
  }

  @Override
  public void write(byte[] buf, int off, int len) {
    _ps1.write(buf, off, len);
    _ps2.write(buf, off, len);
  }

  @Override
  public void write(int b) {
    _ps1.write(b);
    _ps2.write(b);
  }
}
