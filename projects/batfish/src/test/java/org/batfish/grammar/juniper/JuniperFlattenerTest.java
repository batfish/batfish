package org.batfish.grammar.juniper;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.grammar.VendorConfigurationFormatDetector;
import org.batfish.grammar.flattener.Flattener;
import org.batfish.grammar.flattener.FlattenerLineMap;
import org.batfish.main.Batfish;
import org.junit.Test;

public class JuniperFlattenerTest {
  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/juniper/testconfigs/";

  /** Test that sequences of flat lines do not create a linear tree. */
  @Test
  public void testFlatLinesDoNotNest() {
    String hostname = "flatten-flat-lines-no-nesting";

    int numCopies = 33;

    List<String> copies = Collections.nCopies(numCopies, "set routing-options autonomous-system 5");

    String text =
        String.join(
            "\n",
            ImmutableList.<String>builder()
                .add("system { host-name " + hostname + " }")
                .addAll(copies)
                .build());

    Flattener flattener =
        Batfish.flatten(
            text,
            new BatfishLogger(BatfishLogger.LEVELSTR_OUTPUT, false),
            new Settings(),
            new Warnings(),
            ConfigurationFormat.JUNIPER,
            VendorConfigurationFormatDetector.BATFISH_FLATTENED_JUNIPER_HEADER);
    assert flattener instanceof JuniperFlattener;
    // Should not throw
    String flatText = flattener.getFlattenedConfigurationText();

    // All the copies should still be there in sequence
    assertThat(flatText, containsString(String.join("\n", copies)));
  }

  /** Test that configurations with `apply-flags omit` are flattened correctly. */
  @Test
  public void testFlattenWithApplyFlagsOmit() {
    Flattener flattener =
        Batfish.flatten(
            readResource(TESTCONFIGS_PREFIX + "flatten-with-apply-flags-omit", UTF_8),
            new BatfishLogger(BatfishLogger.LEVELSTR_OUTPUT, false),
            new Settings(),
            new Warnings(),
            ConfigurationFormat.JUNIPER,
            VendorConfigurationFormatDetector.BATFISH_FLATTENED_JUNIPER_HEADER);
    assert flattener instanceof JuniperFlattener;
    String flatText = flattener.getFlattenedConfigurationText();
    assertThat(
        flatText,
        equalTo(
            String.join(
                    "\n",
                    new String[] {
                      "####BATFISH FLATTENED JUNIPER CONFIG####",
                      "set system login",
                      "set system root-authentication",
                      "set system host-name flatten-with-apply-flags-omit"
                    })
                + '\n'));
  }

  /** Test for https://github.com/batfish/batfish/issues/6149. */
  @Test
  public void testGH6149Flatten() {
    String hostname = "gh-6149-flatten";
    Flattener flattener =
        Batfish.flatten(
            readResource(TESTCONFIGS_PREFIX + hostname, UTF_8),
            new BatfishLogger(BatfishLogger.LEVELSTR_OUTPUT, false),
            new Settings(),
            new Warnings(),
            ConfigurationFormat.JUNIPER,
            VendorConfigurationFormatDetector.BATFISH_FLATTENED_JUNIPER_HEADER);
    assert flattener instanceof JuniperFlattener;
    String flatText = flattener.getFlattenedConfigurationText();
    // Flattening the configs does not lose either of the filter lines in the input.
    assertThat(
        flatText,
        allOf(
            containsString(
                "set groups FOO interfaces <*> unit <*> family inet filter input-list filterA"),
            containsString(
                "set groups FOO interfaces <*> unit <*> family inet filter input-list filterB")));
  }

  @Test
  public void testNestedConfigLineMap() {
    String hostname = "nested-config-with-flat-statements";
    Flattener flattener =
        Batfish.flatten(
            readResource(TESTCONFIGS_PREFIX + hostname, UTF_8),
            new BatfishLogger(BatfishLogger.LEVELSTR_OUTPUT, false),
            new Settings(),
            new Warnings(),
            ConfigurationFormat.JUNIPER,
            VendorConfigurationFormatDetector.BATFISH_FLATTENED_JUNIPER_HEADER);
    assert flattener instanceof JuniperFlattener;
    FlattenerLineMap lineMap = flattener.getOriginalLineMap();

    /*
     * Flattened config should be 3 lines: header, set-host-name, and set-security-policies.
     * This test checks the latter two.
     */

    {
      /* Confirm original line numbers are preserved for hierarchical words */
      String flatText = flattener.getFlattenedConfigurationText().split("\n", -1)[1];
      assertThat(lineMap.getOriginalLine(2, flatText.indexOf("system")), equalTo(2));
      assertThat(lineMap.getOriginalLine(2, flatText.indexOf("host-name")), equalTo(3));
      assertThat(
          lineMap.getOriginalLine(2, flatText.indexOf("nested-config-with-flat-statements")),
          equalTo(3));
    }
    /* Confirm original line number preserved for flat statements */
    {
      String statement =
          "set security policies from-zone A to-zone B policy P match source-address any";
      for (int index = 0; index < statement.length(); ++index) {
        assertThat(lineMap.getOriginalLine(3, index), equalTo(5));
      }
    }
  }

  /**
   * Test that delete and replace tags do not generate spurious set lines.
   *
   * <p>Top-level delete (delete: protocols bgp) should not generate a set line.
   *
   * <p>Nested delete inside braces (xe-0/0/0 { delete: unit 0; }) should still generate a set line
   * for the parent block (set interfaces xe-0/0/0), since the parent exists even though its content
   * was deleted.
   */
  @Test
  public void testDeleteReplaceNoSpuriousSetLines() {
    String hostname = "flatten-delete-replace";
    Flattener flattener =
        Batfish.flatten(
            readResource(TESTCONFIGS_PREFIX + hostname, UTF_8),
            new BatfishLogger(BatfishLogger.LEVELSTR_OUTPUT, false),
            new Settings(),
            new Warnings(),
            ConfigurationFormat.JUNIPER,
            VendorConfigurationFormatDetector.BATFISH_FLATTENED_JUNIPER_HEADER);
    assert flattener instanceof JuniperFlattener;
    String flatText = flattener.getFlattenedConfigurationText();
    assertThat(
        flatText,
        equalTo(
            """
            ####BATFISH FLATTENED JUNIPER CONFIG####
            replace system host-name "some-device"
            set system host-name "some-device"
            delete protocols bgp
            delete interfaces xe-0/0/0 unit 0
            set interfaces xe-0/0/0
            """));
  }
}
