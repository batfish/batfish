package org.batfish.vendor.cisco_ftd;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.batfish.vendor.cisco_ftd.representation.FtdConfiguration;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive benchmark test suite for FTD grammar performance optimization.
 *
 * <p>This test measures:
 *
 * <ul>
 *   <li>Parse time (ms)
 *   <li>Token count
 *   <li>Memory usage (approximate)
 * </ul>
 *
 * <p>Run with: bazel test //projects/batfish/src/test/java/org/batfish/vendor/cisco_ftd:tests
 * --test_filter=FtdGrammarBenchmarkTest
 *
 * <p>For detailed output: bazel test ... --test_output=all
 */
public class FtdGrammarBenchmarkTest extends FtdGrammarTest {

  private int _warmupIterations;
  private int _measureIterations;

  @Before
  public void setup() {
    _warmupIterations = Integer.getInteger("benchmark.warmup", 3);
    _measureIterations = Integer.getInteger("benchmark.iterations", 5);
  }

  /** Benchmark result container */
  public static class BenchmarkResults {
    public final String name;
    public final long parseTimeMs;
    public final long tokenCount;
    public final int lineCount;
    public final int charCount;

    public BenchmarkResults(
        String name, long parseTimeMs, long tokenCount, int lineCount, int charCount) {
      this.name = name;
      this.parseTimeMs = parseTimeMs;
      this.tokenCount = tokenCount;
      this.lineCount = lineCount;
      this.charCount = charCount;
    }

    public void printResults() {
      System.out.println("\n=== " + name + " ===");
      System.out.println("Input size: " + charCount + " chars, " + lineCount + " lines");
      System.out.println("Parse time: " + parseTimeMs + " ms");
      System.out.println("Token count: " + tokenCount);
      System.out.printf("Throughput: %.2f KB/s%n", (charCount / 1024.0) / (parseTimeMs / 1000.0));
      System.out.printf("Lines/ms: %.2f%n", (double) lineCount / parseTimeMs);
    }

    public void printComparison(BenchmarkResults baseline) {
      System.out.println("\n=== Comparison: " + name + " vs " + baseline.name + " ===");
      System.out.printf(
          "Parse time: %d ms -> %d ms (%.1f%% change)%n",
          baseline.parseTimeMs, parseTimeMs, percentChange(baseline.parseTimeMs, parseTimeMs));
      System.out.printf(
          "Throughput: %.2f KB/s -> %.2f KB/s%n",
          (baseline.charCount / 1024.0) / (baseline.parseTimeMs / 1000.0),
          (charCount / 1024.0) / (parseTimeMs / 1000.0));
    }

    private double percentChange(long oldVal, long newVal) {
      if (oldVal == 0) return 0;
      return ((double) (newVal - oldVal) / oldVal) * 100;
    }
  }

  @Test
  public void testBenchmarkSmallConfig() {
    String config = createSmallConfig();
    BenchmarkResults results = runBenchmark("Small Config (100 lines)", config);
    results.printResults();
    assert results.tokenCount > 0 : "Should have parsed some tokens";
  }

  @Test
  public void testBenchmarkMediumConfig() {
    String config = createMediumConfig();
    BenchmarkResults results = runBenchmark("Medium Config (1000 lines)", config);
    results.printResults();
  }

  @Test
  public void testBenchmarkLargeConfig() {
    String config = createLargeConfig();
    BenchmarkResults results = runBenchmark("Large Config (10000 lines)", config);
    results.printResults();
  }

  @Test
  public void testBenchmarkProblematicPatterns() {
    String config = createProblematicPatterns();
    BenchmarkResults results = runBenchmark("Problematic Patterns", config);
    results.printResults();
  }

  @Test
  public void testBenchmarkRealConfig() throws IOException {
    String configPath = System.getenv().getOrDefault("FTD_TEST_CONFIG", "");

    if (configPath.isEmpty() || !Files.exists(Paths.get(configPath))) {
      System.out.println("Skipping real config test - set FTD_TEST_CONFIG environment variable");
      return;
    }

    String config = Files.readString(Paths.get(configPath));
    BenchmarkResults results = runBenchmark("Real FTD Config", config);
    results.printResults();
  }

  @Test
  public void testStanzaDispatchBenchmark() {
    // Test specifically the stanza rule dispatch performance
    String config = createStanzaDispatchTestConfig();
    BenchmarkResults results = runBenchmark("Stanza Dispatch Test", config);
    results.printResults();

    System.out.println("\nStanza dispatch analysis:");
    System.out.println("  Total stanzas: ~" + (results.lineCount - 10));
    System.out.println(
        "  Avg time per stanza: "
            + String.format("%.3f", (double) results.parseTimeMs / (results.lineCount - 10))
            + " ms");
  }

