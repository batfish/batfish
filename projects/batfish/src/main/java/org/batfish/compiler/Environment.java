package org.batfish.compiler;

import org.batfish.symbolic.IDeepCopy;

public class Environment implements IDeepCopy<Environment> {

  // NV expression for ad
  private String _lp;

  private String _ad;

  private String _cost;

  private String _med;

  private String _communities;

  private String _protocol;

  private String _prefixLength;

  private String _prefixValue;

  public Environment() {
    this._lp = "lp";
    this._ad = "ad";
    this._cost = "cost";
    this._med = "med";
    this._communities = "comms";
    this._protocol = "proto";
    this._prefixLength = "prefixLen";
    this._prefixValue = "prefix";
  }

  public Environment(
      String _lp,
      String _ad,
      String _cost,
      String _med,
      String _communities,
      String _protocol,
      String _prefixLength,
      String _prefixValue) {
    this._lp = _lp;
    this._ad = _ad;
    this._cost = _cost;
    this._med = _med;
    this._communities = _communities;
    this._protocol = _protocol;
    this._prefixLength = _prefixLength;
    this._prefixValue = _prefixValue;
  }

  public String get_prefixValue() {
    return _prefixValue;
  }

  public void set_prefixValue(String _prefixValue) {
    this._prefixValue = _prefixValue;
  }

  public String get_prefixLength() {
    return _prefixLength;
  }

  public void set_prefixLength(String _prefixLength) {
    this._prefixLength = _prefixLength;
  }

  public String get_lp() {
    return _lp;
  }

  public void set_lp(String _lp) {
    this._lp = _lp;
  }

  public String get_ad() {
    return _ad;
  }

  public void set_ad(String _ad) {
    this._ad = _ad;
  }

  public String get_cost() {
    return _cost;
  }

  public void set_cost(String _cost) {
    this._cost = _cost;
  }

  public String get_med() {
    return _med;
  }

  public void set_med(String _med) {
    this._med = _med;
  }

  public String get_communities() {
    return _communities;
  }

  public void set_communities(String _communities) {
    this._communities = _communities;
  }

  public String get_protocol() {
    return _protocol;
  }

  public void set_protocol(String _protocol) {
    this._protocol = _protocol;
  }

  @Override
  public Environment deepCopy() {
    Environment env = new Environment();
    env.set_ad(this.get_ad());
    env.set_lp(this.get_lp());
    env.set_cost(this.get_cost());
    env.set_communities(this.get_communities());
    env.set_med(this.get_med());
    env.set_protocol(this.get_protocol());
    env.set_prefixLength(this.get_prefixLength());
    env.set_prefixValue(this.get_prefixValue());
    return env;
  }
}
