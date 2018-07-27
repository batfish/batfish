package org.batfish.representation.cisco;

import static org.batfish.datamodel.Interface.INVALID_LOCAL_INTERFACE;
import static org.batfish.representation.cisco.CiscoConversions.createAclWithSymmetricalLines;
import static org.batfish.representation.cisco.CiscoConversions.getMatchingPsk;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.batfish.common.Warnings;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IkeKeyType;
import org.batfish.datamodel.IkePhase1Key;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.matchers.IkePhase1KeyMatchers;
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
    _warnings = new Warnings(true, true, true, false);
    _nf = new NetworkFactory();
  }

  @Test
  public void testCreateAclWithSymmetricalLines() {
    IpAccessList ipAccessList =
        _nf.aclBuilder()
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.accepting()
                        .setName("permit ip 1.1.1.1 0.0.0.0 2.2.2.2 0.0.0.0")
                        .setMatchCondition(
                            new MatchHeaderSpace(
                                HeaderSpace.builder()
                                    .setSrcIps(new IpWildcard("1.1.1.1").toIpSpace())
                                    .setDstIps(new IpWildcard("2.2.2.2").toIpSpace())
                                    .setNotSrcIps(new IpWildcard("3.3.3.3").toIpSpace())
                                    .setIpProtocols(ImmutableSet.of(IpProtocol.IP))
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
    ikePhase1Key.setKeyType(IkeKeyType.PRE_SHARED_KEY);
    ikePhase1Key.setLocalInterface("local_interface");

    IkePhase1Key matchingKey =
        getMatchingPsk(isakmpProfile, _warnings, ImmutableMap.of(IKE_PHASE1_KEY, ikePhase1Key));

    assertThat(matchingKey, is(nullValue()));
    assertThat(
        Iterables.getOnlyElement(_warnings.getRedFlagWarnings()).getText(),
        equalTo(
            "Invalid local address interface configured for ISAKMP profile Missing_Local_Interface"));
  }

  @Test
  public void testGetMatchingPskNullKeyring() {
    IsakmpProfile isakmpProfile = new IsakmpProfile("Null_Keyring");
    // Empty interfaceName value represents incorrect local-address value
    isakmpProfile.setLocalInterfaceName("local_interface");

    IkePhase1Key ikePhase1Key = new IkePhase1Key();
    ikePhase1Key.setKeyHash("test_key");
    ikePhase1Key.setKeyType(IkeKeyType.PRE_SHARED_KEY);
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
    ikePhase1Key.setKeyType(IkeKeyType.PRE_SHARED_KEY);
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
    ikePhase1Key.setKeyType(IkeKeyType.PRE_SHARED_KEY);
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
    isakmpProfile.setMatchIdentity(new IpWildcard("1.2.3.4:0.0.0.0"));

    IkePhase1Key ikePhase1Key = new IkePhase1Key();
    ikePhase1Key.setKeyHash("test_key");
    ikePhase1Key.setKeyType(IkeKeyType.PRE_SHARED_KEY);
    ikePhase1Key.setLocalInterface("local_interface");
    ikePhase1Key.setRemoteIdentity(new IpWildcard("1.2.3.4:0.0.0.0").toIpSpace());

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
    isakmpProfile.setMatchIdentity(new IpWildcard("2.4.3.4:255.255.0.0"));

    IkePhase1Key ikePhase1Key = new IkePhase1Key();
    ikePhase1Key.setKeyHash("test_key");
    ikePhase1Key.setKeyType(IkeKeyType.PRE_SHARED_KEY);
    ikePhase1Key.setLocalInterface("local_interface");
    ikePhase1Key.setRemoteIdentity(new IpWildcard("1.2.3.5:0.0.0.0").toIpSpace());

    IkePhase1Key matchingKey =
        getMatchingPsk(isakmpProfile, _warnings, ImmutableMap.of(IKE_PHASE1_KEY, ikePhase1Key));

    assertThat(matchingKey, is(nullValue()));
  }
}
