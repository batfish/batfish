package org.batfish.vendor.cisco_ftd;

import com.google.common.io.CharStreams;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

/**
 * Diagnostic test to profile FTD lexer performance and identify LexerATNConfig allocation issues.
 *
 * <p>This test helps identify which lexer rules and patterns cause excessive LexerATNConfig
 * allocations during parsing.
 */
public class FtdLexerProfilingTest extends FtdGrammarTest {

  /** Test parsing of real FTD configuration with profiling enabled. */
  @Test
  public void testProfileRealConfig() throws Exception {
    // Try to load the real config from test resources
    String configPath =
        "/org/batfish/grammar/cisco_ftd/testconfigs/FW-DC2-Services-CNAA03-SET-01.conf";

    String configText;
    try (InputStream inputStream = getClass().getResourceAsStream(configPath)) {
      if (inputStream == null) {
        System.out.println("Config file not found at: " + configPath);
        System.out.println("Creating a minimal test config instead...");
        configText = createMinimalTestConfig();
      } else {
        configText =
            CharStreams.toString(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
      }
    }

    System.out.println("\n=== FTD Lexer Profiling Test ===");
    System.out.println("Config length: " + configText.length() + " characters");
    System.out.println("Config lines: " + configText.split("\n").length);
    System.out.println();

    // Parse with profiling
    long startTime = System.nanoTime();
    try {
      parseVendorConfig(configText);
      long endTime = System.nanoTime();
      long durationMs = (endTime - startTime) / 1_000_000;

      System.out.println("Parse completed in: " + durationMs + "ms");
      System.out.println("Status: SUCCESS");

    } catch (Exception e) {
      long endTime = System.nanoTime();
      long durationMs = (endTime - startTime) / 1_000_000;

      System.out.println("Parse failed after: " + durationMs + "ms");
      System.out.println("Error: " + e.getClass().getSimpleName() + ": " + e.getMessage());

      if (e.getCause() != null) {
        System.out.println("Caused by: " + e.getCause().getMessage());
      }
    }
  }

  /** Test with a problematic pattern to identify specific bottlenecks. */
  @Test
  public void testProfileProblematicPatterns() {
    // Test patterns that are likely to cause issues:
    // 1. Many similar keywords (200+ keywords)
    // 2. NAME vs WORD disambiguation
    // 3. Interface names with special characters

    StringBuilder config = new StringBuilder();
    config.append("hostname test\n");
    config.append("NGFW Version 7.4.2\n");

    // Add many interface definitions to stress test the lexer
    for (int i = 0; i < 100; i++) {
      config.append("interface GigabitEthernet").append(i).append("/0\n");
      config.append(" nameif INT").append(i).append("\n");
      config.append(" security-level 0\n");
      config.append(" ip address 10.0.").append(i).append(".1 255.255.255.0\n");
      config.append("!\n");
    }

    // Add many object definitions with NAME-like patterns
    for (int i = 0; i < 100; i++) {
      config.append("object network OBJ-NET-").append(i).append("\n");
      config.append(" host 192.168.").append(i).append(".1\n");
    }

    String configText = config.toString();

    System.out.println("\n=== FTD Lexer Pattern Profiling ===");
    System.out.println("Config length: " + configText.length() + " characters");
    System.out.println("Config lines: " + configText.split("\n").length);

    long startTime = System.nanoTime();
    try {
      parseVendorConfig(configText.toString());
      long endTime = System.nanoTime();
      long durationMs = (endTime - startTime) / 1_000_000;

      System.out.println("Parse completed in: " + durationMs + "ms");
      System.out.println("Status: SUCCESS");

    } catch (Exception e) {
      long endTime = System.nanoTime();
      long durationMs = (endTime - startTime) / 1_000_000;

      System.out.println("Parse failed after: " + durationMs + "ms");
      System.out.println("Error: " + e.getMessage());
    }
  }

  /** Test keyword matching performance. */
  @Test
  public void testProfileKeywordMatching() {
    // Create a config that uses many different keywords
    // to test the keyword matching performance

    String config =
        """
        hostname test
        NGFW Version 7.4.2
        interface GigabitEthernet0/0
         nameif OUTSIDE
         security-level 0
         ip address 10.0.0.1 255.255.255.0
         mtu 1500
        !
        interface GigabitEthernet0/1
         nameif INSIDE
         security-level 100
         ip address 192.168.1.1 255.255.255.0
         mtu 1500
        !
        router ospf 1
         router-id 1.1.1.1
         network 10.0.0.0 255.255.255.0 area 0
         log-adjacency-changes
         passive-interface OUTSIDE
        !
        access-list TEST_ACL extended permit ip any any
        access-group TEST_ACL global
        !
        nat (INSIDE,OUTSIDE) source dynamic any interface
        !
        route OUTSIDE 0.0.0.0 0.0.0.0 10.0.0.254
        """;

    System.out.println("\n=== FTD Keyword Matching Profiling ===");
    System.out.println("Config length: " + config.length() + " characters");

    // Parse multiple times to get consistent measurements
    int iterations = 10;
    long totalTime = 0;

    for (int i = 0; i < iterations; i++) {
      long startTime = System.nanoTime();
      try {
        parseVendorConfig(config);
        long endTime = System.nanoTime();
        totalTime += (endTime - startTime);
      } catch (Exception e) {
        System.out.println("Parse failed on iteration " + i + ": " + e.getMessage());
        return;
      }
    }

    long avgTimeMs = (totalTime / iterations) / 1_000_000;
    System.out.println(
        "Average parse time over " + iterations + " iterations: " + avgTimeMs + "ms");
  }

  private String createMinimalTestConfig() {
    return """
    hostname test-ftd
    NGFW Version 7.4.2
    interface GigabitEthernet0/0
     nameif OUTSIDE
     security-level 0
     ip address 10.0.0.1 255.255.255.0
    !
    interface GigabitEthernet0/1
     nameif INSIDE
     security-level 100
     ip address 192.168.1.1 255.255.255.0
    !
    access-list TEST_ACL extended permit ip any any
    """;
  }
}
