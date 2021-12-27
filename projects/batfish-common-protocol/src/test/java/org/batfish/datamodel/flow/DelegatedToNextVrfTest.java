package org.batfish.datamodel.flow;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link DelegatedToNextVrf}. */
public final class DelegatedToNextVrfTest {

  @Test
  public void testJacksonSerialization() {
    DelegatedToNextVrf obj = DelegatedToNextVrf.of("a");
    assertThat(BatfishObjectMapper.clone(obj, ForwardingDetail.class), equalTo(obj));
  }

  @Test
  public void testEquals() {
    DelegatedToNextVrf obj = DelegatedToNextVrf.of("a");
    new EqualsTester()
        .addEqualityGroup(obj, DelegatedToNextVrf.of("a"))
        .addEqualityGroup(DelegatedToNextVrf.of("b"))
        .testEquals();
  }
}
