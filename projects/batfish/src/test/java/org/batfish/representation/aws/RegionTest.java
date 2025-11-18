package org.batfish.representation.aws;

import static org.batfish.datamodel.IpProtocol.TCP;
import static org.batfish.datamodel.matchers.TraceTreeMatchers.hasChildren;
import static org.batfish.datamodel.matchers.TraceTreeMatchers.hasTraceElement;
import static org.batfish.representation.aws.AwsConfiguration.AWS_SERVICES_GATEWAY_NODE_NAME;
import static org.batfish.representation.aws.Region.computeAntiSpoofingFilter;
import static org.batfish.representation.aws.Region.eniIngressAclName;
import static org.batfish.representation.aws.Utils.getTraceElementForRule;
import static org.batfish.representation.aws.Utils.getTraceElementForSecurityGroup;
import static org.batfish.representation.aws.Utils.newAwsConfiguration;
import static org.batfish.representation.aws.Utils.traceElementForAddress;
import static org.batfish.representation.aws.Utils.traceElementForDstPorts;
import static org.batfish.representation.aws.Utils.traceElementForProtocol;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.util.List;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.TestInterface;
import org.batfish.datamodel.acl.AclLineEvaluator;
import org.batfish.datamodel.acl.AclTracer;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.trace.TraceTree;
import org.batfish.referencelibrary.AddressGroup;
import org.batfish.referencelibrary.GeneratedRefBookUtils;
import org.batfish.referencelibrary.GeneratedRefBookUtils.BookType;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.representation.aws.IpPermissions.AddressType;
import org.batfish.representation.aws.IpPermissions.IpRange;
import org.junit.Test;

/** Tests for {@link Region} */
public class RegionTest {

  public static final String CONFIGURATION_NAME = "c";

  /** Test that we don't warn the user upon encountering an empty list with an unknown key type */
  @Test
  public void testAddConfigElementUnknownKeyEmptyList() throws IOException {

    JsonNode json = BatfishObjectMapper.mapper().readTree("{ \"stranger\" :  [] }");

    ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
    Region region = new Region("r1");
    region.addConfigElement(json, null, pvcae);

    assertTrue(pvcae.getWarnings().isEmpty());
  }

  /** Test that we warn the user upon encountering a non-empty list with an unknown key type */
  @Test
  public void testAddConfigElementUnknownKeyNonEmptyList() throws IOException {

    JsonNode json = BatfishObjectMapper.mapper().readTree("{ \"stranger\" :  [1] }");

    ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
    Region region = new Region("r1");
    region.addConfigElement(json, null, pvcae);

    Warning warning =
        Iterables.getOnlyElement(
            Iterables.getOnlyElement(pvcae.getWarnings().values()).getUnimplementedWarnings());
    assertTrue(warning.getText().startsWith("Unrecognized element"));
  }

  /** Test that we warn the user upon encountering a non-list with an unknown key type */
  @Test
  public void testAddConfigElementUnknownKeyNonList() throws IOException {

    JsonNode json = BatfishObjectMapper.mapper().readTree("{ \"stranger\" :  {} }");

    ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
    Region region = new Region("r1");
    region.addConfigElement(json, null, pvcae);

    Warning warning =
        Iterables.getOnlyElement(
            Iterables.getOnlyElement(pvcae.getWarnings().values()).getUnimplementedWarnings());
    assertTrue(warning.getText().startsWith("Unrecognized element"));
  }

  private static Region createTestRegion() {
    Region region = new Region("test");

    // add two security groups
    SecurityGroup sg1 =
        new SecurityGroup(
            "sg-001",
            "sg-1",
            ImmutableList.of(),
            ImmutableList.of(
                new IpPermissions(
                    "tcp",
                    22,
                    22,
                    ImmutableList.of(new IpRange(Prefix.parse("2.2.2.0/24"))),
                    ImmutableList.of(),
                    ImmutableList.of())),
            "vpc");
    region.getSecurityGroups().put(sg1.getId(), sg1);
    SecurityGroup sg2 =
        new SecurityGroup(
            "sg-002",
            "sg-2",
            ImmutableList.of(),
            ImmutableList.of(
                new IpPermissions(
                    "tcp",
                    25,
                    25,
                    ImmutableList.of(new IpRange(Prefix.parse("2.2.2.0/24"))),
                    ImmutableList.of(),
                    ImmutableList.of())),
            "vpc");
    region.getSecurityGroups().put(sg2.getId(), sg2);

    return region;
  }

