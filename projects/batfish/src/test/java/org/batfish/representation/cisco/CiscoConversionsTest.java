package org.batfish.representation.cisco;

import static org.batfish.datamodel.Interface.INVALID_LOCAL_INTERFACE;
import static org.batfish.representation.cisco.CiscoConversions.DEFAULT_OSPF_DEAD_INTERVAL;
import static org.batfish.representation.cisco.CiscoConversions.DEFAULT_OSPF_DEAD_INTERVAL_P2P_AND_BROADCAST;
import static org.batfish.representation.cisco.CiscoConversions.DEFAULT_OSPF_HELLO_INTERVAL;
import static org.batfish.representation.cisco.CiscoConversions.DEFAULT_OSPF_HELLO_INTERVAL_P2P_AND_BROADCAST;
import static org.batfish.representation.cisco.CiscoConversions.OSPF_DEAD_INTERVAL_HELLO_MULTIPLIER;
import static org.batfish.representation.cisco.CiscoConversions.createAclWithSymmetricalLines;
import static org.batfish.representation.cisco.CiscoConversions.getMatchingPsk;
import static org.batfish.representation.cisco.CiscoConversions.sanityCheckDistributeList;
import static org.batfish.representation.cisco.CiscoConversions.sanityCheckEigrpDistributeList;
import static org.batfish.representation.cisco.CiscoConversions.toOspfDeadInterval;
import static org.batfish.representation.cisco.CiscoConversions.toOspfHelloInterval;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IkeKeyType;
import org.batfish.datamodel.IkePhase1Key;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.matchers.IkePhase1KeyMatchers;
import org.batfish.representation.cisco.DistributeList.DistributeListFilterType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link CiscoConversions} class */
@RunWith(JUnit4.class)
public class CiscoConversionsTest {

  private static final String IKE_PHASE1_KEY = "IKE_Phase1_Key";
  private NetworkFactory _nf;
  private Warnings _warnings;

  @Before
  public void before() {
    _warnings = new Warnings(true, true, true);
    _nf = new NetworkFactory();
  }

  @Test
  public void testSanityCheckEigrpDistributeListAbsentFilter() {
    Configuration c =
        _nf.configurationBuilder()
            .setHostname("conf")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    CiscoConfiguration oldConfig = new CiscoConfiguration();
    oldConfig.setWarnings(_warnings);
    DistributeList distributeList =
        new DistributeList("filter", DistributeListFilterType.ACCESS_LIST);

    assertFalse(sanityCheckEigrpDistributeList(c, distributeList, oldConfig));
    assertThat(
        oldConfig.getWarnings().getRedFlagWarnings().iterator().next().getText(),
        equalTo(
            "distribute-list refers an undefined access-list `filter`, it will not filter"
                + " anything"));
  }

  @Test
  public void testCreateAclWithSymmetricalLines() {
    IpAccessList ipAccessList =
        _nf.aclBuilder()
            .setLines(
                ImmutableList.of(
                    ExprAclLine.accepting()
                        .setName("permit ip 1.1.1.1 0.0.0.0 2.2.2.2 0.0.0.0")
                        .setMatchCondition(
                            new MatchHeaderSpace(
                                HeaderSpace.builder()
                                    .setSrcIps(IpWildcard.parse("1.1.1.1").toIpSpace())
                                    .setDstIps(IpWildcard.parse("2.2.2.2").toIpSpace())
                                    .setNotSrcIps(IpWildcard.parse("3.3.3.3").toIpSpace())
                                    .build()))
                        .build()))
            .build();

    // notSrcIps should cause the returned IpAccessList to be null

    assertThat(createAclWithSymmetricalLines(ipAccessList), nullValue());
  }

