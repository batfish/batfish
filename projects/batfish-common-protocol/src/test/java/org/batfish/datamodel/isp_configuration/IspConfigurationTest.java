package org.batfish.datamodel.isp_configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.ip.Ip;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.Test;

/** Tests for {@link IspConfiguration} */
public class IspConfigurationTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new IspConfiguration(
                ImmutableList.of(
                    new BorderInterfaceInfo(NodeInterfacePair.of("node", "interface"))),
                new IspFilter(ImmutableList.of(1234L), ImmutableList.of(Ip.parse("1.1.1.1"))),
                ImmutableList.of(new IspNodeInfo(42, "n1"))),
            new IspConfiguration(
                ImmutableList.of(
                    new BorderInterfaceInfo(NodeInterfacePair.of("node", "interface"))),
                new IspFilter(ImmutableList.of(1234L), ImmutableList.of(Ip.parse("1.1.1.1"))),
                ImmutableList.of(new IspNodeInfo(42, "n1"))))
        .addEqualityGroup(
            new IspConfiguration(
                ImmutableList.of(
                    new BorderInterfaceInfo(NodeInterfacePair.of("node", "interface"))),
                new IspFilter(ImmutableList.of(5678L), ImmutableList.of(Ip.parse("2.2.2.2"))),
                ImmutableList.of(new IspNodeInfo(42, "n1"))))
        .addEqualityGroup(
            new IspConfiguration(
                ImmutableList.of(
                    new BorderInterfaceInfo(NodeInterfacePair.of("diffNode", "diffInterface"))),
                new IspFilter(ImmutableList.of(1234L), ImmutableList.of(Ip.parse("1.1.1.1"))),
                ImmutableList.of(new IspNodeInfo(42, "n1"))))
        .addEqualityGroup(
            new IspConfiguration(
                ImmutableList.of(
                    new BorderInterfaceInfo(NodeInterfacePair.of("node", "interface"))),
                new IspFilter(ImmutableList.of(1234L), ImmutableList.of(Ip.parse("1.1.1.1"))),
                ImmutableList.of(new IspNodeInfo(24, "diffName"))))
        .testEquals();
  }

  @Test
  public void testJsonSerialization() throws IOException {
    IspConfiguration ispConfiguration =
        new IspConfiguration(
            ImmutableList.of(new BorderInterfaceInfo(NodeInterfacePair.of("node", "interface"))),
            new IspFilter(ImmutableList.of(1234L), ImmutableList.of(Ip.parse("1.1.1.1"))),
            ImmutableList.of(new IspNodeInfo(42, "n1")));

    assertThat(
        BatfishObjectMapper.clone(ispConfiguration, IspConfiguration.class),
        equalTo(ispConfiguration));
  }
}
