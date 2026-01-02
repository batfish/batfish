package org.batfish.datamodel.pojo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.pojo.Aggregate.AggregateType;
import org.batfish.datamodel.vendor_family.AwsFamily;
import org.batfish.datamodel.vendor_family.VendorFamily;
import org.junit.Test;

/** Tests of {@link Topology}. */
public class TopologyTest {

  @Test
  public void testJsonSerialization() {
    Node node = new Node("node");
    Link link = new Link("src", "dst");
    Interface iface = new Interface(node.getId(), "iface");
    Aggregate a = new Aggregate("cloud", AggregateType.CLOUD);
    a.setContents(ImmutableSet.of(node.getId()));

    Topology t = new Topology("testrig");
    t.setNodes(ImmutableSet.of(node));
    t.setInterfaces(ImmutableSet.of(iface));
    t.setLinks(ImmutableSet.of(link));
    t.setAggregates(ImmutableSet.of(a));

    Topology topo = BatfishObjectMapper.clone(t, Topology.class);

    assertThat(topo.getId(), equalTo(Topology.getId("testrig")));
    assertThat(topo.getTestrigName(), equalTo("testrig"));
    assertThat(topo.getAggregates().size(), equalTo(1));
    assertThat(
        topo.getOrCreateAggregate("cloud", AggregateType.CLOUD).getContents().size(), equalTo(1));
    assertThat(topo.getInterfaces().size(), equalTo(1));
    assertThat(topo.getLinks().size(), equalTo(1));
    assertThat(topo.getNodes().size(), equalTo(1));
  }

  /** Tests that the correct topology is created. */
  @Test
  public void testCreate() {
    NetworkFactory nf = new NetworkFactory();

    Configuration c1 = nf.configurationBuilder().setHostname("c1").build();
    Configuration c2 = nf.configurationBuilder().setHostname("c2").build();
    Configuration c3 = nf.configurationBuilder().setHostname("c3").build();

    // create line topology: c1 <-> c2 <-> c3
    org.batfish.datamodel.Interface c12 =
        nf.interfaceBuilder().setOwner(c1).setName("to-c2").setType(InterfaceType.UNKNOWN).build();
    org.batfish.datamodel.Interface c21 =
        nf.interfaceBuilder().setOwner(c2).setName("to-c1").setType(InterfaceType.UNKNOWN).build();
    org.batfish.datamodel.Interface c23 =
        nf.interfaceBuilder().setOwner(c2).setName("to-c3").setType(InterfaceType.UNKNOWN).build();
    org.batfish.datamodel.Interface c32 =
        nf.interfaceBuilder().setOwner(c3).setName("to-c2").setType(InterfaceType.UNKNOWN).build();

    org.batfish.datamodel.Topology rawL3Topology =
        new org.batfish.datamodel.Topology(
            ImmutableSortedSet.of(
                new Edge(c12, c21), new Edge(c21, c12), new Edge(c23, c32), new Edge(c32, c23)));

    Topology pojoTopology =
        Topology.create(
            "test",
            ImmutableMap.of(c1.getHostname(), c1, c2.getHostname(), c2, c3.getHostname(), c3),
            rawL3Topology);

    Node c1Node = new Node(c1.getHostname());
    Node c2Node = new Node(c2.getHostname());
    Node c3Node = new Node(c3.getHostname());

    assertThat(pojoTopology.getNodes(), equalTo(ImmutableSet.of(c1Node, c2Node, c3Node)));

    Interface c12Interface = new Interface(c1Node.getId(), c12.getName());
    Interface c21Interface = new Interface(c2Node.getId(), c21.getName());
    Interface c23Interface = new Interface(c2Node.getId(), c23.getName());
    Interface c32Interface = new Interface(c3Node.getId(), c32.getName());
    assertThat(
        pojoTopology.getInterfaces(),
        equalTo(ImmutableSet.of(c12Interface, c21Interface, c23Interface, c32Interface)));

    assertThat(
        pojoTopology.getLinks(),
        equalTo(
            ImmutableSet.of(
                new Link(c12Interface.getId(), c21Interface.getId()),
                new Link(c21Interface.getId(), c12Interface.getId()),
                new Link(c23Interface.getId(), c32Interface.getId()),
                new Link(c32Interface.getId(), c23Interface.getId()))));
  }

  @Test
  public void testCreateInterfaceType() {
    NetworkFactory nf = new NetworkFactory();

    Configuration c1 = nf.configurationBuilder().setHostname("c1").build();
    Configuration c2 = nf.configurationBuilder().setHostname("c2").build();

    // create line topology: c1 <-> c2
    org.batfish.datamodel.Interface c12 =
        nf.interfaceBuilder().setOwner(c1).setName("to-c2").setType(InterfaceType.TUNNEL).build();
    org.batfish.datamodel.Interface c21 =
        nf.interfaceBuilder().setOwner(c2).setName("to-c1").setType(InterfaceType.TUNNEL).build();

    org.batfish.datamodel.Topology rawL3Topology =
        new org.batfish.datamodel.Topology(
            ImmutableSortedSet.of(new Edge(c12, c21), new Edge(c21, c12)));

    Topology pojoTopology =
        Topology.create(
            "test", ImmutableMap.of(c1.getHostname(), c1, c2.getHostname(), c2), rawL3Topology);

    assertThat(
        pojoTopology.getInterfaces(),
        equalTo(
            ImmutableSet.of(
                new Interface("node-c1", "to-c2", InterfaceType.TUNNEL),
                new Interface("node-c2", "to-c1", InterfaceType.TUNNEL))));
  }

  @Test
  public void testAwsAggregateUsesNodes() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c1 =
        nf.configurationBuilder()
            .setHostname("vpc-1")
            .setConfigurationFormat(ConfigurationFormat.AWS)
            .build();
    VendorFamily vf = new VendorFamily();
    AwsFamily af = new AwsFamily();
    String regionName = "us-west-1";
    af.setRegion(regionName);
    vf.setAws(af);
    c1.setVendorFamily(vf);

    Topology topo =
        Topology.create(
            "ss",
            ImmutableMap.of(c1.getHostname(), c1),
            new org.batfish.datamodel.Topology(ImmutableSortedSet.of()));
    Node topoNode = new Node(c1.getHostname());

    Aggregate expectedAgg = new Aggregate(regionName, AggregateType.REGION);
    expectedAgg.setContents(ImmutableSet.of(topoNode.getId()));

    assertThat(topo.getAggregates(), hasItem(expectedAgg));
  }

  @Test
  public void testPutInAwsAggregateCreatesAwsAggregateIfNeededForVpc() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c1 =
        nf.configurationBuilder()
            .setHostname("vpc-1")
            .setConfigurationFormat(ConfigurationFormat.AWS)
            .build();
    VendorFamily vf = new VendorFamily();
    AwsFamily af = new AwsFamily();
    String regionName = "us-west-1";
    af.setRegion(regionName);
    af.setVpcId("vpc-1");
    vf.setAws(af);
    c1.setVendorFamily(vf);
    Topology topo = new Topology("ss");
    Node topoNode = new Node(c1.getHostname());

    Topology.putInAwsAggregate(topo, c1, topoNode);

    // AWS aggregate is present and contains region of the node c1
    Aggregate expectedAgg = new Aggregate("aws", AggregateType.CLOUD);
    expectedAgg.setContents(ImmutableSet.of("aggregate-us-west-1"));

    assertThat(topo.getAggregates(), hasItem(expectedAgg));
  }
}
