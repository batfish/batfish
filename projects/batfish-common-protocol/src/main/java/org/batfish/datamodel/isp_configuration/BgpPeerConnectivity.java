package org.batfish.datamodel.isp_configuration;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A specification for connectivity needed to establish a BGP peering session specified for ISP
 * modeling. Currently, it allows users to specify layer1 connectivity to the ISP via a hostname and
 * a list of interfaces. The interfaces must be physical or exactly one port-channel.
 */
public class BgpPeerConnectivity {
  private static final String PROP_LAYER1 = "layer1";
  private static final String PROP_HOSTNAME = "hostname";
  private static final String PROP_INTERFACE_NAMES = "interfaceNames";

  public static class Layer1 {
    // Null implies the bgp peering node (in the snapshot) itself
    @Nullable private final String _hostname;
    @Nonnull private final List<String> _interfaceNames;

    public Layer1(@Nullable String hostname, List<String> interfaceNames) {
      _hostname = hostname;
      _interfaceNames = ImmutableList.copyOf(interfaceNames);
    }

    @JsonCreator
    private static Layer1 jsonCreator(
        @JsonProperty(PROP_HOSTNAME) @Nullable String hostname,
        @JsonProperty(PROP_INTERFACE_NAMES) @Nullable List<String> interfaceNames) {
      checkArgument(
          interfaceNames != null && !interfaceNames.isEmpty(),
          "Non-empty list of %s should be specified for layer1 connectivity",
          PROP_INTERFACE_NAMES);
      return new Layer1(hostname, interfaceNames);
    }
  }

  @Nullable private final Layer1 _layer1;

  public BgpPeerConnectivity() {
    this(null);
  }

  public BgpPeerConnectivity(@Nullable Layer1 layer1) {
    _layer1 = layer1;
  }

  @JsonCreator
  private static BgpPeerConnectivity jsonCreator(
      @JsonProperty(PROP_LAYER1) @Nullable Layer1 layer1) {
    return new BgpPeerConnectivity(layer1);
  }
}
