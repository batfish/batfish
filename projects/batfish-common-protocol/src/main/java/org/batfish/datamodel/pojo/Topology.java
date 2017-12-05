package org.batfish.datamodel.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashSet;
import java.util.Set;

public class Topology extends BfObject {

  private static final String PROP_TESTRIG_NAME = "testrigName";

  Set<Aggregate> _aggregates;

  Set<Interface> _interfaces;

  Set<Link> _links;

  Set<Node> _nodes;

  String _testrigName;

  @JsonCreator
  public Topology(@JsonProperty(PROP_TESTRIG_NAME) String name) {
    super("topology-" + name);
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

  public Set<Interface> getInterfaces() {
    return _interfaces;
  }

  public Set<Link> getLinks() {
    return _links;
  }

  public Set<Node> getNodes() {
    return _nodes;
  }
}
