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
package net.sf.javabdd.trace;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 *
 * <pre>
 * This class opens and runs a suite of BDD traces from a zip file.
 * A net.sf.javabdd.trace file is assumed to have the '.net.sf.javabdd.trace' suffix. Any file with the 'README' posfix
 * is assumed to be an information file and is dumped to stdout.
 * </pre>
 *
 * Based on the version in JDD.
 *
 * @version $Id: TraceSuite.java,v 1.1 2004/11/17 18:38:38 joewhaley Exp $
 */
public class TraceSuite {

  /**
   * filename is the name of zip file or directory, initial_size is the suggested "base" for
   * deciding the initial of the nodetable. if initial_size is -1, it is ignored and TraceDriver's
   * default value is used
   */
  public TraceSuite(String filename, int initial_size) {
    File dir = new File(filename);
    if (dir.isDirectory()) {
      runDirectory(dir, initial_size);
    } else {
      runZip(filename, initial_size);
    }
  }

  public void runDirectory(File dir, int initial_size) {
    File[] files = dir.listFiles();
    if (files == null) {
      return;
    }
    for (int i = 0; i < files.length; ++i) {
      if (files[i].isDirectory()) {
        runDirectory(files[i], initial_size);
      } else {
        try {
          String name = files[i].getName();
          InputStream is = new BufferedInputStream(new FileInputStream(files[i]));
          if (name.endsWith(".net.sf.javabdd.trace")) {
            runTrace(name, is, initial_size);
          } else if (name.endsWith("README")) {
            showFile(name, is);
          }
        } catch (IOException exx) {
          System.out.println("FAILED: " + exx.getMessage() + "\n");
          exx.printStackTrace();
          System.exit(20);
        }
      }
    }
  }

  public void runZip(String filename, int initial_size) {
    try {
      InputStream is = new FileInputStream(filename);
      ZipInputStream zis = new ZipInputStream(is);

      System.out.println("\n***** [ " + filename + " ] *****");
      // JREInfo.show();

      ZipEntry ze = zis.getNextEntry();
      while (zis.available() != 0) {

        String name = ze.getName();

        if (name.endsWith(".net.sf.javabdd.trace")) {
          runTrace(name, zis, initial_size);
        } else if (name.endsWith("README")) {
          showFile(name, zis);
        }

        zis.closeEntry();
        ze = zis.getNextEntry();
      }
      zis.close();
      is.close();
    } catch (IOException exx) {
      System.out.println("FAILED: " + exx.getMessage() + "\n");
      exx.printStackTrace();
      System.exit(20);
    }
  }

  private void runTrace(String name, InputStream is, int size) {
    // enable verbose temporary!
    boolean save = TraceDriver.verbose;
    TraceDriver.verbose = true;

    System.err.println("Tracing " + name + "...");
    try {
      if (size == -1) {
        new TraceDriver(name, is);
      } else {
        new TraceDriver(name, is, size);
      }
    } catch (Exception exx) {
      System.out.println("FAILED: " + exx.getMessage() + "\n\n");
      exx.printStackTrace();
    }

    TraceDriver.verbose = save; // set back verbose to its old value

    // let's cleanup, so we dont affect the next run so much:
    for (int i = 0; i < 6; i++) {
      System.gc();
    }

    try {
      Thread.sleep(5000);
    } catch (Exception ignored) {
    } // calm down!
  }

  private void showFile(String name, InputStream is) throws IOException {
    System.out.println("File " + name);
    byte[] buffer = new byte[10240];

    for (; ; ) {
      int i = is.read(buffer, 0, buffer.length);
      if (i <= 0) {
        return;
      }
      System.out.println(new String(buffer, 0, i));
    }
  }

  // -------------------------------------------------------------

  public static void main(String[] args) {
    if (args.length == 1) {
      new TraceSuite(args[0], -1);
    } else if (args.length == 2) {
      new TraceSuite(args[0], Integer.parseInt(args[1]));
    } else {
      System.err.println(
          "Usage: java "
              + TraceSuite.class.getName()
              + " <net.sf.javabdd.trace-suite.zip> [initial size _base_]");
    }
  }
}
