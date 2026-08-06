package org.batfish.vendor.fastpath.representation;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Aggregates a FastPath device's DNS-client ({@code ip domain} / {@code ip name}) configuration,
 * keeping the individual settings off of {@link FastpathConfiguration}.
 */
public final class Dns implements Serializable {

  public Dns() {
    _servers = new LinkedHashSet<>();
  }

  /** The default domain name ({@code ip domain name}), or {@code null} if unset. */
  public @Nullable String getDomainName() {
    return _domainName;
  }

  public void setDomainName(@Nullable String domainName) {
    _domainName = domainName;
  }

  /** Configured name servers ({@code ip name server}), in preference order. */
  public @Nonnull Set<String> getServers() {
    return _servers;
  }

  public void addServer(String server) {
    _servers.add(server);
  }

  /** The DNS client source interface ({@code ip name source-interface}), or {@code null}. */
  public @Nullable String getSourceInterface() {
    return _sourceInterface;
  }

  public void setSourceInterface(@Nullable String sourceInterface) {
    _sourceInterface = sourceInterface;
  }

  /**
   * Whether the DNS client is enabled ({@code ip domain lookup} / {@code no ip domain lookup}), or
   * {@code null} if not configured (the device defaults to enabled).
   */
  public @Nullable Boolean getLookupEnabled() {
    return _lookupEnabled;
  }

  public void setLookupEnabled(@Nullable Boolean lookupEnabled) {
    _lookupEnabled = lookupEnabled;
  }

  private @Nullable String _domainName;
  private final @Nonnull Set<String> _servers;
  private @Nullable String _sourceInterface;
  private @Nullable Boolean _lookupEnabled;
}