  @Test
  public void testGetMatchingPskInvalidLocalIface() {
    IsakmpProfile isakmpProfile = new IsakmpProfile("Missing_Local_Interface");
    // Empty interfaceName value represents incorrect local-address value
    isakmpProfile.setLocalInterfaceName(INVALID_LOCAL_INTERFACE);
    isakmpProfile.setKeyring("IKE_Phase1_Key");

    IkePhase1Key ikePhase1Key = new IkePhase1Key();
    ikePhase1Key.setKeyHash("test_key");
    ikePhase1Key.setKeyType(IkeKeyType.PRE_SHARED_KEY_UNENCRYPTED);
    ikePhase1Key.setLocalInterface("local_interface");

    IkePhase1Key matchingKey =
        getMatchingPsk(isakmpProfile, _warnings, ImmutableMap.of(IKE_PHASE1_KEY, ikePhase1Key));

    assertThat(matchingKey, is(nullValue()));
    assertThat(
        Iterables.getOnlyElement(_warnings.getRedFlagWarnings()).getText(),
        equalTo(
            "Invalid local address interface configured for ISAKMP profile"
                + " Missing_Local_Interface"));
  }

  @Test
  public void testGetMatchingPskNullKeyring() {
    IsakmpProfile isakmpProfile = new IsakmpProfile("Null_Keyring");
    // Empty interfaceName value represents incorrect local-address value
    isakmpProfile.setLocalInterfaceName("local_interface");

    IkePhase1Key ikePhase1Key = new IkePhase1Key();
    ikePhase1Key.setKeyHash("test_key");
    ikePhase1Key.setKeyType(IkeKeyType.PRE_SHARED_KEY_UNENCRYPTED);
    ikePhase1Key.setLocalInterface("local_interface");

    IkePhase1Key matchingKey =
        getMatchingPsk(isakmpProfile, _warnings, ImmutableMap.of(IKE_PHASE1_KEY, ikePhase1Key));

    assertThat(matchingKey, is(nullValue()));
    assertThat(
        Iterables.getOnlyElement(_warnings.getRedFlagWarnings()).getText(),
        equalTo("Keyring not set for ISAKMP profile Null_Keyring"));
  }

  @Test
  public void testGetMatchingPskMissingKeyring() {
    IsakmpProfile isakmpProfile = new IsakmpProfile("Missing_Keyring");
    // Empty interfaceName value represents incorrect local-address value
    isakmpProfile.setLocalInterfaceName("local_interface");
    isakmpProfile.setKeyring("Unknown_Key");

    IkePhase1Key ikePhase1Key = new IkePhase1Key();
    ikePhase1Key.setKeyHash("test_key");
    ikePhase1Key.setKeyType(IkeKeyType.PRE_SHARED_KEY_UNENCRYPTED);
    ikePhase1Key.setLocalInterface("local_interface");

    IkePhase1Key matchingKey =
        getMatchingPsk(isakmpProfile, _warnings, ImmutableMap.of(IKE_PHASE1_KEY, ikePhase1Key));

    assertThat(matchingKey, is(nullValue()));
    assertThat(
        Iterables.getOnlyElement(_warnings.getRedFlagWarnings()).getText(),
        equalTo("Cannot find keyring Unknown_Key for ISAKMP profile Missing_Keyring"));
  }

  @Test
  public void testGetMatchingPskInvalidLocalIfaceKr() {
    IsakmpProfile isakmpProfile = new IsakmpProfile("Invalid_Iface_Local_Address");
    // Empty interfaceName value represents incorrect local-address value
    isakmpProfile.setLocalInterfaceName("local_interface");
    isakmpProfile.setKeyring("IKE_Phase1_Key");

    IkePhase1Key ikePhase1Key = new IkePhase1Key();
    ikePhase1Key.setKeyHash("test_key");
    ikePhase1Key.setKeyType(IkeKeyType.PRE_SHARED_KEY_UNENCRYPTED);
    ikePhase1Key.setLocalInterface(INVALID_LOCAL_INTERFACE);

    IkePhase1Key matchingKey =
        getMatchingPsk(isakmpProfile, _warnings, ImmutableMap.of(IKE_PHASE1_KEY, ikePhase1Key));

    assertThat(matchingKey, is(nullValue()));
    assertThat(
        Iterables.getOnlyElement(_warnings.getRedFlagWarnings()).getText(),
        equalTo("Invalid local address interface configured for keyring IKE_Phase1_Key"));
  }

