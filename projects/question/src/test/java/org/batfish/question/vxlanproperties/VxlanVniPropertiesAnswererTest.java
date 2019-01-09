package org.batfish.question.vxlanproperties;

import static org.batfish.question.vxlanproperties.VxlanVniPropertiesAnswerer.COL_NODE;
import static org.batfish.question.vxlanproperties.VxlanVniPropertiesAnswerer.COL_REMOTE_VTEP_MULTICAST_GROUP;
import static org.batfish.question.vxlanproperties.VxlanVniPropertiesAnswerer.COL_REMOTE_VTEP_UNICAST_ADDRESSES;
import static org.batfish.question.vxlanproperties.VxlanVniPropertiesAnswerer.COL_VLAN;
import static org.batfish.question.vxlanproperties.VxlanVniPropertiesAnswerer.COL_VNI;
import static org.batfish.question.vxlanproperties.VxlanVniPropertiesAnswerer.COL_VTEP_ADDRESS;
import static org.batfish.question.vxlanproperties.VxlanVniPropertiesAnswerer.COL_VXLAN_UDP_PORT;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.SortedMap;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.datamodel.BumTransportMethod;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.VniSettings;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Rows;
import org.batfish.datamodel.table.TableAnswerElement;
import org.junit.Test;

public class VxlanVniPropertiesAnswererTest {
  @Test
  public void testAnswer() {
    VxlanVniPropertiesAnswerer answerer =
        new VxlanVniPropertiesAnswerer(
            new VxlanVniPropertiesQuestion(NodesSpecifier.ALL),
            new VxlanVniPropertiesAnswererTest.TestBatfish());
    TableAnswerElement answer = answerer.answer();
    assertThat(
        answer.getRows(),
        equalTo(
            new Rows()
                .add(
                    Row.of(
                        COL_VNI,
                        1,
                        COL_NODE,
                        "hostname",
                        COL_VLAN,
                        10001,
                        COL_VTEP_ADDRESS,
                        Ip.parse("1.2.3.4"),
                        COL_REMOTE_VTEP_MULTICAST_GROUP,
                        null,
                        COL_REMOTE_VTEP_UNICAST_ADDRESSES,
                        ImmutableSet.of(Ip.parse("2.3.4.5"), Ip.parse("2.3.4.6")),
                        COL_VXLAN_UDP_PORT,
                        4242))
                .add(
                    Row.of(
                        COL_VNI,
                        2,
                        COL_NODE,
                        "hostname",
                        COL_VLAN,
                        10002,
                        COL_VTEP_ADDRESS,
                        Ip.parse("1.2.3.4"),
                        COL_REMOTE_VTEP_MULTICAST_GROUP,
                        ImmutableSet.of(Ip.parse("227.10.1.1")),
                        COL_REMOTE_VTEP_UNICAST_ADDRESSES,
                        null,
                        COL_VXLAN_UDP_PORT,
                        4789))));
  }

  private static class TestBatfish extends IBatfishTestAdapter {
    @Override
    public SortedMap<String, Configuration> loadConfigurations() {
      Configuration conf = new Configuration("hostname", ConfigurationFormat.ARISTA);
      conf.setVrfs(
          ImmutableMap.of(Configuration.DEFAULT_VRF_NAME, new Vrf(Configuration.DEFAULT_VRF_NAME)));
      conf.getDefaultVrf()
          .setVniSettings(
              ImmutableSortedMap.of(
                  1,
                  VniSettings.builder()
                      .setVni(1)
                      .setVlan(10001)
                      .setSourceAddress(Ip.parse("1.2.3.4"))
                      .setUdpPort(4242)
                      .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
                      .setBumTransportIps(
                          ImmutableSortedSet.of(Ip.parse("2.3.4.5"), Ip.parse("2.3.4.6")))
                      .build(),
                  2,
                  VniSettings.builder()
                      .setVni(2)
                      .setVlan(10002)
                      .setSourceAddress(Ip.parse("1.2.3.4"))
                      .setUdpPort(4789)
                      .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
                      .setBumTransportIps(ImmutableSortedSet.of(Ip.parse("227.10.1.1")))
                      .build()));
      return ImmutableSortedMap.of("hostname", conf);
    }

    @Override
    public SortedMap<String, Configuration> loadConfigurations(NetworkSnapshot snapshot) {
      return loadConfigurations();
    }
  }
}
