package org.batfish.datamodel.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.Pair;

public class NodeInterfacePair extends Pair<String, String> {

  private static final String PROP_HOSTNAME = "hostname";

  private static final String PROP_INTERFACE = "interface";
  /** */
  private static final long serialVersionUID = 1L;

  public static final NodeInterfacePair NONE = new NodeInterfacePair("", "");

  @JsonCreator
  public NodeInterfacePair(
      @JsonProperty(PROP_HOSTNAME) String node, @JsonProperty(PROP_INTERFACE) String iface) {
    super(node, iface);
  }

  @JsonProperty(PROP_HOSTNAME)
  public String getHostname() {
    return _first;
  }

  @JsonProperty(PROP_INTERFACE)
  public String getInterface() {
    return _second;
  }

  @Override
  public String toString() {
    return _first + ":" + _second;
  }
}
