package org.batfish.representation.juniper;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/** A firewall filter on Juniper that is a concatenation of its member filters. */
public final class CompositeFirewallFilter extends FirewallFilter {

  public CompositeFirewallFilter(String name, @Nonnull List<FirewallFilter> inner) {
    super(name);
    _inner = ImmutableList.copyOf(inner);
    checkArgument(!_inner.isEmpty(), "Invalid composite firewall-filter with no members");
    Set<Family> families =
        _inner.stream().map(FirewallFilter::getFamily).collect(Collectors.toSet());
    checkArgument(
        families.size() == 1,
        "All member lists in a composite firewall-filter must have the same family: %s",
        families);
  }

  @Override
  public Family getFamily() {
    assert !_inner.isEmpty(); // in constructor.
    return _inner.get(0).getFamily();
  }

  @Nonnull
  public List<FirewallFilter> getInner() {
    return _inner;
  }

  @Override
  public boolean isUsedForFBF() {
    return _inner.stream().anyMatch(FirewallFilter::isUsedForFBF);
  }

  @Override
  public Optional<String> getFromZone() {
    return Optional.empty();
  }

  private final @Nonnull List<FirewallFilter> _inner;
}