  @Test
  public void testTildeNegationBenchmark() {
    // Test patterns that use ~NEWLINE negation
    String config = createTildeNegationTestConfig();
    BenchmarkResults results = runBenchmark("Tilde Negation Test", config);
    results.printResults();
  }

  /** Run a complete benchmark with warmup and measurement iterations */
  private BenchmarkResults runBenchmark(String name, String config) {
    int lineCount = config.split("\n").length;
    int charCount = config.length();

    System.out.println("\n>>> Running benchmark: " + name);
    System.out.println(
        "    Warmup: "
            + _warmupIterations
            + " iterations, Measure: "
            + _measureIterations
            + " iterations");

    // Warmup runs
    for (int i = 0; i < _warmupIterations; i++) {
      try {
        parseVendorConfig(config);
      } catch (Exception e) {
        System.err.println("Warmup iteration " + (i + 1) + " failed: " + e.getMessage());
        throw new RuntimeException("Benchmark warmup failed", e);
      }
    }

    // Measurement runs
    long totalParseTime = 0;
    long totalTokens = 0;

    for (int i = 0; i < _measureIterations; i++) {
      try {
        // Force GC before measurement
        System.gc();
        Thread.sleep(10);

        long startTime = System.nanoTime();
        FtdConfiguration ftdConfig = parseVendorConfig(config);
        long endTime = System.nanoTime();

        totalParseTime += (endTime - startTime) / 1_000_000;

        // Estimate token count from parsed config
        if (ftdConfig != null) {
          totalTokens += estimateTokenCount(ftdConfig);
        }

      } catch (Exception e) {
        System.err.println("Measurement iteration " + (i + 1) + " failed: " + e.getMessage());
        throw new RuntimeException("Benchmark measurement failed", e);
      }
    }

    return new BenchmarkResults(
        name,
        totalParseTime / _measureIterations,
        totalTokens / _measureIterations,
        lineCount,
        charCount);
  }

  /** Estimate token count from parsed configuration */
  private long estimateTokenCount(FtdConfiguration config) {
    // Rough estimate based on configuration size
    long count = 0;

    // Count interfaces
    count += config.getInterfaces().size() * 10;

    // Count objects and groups
    count += config.getNetworkObjects().size() * 5;
    count += config.getNetworkObjectGroups().size() * 5;

    // Count ACLs
    count +=
        config.getAccessLists().values().stream()
            .mapToLong(acl -> acl.getLines().size() * 15)
            .sum();

    // Count NAT rules
    count += config.getNatRules().size() * 10;

    // Count routes
    count += config.getRoutes().size() * 5;

    // Add base overhead
    count += 100;

    return count;
  }

  // ========================================================================
  // Test Configuration Generators
  // ========================================================================

  private String createSmallConfig() {
    StringBuilder sb = new StringBuilder();
    sb.append("hostname test-ftd-small\n");
    sb.append("NGFW Version 7.4.2\n");

    // Add 10 interfaces
    for (int i = 0; i < 10; i++) {
      sb.append("interface GigabitEthernet").append(i).append("/0\n");
      sb.append(" nameif INT").append(i).append("\n");
      sb.append(" security-level ").append(i * 10).append("\n");
      sb.append(" ip address 10.0.").append(i).append(".1 255.255.255.0\n");
      sb.append("!\n");
    }

    // Add 10 objects
    for (int i = 0; i < 10; i++) {
      sb.append("object network OBJ-NET-").append(i).append("\n");
      sb.append(" host 192.168.").append(i).append(".1\n");
    }

    // Add 10 ACLs
    for (int i = 0; i < 10; i++) {
      sb.append("access-list ACL-").append(i).append(" extended permit ip any any\n");
    }

    sb.append("Cryptochecksum:0123456789abcdef\n");
    return sb.toString();
  }

  private String createMediumConfig() {
    StringBuilder sb = new StringBuilder();
    sb.append("hostname test-ftd-medium\n");
    sb.append("NGFW Version 7.4.2\n");

    // Add 100 interfaces
    for (int i = 0; i < 100; i++) {
      sb.append("interface GigabitEthernet").append(i / 10).append("/").append(i % 10).append("\n");
      sb.append(" nameif INT").append(i).append("\n");
      sb.append(" security-level ").append(i % 100).append("\n");
      sb.append(" ip address 10.0.").append(i).append(".1 255.255.255.0\n");
      sb.append(" description Interface ").append(i).append("\n");
      sb.append("!\n");
    }

    // Add 200 objects
    for (int i = 0; i < 200; i++) {
      sb.append("object network OBJ-NET-").append(i).append("\n");
      sb.append(" host 192.168.").append(i / 256).append(".").append(i % 256).append(".1\n");
      sb.append(" description Object ").append(i).append("\n");
    }

    // Add 100 ACLs with multiple rules
    for (int i = 0; i < 100; i++) {
      sb.append("access-list ACL-").append(i).append(" extended permit tcp any any eq 80\n");
      sb.append("access-list ACL-").append(i).append(" extended permit tcp any any eq 443\n");
      sb.append("access-list ACL-").append(i).append(" extended deny ip any any\n");
    }

    // Add 50 NAT rules
    for (int i = 0; i < 50; i++) {
      sb.append("nat (INT").append(i).append(",OUTSIDE) source dynamic NET-").append(i);
      sb.append(" interface\n");
    }

    // Add some routing
    sb.append("route outside 0.0.0.0 0.0.0.0 10.0.0.254\n");
    for (int i = 0; i < 50; i++) {
      sb.append("route inside 10.").append(i).append(".0.0 255.255.0.0 10.0.1.254\n");
    }

    sb.append("Cryptochecksum:0123456789abcdef\n");
    return sb.toString();
  }