  @Test
  public void testGetMatchingPskValidKeyring() {
    IsakmpProfile isakmpProfile = new IsakmpProfile("Valid_Keyring");
    // Empty interfaceName value represents incorrect local-address value
    isakmpProfile.setLocalInterfaceName("local_interface");
    isakmpProfile.setKeyring("IKE_Phase1_Key");
    isakmpProfile.setMatchIdentity(IpWildcard.parse("1.2.3.4:0.0.0.0"));

    IkePhase1Key ikePhase1Key = new IkePhase1Key();
    ikePhase1Key.setKeyHash("test_key");
    ikePhase1Key.setKeyType(IkeKeyType.PRE_SHARED_KEY_UNENCRYPTED);
    ikePhase1Key.setLocalInterface("local_interface");
    ikePhase1Key.setRemoteIdentity(IpWildcard.parse("1.2.3.4:0.0.0.0").toIpSpace());

    IkePhase1Key matchingKey =
        getMatchingPsk(isakmpProfile, _warnings, ImmutableMap.of(IKE_PHASE1_KEY, ikePhase1Key));

    assertThat(matchingKey, IkePhase1KeyMatchers.hasKeyHash("test_key"));
  }

  @Test
  public void testGetMatchingPskInvalidKeyring() {
    IsakmpProfile isakmpProfile = new IsakmpProfile("Invalid_Keyring");
    // Empty interfaceName value represents incorrect local-address value
    isakmpProfile.setLocalInterfaceName("local_interface");
    isakmpProfile.setKeyring("IKE_Phase1_Key");
    isakmpProfile.setMatchIdentity(IpWildcard.parse("2.4.3.4:255.255.0.0"));

    IkePhase1Key ikePhase1Key = new IkePhase1Key();
    ikePhase1Key.setKeyHash("test_key");
    ikePhase1Key.setKeyType(IkeKeyType.PRE_SHARED_KEY_UNENCRYPTED);
    ikePhase1Key.setLocalInterface("local_interface");
    ikePhase1Key.setRemoteIdentity(IpWildcard.parse("1.2.3.5:0.0.0.0").toIpSpace());

    IkePhase1Key matchingKey =
        getMatchingPsk(isakmpProfile, _warnings, ImmutableMap.of(IKE_PHASE1_KEY, ikePhase1Key));

    assertThat(matchingKey, is(nullValue()));
  }

  @Test
  public void testSanityCheckDistributeListFilterType() {
    CiscoConfiguration oldConfig = new CiscoConfiguration();
    oldConfig.setHostname("cisco_conf");
    oldConfig.setWarnings(new Warnings(false, true, false));
    DistributeList distributeList =
        new DistributeList("filter", DistributeListFilterType.ACCESS_LIST);

    assertFalse(
        sanityCheckDistributeList(
            distributeList,
            new Configuration("conf", ConfigurationFormat.CISCO_IOS),
            oldConfig,
            "vrf",
            "p1"));

    assertThat(
        oldConfig.getWarnings().getRedFlagWarnings().iterator().next().getText(),
        equalTo(
            "OSPF process vrf:p1 in cisco_conf uses distribute-list of type ACCESS_LIST, only"
                + " prefix-lists are supported in dist-lists by Batfish"));
  }

  @Test
  public void testSanityCheckDistributeListPrefixList() {
    CiscoConfiguration oldConfig = new CiscoConfiguration();
    oldConfig.setHostname("cisco_conf");
    oldConfig.setWarnings(new Warnings(false, true, false));
    DistributeList distributeList =
        new DistributeList("filter", DistributeListFilterType.PREFIX_LIST);

    assertFalse(
        sanityCheckDistributeList(
            distributeList,
            new Configuration("conf", ConfigurationFormat.CISCO_IOS),
            oldConfig,
            "vrf",
            "p1"));

    assertThat(
        oldConfig.getWarnings().getRedFlagWarnings().iterator().next().getText(),
        equalTo(
            "dist-list in OSPF process vrf:p1 uses a prefix-list which is not defined, this"
                + " dist-list will allow everything"));
  }

