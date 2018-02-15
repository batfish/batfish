package org.batfish.datamodel.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.Pair;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;

public final class VerboseNodeInterfacePair extends Pair<Configuration, Interface> {

  private static final String PROP_HOST = "host";

  private static final String PROP_INTERFACE = "interface";
  /** */
  private static final long serialVersionUID = 1L;

  @JsonCreator
  public VerboseNodeInterfacePair(
      @JsonProperty(PROP_HOST) Configuration node, @JsonProperty(PROP_INTERFACE) Interface iface) {
    super(node, iface);
  }

  @JsonProperty(PROP_HOST)
  public Configuration getHost() {
    return _first;
  }

  @JsonProperty(PROP_INTERFACE)
  public Interface getInterface() {
    return _second;
  }

  @Override
  public String toString() {
    return _first + ":" + _second;
  }
}
