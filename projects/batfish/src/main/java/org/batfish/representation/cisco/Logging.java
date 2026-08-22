package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Vendor-specific Cisco IOS syslog/logging settings.
 *
 * <p>Captures IOS logging attributes that are not represented by the shared {@link
 * org.batfish.datamodel.vendor_family.cisco.Logging} model: per-host transport/port, the global
 * logging facility, the global trap severity, and the internal log buffer ({@code logging
 * buffered}).
 */
@ParametersAreNonnullByDefault
public final class Logging implements Serializable {

  private final @Nonnull SortedMap<String, LoggingHost> _hosts;

  // Global settings
  private @Nullable String _facility;
  private @Nullable String _trapSeverity;
  private @Nullable Integer _trapSeverityNum;

  // logging buffered [discriminator <name>] [<size>] [<severity>]
  private @Nullable Integer _bufferedSize;
  private @Nullable String _bufferedSeverity;
  private @Nullable Integer _bufferedSeverityNum;
  private @Nullable String _bufferedDiscriminator;

  public Logging() {
    _hosts = new TreeMap<>();
  }

  public @Nonnull SortedMap<String, LoggingHost> getHosts() {
    return _hosts;
  }

  public @Nullable String getFacility() {
    return _facility;
  }

  public void setFacility(@Nullable String facility) {
    _facility = facility;
  }

  public @Nullable String getTrapSeverity() {
    return _trapSeverity;
  }

  public void setTrapSeverity(@Nullable String trapSeverity) {
    _trapSeverity = trapSeverity;
  }

  public @Nullable Integer getTrapSeverityNum() {
    return _trapSeverityNum;
  }

  public void setTrapSeverityNum(@Nullable Integer trapSeverityNum) {
    _trapSeverityNum = trapSeverityNum;
  }

  public @Nullable Integer getBufferedSize() {
    return _bufferedSize;
  }

  public void setBufferedSize(@Nullable Integer bufferedSize) {
    _bufferedSize = bufferedSize;
  }

  public @Nullable String getBufferedSeverity() {
    return _bufferedSeverity;
  }

  public void setBufferedSeverity(@Nullable String bufferedSeverity) {
    _bufferedSeverity = bufferedSeverity;
  }

  public @Nullable Integer getBufferedSeverityNum() {
    return _bufferedSeverityNum;
  }

  public void setBufferedSeverityNum(@Nullable Integer bufferedSeverityNum) {
    _bufferedSeverityNum = bufferedSeverityNum;
  }

  public @Nullable String getBufferedDiscriminator() {
    return _bufferedDiscriminator;
  }

  public void setBufferedDiscriminator(@Nullable String bufferedDiscriminator) {
    _bufferedDiscriminator = bufferedDiscriminator;
  }
}
