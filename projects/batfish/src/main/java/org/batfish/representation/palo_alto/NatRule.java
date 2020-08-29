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

  // Name of the rule
  @Nonnull private final String _name;

  // Zones (both required to commit)
  @Nonnull private final SortedSet<String> _from;
  @Nullable private String _to;

  // IPs (both required to commit)
  @Nonnull private final List<RuleEndpoint> _source;
  @Nonnull private final List<RuleEndpoint> _destination;

  // Services
  @Nullable private ServiceOrServiceGroupReference _service;

  // Translations
  @Nullable private SourceTranslation _sourceTranslation;
  @Nullable private DestinationTranslation _destinationTranslation;

  private boolean _disabled;

  public NatRule(String name) {
    _name = name;
    _from = new TreeSet<>();
    _source = new LinkedList<>();
    _destination = new LinkedList<>();
    _disabled = false;
  }

  @Nonnull
  public List<RuleEndpoint> getDestination() {
    return _destination;
  }

  @Nullable
  public DestinationTranslation getDestinationTranslation() {
    return _destinationTranslation;
  }

  public boolean getDisabled() {
    return _disabled;
  }

  @Nonnull
  public SortedSet<String> getFrom() {
    return _from;
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  @Nullable
  public ServiceOrServiceGroupReference getService() {
    return _service;
  }

  @Nonnull
  public List<RuleEndpoint> getSource() {
    return _source;
  }

  @Nullable
  public SourceTranslation getSourceTranslation() {
    return _sourceTranslation;
  }

  @Nullable
  public String getTo() {
    return _to;
  }

  public void setDestinationTranslation(DestinationTranslation destinationTranslation) {
    _destinationTranslation = destinationTranslation;
  }

  public void setDisabled(boolean disabled) {
    _disabled = disabled;
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
