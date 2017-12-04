package org.batfish.datamodel.pojo;

public class Interface extends BfObject {

  public enum InterfaceType {
    PHYSICAL,
    UNKNOWN,
    VIRTUAL,
  }

  String _name;

  String _node;

  InterfaceType _type;

  public Interface(String nodeId, String name, InterfaceType type) {
    super("interface-" + nodeId + "-" + name);
    _node = nodeId;
    _name = name;
    _type = type;
  }

}
