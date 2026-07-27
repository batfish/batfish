package org.batfish.representation.cisco_asa;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A Cisco ASA DNS server group, configured via {@code dns server-group <name>} (or implicitly for
 * the default {@code DefaultDNS} group via the global {@code dns name-server} command). Holds the
 * group's name servers plus its tuning parameters.
 */
@ParametersAreNonnullByDefault
public final class DnsServerGroup implements Serializable {

  public DnsServerGroup(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull List<NameServer> getNameServers() {
    return _nameServers;
  }

  public void addNameServer(NameServer nameServer) {
    _nameServers.add(nameServer);
  }

  public @Nullable String getDomainName() {
    return _domainName;
  }

  public void setDomainName(@Nullable String domainName) {
    _domainName = domainName;
  }

  public @Nullable Integer getTimeoutSeconds() {
    return _timeoutSeconds;
  }

  public void setTimeoutSeconds(@Nullable Integer timeoutSeconds) {
    _timeoutSeconds = timeoutSeconds;
  }

  public @Nullable Integer getRetries() {
    return _retries;
  }

  public void setRetries(@Nullable Integer retries) {
    _retries = retries;
  }

  public @Nullable Integer getPollTimerMinutes() {
    return _pollTimerMinutes;
  }

  public void setPollTimerMinutes(@Nullable Integer pollTimerMinutes) {
    _pollTimerMinutes = pollTimerMinutes;
  }

  public @Nullable Integer getExpireEntryTimerMinutes() {
    return _expireEntryTimerMinutes;
  }

  public void setExpireEntryTimerMinutes(@Nullable Integer expireEntryTimerMinutes) {
    _expireEntryTimerMinutes = expireEntryTimerMinutes;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DnsServerGroup)) {
      return false;
    }
    DnsServerGroup that = (DnsServerGroup) o;
    return _name.equals(that._name)
        && _nameServers.equals(that._nameServers)
        && Objects.equals(_domainName, that._domainName)
        && Objects.equals(_timeoutSeconds, that._timeoutSeconds)
        && Objects.equals(_retries, that._retries)
        && Objects.equals(_pollTimerMinutes, that._pollTimerMinutes)
        && Objects.equals(_expireEntryTimerMinutes, that._expireEntryTimerMinutes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _name,
        _nameServers,
        _domainName,
        _timeoutSeconds,
        _retries,
        _pollTimerMinutes,
        _expireEntryTimerMinutes);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", _name)
        .add("nameServers", _nameServers)
        .add("domainName", _domainName)
        .add("timeoutSeconds", _timeoutSeconds)
        .add("retries", _retries)
        .add("pollTimerMinutes", _pollTimerMinutes)
        .add("expireEntryTimerMinutes", _expireEntryTimerMinutes)
        .toString();
  }

  private final @Nonnull String _name;
  private final @Nonnull List<NameServer> _nameServers = new ArrayList<>();
  private @Nullable String _domainName;
  private @Nullable Integer _timeoutSeconds;
  private @Nullable Integer _retries;
  private @Nullable Integer _pollTimerMinutes;
  private @Nullable Integer _expireEntryTimerMinutes;
}
