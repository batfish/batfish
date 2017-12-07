package org.batfish.datamodel.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.pojo.Aggregate.AggregateType;

public class Topology extends BfObject {

  private static final String PROP_TESTRIG_NAME = "testrigName";

  private Set<Aggregate> _aggregates;

  private Set<Interface> _interfaces;

  private Set<Link> _links;

  private Set<Node> _nodes;

  private final String _testrigName;

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
                    Node.getId(edge.getNode2()), edge.getInt2()));

        pojoTopology.getInterfaces().add(pojoInterface);
        pojoTopology.getLinks().add(pojoLink);
      }

      // add AWS aggregates
      if (configuration.getConfigurationFormat() == ConfigurationFormat.AWS_VPC) {
        Aggregate awsAggregate = pojoTopology.getAggregateById(Aggregate.getId("aws"));
        if (awsAggregate == null) {
          awsAggregate = new Aggregate("aws", AggregateType.CLOUD);
          pojoTopology.getAggregates().add(awsAggregate);
        }
        awsAggregate.getContents().add(pojoNode.getId());

        if (configuration.getVendorFamily().getAws().getVpcId() != null) {
          String vpcId = configuration.getVendorFamily().getAws().getVpcId();
          Aggregate vpcAggregate = pojoTopology.getAggregateById(Aggregate.getId(vpcId));
          if (vpcAggregate == null) {
            vpcAggregate = new Aggregate(vpcId, AggregateType.VNET);
            pojoTopology.getAggregates().add(vpcAggregate);
          }
          vpcAggregate.getContents().add(pojoNode.getId());
        }

        if (configuration.getVendorFamily().getAws().getSubnetId() != null) {
          String subnetId = configuration.getVendorFamily().getAws().getSubnetId();
          Aggregate subnetAggregate = pojoTopology.getAggregateById(Aggregate.getId(subnetId));
          if (subnetAggregate == null) {
            subnetAggregate = new Aggregate(subnetId, AggregateType.SUBNET);
            pojoTopology.getAggregates().add(subnetAggregate);
          }
          subnetAggregate.getContents().add(pojoNode.getId());
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

  @JsonCreator
  public Topology(@JsonProperty(PROP_TESTRIG_NAME) String name) {
    super(getId(name));
    _testrigName = name;
    _aggregates = new HashSet<>();
    _interfaces = new HashSet<>();
    _links = new HashSet<>();
    _nodes = new HashSet<>();
  }

  public Aggregate getAggregateById(String id) {
    for (Aggregate aggregate : _aggregates) {
      if (aggregate.getId().equals(id)) {
        return aggregate;
      }
    }
    return null;
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
