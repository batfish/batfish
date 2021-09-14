package org.batfish.question.vrrpproperties;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.AllInterfacesInterfaceSpecifier;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.InterfaceSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;

/** A question that returns a table with VRRP groups on interfaces and their properties. */
@ParametersAreNonnullByDefault
public final class VrrpPropertiesQuestion extends Question {
  private static final String PROP_NODES = "nodes";
  private static final String PROP_INTERFACES = "interfaces";
  private static final String PROP_EXCLUDE_SHUT_INTERFACES = "excludeShutInterfaces";

  @Nullable private final String _nodes;
  @Nullable private final String _interfaces;
  private final boolean _excludeShutInterfaces;

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "vrrpProperties";
  }

  @JsonCreator
  private static @Nonnull VrrpPropertiesQuestion create(
      @Nullable @JsonProperty(PROP_NODES) String nodes,
      @Nullable @JsonProperty(PROP_INTERFACES) String interfaces,
      @Nullable @JsonProperty(PROP_EXCLUDE_SHUT_INTERFACES) Boolean excludeShutInterfaces) {
    return new VrrpPropertiesQuestion(
        nodes, interfaces, firstNonNull(excludeShutInterfaces, false));
  }

  public VrrpPropertiesQuestion(
      @Nullable String nodes, @Nullable String interfaces, boolean excludeShutInterfaces) {
    _nodes = nodes;
    _interfaces = interfaces;
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

  @Override
  public boolean equals(@Nullable Object o) {
    if (!(o instanceof VrrpPropertiesQuestion)) {
      return false;
    }
    VrrpPropertiesQuestion that = (VrrpPropertiesQuestion) o;
    return Objects.equals(_nodes, that._nodes) && Objects.equals(_interfaces, that._interfaces);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_nodes, _interfaces);
  }
}
