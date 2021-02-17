package org.batfish.datamodel.flow;

import static org.batfish.datamodel.flow.PreNatFibLookup.INSTANCE;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link PreNatFibLookup}. */
@ParametersAreNonnullByDefault
public final class PreNatFibLookupTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(INSTANCE, INSTANCE)
        .testEquals();
  }

  @Test
  public void testSerialization() {
    SessionAction clone = BatfishObjectMapper.clone(PreNatFibLookup.INSTANCE, SessionAction.class);
    assertThat(clone, equalTo(PreNatFibLookup.INSTANCE));
  }
}
