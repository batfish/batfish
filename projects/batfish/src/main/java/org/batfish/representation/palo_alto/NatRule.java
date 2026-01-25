package org.batfish.representation.palo_alto;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** PAN NAT rule configuration */
public final class NatRule implements Serializable {

  public enum ActiveActiveDeviceBinding {
    BOTH,
    PRIMARY,
    ONE,
    ZERO
  }

  // For HA systems, determines which system the rule is installed on
  private @Nullable ActiveActiveDeviceBinding _activeActiveDeviceBinding;

  // Name of the rule
  private final @Nonnull String _name;

  // Description of the rule
  private @Nullable String _description;

  // Zones (both required to commit)
  private final @Nonnull SortedSet<String> _from;
  private @Nullable String _to;

  // IPs (both required to commit)
  private final @Nonnull List<RuleEndpoint> _source;
  private final @Nonnull List<RuleEndpoint> _destination;

  // Services
  private @Nullable ServiceOrServiceGroupReference _service;

  // Translations
  private @Nullable SourceTranslation _sourceTranslation;
  private @Nullable DestinationTranslation _destinationTranslation;

  private boolean _disabled;

  public NatRule(String name) {
    _name = name;
    _from = new TreeSet<>();
    _source = new LinkedList<>();
    _destination = new LinkedList<>();
    _disabled = false;
  }

  public @Nullable ActiveActiveDeviceBinding getActiveActiveDeviceBinding() {
    return _activeActiveDeviceBinding;
  }

  public void setActiveActiveDeviceBinding(
      @Nullable ActiveActiveDeviceBinding activeActiveDeviceBinding) {
    _activeActiveDeviceBinding = activeActiveDeviceBinding;
  }

  public @Nonnull List<RuleEndpoint> getDestination() {
    return _destination;
  }

  public @Nullable DestinationTranslation getDestinationTranslation() {
    return _destinationTranslation;
  }

  public boolean getDisabled() {
    return _disabled;
  }

  public @Nonnull SortedSet<String> getFrom() {
    return _from;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public @Nullable ServiceOrServiceGroupReference getService() {
    return _service;
  }

  public @Nonnull List<RuleEndpoint> getSource() {
    return _source;
  }

  public @Nullable SourceTranslation getSourceTranslation() {
    return _sourceTranslation;
  }

  public @Nullable String getTo() {
    return _to;
  }

  public void setDestinationTranslation(DestinationTranslation destinationTranslation) {
    _destinationTranslation = destinationTranslation;
  }

  public void setDisabled(boolean disabled) {
    _disabled = disabled;
  }

  public void setDescription(@Nullable String description) {
    _description = description;
  }

  public void setService(ServiceOrServiceGroupReference service) {
    _service = service;
  }

  public void setSourceTranslation(SourceTranslation sourceTranslation) {
    _sourceTranslation = sourceTranslation;
  }

  public void setTo(@Nullable String to) {
    _to = to;
  }
}
