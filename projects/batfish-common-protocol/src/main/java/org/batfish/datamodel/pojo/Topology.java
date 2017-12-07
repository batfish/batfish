package org.batfish.datamodel.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashSet;
import java.util.Set;

public class Topology extends BfObject {

  private static final String PROP_TESTRIG_NAME = "testrigName";

  private Set<Aggregate> _aggregates;

  private Set<Interface> _interfaces;

  private Set<Link> _links;

  private Set<Node> _nodes;

  private final String _testrigName;

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
