package org.batfish.grammar.flatjuniper;

import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.Interface.InterfaceType.PHYSICAL;
import static org.batfish.representation.juniper.Interface.InterfaceType.PHYSICAL_UNIT;
import static org.batfish.representation.juniper.Interface.VlanTaggingMode.FLEXIBLE_VLAN_TAGGING;
import static org.batfish.representation.juniper.Interface.VlanTaggingMode.VLAN_TAGGING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.Interface;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosChannelizedInterfaceTest {

  private static final String HOSTNAME = "junos-channelized-interface";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testChannelizedInterfaces() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration jc = getVendorConfiguration(batfish, HOSTNAME);

    Interface vlanTagging = jc.getMasterLogicalSystem().getInterfaces().get("et-0/0/0:0");
    assertThat(vlanTagging.getType(), equalTo(PHYSICAL));
    assertThat(vlanTagging.getVlanTagging(), equalTo(VLAN_TAGGING));
    assertThat(vlanTagging.getUnits().get("et-0/0/0:0.100").getType(), equalTo(PHYSICAL_UNIT));
    assertThat(vlanTagging.getUnits().get("et-0/0/0:0.100").getVlanId(), equalTo(100));

    Interface flexibleVlanTagging = jc.getMasterLogicalSystem().getInterfaces().get("et-0/0/0:1");
    assertThat(flexibleVlanTagging.getType(), equalTo(PHYSICAL));
    assertThat(flexibleVlanTagging.getVlanTagging(), equalTo(FLEXIBLE_VLAN_TAGGING));
    assertThat(
        flexibleVlanTagging.getUnits().get("et-0/0/0:1.200").getType(), equalTo(PHYSICAL_UNIT));
    assertThat(flexibleVlanTagging.getUnits().get("et-0/0/0:1.200").getVlanId(), equalTo(200));

    assertThat(getParseWarnings(batfish, HOSTNAME), empty());
  }
}