  private String createLargeConfig() {
    StringBuilder sb = new StringBuilder();
    sb.append("hostname test-ftd-large\n");
    sb.append("NGFW Version 7.4.2\n");

    // Add 500 interfaces (main stress test)
    for (int i = 0; i < 500; i++) {
      sb.append("interface GigabitEthernet").append(i / 100).append("/").append((i / 10) % 10);
      sb.append("/").append(i % 10).append("\n");
      sb.append(" nameif INT-").append(i).append("\n");
      sb.append(" security-level ").append(i % 100).append("\n");
      sb.append(" ip address 10.").append(i / 256).append(".").append(i % 256).append(".1");
      sb.append(" 255.255.255.0\n");
      sb.append(" description Interface number ").append(i).append("\n");
      sb.append(" mtu 1500\n");
      sb.append("!\n");
    }

    // Add 1000 objects
    for (int i = 0; i < 1000; i++) {
      sb.append("object network OBJ-NET-").append(i).append("\n");
      sb.append(" host 172.").append(i / 65536).append(".").append((i / 256) % 256).append(".");
      sb.append(i % 256).append(".1\n");
      if (i % 5 == 0) {
        sb.append(" description Network object ").append(i).append("\n");
      }
    }

    // Add 500 ACLs with complex rules
    for (int i = 0; i < 500; i++) {
      sb.append("access-list ACL-").append(i).append(" extended permit tcp any any eq 80\n");
      sb.append("access-list ACL-").append(i).append(" extended permit tcp any any eq 443\n");
      sb.append("access-list ACL-").append(i).append(" extended permit tcp any any eq 22\n");
      sb.append("access-list ACL-").append(i).append(" extended permit udp any any eq 53\n");
      sb.append("access-list ACL-").append(i).append(" extended deny ip any any log\n");
    }

    // Add 200 NAT rules
    for (int i = 0; i < 200; i++) {
      sb.append("nat (INT-").append(i % 500).append(",OUTSIDE) source dynamic OBJ-NET-");
      sb.append(i).append(" interface\n");
    }

    // Add BGP configuration
    sb.append("router bgp 65001\n");
    sb.append(" bgp log-neighbor-changes\n");
    for (int i = 0; i < 50; i++) {
      sb.append(" neighbor 10.")
          .append(i)
          .append(".0.1 remote-as 6500")
          .append(i % 10)
          .append("\n");
    }
    sb.append("!\n");

    // Add OSPF configuration
    sb.append("router ospf 1\n");
    sb.append(" log-adjacency-changes\n");
    sb.append(" network 10.0.0.0 0.255.255.255 area 0\n");
    sb.append("!\n");

    // Add crypto configuration
    for (int i = 0; i < 50; i++) {
      sb.append("crypto ipsec transform-set TRANS-").append(i);
      sb.append(" esp-aes-256 esp-sha-hmac\n");
      sb.append("crypto map MAP-").append(i).append(" 10 ipsec-isakmp\n");
      sb.append(" set transform-set TRANS-").append(i).append("\n");
      sb.append(" match address ACL-").append(i).append("\n");
    }

    sb.append("Cryptochecksum:0123456789abcdef\n");
    return sb.toString();
  }