  @Test
  public void testApplyInstanceInterfaceAcls() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname(CONFIGURATION_NAME)
            .build();
    Interface iface =
        nf.interfaceBuilder()
            .setOwner(c)
            .setAddress(ConcreteInterfaceAddress.parse("12.12.12.1/24"))
            .setVrf(nf.vrfBuilder().setOwner(c).build())
            .build();
    Region region = createTestRegion();
    NetworkInterface ni =
        new NetworkInterface(
            iface.getName(),
            "subnet",
            "vpc",
            ImmutableList.of("sg-001", "sg-002"),
            ImmutableList.of(new PrivateIpAddress(true, Ip.parse("12.12.12.1"), null)),
            "desc",
            c.getHostname(),
            ImmutableMap.of());
    region.getNetworkInterfaces().put(ni.getId(), ni);
    ConvertedConfiguration cfg = new ConvertedConfiguration();
    cfg.addNode(c);
    region.convertSecurityGroups(cfg, new Warnings());
    region.applySecurityGroups(cfg, new Warnings());

    // security groups sg-001 and sg-002 converted to ExprAclLines
    assertThat(c.getIpAccessLists(), hasKey("~INGRESS~SECURITY-GROUP~sg-1~sg-001~"));
    assertThat(c.getIpAccessLists(), hasKey("~INGRESS~SECURITY-GROUP~sg-2~sg-002~"));

    // Decision: empty egress ACL is not converted
    assertThat(c.getIpAccessLists(), not(hasKey("~EGRESS~SECURITY-GROUP~sg-1~sg-001~")));
    assertThat(c.getIpAccessLists(), not(hasKey("~EGRESS~SECURITY-GROUP~sg-2~sg-002~")));

    // incoming and outgoing filter on the interface refers to the two ACLs using AclAclLines
    assertThat(
        iface.getIncomingFilter().getLines(),
        equalTo(
            ImmutableList.of(
                new AclAclLine(
                    "Security Group sg-1",
                    "~INGRESS~SECURITY-GROUP~sg-1~sg-001~",
                    getTraceElementForSecurityGroup("sg-1"),
                    null),
                new AclAclLine(
                    "Security Group sg-2",
                    "~INGRESS~SECURITY-GROUP~sg-2~sg-002~",
                    getTraceElementForSecurityGroup("sg-2"),
                    null))));

