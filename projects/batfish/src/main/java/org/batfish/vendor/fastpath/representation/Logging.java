package org.batfish.vendor.fastpath.representation;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Aggregates a FastPath device's {@code logging} configuration, keeping the individual settings off
 * of {@link FastpathConfiguration}.
 */
public final class Logging implements Serializable {

  public Logging() {
    _servers = new LinkedHashMap<>();
  }

  /** Remote logging (syslog) hosts, keyed by host (IP address or hostname). */
  public @Nonnull Map<String, LoggingServer> getServers() {
    return _servers;
  }

  /** In-memory buffered log settings ({@code logging buffered}). */
  public @Nullable LoggingBuffered getBuffered() {
    return _buffered;
  }

  /** Returns the existing {@link LoggingBuffered}, creating it if necessary. */
  public @Nonnull LoggingBuffered getOrCreateBuffered() {
    if (_buffered == null) {
      _buffered = new LoggingBuffered();
    }
    return _buffered;
  }

  /** Persistent (flash) log severity (0-7) from {@code logging persistent}, or {@code null}. */
  public @Nullable Integer getPersistentSeverity() {
    return _persistentSeverity;
  }

  public void setPersistentSeverity(@Nullable Integer persistentSeverity) {
    _persistentSeverity = persistentSeverity;
  }

  /** Whether console logging is enabled ({@code logging console} / {@code no logging console}). */
  public @Nullable Boolean getConsoleEnabled() {
    return _consoleEnabled;
  }

  public void setConsoleEnabled(@Nullable Boolean consoleEnabled) {
    _consoleEnabled = consoleEnabled;
  }

  /** Console logging severity (0-7) from {@code logging console}, or {@code null}. */
  public @Nullable Integer getConsoleSeverity() {
    return _consoleSeverity;
  }

  public void setConsoleSeverity(@Nullable Integer consoleSeverity) {
    _consoleSeverity = consoleSeverity;
  }

  /** Whether CLI command logging is enabled ({@code logging cli-command}). */
  public @Nullable Boolean getCliCommand() {
    return _cliCommand;
  }

  public void setCliCommand(@Nullable Boolean cliCommand) {
    _cliCommand = cliCommand;
  }

  /** Whether syslog logging is enabled ({@code logging syslog}). */
  public @Nullable Boolean getSyslogEnabled() {
    return _syslogEnabled;
  }

  public void setSyslogEnabled(@Nullable Boolean syslogEnabled) {
    _syslogEnabled = syslogEnabled;
  }

  /**
   * The syslog client source interface ({@code logging syslog source-interface}), or {@code null}.
   */
  public @Nullable String getSourceInterface() {
    return _sourceInterface;
  }

  public void setSourceInterface(@Nullable String sourceInterface) {
    _sourceInterface = sourceInterface;
  }

  private final @Nonnull Map<String, LoggingServer> _servers;
  private @Nullable LoggingBuffered _buffered;
  private @Nullable Integer _persistentSeverity;
  private @Nullable Boolean _consoleEnabled;
  private @Nullable Integer _consoleSeverity;
  private @Nullable Boolean _cliCommand;
  private @Nullable Boolean _syslogEnabled;
  private @Nullable String _sourceInterface;
}
