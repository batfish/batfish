package org.batfish.datamodel.flow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Test of {@link Accept}. */
@ParametersAreNonnullByDefault
final class AcceptTest {

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(Accept.INSTANCE, Accept.INSTANCE)
        .testEquals();
  }

  @Test
  void testSerialization() {
    SessionAction clone = BatfishObjectMapper.clone(Accept.INSTANCE, SessionAction.class);
    assertThat(clone, equalTo(Accept.INSTANCE));
  }
}
