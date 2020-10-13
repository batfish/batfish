package org.batfish.datamodel.flow;

import static org.batfish.datamodel.flow.FibLookup.INSTANCE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link FibLookup}. */
@ParametersAreNonnullByDefault
public final class FibLookupTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(INSTANCE, INSTANCE)
        .testEquals();
  }

  @Test
  public void testSerialization() {
    SessionAction clone = BatfishObjectMapper.clone(FibLookup.INSTANCE, SessionAction.class);
    assertThat(clone, equalTo(FibLookup.INSTANCE));
  }
}
