package org.batfish.datamodel.eigrp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.eigrp.ClassicMetric.Builder;
import org.junit.Test;

public class ClassicMetricTest {

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
    Builder builder = ClassicMetric.builder().setValues(_values);
    ClassicMetric cm = builder.build();
    new EqualsTester()
        .addEqualityGroup(cm, cm, builder.build())
        .addEqualityGroup(builder.setK1(11).build())
        .addEqualityGroup(builder.setK3(11).build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    ClassicMetric cm = ClassicMetric.builder().setValues(_values).setK1(2).setK1(2).build();
    assertThat(SerializationUtils.clone(cm), equalTo(cm));
  }

  @Test
  public void testJsonSerialization() {
    ClassicMetric cm = ClassicMetric.builder().setValues(_values).setK1(2).setK1(2).build();
    assertThat(BatfishObjectMapper.clone(cm, ClassicMetric.class), equalTo(cm));
  }

  @Test
  public void testComputeCost() {
    // Example taken from
    // https://www.cisco.com/c/en/us/td/docs/ios-xml/ios/iproute_eigrp/configuration/xe-3s/ire-xe-3s-book/ire-wid-met.html#GUID-736131DE-0B64-46F4-A19A-B0526D24F95B
    ClassicMetric cm =
        ClassicMetric.builder()
            .setValues(
                EigrpMetricValues.builder().setBandwidth(128).setDelay(84_000_000_000L).build())
            .build();
    // In this example, the metric is not affected by precision issues beween V1 and V2.
    assertThat(cm.ribMetric(EigrpMetricVersion.V1), equalTo(22150400L));
    assertThat(cm.ribMetric(EigrpMetricVersion.V2), equalTo(22150400L));
  }

  @Test
  public void testComputeCostDifferentVersions() {
    // Example taken from lab validation on IOS-XE vs NX-OS.
    // set metric 900 100 255 1 1500
    // + 1 * 10us for the receiving interface.
    ClassicMetric cm =
        ClassicMetric.builder()
            .setValues(
                EigrpMetricValues.builder()
                    .setBandwidth(900)
                    .setDelay((100 + 1) * 10_000_000L)
                    .setEffectiveBandwidth(1)
                    .setReliability(255)
                    .setMtu(1500)
                    .build())
            .build();
    // V1 lower value - aka, lower precision on bandwidth.
    assertThat(cm.ribMetric(EigrpMetricVersion.V1), equalTo(2870272L));
    // V2 higher value - aka, higher precision on bandwidth.
    assertThat(cm.ribMetric(EigrpMetricVersion.V2), equalTo(2870300L));
  }
}
