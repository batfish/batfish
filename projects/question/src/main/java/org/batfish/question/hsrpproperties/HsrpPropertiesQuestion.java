package org.batfish.question.hsrpproperties;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.AllInterfacesInterfaceSpecifier;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.ConstantIpSpaceSpecifier;
import org.batfish.specifier.InterfaceSpecifier;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;

/** A question that returns a table with HSRP groups on interfaces and their properties. */
public final class HsrpPropertiesQuestion extends Question {
  private static final String PROP_NODES = "nodes";
  private static final String PROP_INTERFACES = "interfaces";
  private static final String PROP_VIRTUAL_ADDRESSES = "virtualAddresses";
  private static final String PROP_EXCLUDE_SHUT_INTERFACES = "excludeShutInterfaces";

  private final @Nullable String _nodes;
  private final @Nullable String _interfaces;
  private final @Nullable String _virtualAddresses;
  private final boolean _excludeShutInterfaces;

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "hsrpProperties";
  }

  @JsonCreator
  private static @Nonnull HsrpPropertiesQuestion create(
      @JsonProperty(PROP_NODES) @Nullable String nodes,
      @JsonProperty(PROP_INTERFACES) @Nullable String interfaces,
      @JsonProperty(PROP_VIRTUAL_ADDRESSES) @Nullable String virtualAddresses,
      @JsonProperty(PROP_EXCLUDE_SHUT_INTERFACES) @Nullable Boolean excludeShutInterfaces) {
    return new HsrpPropertiesQuestion(
        nodes, interfaces, virtualAddresses, firstNonNull(excludeShutInterfaces, false));
  }

  public HsrpPropertiesQuestion(
      @Nullable String nodes,
      @Nullable String interfaces,
      @Nullable String virtualAddresses,
      boolean excludeShutInterfaces) {
    _nodes = nodes;
    _interfaces = interfaces;
    _virtualAddresses = virtualAddresses;
    _excludeShutInterfaces = excludeShutInterfaces;
  }

  @JsonProperty(PROP_NODES)
  public @Nullable String getNodes() {
    return _nodes;
  }

  @JsonProperty(PROP_INTERFACES)
  public @Nullable String getInterfaces() {
    return _interfaces;
  }

  @JsonProperty(PROP_VIRTUAL_ADDRESSES)
  public @Nullable String getVirtualAddresses() {
    return _virtualAddresses;
  }

  @JsonProperty(PROP_EXCLUDE_SHUT_INTERFACES)
  public boolean getExcludeShutInterfaces() {
    return _excludeShutInterfaces;
  }

  @JsonIgnore
  public @Nonnull NodeSpecifier getNodesSpecifier() {
    return SpecifierFactories.getNodeSpecifierOrDefault(_nodes, AllNodesNodeSpecifier.INSTANCE);
  }

  @JsonIgnore
  public @Nonnull InterfaceSpecifier getInterfacesSpecifier() {
    return SpecifierFactories.getInterfaceSpecifierOrDefault(
        _interfaces, AllInterfacesInterfaceSpecifier.INSTANCE);
  }

  @JsonIgnore
  public @Nonnull IpSpaceSpecifier getVirtualAddressSpecifier() {
    return SpecifierFactories.getIpSpaceSpecifierOrDefault(
        _virtualAddresses, new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE));
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (!(o instanceof HsrpPropertiesQuestion)) {
      return false;
    }
    HsrpPropertiesQuestion that = (HsrpPropertiesQuestion) o;
    return Objects.equals(_nodes, that._nodes) && Objects.equals(_interfaces, that._interfaces);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_nodes, _interfaces);
  }
}
