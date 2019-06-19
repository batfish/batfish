package org.batfish.datamodel.ospf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link OspfAreaSummary} */
public class OspfAreaSummaryTest {
  @Test
  public void testEquals() {
    OspfAreaSummary summary = new OspfAreaSummary(true, 100L);
    new EqualsTester()
        .addEqualityGroup(summary, summary, new OspfAreaSummary(true, 100L))
        .addEqualityGroup(new OspfAreaSummary(false, 100L))
        .addEqualityGroup(new OspfAreaSummary(true, 200L))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    OspfAreaSummary summary = new OspfAreaSummary(true, 100L);
    assertThat(SerializationUtils.clone(summary), equalTo(summary));
  }

  @Test
  public void testJsonSerialization() throws IOException {
    OspfAreaSummary summary = new OspfAreaSummary(true, 100L);
    assertThat(BatfishObjectMapper.clone(summary, OspfAreaSummary.class), equalTo(summary));
  }
}
