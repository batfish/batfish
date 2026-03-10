package org.batfish.datamodel.eigrp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.eigrp.EigrpMetricValues.Builder;
import org.junit.Test;

public class EigrpMetricValuesTest {
  @Test
  public void testEquals() {
    Builder builder =
        EigrpMetricValues.builder()
            .setBandwidth(1)
            .setDelay(2)
            .setReliability(3)
            .setEffectiveBandwidth(4)
            .setMtu(5);
    EigrpMetricValues v = builder.build();
    new EqualsTester()
        .addEqualityGroup(v, v, builder.build())
        .addEqualityGroup(builder.setBandwidth(10).build())
        .addEqualityGroup(builder.setDelay(20).build())
        .addEqualityGroup(builder.setReliability(30).build())
        .addEqualityGroup(builder.setEffectiveBandwidth(40).build())
        .addEqualityGroup(builder.setMtu(50).build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    EigrpMetricValues v =
        EigrpMetricValues.builder()
            .setBandwidth(1)
            .setDelay(2)
            .setReliability(3)
            .setEffectiveBandwidth(4)
            .setMtu(5)
            .build();
    assertThat(SerializationUtils.clone(v), equalTo(v));
  }

  @Test
  public void testJsonSerialization() {
    EigrpMetricValues v =
        EigrpMetricValues.builder()
            .setBandwidth(1)
            .setDelay(2)
            .setReliability(3)
            .setEffectiveBandwidth(4)
            .setMtu(5)
            .build();
    assertThat(BatfishObjectMapper.clone(v, EigrpMetricValues.class), equalTo(v));
  }
}
