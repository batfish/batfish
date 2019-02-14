package org.batfish.datamodel.isp_configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.Test;

/** Tests for {@link BorderInterfaceInfo} */
public class BorderInterfaceInfoTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new BorderInterfaceInfo(new NodeInterfacePair("node", "interface")),
            new BorderInterfaceInfo(new NodeInterfacePair("node", "interface")))
        .addEqualityGroup(
            new BorderInterfaceInfo(new NodeInterfacePair("diffNode", "diffInterface")))
        .testEquals();
  }

  @Test
  public void testJsonSerialization() throws IOException {
    BorderInterfaceInfo borderInterfaceInfo =
        new BorderInterfaceInfo(new NodeInterfacePair("node", "interface"));

    assertThat(
        BatfishObjectMapper.clone(borderInterfaceInfo, BorderInterfaceInfo.class),
        equalTo(borderInterfaceInfo));
  }
}