  private static CiscoConfiguration basicCiscoConfig() {
    CiscoConfiguration c = new CiscoConfiguration();
    c.setHostname("cisco_conf");
    c.setVendor(ConfigurationFormat.CISCO_IOS);
    return c;
  }

  @Test
  public void testToOspfDeadIntervalExplicit() {
    CiscoConfiguration c = basicCiscoConfig();
    Interface iface = new Interface("FastEthernet0/0", c);
    iface.setOspfDeadInterval(7);
    // Explicitly set dead interval should be preferred over inference
    assertThat(
        toOspfDeadInterval(iface, org.batfish.datamodel.ospf.OspfNetworkType.BROADCAST),
        equalTo(7));
  }

  @Test
  public void testToOspfDeadIntervalFromHello() {
    CiscoConfiguration c = basicCiscoConfig();
    Interface iface = new Interface("FastEthernet0/0", c);

    int helloInterval = 1;
    iface.setOspfHelloInterval(helloInterval);
    // Since the dead interval is not set, it should be inferred as four times the hello interval
    assertThat(
        toOspfDeadInterval(iface, org.batfish.datamodel.ospf.OspfNetworkType.BROADCAST),
        equalTo(OSPF_DEAD_INTERVAL_HELLO_MULTIPLIER * helloInterval));
  }

  @Test
  public void testToOspfDeadIntervalFromType() {
    CiscoConfiguration c = basicCiscoConfig();
    Interface iface = new Interface("FastEthernet0/0", c);

    // Since the dead interval and hello interval are not set, it should be inferred from the
    // network type
    assertThat(
        toOspfDeadInterval(iface, org.batfish.datamodel.ospf.OspfNetworkType.POINT_TO_POINT),
        equalTo(DEFAULT_OSPF_DEAD_INTERVAL_P2P_AND_BROADCAST));
    assertThat(
        toOspfDeadInterval(
            iface, org.batfish.datamodel.ospf.OspfNetworkType.NON_BROADCAST_MULTI_ACCESS),
        equalTo(DEFAULT_OSPF_DEAD_INTERVAL));
  }

  @Test
  public void testToOspfHelloIntervalExplicit() {
    CiscoConfiguration c = basicCiscoConfig();
    Interface iface = new Interface("FastEthernet0/0", c);

    iface.setOspfHelloInterval(7);
    // Explicitly set hello interval should be preferred over inference
    assertThat(
        toOspfHelloInterval(iface, org.batfish.datamodel.ospf.OspfNetworkType.BROADCAST),
        equalTo(7));
  }

  @Test
  public void testToOspfHelloIntervalFromType() {
    CiscoConfiguration c = basicCiscoConfig();
    Interface iface = new Interface("FastEthernet0/0", c);

    // Since the hello interval is not set, it should be inferred from the network type
    assertThat(
        toOspfHelloInterval(iface, org.batfish.datamodel.ospf.OspfNetworkType.POINT_TO_POINT),
        equalTo(DEFAULT_OSPF_HELLO_INTERVAL_P2P_AND_BROADCAST));
    assertThat(
        toOspfHelloInterval(
            iface, org.batfish.datamodel.ospf.OspfNetworkType.NON_BROADCAST_MULTI_ACCESS),
        equalTo(DEFAULT_OSPF_HELLO_INTERVAL));
  }

  @Test
  public void testToOspfNetworkType() {
    assertThat(
        CiscoConversions.toOspfNetworkType(null, new Warnings()),
        equalTo(org.batfish.datamodel.ospf.OspfNetworkType.BROADCAST));
  }
}