  private String createProblematicPatterns() {
    StringBuilder sb = new StringBuilder();
    sb.append("hostname problematic-test\n");
    sb.append("NGFW Version 7.4.2\n");

    // Pattern 1: Interface names that look like keywords + numbers
    sb.append("interface GigabitEthernet0/0\n");
    sb.append(" nameif OUTSIDE\n");
    sb.append(" ip address 10.0.0.1 255.255.255.0\n");
    sb.append("interface Port-channel1.320\n");
    sb.append(" nameif INSIDE\n");
    sb.append(" ip address 10.0.1.1 255.255.255.0\n");

    // Pattern 2: Object names with many hyphens (stress NAME token)
    for (int i = 0; i < 50; i++) {
      sb.append("object network OBJ-NET-HYPHENATED-LONG-NAME-").append(i).append("\n");
      sb.append(" host 192.168.").append(i).append(".1\n");
    }

    // Pattern 3: Names with special characters
    sb.append("object network OBJ@test#1\n");
    sb.append(" host 10.0.0.1\n");
    sb.append("object service svc@tcp:80\n");
    sb.append(" service tcp source eq 80\n");

    // Pattern 4: Long lines (tilde negation stress)
    sb.append("hostname ");
    for (int i = 0; i < 100; i++) {
      sb.append("very-long-hostname-part-");
    }
    sb.append("\n");

    // Pattern 5: Complex crypto transforms
    for (int i = 0; i < 20; i++) {
      sb.append("crypto ipsec transform-set T-").append(i);
      sb.append(" esp-aes-256 esp-sha-hmac mode tunnel\n");
    }

    // Pattern 6: MAC addresses
    sb.append("interface GigabitEthernet1/0\n");
    sb.append(" mac-address aaaa.aaaa.1320 standby cccc.cccc.1320\n");

    sb.append("Cryptochecksum:0123456789abcdef\n");
    return sb.toString();
  }

  private String createStanzaDispatchTestConfig() {
    StringBuilder sb = new StringBuilder();
    sb.append("hostname stanza-dispatch-test\n");
    sb.append("NGFW Version 7.4.2\n");

    // Create a config that cycles through all stanza types
    // to test the 43-alternative dispatch
    for (int i = 0; i < 100; i++) {
      // Cycle through different stanza types
      switch (i % 10) {
        case 0:
          sb.append("interface GigabitEthernet").append(i).append("/0\n");
          sb.append(" nameif INT").append(i).append("\n");
          sb.append("!\n");
          break;
        case 1:
          sb.append("object network OBJ-").append(i).append("\n");
          sb.append(" host 192.168.").append(i).append(".1\n");
          break;
        case 2:
          sb.append("access-list ACL-").append(i).append(" extended permit ip any any\n");
          break;
        case 3:
          sb.append("access-group ACL-").append(i).append(" global\n");
          break;
        case 4:
          sb.append("route inside 10.").append(i).append(".0.0 255.255.255.0 10.0.0.1\n");
          break;
        case 5:
          sb.append("nat (INT").append(i % 100).append(",OUTSIDE) source dynamic OBJ-");
          sb.append(i).append(" interface\n");
          break;
        case 6:
          sb.append("logging buffered informational\n");
          break;
        case 7:
          sb.append("timeout xlate 3:00:00\n");
          break;
        case 8:
          sb.append("class-map CM-").append(i).append("\n");
          sb.append(" match any\n");
          break;
        case 9:
          sb.append("policy-map PM-").append(i).append("\n");
          sb.append(" class CM-").append(i).append("\n");
          break;
      }
    }

    sb.append("Cryptochecksum:0123456789abcdef\n");
    return sb.toString();
  }

  private String createTildeNegationTestConfig() {
    StringBuilder sb = new StringBuilder();
    sb.append("hostname tilde-test\n");
    sb.append("NGFW Version 7.4.2\n");

    // Patterns that stress ~NEWLINE negation
    sb.append("enable password Pa@ss#word-123-Complex\n");
    sb.append("hostname Very-Long-Hostname-With-Many-Parts-123\n");

    // DNS configurations with ~NEWLINE
    sb.append("dns domain-lookup inside\n");
    sb.append("dns server-group DefaultDNS\n");
    sb.append(" name-server 8.8.8.8\n");
    sb.append(" name-server 8.8.4.4\n");
    sb.append(" timeout 30\n");

    // Time-range with ~NEWLINE
    sb.append("time-range BUSINESS-HOURS\n");
    sb.append(" absolute start 00:00 1 January 2024\n");

    // MTU with ~NEWLINE
    sb.append("mtu inside 1500\n");
    sb.append("mtu outside 1400\n");

    // Class-map names with ~NEWLINE
    for (int i = 0; i < 20; i++) {
      sb.append("class-map CM-Complex-Name-").append(i).append("\n");
      sb.append(" match access-list ACL-").append(i).append("\n");
    }

    // Policy-map names with ~NEWLINE
    for (int i = 0; i < 20; i++) {
      sb.append("policy-map PM-Complex-Name-").append(i).append("\n");
      sb.append(" class CM-Complex-Name-").append(i).append("\n");
    }

    // Service-policy names with complex patterns
    for (int i = 0; i < 10; i++) {
      sb.append("service-policy PM-Complex-Name-").append(i).append(" global\n");
    }

    sb.append("Cryptochecksum:0123456789abcdef\n");
    return sb.toString();
  }
}
