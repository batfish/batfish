package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getNamedBgpGroup;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.NamedBgpGroup;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosBgpAcceptRemoteNexthopTest {

  private static final String HOSTNAME = "bgp-accept-remote-nexthop";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testAcceptRemoteNexthop() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    NamedBgpGroup group = getNamedBgpGroup(batfish, HOSTNAME, "EBGP-PEERS");

    assertThat(group.getAcceptRemoteNexthop(), equalTo(true));
    assertThat(getParseWarnings(batfish, HOSTNAME), contains(isTodo("accept-remote-nexthop")));
  }
}
