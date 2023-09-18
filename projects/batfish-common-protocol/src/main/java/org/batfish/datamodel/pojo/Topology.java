package org.batfish.datamodel.pojo;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.pojo.Aggregate.AggregateType;
import org.batfish.datamodel.pojo.Link.LinkType;
import org.batfish.datamodel.vendor_family.VendorFamily;

public class Topology extends BfObject {
  private static final String PROP_TESTRIG_NAME = "testrigName";

  private @Nonnull Set<Aggregate> _aggregates;

  private @Nonnull Set<Interface> _interfaces;

  private @Nonnull Set<Link> _links;

  private @Nonnull Set<Node> _nodes;

  private final @Nonnull String _testrigName;

  public static Topology create(
      String testrigName,
      Map<String, Configuration> configurations,
      org.batfish.datamodel.Topology topology) {

    Topology pojoTopology = new Topology(testrigName);

    for (String nodeName : topology.getNodeEdges().keySet()) {
      Configuration configuration = configurations.get(nodeName);
      if (configuration == null) {
        throw new BatfishException("Node '" + nodeName + "' not found in configurations");
      }
      Node pojoNode =
          new Node(
              configuration.getHostname(),
              configuration.getDeviceModel(),
              configuration.getDeviceType());
      pojoTopology.getNodes().add(pojoNode);

      Map<String, org.batfish.datamodel.Interface> nodeInterfaces =
          configuration.getAllInterfaces();
      // add interfaces and links
      for (Edge edge : topology.getNodeEdges().get(nodeName)) {
        if (!edge.getNode1().equals(nodeName)) {
          // only consider edges where node1 is the source
          continue;
        }
        String interface1 = edge.getInt1();
        String interface2 = edge.getInt2();

        InterfaceType iface1type =
            nodeInterfaces.containsKey(interface1)
                ? nodeInterfaces.get(interface1).getInterfaceType()
                : InterfaceType.UNKNOWN;

        Configuration configuration2 = configurations.get(edge.getNode2());
        assert configuration2 != null; // assuming topology is valid
        InterfaceType iface2type =
            configuration2.getAllInterfaces().containsKey(interface2)
                ? configuration2.getAllInterfaces().get(interface2).getInterfaceType()
                : InterfaceType.UNKNOWN;

        Interface pojoInterface = new Interface(pojoNode.getId(), interface1, iface1type);

        LinkType linkType = Link.interfaceTypesToLinkType(iface1type, iface2type);
        Link pojoLink =
            new Link(
                pojoInterface.getId(),
                Interface.getId(Node.makeId(edge.getNode2()), interface2),
                linkType);

        pojoTopology.getInterfaces().add(pojoInterface);
        pojoTopology.getLinks().add(pojoLink);
      }
    }

    // add nodes that were not in Topology (because they have no Edges)
    for (Configuration configuration : configurations.values()) {
      Node pojoNode =
          new Node(
              configuration.getHostname(),
              configuration.getDeviceModel(),
              configuration.getDeviceType());
      pojoTopology.getNodes().add(pojoNode);
      // add AWS aggregates; put node in the smallest container we can find for them,
      // and then put that container in their container
      if (configuration.getConfigurationFormat() == ConfigurationFormat.AWS) {
        putInAwsAggregate(pojoTopology, configuration, pojoNode);
      }
    }
    return pojoTopology;
  }

  @VisibleForTesting
  static void putInAwsAggregate(Topology pojoTopology, Configuration configuration, Node pojoNode) {
    VendorFamily vendorFamily = configuration.getVendorFamily();
    if (vendorFamily.getAws().getSubnetId() != null) {
      String subnetId = vendorFamily.getAws().getSubnetId();
      Aggregate subnetAggregate = pojoTopology.getOrCreateAggregate(subnetId, AggregateType.SUBNET);
      subnetAggregate.getContents().add(pojoNode.getId());

      String vpcId = vendorFamily.getAws().getVpcId();
      Aggregate vpcAggregate = pojoTopology.getOrCreateAggregate(vpcId, AggregateType.VNET);
      vpcAggregate.getContents().add(subnetAggregate.getId());
    } else if (vendorFamily.getAws().getVpcId() != null) {
      String vpcId = vendorFamily.getAws().getVpcId();
      Aggregate vpcAggregate = pojoTopology.getOrCreateAggregate(vpcId, AggregateType.VNET);
      vpcAggregate.getContents().add(pojoNode.getId());

      String region = vendorFamily.getAws().getRegion();
      Aggregate regionAggregate = pojoTopology.getOrCreateAggregate(region, AggregateType.REGION);
      regionAggregate.getContents().add(vpcAggregate.getId());
      Aggregate awsAggregate = pojoTopology.getOrCreateAggregate("aws", AggregateType.CLOUD);
      awsAggregate.getContents().add(regionAggregate.getId());
    } else if (vendorFamily.getAws().getRegion() != null) {
      String region = vendorFamily.getAws().getRegion();
      Aggregate regionAggregate = pojoTopology.getOrCreateAggregate(region, AggregateType.REGION);
      regionAggregate.getContents().add(pojoNode.getId());

      Aggregate awsAggregate = pojoTopology.getOrCreateAggregate("aws", AggregateType.CLOUD);
      awsAggregate.getContents().add(regionAggregate.getId());
    } else {
      Aggregate awsAggregate = pojoTopology.getOrCreateAggregate("aws", AggregateType.CLOUD);
      awsAggregate.getContents().add(pojoNode.getId());
    }
  }

  public Topology(String testrigName) {
    this(getId(testrigName), testrigName);
  }

  @JsonCreator
  public Topology(
      @JsonProperty(PROP_ID) String topologyId,
      @JsonProperty(PROP_TESTRIG_NAME) String testrigName) {
    super(firstNonNull(topologyId, getId(testrigName)));
    _testrigName = testrigName;
    _aggregates = new HashSet<>();
    _interfaces = new HashSet<>();
    _links = new HashSet<>();
    _nodes = new HashSet<>();
    if (testrigName == null) {
      throw new IllegalArgumentException("Cannot build Topology: testrigName is null");
    }
  }

  public Set<Aggregate> getAggregates() {
    return _aggregates;
  }

  public static String getId(String testrigName) {
    return "topology-" + testrigName;
  }

  public Set<Interface> getInterfaces() {
    return _interfaces;
  }

  public Set<Link> getLinks() {
    return _links;
  }

  public Set<Node> getNodes() {
    return _nodes;
  }

  public Aggregate getOrCreateAggregate(String name, AggregateType aggType) {
    String aggId = Aggregate.makeId(name);
    for (Aggregate aggregate : _aggregates) {
      if (aggregate.getId().equals(aggId)) {
        return aggregate;
      }
    }
    Aggregate aggregate = new Aggregate(name, aggType);
    getAggregates().add(aggregate);
    return aggregate;
  }

  @JsonProperty(PROP_TESTRIG_NAME)
  public String getTestrigName() {
    return _testrigName;
  }

  public void setAggregates(Set<Aggregate> aggregates) {
    _aggregates = aggregates;
  }

  public void setInterfaces(Set<Interface> interfaces) {
    _interfaces = interfaces;
  }

  public void setLinks(Set<Link> links) {
    _links = links;
  }

  public void setNodes(Set<Node> nodes) {
    _nodes = nodes;
  }
}
