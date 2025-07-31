package jdd.examples;

import jdd.util.Configuration;
import jdd.util.JDDConsole;

/**
 * This examples shows how the configuration of the BDD package can affect the running-time
 * performance of an application.
 *
 * <p>We will time the Adder example under different configurations. The result will hopefully
 * convince you that for good performance, you must tune the BDD package for the specific type of
 * problem you are working with.
 *
 * @see jdd.util.Configuration
 * @see Adder
 */
public class ConfigExample {
  /** the size of our Adder */
  private static final int N = 256;

  /** build the adder once and print its memory and time consumption */
  private static void test() {
    long time = System.currentTimeMillis();
    Adder adder = new Adder(N);
    long memory = (long) (adder.getMemoryUsage() / 1024);
    time = System.currentTimeMillis() - time;

    // REMOVE this line if you getting too much information :)
    adder.showStats();

    adder.cleanup();

    JDDConsole.out.println("**** TIME = " + time + "ms , MEMORY = " + memory + "KB ****\n");
  }

  public static void main(String[] args) {

    JDDConsole.out.println("ConfigExample.java:");
    JDDConsole.out.println("We will now profile Adder(" + N + ") under different configurations");

    // NOTE:
    // every time you change something, make sure to change it back when you are done!

    JDDConsole.out.println("\nDefault configuration");
    test();

    JDDConsole.out.println("\nSmaller OP cache");
    Configuration.bddOpcacheDiv = 8;
    test();
    Configuration.bddOpcacheDiv = Configuration.DEFAULT_BDD_OPCACHE_DIV;

    JDDConsole.out.println("\nToo small OP cache");
    Configuration.bddOpcacheDiv = 1000;
    test();
    Configuration.bddOpcacheDiv = Configuration.DEFAULT_BDD_OPCACHE_DIV;

    JDDConsole.out.println("\nFaster nodetable grow:");
    Configuration.nodetableGrowMin = Configuration.nodetableGrowMax = 500000;
    test();
    Configuration.nodetableGrowMin = Configuration.DEFAULT_NODETABLE_GROW_MIN;
    Configuration.nodetableGrowMax = Configuration.DEFAULT_NODETABLE_GROW_MAX;

    JDDConsole.out.println("\nComputation caches are NOT allowed to grow:");
    Configuration.maxSimplecacheGrows = 0;
    test();
    Configuration.maxSimplecacheGrows = Configuration.DEFAULT_MAX_SIMPLECACHE_GROWS;

    JDDConsole.out.println(
        "\nComputation caches are allowed to grow, but only under very high hitrate:");
    Configuration.minSimplecacheHitrateToGrow = 85;
    test();
    Configuration.minSimplecacheHitrateToGrow =
        Configuration.DEFAULT_MIN_SIMPLECACHE_HITRATE_TO_GROW;

    // we are done
    JDDConsole.out.println("\n\nThe results wasn't what you were expecting huh?");
    JDDConsole.out.println("Hope this example has learned you the importance of BDD tuning!");
    JDDConsole.out.printf("\n");
  }
}
