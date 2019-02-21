package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link PrefixRange}. */
@RunWith(JUnit4.class)
public class PrefixRangeTest {
  @Test
  public void testToString() {
    PrefixRange pr = PrefixRange.fromString("1.2.3.4/8:17-19");
    assertThat(pr.toString(), equalTo("1.0.0.0/8:17-19"));
  }

  @Test
  public void testSerialization() throws JsonProcessingException {
    PrefixRange pr = PrefixRange.fromString("1.2.3.4/8:17-19");
    assertThat(BatfishObjectMapper.writeString(pr), equalTo("\"1.0.0.0/8:17-19\""));
  }

  @Test
  public void testDeSerialization() throws IOException {
    PrefixRange pr = PrefixRange.fromString("1.2.3.4/8:17-19");
    assertThat(
        BatfishObjectMapper.mapper().readValue("\"1.0.0.0/8:17-19\"", PrefixRange.class),
        equalTo(pr));
  }

  /**
   * Tests that getting a range more specific than a /32 does not crash and is empty -- or at least
   * does not contain the initial /32.
   */
  @Test
  public void testEmptyRange() {
    Prefix slash32 = Prefix.parse("1.2.3.4/32");
    PrefixRange empty = PrefixRange.moreSpecificThan(slash32);
    assertFalse(empty.includesPrefixRange(PrefixRange.fromPrefix(slash32)));
  }
}
