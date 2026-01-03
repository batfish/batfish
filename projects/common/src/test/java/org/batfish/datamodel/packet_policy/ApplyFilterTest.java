package org.batfish.datamodel.packet_policy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link ApplyTransformation} */
public class ApplyFilterTest {
  @Test
  public void testEquals() {
    ApplyFilter af = new ApplyFilter("filter");
    new EqualsTester()
        .addEqualityGroup(af, af, new ApplyFilter("filter"))
        .addEqualityGroup(new ApplyFilter("other"))
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    ApplyFilter af = new ApplyFilter("filter");
    assertThat(SerializationUtils.clone(af), equalTo(af));
  }

  @Test
  public void testJsonSerialization() {
    ApplyFilter af = new ApplyFilter("filter");
    assertThat(BatfishObjectMapper.clone(af, ApplyFilter.class), equalTo(af));
  }
}
