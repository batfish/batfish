package org.batfish.datamodel.flow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link Accept}. */
@ParametersAreNonnullByDefault
public final class AcceptTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(Accept.INSTANCE, Accept.INSTANCE)
        .testEquals();
  }

  @Test
  public void testSerialization() {
    SessionAction clone = BatfishObjectMapper.clone(Accept.INSTANCE, SessionAction.class);
    assertThat(clone, equalTo(Accept.INSTANCE));
  }
}
