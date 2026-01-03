package org.batfish.datamodel.ospf;

import static org.batfish.datamodel.ospf.OspfAreaSummary.SummaryRouteBehavior.ADVERTISE_AND_INSTALL_DISCARD;
import static org.batfish.datamodel.ospf.OspfAreaSummary.SummaryRouteBehavior.NOT_ADVERTISE_AND_NO_DISCARD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link OspfAreaSummary} */
public class OspfAreaSummaryTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new OspfAreaSummary(ADVERTISE_AND_INSTALL_DISCARD, 100L),
            new OspfAreaSummary(ADVERTISE_AND_INSTALL_DISCARD, 100L))
        .addEqualityGroup(new OspfAreaSummary(NOT_ADVERTISE_AND_NO_DISCARD, 100L))
        .addEqualityGroup(new OspfAreaSummary(NOT_ADVERTISE_AND_NO_DISCARD, 200L))
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    OspfAreaSummary summary = new OspfAreaSummary(ADVERTISE_AND_INSTALL_DISCARD, 100L);
    assertThat(SerializationUtils.clone(summary), equalTo(summary));
  }

  @Test
  public void testJsonSerialization() {
    OspfAreaSummary summary = new OspfAreaSummary(ADVERTISE_AND_INSTALL_DISCARD, 100L);
    assertThat(BatfishObjectMapper.clone(summary, OspfAreaSummary.class), equalTo(summary));
  }
}
