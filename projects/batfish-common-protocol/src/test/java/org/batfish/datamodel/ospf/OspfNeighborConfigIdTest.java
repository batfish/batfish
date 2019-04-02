package org.batfish.datamodel.ospf;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Tests of {@link OspfNeighborConfigId} */
public class OspfNeighborConfigIdTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new OspfNeighborConfigId("h", "v", "p", "i"),
            new OspfNeighborConfigId("h", "v", "p", "i"))
        .addEqualityGroup(new OspfNeighborConfigId("h2", "v", "p", "i"))
        .addEqualityGroup(new OspfNeighborConfigId("h", "v2", "p", "i"))
        .addEqualityGroup(new OspfNeighborConfigId("h", "v", "p2", "i"))
        .addEqualityGroup(new OspfNeighborConfigId("h", "v", "p", "i2"))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    OspfNeighborConfigId cid = new OspfNeighborConfigId("h", "v", "p", "i");
    assertThat(SerializationUtils.clone(cid), equalTo(cid));
  }
}
