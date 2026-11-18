package org.batfish.datamodel.isp_configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.jupiter.api.Test;

/** Tests for {@link BorderInterfaceInfo} */
class BorderInterfaceInfoTest {
  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new BorderInterfaceInfo(NodeInterfacePair.of("node", "interface")),
            new BorderInterfaceInfo(NodeInterfacePair.of("node", "interface")))
        .addEqualityGroup(
            new BorderInterfaceInfo(NodeInterfacePair.of("diffNode", "diffInterface")))
        .testEquals();
  }

  @Test
  void testJsonSerialization() {
    BorderInterfaceInfo borderInterfaceInfo =
        new BorderInterfaceInfo(NodeInterfacePair.of("node", "interface"));

    assertThat(
        BatfishObjectMapper.clone(borderInterfaceInfo, BorderInterfaceInfo.class),
        equalTo(borderInterfaceInfo));
  }
}
