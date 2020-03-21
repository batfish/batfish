package org.batfish.common.util;

import static org.batfish.common.util.CompletionMetadataUtils.WELL_KNOWN_IPS;
import static org.batfish.common.util.CompletionMetadataUtils.addressGroupDisplayString;
import static org.batfish.common.util.CompletionMetadataUtils.getFilterNames;
import static org.batfish.common.util.CompletionMetadataUtils.getInterfaces;
import static org.batfish.common.util.CompletionMetadataUtils.getIps;
import static org.batfish.common.util.CompletionMetadataUtils.getMlagIds;
import static org.batfish.common.util.CompletionMetadataUtils.getNodes;
import static org.batfish.common.util.CompletionMetadataUtils.getPrefixes;
import static org.batfish.common.util.CompletionMetadataUtils.getRoutingPolicyNames;
import static org.batfish.common.util.CompletionMetadataUtils.getStructureNames;
import static org.batfish.common.util.CompletionMetadataUtils.getVrfs;
import static org.batfish.common.util.CompletionMetadataUtils.getZones;
import static org.batfish.common.util.CompletionMetadataUtils.interfaceDisplayString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.batfish.common.autocomplete.IpCompletionMetadata;
import org.batfish.common.autocomplete.IpCompletionRelevance;
import org.batfish.common.autocomplete.NodeCompletionMetadata;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.AuthenticationKeyChain;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.IkePhase1Key;
import org.batfish.datamodel.IkePhase1Policy;
import org.batfish.datamodel.IkePhase1Proposal;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6AccessList;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpsecPhase2Policy;
import org.batfish.datamodel.IpsecPhase2Proposal;
import org.batfish.datamodel.IpsecStaticPeerConfig;
import org.batfish.datamodel.Mlag;
import org.batfish.datamodel.Route6FilterList;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.Zone;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.referencelibrary.AddressGroup;
import org.batfish.referencelibrary.GeneratedRefBookUtils;
import org.batfish.referencelibrary.GeneratedRefBookUtils.BookType;
import org.batfish.referencelibrary.ReferenceBook;
import org.junit.Test;

public final class CompletionMetadataUtilsTest {

  private static Configuration createTestConfiguration(
      String nodeName, ConfigurationFormat configFormat, String... interfaceNames) {
    Configuration config = new Configuration(nodeName, configFormat);
    for (String interfaceName : interfaceNames) {
      config
          .getAllInterfaces()
          .put(interfaceName, Interface.builder().setName(interfaceName).setOwner(config).build());
    }
    return config;
  }

