package org.batfish.question.switchedvlanproperties;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.questions.InterfacesSpecifier;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

@ParametersAreNonnullByDefault
public final class SwitchedVlanPropertiesQuestion extends Question {

  private static final IntegerSpace ALL_VLANS = IntegerSpace.of(new SubRange(1, 4094));

  private static final boolean DEFAULT_EXCLUDE_SHUT_INTERFACES = false;

  private static final String PROP_EXCLUDE_SHUT_INTERFACES = "excludeShutInterfaces";
  private static final String PROP_INTERFACES = "interfaces";
  private static final String PROP_NODES = "nodes";
  private static final String PROP_VLANS = "vlans";

  @JsonCreator
  private static @Nonnull SwitchedVlanPropertiesQuestion create(
      @JsonProperty(PROP_EXCLUDE_SHUT_INTERFACES) @Nullable Boolean excludeShutInterfaces,
      @JsonProperty(PROP_INTERFACES) @Nullable InterfacesSpecifier interfaces,
      @JsonProperty(PROP_NODES) @Nullable NodesSpecifier nodes,
      @JsonProperty(PROP_VLANS) @Nullable IntegerSpace vlans) {
    return new SwitchedVlanPropertiesQuestion(
        firstNonNull(excludeShutInterfaces, DEFAULT_EXCLUDE_SHUT_INTERFACES),
        firstNonNull(interfaces, InterfacesSpecifier.ALL),
        firstNonNull(nodes, NodesSpecifier.ALL),
        firstNonNull(vlans, ALL_VLANS));
  }

  private boolean _excludeShutInterfaces;

  private final InterfacesSpecifier _interfaces;

  private final NodesSpecifier _nodes;

  private final IntegerSpace _vlans;

  SwitchedVlanPropertiesQuestion() {
    this(DEFAULT_EXCLUDE_SHUT_INTERFACES, InterfacesSpecifier.ALL, NodesSpecifier.ALL, ALL_VLANS);
  }

  public SwitchedVlanPropertiesQuestion(
      boolean excludeShutInterfaces,
      InterfacesSpecifier interfaces,
      NodesSpecifier nodes,
      IntegerSpace vlans) {
    _excludeShutInterfaces = excludeShutInterfaces;
    _interfaces = interfaces;
    _nodes = nodes;
    _vlans = vlans;
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @JsonProperty(PROP_EXCLUDE_SHUT_INTERFACES)
  public boolean getExcludeShutInterfaces() {
    return _excludeShutInterfaces;
  }

  @JsonProperty(PROP_INTERFACES)
  public @Nonnull InterfacesSpecifier getInterfaces() {
    return _interfaces;
  }

  @Override
  public String getName() {
    return "switchedVlanProperties";
  }

  @JsonProperty(PROP_NODES)
  public @Nonnull NodesSpecifier getNodes() {
    return _nodes;
  }

  @JsonProperty(PROP_VLANS)
  public @Nonnull IntegerSpace getVlans() {
    return _vlans;
  }
}
