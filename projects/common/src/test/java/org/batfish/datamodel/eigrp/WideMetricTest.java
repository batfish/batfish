package org.batfish.datamodel.eigrp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.eigrp.WideMetric.Builder;
import org.junit.Test;

public class WideMetricTest {
  private final EigrpMetricValues _values =
      EigrpMetricValues.builder()
          .setBandwidth(1)
          .setDelay(2)
          .setReliability(3)
          .setEffectiveBandwidth(4)
          .setMtu(5)
          .build();

  @Test
  public void testEquals() {
    Builder builder = WideMetric.builder().setValues(_values);
    WideMetric cm = builder.build();
    new EqualsTester()
        .addEqualityGroup(cm, cm, builder.build())
        .addEqualityGroup(builder.setK1(11).build())
        .addEqualityGroup(builder.setK3(11).build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    WideMetric cm = WideMetric.builder().setValues(_values).setK1(2).setK1(2).build();
    assertThat(SerializationUtils.clone(cm), equalTo(cm));
  }

  @Test
  public void testJsonSerialization() {
    WideMetric cm = WideMetric.builder().setValues(_values).setK1(2).setK1(2).build();
    assertThat(BatfishObjectMapper.clone(cm, WideMetric.class), equalTo(cm));
  }
}
