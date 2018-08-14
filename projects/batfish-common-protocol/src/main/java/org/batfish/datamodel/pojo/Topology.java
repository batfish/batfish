package org.batfish.datamodel.pojo;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.pojo.Aggregate.AggregateType;

public class Topology extends BfObject {

  private static final String PROP_TESTRIG_NAME = "testrigName";

  @Nonnull private Set<Aggregate> _aggregates;

  @Nonnull private Set<Interface> _interfaces;

  @Nonnull private Set<Link> _links;

  @Nonnull private Set<Node> _nodes;

  @Nonnull private final String _testrigName;

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
      Node pojoNode = new Node(configuration.getHostname(), configuration.getDeviceType());
      pojoTopology.getNodes().add(pojoNode);

      // add interfaces and links
      for (Edge edge : topology.getNodeEdges().get(nodeName)) {
        org.batfish.datamodel.pojo.Interface pojoInterface =
            new org.batfish.datamodel.pojo.Interface(
                pojoNode.getId(), edge.getInterface1().getInterface());

        Link pojoLink =
            new Link(
                pojoInterface.getId(),
                org.batfish.datamodel.pojo.Interface.getId(
                    Node.makeId(edge.getNode2()), edge.getInt2()));

        pojoTopology.getInterfaces().add(pojoInterface);
        pojoTopology.getLinks().add(pojoLink);
      }

      // add AWS aggregates; put node in the smallest container we can find for them,
      // and then put that container in their container
      if (configuration.getConfigurationFormat() == ConfigurationFormat.AWS) {
        if (configuration.getVendorFamily().getAws().getSubnetId() != null) {
          String subnetId = configuration.getVendorFamily().getAws().getSubnetId();
          Aggregate subnetAggregate =
              pojoTopology.getOrCreateAggregate(subnetId, AggregateType.SUBNET);
          subnetAggregate.getContents().add(pojoNode.getId());

          String vpcId = configuration.getVendorFamily().getAws().getVpcId();
          Aggregate vpcAggregate = pojoTopology.getOrCreateAggregate(vpcId, AggregateType.VNET);
          vpcAggregate.getContents().add(subnetAggregate.getId());
        } else if (configuration.getVendorFamily().getAws().getVpcId() != null) {
          String vpcId = configuration.getVendorFamily().getAws().getVpcId();
          Aggregate vpcAggregate = pojoTopology.getOrCreateAggregate(vpcId, AggregateType.VNET);
          vpcAggregate.getContents().add(pojoNode.getId());

          String region = configuration.getVendorFamily().getAws().getRegion();
          Aggregate regionAggregate =
              pojoTopology.getOrCreateAggregate(region, AggregateType.REGION);
          regionAggregate.getContents().add(vpcAggregate.getId());
        } else if (configuration.getVendorFamily().getAws().getRegion() != null) {
          String region = configuration.getVendorFamily().getAws().getRegion();
          Aggregate regionAggregate =
              pojoTopology.getOrCreateAggregate(region, AggregateType.REGION);
          regionAggregate.getContents().add(pojoNode.getId());

          Aggregate awsAggregate = pojoTopology.getOrCreateAggregate("aws", AggregateType.CLOUD);
          awsAggregate.getContents().add(regionAggregate.getId());
        } else {
          Aggregate awsAggregate = pojoTopology.getOrCreateAggregate("aws", AggregateType.CLOUD);
          awsAggregate.getContents().add(pojoNode.getId());
        }
      }
    }

    // add nodes that were not in Topology (because they have no Edges)
    for (Configuration configuration : configurations.values()) {
      Node pojoNode = new Node(configuration.getHostname(), configuration.getDeviceType());
      pojoTopology.getNodes().add(pojoNode);
    }
    return pojoTopology;
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
    String aggId = Aggregate.getId(name);
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
