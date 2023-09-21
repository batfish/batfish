package org.batfish.question.a10;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.ConstantIpSpaceSpecifier;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;

/** A question that returns VIP configuration of A10 devices. */
public class A10VirtualServerConfigurationQuestion extends Question {
  private static final String PROP_NODES = "nodes";
  private static final String PROP_VIRTUAL_SERVER_IPS = "virtualServerIps";

  private final @Nullable String _nodes;
  private final @Nullable String _virtualServerIps;

  @JsonCreator
  private static A10VirtualServerConfigurationQuestion create(
      @JsonProperty(PROP_NODES) @Nullable String nodes,
      @JsonProperty(PROP_VIRTUAL_SERVER_IPS) @Nullable String virtualServerIps) {
    return new A10VirtualServerConfigurationQuestion(nodes, virtualServerIps);
  }

  public A10VirtualServerConfigurationQuestion(
      @Nullable String nodes, @Nullable String virtualServerIps) {
    _nodes = nodes;
    _virtualServerIps = virtualServerIps;
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "a10VirtualServerConfiguration";
  }

  @JsonProperty(PROP_NODES)
  public @Nullable String getNodes() {
    return _nodes;
  }

  @JsonIgnore
  @Nonnull
  NodeSpecifier getNodeSpecifier() {
    return SpecifierFactories.getNodeSpecifierOrDefault(_nodes, AllNodesNodeSpecifier.INSTANCE);
  }

  @JsonIgnore
  @Nonnull
  IpSpaceSpecifier getVirtualServerIpSpecifier() {
    return SpecifierFactories.getIpSpaceSpecifierOrDefault(
        _virtualServerIps, new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE));
  }
}
