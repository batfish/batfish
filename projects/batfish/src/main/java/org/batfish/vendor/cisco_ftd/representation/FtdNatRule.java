package org.batfish.vendor.cisco_ftd.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FtdNatRule implements Serializable {
  public enum NatPosition {
    AUTO,
    BEFORE_AUTO,
    AFTER_AUTO
  }

  private final @Nonnull String _sourceInterface;
  private final @Nonnull String _destinationInterface;
  private final @Nonnull NatPosition _position;
  private @Nullable FtdNatSource _sourceTranslation;
  private @Nullable FtdNatDestination _destinationTranslation;
  private @Nullable FtdNatService _serviceTranslation;

  public FtdNatRule(
      @Nonnull String sourceInterface,
      @Nonnull String destinationInterface,
      @Nonnull NatPosition position) {
    _sourceInterface = sourceInterface;
    _destinationInterface = destinationInterface;
    _position = position;
  }

  public @Nonnull String getSourceInterface() {
    return _sourceInterface;
  }

  public @Nonnull String getDestinationInterface() {
    return _destinationInterface;
  }

  public @Nonnull NatPosition getPosition() {
    return _position;
  }

  public @Nullable FtdNatSource getSourceTranslation() {
    return _sourceTranslation;
  }

  public void setSourceTranslation(@Nullable FtdNatSource sourceTranslation) {
    _sourceTranslation = sourceTranslation;
  }

  public @Nullable FtdNatDestination getDestinationTranslation() {
    return _destinationTranslation;
  }

  public void setDestinationTranslation(@Nullable FtdNatDestination destinationTranslation) {
    _destinationTranslation = destinationTranslation;
  }

  public @Nullable FtdNatService getServiceTranslation() {
    return _serviceTranslation;
  }

  public void setServiceTranslation(@Nullable FtdNatService serviceTranslation) {
    _serviceTranslation = serviceTranslation;
  }

  @Override
  public String toString() {
    return String.format(
        "FtdNatRule[%s -> %s, %s, source=%s, destination=%s, service=%s]",
        _sourceInterface,
        _destinationInterface,
        _position,
        _sourceTranslation,
        _destinationTranslation,
        _serviceTranslation);
  }
}
