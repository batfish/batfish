package org.batfish.datamodel;

public abstract class AbstractRoute6Builder<T extends AbstractRoute6> {

  protected int _admin;

  protected int _metric;

  protected Prefix6 _network;

  protected Ip6 _nextHopIp;

  protected int _tag;

  public abstract T build();

  public final Integer getAdmin() {
    return _admin;
  }

  public final Integer getMetric() {
    return _metric;
  }

  public final Prefix6 getNetwork() {
    return _network;
  }

  public final Ip6 getNextHopIp() {
    return _nextHopIp;
  }

  public final void setAdmin(int admin) {
    _admin = admin;
  }

  public final void setMetric(int metric) {
    _metric = metric;
  }

  public final void setNetwork(Prefix6 network) {
    _network = network;
  }

  public final void setNextHopIp(Ip6 nextHopIp) {
    _nextHopIp = nextHopIp;
  }

  public final void setTag(int tag) {
    _tag = tag;
  }
}
