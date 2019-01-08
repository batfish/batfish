package org.batfish.grammar.flatjuniper;

import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.regex.Pattern;
import org.junit.Test;

/** Tests of {@link BgpCommunityRegex}. */
public class BgpCommunityRegexTest {
  private static final String[] JUNIPER_REGEX = {
    "no-advertise",
    "no-export",
    "no-export-subconfed",
    "123+:123+",
    "^(56):123$",
    "^56:(2.*)$",
    "^(.*):(.*[579])$",
    "^((56) | (78)):(2.*[2-8])$",
  };

  private static final String[][] MATCHES = {
    {"65535:65282"},
    {"65535:65281"},
    {"65535:65283"},
    {"123:123", "1233:123", "123:1233"},
    {"56:123"},
    {"56:2", "56:222", "56:234"},
    {"1234:5", "78:2357", "34:64509"},
    {"56:22", "56:21197", "78:2678"},
  };

  private static final String[][] NO_MATCHES = {
    {"65535:65281", "0:0", "65282:65535"},
    {"65535:65282", "0:0", "65281:65535"},
    {"65535:65284", "0:0", "65283:65535"},
    {"12:123", "123:12", "1234:123", "123:1234"},
    {"456:123", "567:123", "56:1234"},
    {"56:3", "2:56"},
    {"1234:51"},
    {"56:122", "56:82", "56:828", "56:21", "78:29", "78:299"},
  };

  @Test
  public void testRegexes() {
    for (int i = 0; i < JUNIPER_REGEX.length; ++i) {
      String regex = JUNIPER_REGEX[i];
      String javaRegex;
      try {
        javaRegex = BgpCommunityRegex.convertToJavaRegex(regex);
      } catch (Exception e) {
        throw new AssertionError(String.format("Error converting %s to java regex", regex), e);
      }
      Pattern p;
      try {
        p = Pattern.compile(javaRegex);
      } catch (Exception e) {
        throw new AssertionError(
            String.format("Error compiling Java regex %s (from %s) to Pattern", javaRegex, regex),
            e);
      }
      for (String shouldMatch : MATCHES[i]) {
        assertThat(shouldMatch, matchesPattern(p));
      }
      for (String shouldNotMatch : NO_MATCHES[i]) {
        assertThat(shouldNotMatch, not(matchesPattern(p)));
      }
    }
  }
}
