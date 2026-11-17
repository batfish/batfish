package org.batfish.tools.junit;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.PostDiscoveryFilter;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.junit.platform.reporting.legacy.xml.LegacyXmlReportGeneratingListener;

/**
 * Runs JUnit Platform tests as a Bazel {@code java_test}.
 *
 * <p>The only argument is a file listing the test classes to run, one fully-qualified name per
 * line. Implements the parts of the Bazel test protocol the build uses: the {@code --test_filter}
 * regex (matched against {@code ClassName#methodName}), the JUnit XML report, and premature-exit
 * detection. Sharding is not supported.
 */
public final class JUnitPlatformRunner {

  public static void main(String[] args) throws IOException {
    Optional<Path> prematureExitFile = envPath("TEST_PREMATURE_EXIT_FILE");
    if (prematureExitFile.isPresent()) {
      Files.write(prematureExitFile.get(), new byte[0]);
    }

    List<String> classNames =
        Files.readAllLines(Path.of(args[0]), StandardCharsets.UTF_8).stream()
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
    LauncherDiscoveryRequestBuilder request =
        LauncherDiscoveryRequestBuilder.request()
            .selectors(classNames.stream().map(c -> selectClass(c)).toList());
    String filter = System.getenv("TESTBRIDGE_TEST_ONLY");
    if (filter != null && !filter.isEmpty()) {
      request.filters(testFilter(Pattern.compile(filter)));
    }

    Path reportsDir =
        Files.createTempDirectory(envPath("TEST_TMPDIR").orElse(Path.of(".")), "junit-reports");
    PrintWriter out = new PrintWriter(System.out, true, StandardCharsets.UTF_8);
    SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();
    Launcher launcher = LauncherFactory.create();
    launcher.execute(
        request.build(), summaryListener, new LegacyXmlReportGeneratingListener(reportsDir, out));

    Optional<Path> xmlOutputFile = envPath("XML_OUTPUT_FILE");
    if (xmlOutputFile.isPresent()) {
      mergeReports(reportsDir, xmlOutputFile.get());
    }

    TestExecutionSummary summary = summaryListener.getSummary();
    summary.printFailuresTo(out, 50);
    summary.printTo(out);
    int exitCode = 0;
    if (summary.getTotalFailureCount() > 0) {
      exitCode = 1;
    } else if (summary.getTestsFoundCount() == 0) {
      out.println("No tests found" + (filter == null ? "" : " matching filter: " + filter));
      exitCode = 1;
    }
    out.flush();

    if (prematureExitFile.isPresent()) {
      Files.deleteIfExists(prematureExitFile.get());
    }
    // Exit explicitly: tests may leave non-daemon threads running.
    System.exit(exitCode);
  }

  /**
   * Keeps tests whose {@code ClassName#methodName} contains a match for {@code pattern}, the same
   * matching Bazel's JUnit 4 runner applies to {@code --test_filter}. Containers are kept; the
   * launcher prunes the ones left without tests.
   */
  private static PostDiscoveryFilter testFilter(Pattern pattern) {
    return descriptor -> {
      Optional<TestSource> source = descriptor.getSource();
      if (source.isPresent() && source.get() instanceof MethodSource method) {
        String name = method.getClassName() + "#" + method.getMethodName();
        return FilterResult.includedIf(pattern.matcher(name).find());
      }
      return FilterResult.included("not a test method");
    };
  }

  /** Writes the per-engine reports in {@code reportsDir} to {@code dest} as one document. */
  private static void mergeReports(Path reportsDir, Path dest) throws IOException {
    List<Path> reports;
    try (Stream<Path> files = Files.list(reportsDir)) {
      reports = files.filter(p -> p.toString().endsWith(".xml")).sorted().toList();
    }
    try (Writer w = Files.newBufferedWriter(dest, StandardCharsets.UTF_8)) {
      w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<testsuites>\n");
      for (Path report : reports) {
        String xml = Files.readString(report, StandardCharsets.UTF_8);
        if (xml.startsWith("<?xml")) {
          xml = xml.substring(xml.indexOf("?>") + 2);
        }
        w.write(xml.strip());
        w.write("\n");
      }
      w.write("</testsuites>\n");
    }
  }

  private static Optional<Path> envPath(String name) {
    String value = System.getenv(name);
    return value == null || value.isEmpty() ? Optional.empty() : Optional.of(Path.of(value));
  }

  private JUnitPlatformRunner() {}
}
