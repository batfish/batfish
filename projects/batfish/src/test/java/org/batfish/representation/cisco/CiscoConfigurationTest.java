package org.batfish.representation.cisco;

import static org.batfish.representation.cisco.CiscoConversions.getMatchingPsk;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.util.Collections;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.IkeKeyType;
import org.batfish.datamodel.IkePhase1Key;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.SourceNat;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.matchers.IkePhase1KeyMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CiscoConfigurationTest {
  private CiscoConfiguration _config;
  private Interface _interface;
  private static final String ACL = "acl";
  private static final String POOL = "pool";
  private static final Ip IP = new Ip("1.2.3.4");
  private Warnings _warnings;

  // Initializes an empty CiscoConfiguration with a single Interface and minimal settings to not
  // crash.
  @Before
  public void before() {
    _config = new CiscoConfiguration(Collections.emptySet());
    _config.setVendor(ConfigurationFormat.ARISTA);
    _config.setHostname("host");
    _config.setAnswerElement(new ConvertConfigurationAnswerElement());
    _interface = new Interface("iface", _config);
    _warnings = new Warnings();
    _warnings.setRedFlagRecord(true);
  }

  @Test
  public void processSourceNatIsConverted() {
    CiscoSourceNat nat = new CiscoSourceNat();
    nat.setAclName(ACL);
    nat.setNatPool(POOL);
    NatPool pool = new NatPool(POOL);
    pool.setFirst(IP);
    pool.setLast(IP);
    _config.getNatPools().put(POOL, pool);

    SourceNat convertedNat =
        _config.processSourceNat(
            nat,
            _interface,
            Collections.singletonMap(ACL, new IpAccessList(ACL, Collections.emptyList())));

    assertThat(convertedNat, notNullValue());
    assertThat(convertedNat.getAcl().getName(), equalTo(ACL));
    assertThat(convertedNat.getPoolIpFirst(), equalTo(IP));
    assertThat(_config.getAnswerElement().getUndefinedReferences().size(), equalTo(0));
  }

  @Test
  public void testGetMatchingPskInvalidLocalIface() {
    IsakmpProfile isakmpProfile = new IsakmpProfile("Missing_Local_Interface");
    // Empty interfaceName value represents incorrect local-address value
    isakmpProfile.setLocalInterfaceName("");
    isakmpProfile.setKeyring("IKE_Phase1_Key");

    IkePhase1Key ikePhase1Key = new IkePhase1Key("IKE_Phase1_Key");
    ikePhase1Key.setKey("test_key");
    ikePhase1Key.setKeyType(IkeKeyType.PRE_SHARED_KEY);
    ikePhase1Key.setLocalInterface("local_interface");

    IkePhase1Key matchingKey =
        getMatchingPsk(
            isakmpProfile, _warnings, ImmutableMap.of(ikePhase1Key.getName(), ikePhase1Key));

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

    IkePhase1Key ikePhase1Key = new IkePhase1Key("IKE_Phase1_Key");
    ikePhase1Key.setKey("test_key");
    ikePhase1Key.setKeyType(IkeKeyType.PRE_SHARED_KEY);
    ikePhase1Key.setLocalInterface("local_interface");

    IkePhase1Key matchingKey =
        getMatchingPsk(
            isakmpProfile, _warnings, ImmutableMap.of(ikePhase1Key.getName(), ikePhase1Key));

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

    IkePhase1Key ikePhase1Key = new IkePhase1Key("IKE_Phase1_Key");
    ikePhase1Key.setKey("test_key");
    ikePhase1Key.setKeyType(IkeKeyType.PRE_SHARED_KEY);
    ikePhase1Key.setLocalInterface("local_interface");

    IkePhase1Key matchingKey =
        getMatchingPsk(
            isakmpProfile, _warnings, ImmutableMap.of(ikePhase1Key.getName(), ikePhase1Key));

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

    IkePhase1Key ikePhase1Key = new IkePhase1Key("IKE_Phase1_Key");
    ikePhase1Key.setKey("test_key");
    ikePhase1Key.setKeyType(IkeKeyType.PRE_SHARED_KEY);
    ikePhase1Key.setLocalInterface("");

    IkePhase1Key matchingKey =
        getMatchingPsk(
            isakmpProfile, _warnings, ImmutableMap.of(ikePhase1Key.getName(), ikePhase1Key));

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
    isakmpProfile.setMatchIdentity(new Ip("1.2.3.4"), new Ip("255.255.255.255"));

    IkePhase1Key ikePhase1Key = new IkePhase1Key("IKE_Phase1_Key");
    ikePhase1Key.setKey("test_key");
    ikePhase1Key.setKeyType(IkeKeyType.PRE_SHARED_KEY);
    ikePhase1Key.setLocalInterface("local_interface");
    ikePhase1Key.setRemoteIdentity(new IpWildcard("1.2.3.4:0.0.0.0"));

    IkePhase1Key matchingKey =
        getMatchingPsk(
            isakmpProfile, _warnings, ImmutableMap.of(ikePhase1Key.getName(), ikePhase1Key));

    assertThat(matchingKey, IkePhase1KeyMatchers.hasKey("test_key"));
  }

  @Test
  public void testGetMatchingPskInvalidKeyring() {
    IsakmpProfile isakmpProfile = new IsakmpProfile("Invalid_Keyring");
    // Empty interfaceName value represents incorrect local-address value
    isakmpProfile.setLocalInterfaceName("local_interface");
    isakmpProfile.setKeyring("IKE_Phase1_Key");
    isakmpProfile.setMatchIdentity(new Ip("1.2.3.4"), new Ip("0.0.0.0"));

    IkePhase1Key ikePhase1Key = new IkePhase1Key("IKE_Phase1_Key");
    ikePhase1Key.setKey("test_key");
    ikePhase1Key.setKeyType(IkeKeyType.PRE_SHARED_KEY);
    ikePhase1Key.setLocalInterface("local_interface");
    ikePhase1Key.setRemoteIdentity(new IpWildcard("1.2.3.5:0.0.0.0"));

    IkePhase1Key matchingKey =
        getMatchingPsk(
            isakmpProfile, _warnings, ImmutableMap.of(ikePhase1Key.getName(), ikePhase1Key));

    assertThat(matchingKey, is(nullValue()));
  }
}