  private static Map<Ip, IpCompletionMetadata> createWellKnownIpCompletion(Map<Ip, String> ipMap) {
    return ipMap.entrySet().stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey,
                e ->
                    new IpCompletionMetadata(
                        new IpCompletionRelevance(e.getValue(), e.getValue()))));
  }

  @Test
  public void testAddressGroupDisplayString() {
    Configuration cfg = new Configuration("host", ConfigurationFormat.CISCO_IOS);
    Configuration cfgHuman = new Configuration("host", ConfigurationFormat.CISCO_IOS);
    cfgHuman.setHumanName("human");

    String bookName = GeneratedRefBookUtils.getName(cfg.getHostname(), BookType.PublicIps);
    assertThat(addressGroupDisplayString(cfg, bookName, "group"), equalTo("host public IP"));
    assertThat(
        addressGroupDisplayString(cfgHuman, bookName, "group"), equalTo("host public IP (human)"));
  }

  @Test
  public void testInterfaceDisplayString() {
    Configuration cfg = new Configuration("host", ConfigurationFormat.CISCO_IOS);
    Configuration cfgHuman = new Configuration("host", ConfigurationFormat.CISCO_IOS);
    cfgHuman.setHumanName("human");
    Interface iface = Interface.builder().setName("iface").build();

    assertThat(interfaceDisplayString(cfg, iface), equalTo("host[iface]"));
    assertThat(interfaceDisplayString(cfgHuman, iface), equalTo("host[iface] (human)"));
  }

  @Test
  public void testGetFilterNames() {
    String filter1 = "filter1";
    String filter2 = "filter2";

    IpAccessList ipAccessList1 = IpAccessList.builder().setName(filter1).build();
    IpAccessList ipAccessList2 = IpAccessList.builder().setName(filter2).build();

    Map<String, Configuration> configs = new HashMap<>();
    Configuration config = createTestConfiguration("config1", ConfigurationFormat.HOST);
    config.setIpAccessLists(ImmutableSortedMap.of(filter1, ipAccessList1, filter2, ipAccessList2));
    configs.put("config1", config);

    assertThat(getFilterNames(configs), equalTo(ImmutableSet.of(filter1, filter2)));
  }

  @Test
  public void testGetInterfaces() {
    String nodeName = "nodeName";

    String int1 = "int1";
    String int2 = "int2";

    Map<String, Configuration> configs = new HashMap<>();
    Configuration config = createTestConfiguration(nodeName, ConfigurationFormat.HOST, int1, int2);
    configs.put(nodeName, config);

    assertThat(
        getInterfaces(configs),
        equalTo(
            ImmutableSet.of(
                NodeInterfacePair.of(nodeName, int1), NodeInterfacePair.of(nodeName, int2))));
  }

  @Test
  public void testGetIps() {
    String nodeName = "nodeName";

    String int1 = "int1";
    String int2 = "int2";

    Ip ip1 = Ip.parse("10.1.3.7");
    Ip ip2 = Ip.parse("128.212.155.30");
    Ip ip3 = Ip.parse("124.51.32.2");

    String address1 = ip1 + "/30";
    String address2 = ip2 + "/24";
    String address3 = ip3 + "/20";

    InterfaceAddress interfaceAddress1 = ConcreteInterfaceAddress.parse(address1);
    InterfaceAddress interfaceAddress2 = ConcreteInterfaceAddress.parse(address2);
    InterfaceAddress interfaceAddress3 = ConcreteInterfaceAddress.parse(address3);

    Map<String, Configuration> configs = new HashMap<>();
    Configuration config = createTestConfiguration(nodeName, ConfigurationFormat.HOST, int1, int2);

    Interface iface1 = config.getAllInterfaces().get(int1);
    iface1.setAllAddresses(ImmutableSet.of(interfaceAddress1, interfaceAddress2));

    Interface iface2 = config.getAllInterfaces().get(int2);
    iface2.setAllAddresses(ImmutableSet.of(interfaceAddress2, interfaceAddress3));

    configs.put(nodeName, config);

    assertThat(
        getIps(configs),
        equalTo(
            ImmutableMap.builder()
                .put(
                    ip1,
                    new IpCompletionMetadata(
                        new IpCompletionRelevance(
                            interfaceDisplayString(config, iface1),
                            config.getHostname(),
                            iface1.getName())))
                .put(
                    ip2,
                    new IpCompletionMetadata(
                        ImmutableList.of(
                            new IpCompletionRelevance(
                                interfaceDisplayString(config, iface1),
                                config.getHostname(),
                                iface1.getName()),
                            new IpCompletionRelevance(
                                interfaceDisplayString(config, iface2),
                                config.getHostname(),
                                iface2.getName()))))
                .put(
                    ip3,
                    new IpCompletionMetadata(
                        new IpCompletionRelevance(
                            interfaceDisplayString(config, iface2),
                            config.getHostname(),
                            iface2.getName())))
                .putAll(createWellKnownIpCompletion(WELL_KNOWN_IPS))
                .build()));
  }

  @Test
  public void testGetIpsGeneratedReferenceBooks() {
    ReferenceBook book1 =
        ReferenceBook.builder("book1")
            .setAddressGroups(
                ImmutableList.of(
                    new AddressGroup(ImmutableSortedSet.of("11.11.11.11", "2.2.2.2"), "ag1"),
                    new AddressGroup(
                        ImmutableSortedSet.of("3.3.3.3", "1.1.1.1/24", "1.1.1.1:0.0.0.8"), "ag2")))
            .build();

    ReferenceBook book2 =
        ReferenceBook.builder("book2")
            .setAddressGroups(
                ImmutableList.of(
                    new AddressGroup(ImmutableSortedSet.of("3.3.3.3", "4.4.4.4"), "ag1")))
            .build();

    Map<String, Configuration> configs = new HashMap<>();
    Configuration config = createTestConfiguration("node", ConfigurationFormat.HOST);

    config.getGeneratedReferenceBooks().put(book1.getName(), book1);
    config.getGeneratedReferenceBooks().put(book2.getName(), book2);

    configs.put("node", config);

    assertThat(
        getIps(configs),
        equalTo(
            ImmutableMap.builder()
                .put(
                    Ip.parse("11.11.11.11"),
                    new IpCompletionMetadata(
                        new IpCompletionRelevance(
                            addressGroupDisplayString(config, book1.getName(), "ag1"),
                            config.getHostname(),
                            config.getHumanName(),
                            "ag1")))
                .put(
                    Ip.parse("2.2.2.2"),
                    new IpCompletionMetadata(
                        new IpCompletionRelevance(
                            addressGroupDisplayString(config, book1.getName(), "ag1"),
                            config.getHostname(),
                            config.getHumanName(),
                            "ag1")))
                .put(
                    Ip.parse("3.3.3.3"),
                    new IpCompletionMetadata(
                        ImmutableList.of(
                            new IpCompletionRelevance(
                                addressGroupDisplayString(config, book1.getName(), "ag2"),
                                config.getHostname(),
                                config.getHumanName(),
                                "ag2"),
                            new IpCompletionRelevance(
                                addressGroupDisplayString(config, book2.getName(), "ag1"),
                                config.getHostname(),
                                config.getHumanName(),
                                "ag1"))))
                .put(
                    Ip.parse("4.4.4.4"),
                    new IpCompletionMetadata(
                        new IpCompletionRelevance(
                            addressGroupDisplayString(config, book2.getName(), "ag1"),
                            config.getHostname(),
                            config.getHumanName(),
                            "ag1")))
                .putAll(createWellKnownIpCompletion(WELL_KNOWN_IPS))
                .build()));
  }

  /** Test that the well-known IPs are added only if they otherwise don't exist in the snapshot */
  @Test
  public void testGetIpsNoInterference() {
    Ip ip1 = Ip.parse("8.8.8.8");
    assertTrue("Test assumes that 8.8.8.8 is a well-known IP", WELL_KNOWN_IPS.containsKey(ip1));
    String nodeName = "nodeName";
    String int1 = "int1";
    String address1 = ip1 + "/30";
    InterfaceAddress interfaceAddress1 = ConcreteInterfaceAddress.parse(address1);

    Map<String, Configuration> configs = new HashMap<>();
    Configuration config = createTestConfiguration(nodeName, ConfigurationFormat.HOST, int1);

    Interface iface1 = config.getAllInterfaces().get(int1);
    iface1.setAllAddresses(ImmutableSet.of(interfaceAddress1));

    configs.put(nodeName, config);

    // we should get 8.8.8.8 from the config data and rest from well-known ips
    assertThat(
        getIps(configs),
        equalTo(
            ImmutableMap.builder()
                .put(
                    ip1,
                    new IpCompletionMetadata(
                        new IpCompletionRelevance(
                            interfaceDisplayString(config, iface1),
                            config.getHostname(),
                            iface1.getName())))
                .putAll(
                    createWellKnownIpCompletion(
                        WELL_KNOWN_IPS.entrySet().stream()
                            .filter(e -> !e.getKey().equals(ip1))
                            .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue))))
                .build()));
  }

  @Test
  public void testGetMlags() {
    String mlag1 = "mlag1";
    String mlag2 = "mlag2";

    Map<String, Configuration> configs = new HashMap<>();
    Configuration config = createTestConfiguration("config1", ConfigurationFormat.HOST);

    config.setMlags(
        ImmutableSortedMap.of(
            mlag1,
            Mlag.builder().setId(mlag1).build(),
            mlag2,
            Mlag.builder().setId(mlag2).build()));

    configs.put("config1", config);

    assertThat(getMlagIds(configs), equalTo(ImmutableSet.of(mlag1, mlag2)));
  }

  @Test
  public void testGetNodes() {
    String node1 = "node1";
    String node2 = "node2";
    String humanName2 = "humanName2";

    Configuration config1 = createTestConfiguration(node1, ConfigurationFormat.HOST);
    Configuration config2 = createTestConfiguration(node2, ConfigurationFormat.HOST);
    config2.setHumanName(humanName2);

    Map<String, Configuration> configs = ImmutableMap.of(node1, config1, node2, config2);

    assertThat(
        getNodes(configs),
        equalTo(
            ImmutableMap.of(
                node1,
                new NodeCompletionMetadata(null),
                node2,
                new NodeCompletionMetadata(humanName2))));
  }

  @Test
  public void testGetPrefixes() {
    String nodeName = "nodeName";

    String int1 = "int1";
    String int2 = "int2";

    String address1 = "10.1.3.7/30";
    String address2 = "128.212.155.30/24";
    String address3 = "124.51.32.2/20";

    ConcreteInterfaceAddress interfaceAddress1 = ConcreteInterfaceAddress.parse(address1);
    ConcreteInterfaceAddress interfaceAddress2 = ConcreteInterfaceAddress.parse(address2);
    ConcreteInterfaceAddress interfaceAddress3 = ConcreteInterfaceAddress.parse(address3);

    Map<String, Configuration> configs = new HashMap<>();
    Configuration config = createTestConfiguration(nodeName, ConfigurationFormat.HOST, int1, int2);

    config
        .getAllInterfaces()
        .get(int1)
        .setAllAddresses(ImmutableSet.of(interfaceAddress1, interfaceAddress2));
    config
        .getAllInterfaces()
        .get(int2)
        .setAllAddresses(ImmutableSet.of(interfaceAddress2, interfaceAddress3));

    configs.put(nodeName, config);

    assertThat(
        getPrefixes(configs),
        equalTo(
            ImmutableSet.of(
                interfaceAddress1.getPrefix().toString(),
                interfaceAddress2.getPrefix().toString(),
                interfaceAddress3.getPrefix().toString())));
  }

  @Test
  public void testGetPrefixesGeneratedReferenceBooks() {
    ReferenceBook book1 =
        ReferenceBook.builder("book1")
            .setAddressGroups(
                ImmutableList.of(
                    new AddressGroup(ImmutableSortedSet.of("1.1.1.1/1", "2.2.2.2/2"), "ag1"),
                    new AddressGroup(ImmutableSortedSet.of("3.3.3.3", "1.1.1.1:0.0.0.8"), "ag2")))
            .build();

    ReferenceBook book2 =
        ReferenceBook.builder("book2")
            .setAddressGroups(
                ImmutableList.of(
                    new AddressGroup(ImmutableSortedSet.of("3.3.3.3/3", "4.4.4.4"), "ag1")))
            .build();

    Map<String, Configuration> configs = new HashMap<>();
    Configuration config = createTestConfiguration("node", ConfigurationFormat.HOST);

    config.getGeneratedReferenceBooks().put(book1.getName(), book1);
    config.getGeneratedReferenceBooks().put(book2.getName(), book2);

    configs.put("node", config);

    assertThat(
        getPrefixes(configs), equalTo(ImmutableSet.of("1.1.1.1/1", "2.2.2.2/2", "3.3.3.3/3")));
  }

  @Test
  public void testGetRoutingPolicyNames() {
    String policy1 = "policy1";
    String policy2 = "policy2";

    RoutingPolicy routingPolicy1 = RoutingPolicy.builder().setName(policy1).build();
    RoutingPolicy routingPolicy2 = RoutingPolicy.builder().setName(policy2).build();

    Map<String, Configuration> configs = new HashMap<>();
    Configuration config = createTestConfiguration("config1", ConfigurationFormat.HOST);
    config.setRoutingPolicies(
        ImmutableSortedMap.of(policy1, routingPolicy1, policy2, routingPolicy2));
    configs.put("config1", config);

    assertThat(getRoutingPolicyNames(configs), equalTo(ImmutableSet.of(policy1, policy2)));
  }

  @Test
  public void testGetStructureNames() {
    String nodeName = "nodeName";

    String asPathAccessListName = "asPathAccessList";
    String authenticationKeyChainName = "authenticationKeyChain";
    String communityListName = "communityList";
    String ikePhase1KeyName = "ikePhase1Key";
    String ikePhase1PolicyName = "ikePhase1Policy";
    String ikePhase1ProposalName = "ikePhase1Proposal";
    String ipAccessListName = "ipAccessList";
    String ip6AccessListName = "ip6AccessList";
    String ipsecPhase2PolicyName = "ipsecPhase2Policy";
    String ipsecPhase2ProposalName = "ipsecPhase2Proposal";
    String ipsecPeerConfigName = "ipsecPeerConfig";
    String routeFilterListName = "routeFilterList";
    String route6FilterListName = "route6FilterList";
    String routingPolicyName = "routingPolicyName";
    String vrfName = "vrf";
    String zoneName = "zone";

    Map<String, Configuration> configs = new HashMap<>();
    Configuration config = createTestConfiguration(nodeName, ConfigurationFormat.HOST);

    config.setAsPathAccessLists(
        ImmutableSortedMap.of(
            asPathAccessListName, new AsPathAccessList(asPathAccessListName, null)));
    config.setAuthenticationKeyChains(
        ImmutableSortedMap.of(
            authenticationKeyChainName, new AuthenticationKeyChain(authenticationKeyChainName)));
    config.setCommunityLists(
        ImmutableSortedMap.of(
            communityListName, new CommunityList(communityListName, ImmutableList.of(), true)));
    config.setIkePhase1Keys(ImmutableSortedMap.of(ikePhase1KeyName, new IkePhase1Key()));
    config.setIkePhase1Policies(
        ImmutableSortedMap.of(ikePhase1PolicyName, new IkePhase1Policy(ikePhase1PolicyName)));
    config.setIkePhase1Proposals(
        ImmutableSortedMap.of(ikePhase1ProposalName, new IkePhase1Proposal(ikePhase1ProposalName)));
    config.setIpAccessLists(
        ImmutableSortedMap.of(
            ipAccessListName, IpAccessList.builder().setName(ipAccessListName).build()));
    config.setIp6AccessLists(
        ImmutableSortedMap.of(ip6AccessListName, new Ip6AccessList(ip6AccessListName)));
    config.setIpsecPhase2Policies(
        ImmutableSortedMap.of(ipsecPhase2PolicyName, new IpsecPhase2Policy()));
    config.setIpsecPhase2Proposals(
        ImmutableSortedMap.of(ipsecPhase2ProposalName, new IpsecPhase2Proposal()));
    config.setIpsecPeerConfigs(
        ImmutableSortedMap.of(ipsecPeerConfigName, IpsecStaticPeerConfig.builder().build()));
    config.setRouteFilterLists(
        ImmutableSortedMap.of(routeFilterListName, new RouteFilterList(routeFilterListName)));
    config.setRoute6FilterLists(
        ImmutableSortedMap.of(route6FilterListName, new Route6FilterList(route6FilterListName)));
    config.setRoutingPolicies(
        ImmutableSortedMap.of(routingPolicyName, new RoutingPolicy(routingPolicyName, null)));
    config.setVrfs(ImmutableSortedMap.of(vrfName, new Vrf(vrfName)));
    config.setZones(ImmutableSortedMap.of(zoneName, new Zone(zoneName)));

    configs.put(nodeName, config);

    assertThat(
        getStructureNames(configs),
        equalTo(
            ImmutableSet.of(
                asPathAccessListName,
                authenticationKeyChainName,
                communityListName,
                ikePhase1KeyName,
                ikePhase1PolicyName,
                ikePhase1ProposalName,
                ipAccessListName,
                ip6AccessListName,
                ipsecPhase2PolicyName,
                ipsecPhase2ProposalName,
                ipsecPeerConfigName,
                routeFilterListName,
                route6FilterListName,
                routingPolicyName,
                vrfName,
                zoneName)));
  }

  @Test
  public void testGetVrfs() {
    String int1 = "int1";
    String int2 = "int2";
    String int3 = "int3";

    String vrf1 = "vrf1";
    String vrf2 = "vrf2";

    Map<String, Configuration> configs = new HashMap<>();
    Configuration config =
        createTestConfiguration("config1", ConfigurationFormat.HOST, int1, int2, int3);
    config.getAllInterfaces().get(int1).setVrfName(vrf1);
    config.getAllInterfaces().get(int2).setVrfName(vrf2);
    config.getAllInterfaces().get(int3).setVrfName(vrf1);
    configs.put("config1", config);

    assertThat(getVrfs(configs), equalTo(ImmutableSet.of(vrf1, vrf2)));
  }

  @Test
  public void testGetZones() {
    String zone1 = "zone1";
    String zone2 = "zone2";

    Map<String, Configuration> configs = new HashMap<>();
    Configuration config = createTestConfiguration("config1", ConfigurationFormat.HOST);

    config.setZones(ImmutableSortedMap.of(zone1, new Zone(zone1), zone2, new Zone(zone2)));

    configs.put("config1", config);

    assertThat(getZones(configs), equalTo(ImmutableSet.of(zone1, zone2)));
  }
}
