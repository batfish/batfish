package org.batfish.datamodel.flow;

import static org.batfish.datamodel.flow.PreNatFibLookup.INSTANCE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Test of {@link PreNatFibLookup}. */
@ParametersAreNonnullByDefault
final class PreNatFibLookupTest {

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(INSTANCE, INSTANCE)
        .testEquals();
  }

  @Test
  void testSerialization() {
    SessionAction clone = BatfishObjectMapper.clone(PreNatFibLookup.INSTANCE, SessionAction.class);
    assertThat(clone, equalTo(PreNatFibLookup.INSTANCE));
  }
}
