package org.batfish.representation.cisco_asa;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Vendor-specific ASA syslog/logging settings.
 *
 * <p>Captures ASA logging attributes that are not represented by the shared {@link
 * org.batfish.datamodel.vendor_family.cisco.Logging} model: per-host transport/port, the global
 * logging facility, the global trap severity, and the internal log buffer size/severity.
 *
 * <p>Severities are captured both by name (e.g. {@code errors}) and by numeric level (0-7), since
 * the ASA CLI accepts either form.
 */
@ParametersAreNonnullByDefault
public final class Logging implements Serializable {

  private final @Nonnull SortedMap<String, LoggingHost> _hosts;
  private @Nullable Integer _facility;
  private @Nullable String _trapSeverity;
  private @Nullable Integer _trapSeverityNum;
  private @Nullable Integer _bufferSize;
  private @Nullable String _bufferedSeverity;
  private @Nullable Integer _bufferedSeverityNum;

  public Logging() {
    _hosts = new TreeMap<>();
  }

  public @Nonnull SortedMap<String, LoggingHost> getHosts() {
    return _hosts;
  }

  public @Nullable Integer getFacility() {
    return _facility;
  }

  public void setFacility(@Nullable Integer facility) {
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

  public @Nullable Integer getBufferSize() {
    return _bufferSize;
  }

  public void setBufferSize(@Nullable Integer bufferSize) {
    _bufferSize = bufferSize;
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
}
