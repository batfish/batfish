package org.batfish.symbolic.abstraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;
import org.batfish.symbolic.CommunityVar;
import org.batfish.symbolic.Protocol;

/**
 * A collection of attributes describing an advertisement, represented using BDDs
 *
 * @author Ryan Beckett
 */
public class BDDRecord {

  static BDDFactory factory = JFactory.init(500, 1000);

  private BDDInteger _prefix;

  private BDDInteger _prefixLength;

  private BDDInteger _adminDist;

  private BDDInteger _metric;

  private BDDInteger _med;

  private BDDInteger _localPref;

  private BDDDomain<Protocol> _protocolHistory;

  private Map<CommunityVar, BDD> _communities;

  // TODO: Same parent route reflectors

  // TODO: BGP internal on interface

  // TODO: OSPF area on interface

  // TODO: MED


  public BDDRecord(Set<CommunityVar> comms) {
    // Make sure we have the right number of variables
    int numVars = factory.varNum();
    int numNeeded = 32 * 6 + comms.size() + 2;
    if (numVars < numNeeded) {
      factory.setVarNum(numNeeded);
    }

    // Initialize integer values
    _prefixLength = BDDInteger.makeFromIndex(32, 0);
    _prefix = BDDInteger.makeFromIndex(32, 32);
    _metric = BDDInteger.makeFromIndex(32, 64);
    _adminDist = BDDInteger.makeFromIndex(32, 96);
    _med = BDDInteger.makeFromIndex(32, 128);
    _localPref = BDDInteger.makeFromIndex(32, 160);

    // Initialize communities
    _communities = new HashMap<>();
    int i = 192;
    for (CommunityVar comm : comms) {
      _communities.put(comm, factory.ithVar(i));
      i++;
    }

    // Initialize the choice of protocol
    List<Protocol> allProtos = new ArrayList<>();
    allProtos.add(Protocol.CONNECTED);
    allProtos.add(Protocol.STATIC);
    allProtos.add(Protocol.OSPF);
    allProtos.add(Protocol.BGP);
    _protocolHistory = new BDDDomain<>(allProtos, 192 + comms.size());
  }

  public BDDRecord(BDDRecord other) {
    _communities = new HashMap<>(other._communities);
    _prefixLength = new BDDInteger(other._prefixLength);
    _prefix = new BDDInteger(other._prefix);
    _metric = new BDDInteger(other._metric);
    _adminDist = new BDDInteger(other._adminDist);
    _med = new BDDInteger(other._med);
    _localPref = new BDDInteger(other._localPref);
    _protocolHistory = new BDDDomain<>(other._protocolHistory);
  }


  public BDDInteger getPrefixLength() {
    return _prefixLength;
  }

  public void setPrefixLength(BDDInteger prefixLength) {
    this._prefixLength = prefixLength;
  }

  public BDDInteger getPrefix() {
    return _prefix;
  }

  public void setPrefix(BDDInteger prefix) {
    this._prefix = prefix;
  }

  public BDDInteger getAdminDist() {
    return _adminDist;
  }

  public void setAdminDist(BDDInteger adminDist) {
    this._adminDist = adminDist;
  }

  public BDDInteger getMetric() {
    return _metric;
  }

  public void setMetric(BDDInteger metric) {
    this._metric = metric;
  }

  public BDDInteger getMed() {
    return _med;
  }

  public void setMed(BDDInteger med) {
    this._med = med;
  }

  public BDDInteger getLocalPref() {
    return _localPref;
  }

  public void setLocalPref(BDDInteger localPref) {
    this._localPref = localPref;
  }

  public BDDDomain<Protocol> getProtocolHistory() {
    return _protocolHistory;
  }

  public void setProtocolHistory(BDDDomain<Protocol> protocolHistory) {
    this._protocolHistory = protocolHistory;
  }

  public Map<CommunityVar, BDD> getCommunities() {
    return _communities;
  }

  public void setCommunities(Map<CommunityVar, BDD> communities) {
    this._communities = communities;
  }

  public BDDRecord copy() {
    return new BDDRecord(this);
  }
}
