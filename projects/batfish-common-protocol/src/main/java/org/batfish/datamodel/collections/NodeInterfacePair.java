package org.batfish.datamodel.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.Pair;

public class NodeInterfacePair extends Pair<String, String> {

  private static final String HOSTNAME_VAR = "hostname";

  private static final String INTERFACE_VAR = "interface";
  /** */
  private static final long serialVersionUID = 1L;

  @JsonCreator
  public NodeInterfacePair(
      @JsonProperty(HOSTNAME_VAR) String node, @JsonProperty(INTERFACE_VAR) String iface) {
    super(node, iface);
  }

  @JsonProperty(HOSTNAME_VAR)
  public String getHostname() {
    return _first;
  }

  @JsonProperty(INTERFACE_VAR)
  public String getInterface() {
    return _second;
  }

  @Override
  public String toString() {
    return _first + ":" + _second;
  }
}
