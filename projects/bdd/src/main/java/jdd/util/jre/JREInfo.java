package jdd.util.jre;

import java.util.Date;
import java.util.Properties;
import jdd.util.JDDConsole;
import jdd.util.math.Digits;

/** print some info about the JRE... */
public class JREInfo {
  public static Runtime rt = Runtime.getRuntime();

  public static long usedMemory() {
    return rt.totalMemory() - rt.freeMemory();
  }

  public static long totalMemory() {
    return rt.totalMemory();
  }

  public static long freeMemory() {
    return rt.freeMemory();
  }

  public static long maxMemory() {
    return rt.maxMemory();
  }

  /** print out some info about the system and JVM etc. */
  public static void show() {
    Properties prop = System.getProperties();

    JDDConsole.out.printf("Using JDD version=%s compiled=%s\n", jdd.Version.VERSION, new Date());

    JDDConsole.out.printf(
        "Java vendor=%s version=%s compiler=%s name=%s\n",
        prop.getProperty("java.vendor"),
        prop.getProperty("java.version"),
        prop.getProperty("java.compiler"),
        prop.getProperty("java.vm.name"));

    JDDConsole.out.printf(
        "OS name=%s arch=%s cpus=%d\n",
        prop.getProperty("os.name"), prop.getProperty("os.arch"), rt.availableProcessors());

    JDDConsole.out.printf(
        "JRE memory: total=%s, reserved=%s\n",
        Digits.prettify1024(rt.maxMemory()), Digits.prettify1024(usedMemory()));
  }

  public static void main(String[] args) {
    show();
  }
}
