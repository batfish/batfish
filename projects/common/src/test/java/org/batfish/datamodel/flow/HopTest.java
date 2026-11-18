package org.batfish.datamodel.flow;

import static org.batfish.datamodel.flow.HopTestUtils.acceptedHop;
import static org.batfish.datamodel.flow.HopTestUtils.forwardedHop;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Test of {@link Hop}. */
@ParametersAreNonnullByDefault
final class HopTest {

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(acceptedHop("n"), acceptedHop("n"))
        .addEqualityGroup(acceptedHop("m"))
        .addEqualityGroup(forwardedHop("n", "v"))
        .testEquals();
  }

  @Test
  void testSerialization() {
    Hop hop = acceptedHop("n");
    assertThat(hop, equalTo(BatfishObjectMapper.clone(acceptedHop("n"), Hop.class)));
  }
}
