package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.io.IOException;
import org.batfish.main.Batfish;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosInterfaceRangeUnitTest {

  private static final String HOSTNAME = "junos-interface-range-unit";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testInterfaceRangeUnit() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    batfish.loadConfigurations(batfish.getSnapshot());

    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        containsInAnyOrder(
            isTodo("unit 0 description RANGE_UNIT"),
            isTodo("unit 0 family inet address 192.0.2.1/24")));
  }
}
