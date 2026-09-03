package tools.profiling;

import static org.batfish.main.TestrigText.loadTestrig;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import javax.management.ObjectName;
import org.batfish.common.NetworkSnapshot;
import org.batfish.config.Settings;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FibEntry;
import org.batfish.datamodel.FinalMainRib;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;

/**
 * Parses a snapshot directory and computes its dataplane with no coordinator or client, then
 * reports timing, a digest of the result, and optionally the live heap.
 *
 * <pre>
 * dataplaneRunner SNAPSHOT_DIR [--filter REGEX] [--histogram N] [--retain dataplane|nothing]
 *                              [--dump-prefixes FILE] [--jfr FILE] [--debug-flag FLAG]...
 * </pre>
 *
 * <ul>
 *   <li>{@code --filter REGEX}: only load configs whose file name matches (find, not full match).
 *   <li>{@code --histogram N}: after a full GC, print the heap in use and the top N lines of the
 *       class histogram, taken in-process so nothing has to attach to the JVM.
 *   <li>{@code --retain dataplane}: drop the {@link Batfish} instance before the histogram, leaving
 *       what a server holds to answer questions; {@code --retain nothing} also drops the dataplane.
 *   <li>{@code --dump-prefixes FILE}: write the prefixes of the largest main RIB, one per line, for
 *       use as microbenchmark input.
 *   <li>{@code --jfr FILE}: dump the running flight recording there with reference chains to GC
 *       roots; start the JVM with {@code -XX:StartFlightRecording:settings=profile,
 *       path-to-gc-roots=true} for old-object samples to be in it.
 *   <li>{@code --debug-flag FLAG}: enable a Batfish debug flag, e.g. {@code
 *       enableTopologyContextModifier} as production does; may be repeated.
 * </ul>
 *
 * <p>The RIB and FIB lines give total counts and order-independent hashes, so two builds can be
 * checked for identical results. Exits with {@link Runtime#halt} because Batfish leaves non-daemon
 * threads that would otherwise keep the JVM alive.
 */
public final class DataplaneRunner {
  private DataplaneRunner() {}

  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.err.println(
          "Usage: dataplaneRunner SNAPSHOT_DIR [--filter REGEX] [--histogram N]"
              + " [--retain dataplane|nothing] [--dump-prefixes FILE] [--jfr FILE]"
              + " [--debug-flag FLAG]...");
      System.exit(2);
    }
    String snapshotDir = args[0];
    Pattern filter = null;
    int histoLines = 0;
    String retain = "all";
    Path dumpPrefixes = null;
    Path jfr = null;
    List<String> debugFlags = new ArrayList<>();
    for (int i = 1; i < args.length; i += 2) {
      String value = i + 1 < args.length ? args[i + 1] : null;
      switch (args[i]) {
        case "--filter" -> filter = Pattern.compile(required(args[i], value));
        case "--histogram" -> histoLines = Integer.parseInt(required(args[i], value));
        case "--retain" -> retain = required(args[i], value);
        case "--dump-prefixes" -> dumpPrefixes = Path.of(required(args[i], value));
        case "--jfr" -> jfr = Path.of(required(args[i], value));
        case "--debug-flag" -> debugFlags.add(required(args[i], value));
        default -> throw new IllegalArgumentException("Unknown option " + args[i]);
      }
    }

    Path tmp = Files.createTempDirectory("DataplaneRunner");
    long t0 = System.currentTimeMillis();
    Pattern configFilter = filter;
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            loadTestrig(snapshotDir, f -> configFilter == null || configFilter.matcher(f).find()),
            tmp);
    Settings settings = batfish.getSettings();
    settings.setDisableUnrecognized(false);
    settings.setHaltOnConvertError(false);
    settings.setHaltOnParseError(false);
    settings.setThrowOnLexerError(false);
    settings.setThrowOnParserError(false);
    settings.setDebugFlags(debugFlags);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    SortedMap<String, Configuration> configs = batfish.loadConfigurations(snapshot);
    long t1 = System.currentTimeMillis();
    System.out.printf("PARSE_CONVERT configs=%d ms=%d%n", configs.size(), t1 - t0);
    batfish.computeDataPlane(snapshot);
    long t2 = System.currentTimeMillis();
    System.out.printf("DATAPLANE ms=%d%n", t2 - t1);
    DataPlane dp = batfish.loadDataPlane(snapshot);
    reportDigest(dp);
    if (dumpPrefixes != null) {
      dumpPrefixes(dp, dumpPrefixes);
    }
    if (histoLines > 0) {
      if (!retain.equals("all")) {
        batfish = null;
        configs = null;
      }
      if (retain.equals("nothing")) {
        dp = null;
      }
      histogram(histoLines);
    }
    if (jfr != null) {
      diagnosticCommand("jfrDump", "filename=" + jfr, "path-to-gc-roots=true");
      System.out.println("JFR dumped to " + jfr);
    }
    // The snapshot storage under tmp is several GB and tmp is often a tmpfs, so do not leave it.
    MoreFiles.deleteRecursively(tmp, RecursiveDeleteOption.ALLOW_INSECURE);
    System.out.println("DONE");
    System.out.flush();
    Runtime.getRuntime().halt(0);
  }

  private static String required(String option, @Nullable String value) {
    if (value == null) {
      throw new IllegalArgumentException(option + " needs a value");
    }
    return value;
  }

  private static void reportDigest(DataPlane dp) {
    long ribRoutes = 0;
    long ribHash = 0;
    for (FinalMainRib rib : dp.getRibs().values()) {
      for (AbstractRoute r : rib.getRoutes()) {
        ribRoutes++;
        ribHash += r.hashCode();
      }
    }
    long fibEntries = 0;
    long fibHash = 0;
    for (Map<String, Fib> byVrf : dp.getFibs().values()) {
      for (Fib fib : byVrf.values()) {
        for (FibEntry e : fib.allEntries()) {
          fibEntries++;
          fibHash += e.hashCode();
        }
      }
    }
    System.out.printf(
        "RIB_ROUTES %d hash=%x FIB_ENTRIES %d hash=%x%n", ribRoutes, ribHash, fibEntries, fibHash);
  }

  private static void dumpPrefixes(DataPlane dp, Path file) throws Exception {
    FinalMainRib largest = null;
    for (FinalMainRib rib : dp.getRibs().values()) {
      if (largest == null || rib.getRoutes().size() > largest.getRoutes().size()) {
        largest = rib;
      }
    }
    List<String> lines = new ArrayList<>();
    if (largest != null) {
      for (AbstractRoute r : largest.getRoutes()) {
        lines.add(r.getNetwork().toString());
      }
    }
    Files.write(file, lines);
    System.out.printf("DUMPED %d prefixes (one per route) to %s%n", lines.size(), file);
  }

  private static void histogram(int lines) throws Exception {
    System.gc();
    System.gc();
    Runtime rt = Runtime.getRuntime();
    System.out.printf("HEAP_USED_MB %d%n", (rt.totalMemory() - rt.freeMemory()) >> 20);
    // Three header lines precede the entries.
    diagnosticCommand("gcClassHistogram").lines().limit(lines + 3).forEach(System.out::println);
  }

  private static String diagnosticCommand(String command, String... commandArgs) throws Exception {
    return (String)
        ManagementFactory.getPlatformMBeanServer()
            .invoke(
                new ObjectName("com.sun.management:type=DiagnosticCommand"),
                command,
                new Object[] {commandArgs},
                new String[] {String[].class.getName()});
  }
}
