package org.batfish.vendor.aruba_aoscx.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.LineAction;

/** One IPv4 ACE in an Aruba AOS-CX access list. */
public final class AosCxIpAccessListEntry implements Serializable {

  public AosCxIpAccessListEntry(
      long sequence,
      LineAction action,
      String protocol,
      String source,
      String destination) {
    _sequence = sequence;
    _action = action;
    _protocol = protocol;
    _source = source;
    _destination = destination;
  }

  public long getSequence() {
    return _sequence;
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  public @Nonnull String getProtocol() {
    return _protocol;
  }

  public @Nonnull String getSource() {
    return _source;
  }

  public @Nonnull String getDestination() {
    return _destination;
  }

  private final long _sequence;
  private final @Nonnull LineAction _action;
  private final @Nonnull String _protocol;
  private final @Nonnull String _source;
  private final @Nonnull String _destination;
}
