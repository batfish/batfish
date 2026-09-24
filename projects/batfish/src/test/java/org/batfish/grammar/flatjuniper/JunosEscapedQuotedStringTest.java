package org.batfish.grammar.flatjuniper;

import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;

import java.io.IOException;
import org.batfish.main.Batfish;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosEscapedQuotedStringTest {

  private static final String ESCAPED_LOGIN_MESSAGE = "junos-escaped-login-message";
  private static final String HOSTNAME = "junos-escaped-quoted-string";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testEscapedQuotedString() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    assertThat(batfish.loadConfigurations(batfish.getSnapshot()), hasKey(HOSTNAME));
    assertThat(getParseWarnings(batfish, HOSTNAME), empty());
  }

  @Test
  public void testEscapedLoginMessage() throws IOException {
    Batfish batfish = getBatfish(_folder, ESCAPED_LOGIN_MESSAGE);
    assertThat(batfish.loadConfigurations(batfish.getSnapshot()), hasKey(ESCAPED_LOGIN_MESSAGE));
    assertThat(getParseWarnings(batfish, ESCAPED_LOGIN_MESSAGE), empty());
  }
}
