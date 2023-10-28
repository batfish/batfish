package org.batfish.question.interfaceproperties;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.InterfacePropertySpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.AllInterfacesInterfaceSpecifier;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.InterfaceSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;

/**
 * A question that returns properties of interfaces in a tabular format. {@link #_nodes}, {@link
 * #_interfaces}, and {@link #_properties} determine which nodes, interfaces, and properties are
 * included. The default is to include everything.
 */
@ParametersAreNonnullByDefault
public class InterfacePropertiesQuestion extends Question {

  public static final boolean DEFAULT_EXCLUDE_SHUT_INTERFACES = false;
  private static final String PROP_EXCLUDE_SHUT_INTERFACES = "excludeShutInterfaces";
  private static final String PROP_INTERFACES = "interfaces";
  private static final String PROP_NODES = "nodes";
  private static final String PROP_PROPERTIES = "properties";

  private final @Nullable String _interfaces;
  private final @Nonnull InterfaceSpecifier _interfaceSpecifier;
  private final @Nullable String _nodes;
  private final @Nonnull NodeSpecifier _nodeSpecifier;
  private boolean _onlyActive;
  private final @Nullable String _properties;
  private final @Nonnull InterfacePropertySpecifier _propertySpecifier;

  @JsonCreator
  private static InterfacePropertiesQuestion create(
      @JsonProperty(PROP_EXCLUDE_SHUT_INTERFACES) @Nullable Boolean excludeShutInterfaces,
      @JsonProperty(PROP_INTERFACES) @Nullable String interfaces,
      @JsonProperty(PROP_NODES) @Nullable String nodes,
      @JsonProperty(PROP_PROPERTIES) @Nullable String properties) {
    return new InterfacePropertiesQuestion(
        nodes,
        interfaces,
        properties,
        firstNonNull(excludeShutInterfaces, DEFAULT_EXCLUDE_SHUT_INTERFACES));
  }

  public InterfacePropertiesQuestion(
      @Nullable String nodes,
      @Nullable String interfaces,
      @Nullable String properties,
      boolean excludeShutInterfaces) {
    this(
        nodes,
        SpecifierFactories.getNodeSpecifierOrDefault(nodes, AllNodesNodeSpecifier.INSTANCE),
        interfaces,
        SpecifierFactories.getInterfaceSpecifierOrDefault(
            interfaces, AllInterfacesInterfaceSpecifier.INSTANCE),
        properties,
        InterfacePropertySpecifier.create(properties),
        firstNonNull(excludeShutInterfaces, DEFAULT_EXCLUDE_SHUT_INTERFACES));
  }

  public InterfacePropertiesQuestion(
      NodeSpecifier nodeSpecifier,
      InterfaceSpecifier interfaceSpecifier,
      InterfacePropertySpecifier propertySpec,
      boolean excludeShutInterfaces) {
    this(null, nodeSpecifier, null, interfaceSpecifier, null, propertySpec, excludeShutInterfaces);
  }

  private InterfacePropertiesQuestion(
      @Nullable String nodes,
      NodeSpecifier nodeSpecifier,
      @Nullable String interfaces,
      InterfaceSpecifier interfaceSpecifier,
      @Nullable String properties,
      InterfacePropertySpecifier propertySpec,
      boolean excludeShutInterfaces) {
    _nodes = nodes;
    _nodeSpecifier = nodeSpecifier;
    _interfaces = interfaces;
    _interfaceSpecifier = interfaceSpecifier;
    _properties = properties;
    _propertySpecifier = propertySpec;
    _onlyActive = excludeShutInterfaces;
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "interfaceProperties";
  }

  @JsonProperty(PROP_EXCLUDE_SHUT_INTERFACES)
  public boolean getOnlyActive() {
    return _onlyActive;
  }

  @JsonProperty(PROP_INTERFACES)
  public @Nullable String getInterfaces() {
    return _interfaces;
  }

  @JsonIgnore
  public @Nonnull InterfaceSpecifier getInterfaceSpecifier() {
    return _interfaceSpecifier;
  }

  @JsonProperty(PROP_NODES)
  public @Nullable String getNodes() {
    return _nodes;
  }

  @JsonIgnore
  public @Nonnull NodeSpecifier getNodeSpecifier() {
    return _nodeSpecifier;
  }

  @JsonProperty(PROP_PROPERTIES)
  public @Nullable String getProperties() {
    return _properties;
  }

  @JsonIgnore
  public @Nonnull InterfacePropertySpecifier getPropertySpecifier() {
    return _propertySpecifier;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof InterfacePropertiesQuestion)) {
      return false;
    }
    InterfacePropertiesQuestion that = (InterfacePropertiesQuestion) o;
    return Objects.equals(_nodes, that._nodes)
        && Objects.equals(_nodeSpecifier, that._nodeSpecifier)
        && Objects.equals(_interfaces, that._interfaces)
        && Objects.equals(_interfaceSpecifier, that._interfaceSpecifier)
        && Objects.equals(_properties, that._properties)
        && Objects.equals(_propertySpecifier, that._propertySpecifier)
        && _onlyActive == that._onlyActive;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _nodes,
        _nodeSpecifier,
        _interfaces,
        _interfaceSpecifier,
        _properties,
        _propertySpecifier,
        _onlyActive);
  }
}
