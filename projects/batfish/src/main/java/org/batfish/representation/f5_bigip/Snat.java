package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;

/** Configuration for default SNAT to be applied when traffic does not match a virtual service. */
@ParametersAreNonnullByDefault
public final class Snat implements Serializable {

  private final @Nonnull Map<Prefix, Ipv4Origin> _ipv4Origins;
  private final @Nonnull Map<Prefix6, Ipv6Origin> _ipv6Origins;
  private final @Nonnull String _name;
  private @Nullable String _snatpool;
  private @Nonnull Set<String> _vlans;
  private boolean _vlansEnabled;

  public Snat(String name) {
    _name = name;
    _ipv4Origins = new HashMap<>();
    _ipv6Origins = new HashMap<>();
    _vlans = new HashSet<>();
  }

  public @Nonnull Map<Prefix, Ipv4Origin> getIpv4Origins() {
    return _ipv4Origins;
  }

  public @Nonnull Map<Prefix6, Ipv6Origin> getIpv6Origins() {
    return _ipv6Origins;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable String getSnatpool() {
    return _snatpool;
  }

  public @Nonnull Set<String> getVlans() {
    return _vlans;
  }

  public boolean getVlansEnabled() {
    return _vlansEnabled;
  }

  public void setSnatpool(@Nullable String snatpool) {
    _snatpool = snatpool;
  }

  public void setVlansEnabled(boolean vlansEnabled) {
    _vlansEnabled = vlansEnabled;
  }
}
