package org.batfish.datamodel.pojo;

import java.util.List;

public class Topology extends BfObject {

  List<Aggregate> _aggregates;

  List<Interface> _interfaces;

  List<Link> _links;

  List<Node> _nodes;

  public Topology(String id) {
    super(id);
  }
}
