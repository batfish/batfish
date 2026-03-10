package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

public class MockFib implements Fib {

  public static class Builder {
    private Map<Ip, Set<FibEntry>> _fibEntries;

    private Map<Prefix, IpSpace> _matchingIps;

    private Builder() {
      _matchingIps = ImmutableMap.of();
      _fibEntries = ImmutableMap.of();
    }

    public MockFib build() {
      return new MockFib(this);
    }

    public Builder setMatchingIps(@Nonnull Map<Prefix, IpSpace> matchingIps) {
      _matchingIps = matchingIps;
      return this;
    }

    public Builder setFibEntries(@Nonnull Map<Ip, Set<FibEntry>> fibEntries) {
      // TODO: remove once Route.UNSET_NEXT_HOP_IP and Ip.AUTO are killed
      assert !fibEntries.containsKey(Route.UNSET_ROUTE_NEXT_HOP_IP);
      _fibEntries = fibEntries;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private final Map<Prefix, IpSpace> _matchingIps;

  private final Map<Ip, Set<FibEntry>> _fibEntries;

  private MockFib(Builder builder) {
    _matchingIps = ImmutableMap.copyOf(builder._matchingIps);
    _fibEntries = ImmutableMap.copyOf(builder._fibEntries);
  }

  @Override
  public @Nonnull Set<FibEntry> allEntries() {
    return _fibEntries.values().stream()
        .flatMap(Set::stream)
        .collect(ImmutableSet.toImmutableSet());
  }

  @Override
  public @Nonnull Set<FibEntry> get(Ip ip) {
    return firstNonNull(_fibEntries.get(ip), ImmutableSet.of());
  }

  @Override
  public @Nonnull Map<Prefix, IpSpace> getMatchingIps() {
    return _matchingIps;
  }
}
