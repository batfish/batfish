package org.batfish.dataplane.ibdp;

import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpSession;

/**
 * Represents an undirected peering between two BGP neighbors, with deterministic ordering of peers.
 * {@link UndirectedBgpSession} is immutable.
 */
public class UndirectedBgpSession implements Comparable<UndirectedBgpSession> {
  @Nonnull private final BgpPeerConfig _first;
  @Nonnull private final BgpPeerConfig _second;

  /**
   * Compare BGP neighbors using prefix and hostname. This comparator is **NOT** consistent with
   * equals
   */
  private static final Comparator<BgpPeerConfig> COMPARATOR =
      Comparator.comparing(BgpPeerConfig::getPrefix).thenComparing(BgpPeerConfig::getOwner);

  UndirectedBgpSession(@Nonnull BgpPeerConfig n1, @Nonnull BgpPeerConfig n2) {
    if (COMPARATOR.compare(n1, n2) < 0) {
      _first = n1;
      _second = n2;
    } else {
      _first = n2;
      _second = n1;
    }
  }

  @Nonnull
  public BgpPeerConfig getFirst() {
    return _first;
  }

  @Nonnull
  public BgpPeerConfig getSecond() {
    return _second;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UndirectedBgpSession other = (UndirectedBgpSession) o;
    return Objects.equals(getFirst().getPrefix(), other.getFirst().getPrefix())
        && Objects.equals(getFirst().getOwner(), other.getFirst().getOwner())
        && Objects.equals(getSecond().getPrefix(), other.getSecond().getPrefix())
        && Objects.equals(getSecond().getOwner(), other.getSecond().getOwner());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getFirst(), getFirst().getOwner(), getSecond(), getSecond().getOwner());
  }

  /** Check if this session is eBGP. */
  public boolean isEbgpSession() {
    return !Objects.equals(_first.getLocalAs(), _second.getLocalAs());
  }

  @Override
  public int compareTo(@Nonnull UndirectedBgpSession o) {
    return Comparator.comparing(UndirectedBgpSession::getFirst, COMPARATOR)
        .thenComparing(UndirectedBgpSession::getSecond, COMPARATOR)
        .compare(this, o);
  }

  public static UndirectedBgpSession from(@Nonnull BgpSession session) {
    return new UndirectedBgpSession(session.getSrc(), session.getDst());
  }

  public static UndirectedBgpSession from(@Nonnull BgpPeerConfig n1, @Nonnull BgpPeerConfig n2) {
    return new UndirectedBgpSession(n1, n2);
  }
}
