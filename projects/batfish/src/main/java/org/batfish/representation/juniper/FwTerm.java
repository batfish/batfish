package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class FwTerm implements Serializable {

  enum Field {
    ADDRESS,
    DESTINATION,
    DESTINATION_EXCEPT,
    DESTINATION_PORT,
    DSCP,
    FRAGMENT_OFFSET,
    FRAGMENT_OFFSET_EXCEPT,
    ICMP_CODE,
    ICMP_CODE_EXCEPT,
    ICMP_TYPE,
    ICMP_TYPE_EXCEPT,
    PACKET_LENGTH,
    PACKET_LENGTH_EXCEPT,
    PORT,
    PREFIX_LIST,
    PROTOCOL,
    SOURCE,
    SOURCE_EXCEPT,
    SOURCE_INTERFACE,
    SOURCE_PORT,
    TCP_FLAG,
    TTL,
    TTL_EXCEPT
  }

  private final List<FwFromApplicationSetMember> _fromApplicationSetMembers;

  private final List<HostProtocol> _fromHostProtocols;

  private final List<HostSystemService> _fromHostServices;

  private final List<FwFrom> _froms;

  private @Nullable FwFromIpOptions _fromIpOptions;

  private boolean _ipv6;

  private final String _name;

  private final List<FwThen> _thens;

  public FwTerm(String name) {
    _froms = new ArrayList<>();
    _fromApplicationSetMembers = new ArrayList<>();
    _fromHostProtocols = new ArrayList<>();
    _fromHostServices = new ArrayList<>();
    _name = name;
    _thens = new ArrayList<>();
  }

  public List<FwFromApplicationSetMember> getFromApplicationSetMembers() {
    return _fromApplicationSetMembers;
  }

  public List<HostProtocol> getFromHostProtocols() {
    return _fromHostProtocols;
  }

  public List<HostSystemService> getFromHostServices() {
    return _fromHostServices;
  }

  public @Nullable FwFromIpOptions getFromIpOptions() {
    return _fromIpOptions;
  }

  public @Nonnull FwFromIpOptions getOrCreateFromIpOptions() {
    if (_fromIpOptions == null) {
      _fromIpOptions = new FwFromIpOptions();
    }
    return _fromIpOptions;
  }

  public List<FwFrom> getFroms() {
    return _froms;
  }

  public boolean getIpv6() {
    return _ipv6;
  }

  public String getName() {
    return _name;
  }

  public List<FwThen> getThens() {
    return _thens;
  }

  public void setIpv6(boolean ipv6) {
    _ipv6 = ipv6;
  }
}
