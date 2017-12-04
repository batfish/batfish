package org.batfish.datamodel.pojo;

import java.util.List;

public class Node extends BfObject {

  // remove this class and point to the datamodel.devicetype in PR #661
  public enum DeviceType {
    HOST,
    ROUTER,
    SWITCH
  }

  private List<Interface> _interfaces;

  private String _name;

  private DeviceType _type;

  public Node(String name, DeviceType type) {
    super("node-" + name);
    _name = name;
    _type = type;
  }
}