    assertThat(
        iface.getOutgoingFilter().getLines(),
        equalTo(ImmutableList.of(computeAntiSpoofingFilter(iface))));
  }

  @Test
  public void testSecurityGroupAclTracer() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname(CONFIGURATION_NAME)
            .build();
    Interface i =
        nf.interfaceBuilder()
            .setOwner(c)
            .setAddress(ConcreteInterfaceAddress.parse("12.12.12.1/24"))
            .setVrf(nf.vrfBuilder().setOwner(c).build())
            .build();
    Region region = createTestRegion();
    NetworkInterface ni =
        new NetworkInterface(
            i.getName(),
            "subnet",
            "vpc",
            ImmutableList.of("sg-001", "sg-002"),
            ImmutableList.of(new PrivateIpAddress(true, Ip.parse("12.12.12.1"), null)),
            "desc",
            c.getHostname(),
            ImmutableMap.of());
    region.getNetworkInterfaces().put(ni.getId(), ni);
    ConvertedConfiguration cfg = new ConvertedConfiguration();
    cfg.addNode(c);
    region.convertSecurityGroups(cfg, new Warnings());
    region.applySecurityGroups(cfg, new Warnings());
    IpAccessList ingressAcl = c.getIpAccessLists().get(eniIngressAclName(ni.getId()));

    Flow permittedFlow =
        Flow.builder()
            .setIpProtocol(TCP)
            .setSrcPort(22)
            .setSrcIp(Ip.parse("2.2.2.2"))
            .setDstPort(22)
            .setIngressNode("c")
            .build();
    Flow deniedFlow =
        Flow.builder()
            .setIpProtocol(TCP)
            .setSrcPort(23)
            .setSrcIp(Ip.parse("2.2.2.2"))
            .setDstPort(23)
            .setIngressNode("c")
            .build();
    List<TraceTree> root =
        AclTracer.trace(
            ingressAcl,
            permittedFlow,
            null,
            c.getIpAccessLists(),
            ImmutableMap.of(),
            ImmutableMap.of());

    assertThat(
        root,
        contains(
            allOf(
                hasTraceElement(getTraceElementForSecurityGroup("sg-1")),
                hasChildren(
                    contains(
                        allOf(
                            hasTraceElement(getTraceElementForRule(null)),
                            hasChildren(
                                containsInAnyOrder(
                                    hasTraceElement(traceElementForProtocol(TCP)),
                                    hasTraceElement(traceElementForDstPorts(22, 22)),
                                    hasTraceElement(
                                        traceElementForAddress(
                                            "source", "2.2.2.0/24", AddressType.CIDR_IP))))))))));
    root =
        AclTracer.trace(
            ingressAcl,
            deniedFlow,
            null,
            c.getIpAccessLists(),
            ImmutableMap.of(),
            ImmutableMap.of());
    assertThat(root, empty());
  }

  @Test
  public void testComputeAntiSpoofingFilter() {
    Ip validSourceIp = Ip.parse("10.10.10.10");
    Interface iface =
        TestInterface.builder()
            .setName("test")
            .setAddresses(ConcreteInterfaceAddress.create(validSourceIp, Prefix.MAX_PREFIX_LENGTH))
            .build();
    AclLine antiSpoofingLine = computeAntiSpoofingFilter(iface);
    assertThat(
        new AclLineEvaluator(
                Flow.builder().setSrcIp(validSourceIp).setIngressNode("aa").build(),
                "blah",
                ImmutableMap.of(),
                ImmutableMap.of())
            .visit(antiSpoofingLine),
        nullValue()); // pass through
    assertThat(
        new AclLineEvaluator(
                Flow.builder().setSrcIp(Ip.parse("6.6.6.6")).setIngressNode("aa").build(),
                "blah",
                ImmutableMap.of(),
                ImmutableMap.of())
            .visit(antiSpoofingLine),
        equalTo(LineAction.DENY));
  }

  @Test
  public void testGetAddresses() {
    Address addr1 = new Address(Ip.parse("1.2.3.4"), "i-1234", Ip.parse("4.3.2.1"), "alloc-1");
    Address addr2 = new Address(Ip.parse("11.22.33.44"), null, null, "alloc-2");
    Region region =
        Region.builder("r1")
            .setAddresses(ImmutableMap.of("1.2.3.4", addr1, "11.22.33.44", addr2))
            .build();
    assertThat(region.getAddresses(), equalTo(ImmutableSet.of(addr1, addr2)));
  }

  @Test
  public void testAddPrefixListAddressBook() {
    PrefixList plist1 =
        new PrefixList("plist1", ImmutableList.of(Prefix.parse("1.1.1.0/24")), "plist1Name");
    PrefixList plist2 =
        new PrefixList("plist2", ImmutableList.of(Prefix.parse("2.2.2.2/32")), "plist2Name");
    Region region =
        Region.builder("r")
            .setPrefixLists(ImmutableMap.of(plist1.getId(), plist1, plist2.getId(), plist2))
            .build();

    String bookName =
        GeneratedRefBookUtils.getName(AWS_SERVICES_GATEWAY_NODE_NAME, BookType.AwsSeviceIps);

    AddressGroup currentAddressGroup =
        new AddressGroup(ImmutableSortedSet.of("3.3.3.3"), "current");

    ConvertedConfiguration viConfigs = new ConvertedConfiguration();
    Configuration awsServicesNode = newAwsConfiguration(AWS_SERVICES_GATEWAY_NODE_NAME, "aws");
    awsServicesNode
        .getGeneratedReferenceBooks()
        .put(
            bookName,
            ReferenceBook.builder(bookName)
                .setAddressGroups(ImmutableList.of(currentAddressGroup))
                .build());
    viConfigs.addNode(awsServicesNode);

    region.addPrefixListReferenceBook(viConfigs, new Warnings());

    assertThat(
        awsServicesNode.getGeneratedReferenceBooks().get(bookName),
        equalTo(
            ReferenceBook.builder(bookName)
                .setAddressGroups(
                    ImmutableList.of(
                        new AddressGroup(
                            // .1 address is picked for 1.1.1.0/24
                            ImmutableSortedSet.of("1.1.1.1"), plist1.getPrefixListName()),
                        new AddressGroup(
                            // the first and only address is picked for 2.2.2.2
                            ImmutableSortedSet.of("2.2.2.2"), plist2.getPrefixListName()),
                        // the original address group is still present
                        currentAddressGroup))
                .build()));
  }
}
