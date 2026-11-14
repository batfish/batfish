package org.batfish.grammar.flatjuniper;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.grammar.JunosGrammarTestUtils.parseConfig;
import static org.batfish.grammar.JunosGrammarTestUtils.parseJuniperConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.batfish.common.Warning;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Prefix;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Observations from lab-validation's junos_route_filter_bounds lab on Junos 25.4R1.12. */
public final class JunosRouteFilterBoundsTest {
  private static final String RESOURCES = "org/batfish/grammar/juniper/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testDiagnostics() {
    JuniperConfiguration jc = parseJuniperConfig(_folder, "route-filter-bounds");
    assertThat(jc.getWarnings().getParseWarnings(), empty());
    Set<String> expected =
        readResource(RESOURCES + "route-filter-bounds-warnings", UTF_8)
            .lines()
            .collect(Collectors.toSet());
    Set<String> actual = new TreeSet<>();
    Pattern policy = Pattern.compile("(FATAL|RISK): .*policy-statement (\\S+) term MATCH");
    for (Warning warning : jc.getWarnings().getRedFlagWarnings()) {
      Matcher matcher = policy.matcher(warning.getText());
      assertTrue(warning.getText(), matcher.find());
      actual.add(matcher.group(1) + " " + matcher.group(2));
    }
    assertThat(actual, equalTo(expected));
    assertThat(jc.getWarnings().getRedFlagWarnings().size(), equalTo(expected.size()));
  }

  @Test
  public void testCommittedIpv4Matches() {
    Configuration c = parseConfig(_folder, "route-filter-bounds-valid");
    readResource(RESOURCES + "route-filter-bounds-matches", UTF_8)
        .lines()
        .forEach(line -> {
          String[] fields = line.split(" ");
          assertEquals(
              line,
              Boolean.parseBoolean(fields[2]),
              c.getRouteFilterLists().get(fields[0] + ":MATCH").permits(Prefix.parse(fields[1])));
        });
  }

  @Test
  public void testInvalidAddressFamilyWithInlineAction() {
    JuniperConfiguration jc = parseJuniperConfig(_folder, "route-filter-bounds-inline-invalid");
    assertThat(jc.getWarnings().getParseWarnings(), empty());
    assertThat(jc.getWarnings().getFatalRedFlagWarnings().size(), equalTo(4));
  }

  @Test
  public void testSharedSourceAddressFilterGrammar() {
    JuniperConfiguration jc = parseJuniperConfig(_folder, "route-filter-bounds-source-address");
    assertThat(jc.getWarnings().getRedFlagWarnings(), empty());
  }
}
