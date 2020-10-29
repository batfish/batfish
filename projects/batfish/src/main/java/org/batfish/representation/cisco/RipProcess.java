package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;

public class RipProcess implements Serializable {

  private static final int DEFAULT_DEFAULT_INFORMATION_METRIC = 1;

  private final SortedSet<String> _activeInterfaceList;

  private long _defaultInformationMetric;

  private boolean _defaultInformationOriginate;

  private String _defaultInformationOriginateMap;

  private String _distributeListIn;

  private boolean _distributeListInAcl;

  private int _distributeListInLine;

  private String _distributeListOut;

  private boolean _distributeListOutAcl;

  private int _distributeListOutLine;

  private final SortedSet<Prefix> _networks;

  private boolean _passiveInterfaceDefault;

  private final SortedSet<String> _passiveInterfaceList;

  private final SortedMap<RoutingProtocol, RipRedistributionPolicy> _redistributionPolicies;

  public RipProcess() {
    _activeInterfaceList = new TreeSet<>();
    _defaultInformationMetric = DEFAULT_DEFAULT_INFORMATION_METRIC;
    _networks = new TreeSet<>();
    _passiveInterfaceList = new TreeSet<>();
    _redistributionPolicies = new TreeMap<>();
  }

  public SortedSet<String> getActiveInterfaceList() {
    return _activeInterfaceList;
  }

  public long getDefaultInformationMetric() {
    return _defaultInformationMetric;
  }

  public boolean getDefaultInformationOriginate() {
    return _defaultInformationOriginate;
  }

  public String getDefaultInformationOriginateMap() {
    return _defaultInformationOriginateMap;
  }

  public String getDistributeListIn() {
    return _distributeListIn;
  }

  public boolean getDistributeListInAcl() {
    return _distributeListInAcl;
  }

  public int getDistributeListInLine() {
    return _distributeListInLine;
  }

  public String getDistributeListOut() {
    return _distributeListOut;
  }

  public boolean getDistributeListOutAcl() {
    return _distributeListOutAcl;
  }

  public int getDistributeListOutLine() {
    return _distributeListOutLine;
  }

  public SortedSet<Prefix> getNetworks() {
    return _networks;
  }

  public boolean getPassiveInterfaceDefault() {
    return _passiveInterfaceDefault;
  }

  public SortedSet<String> getPassiveInterfaceList() {
    return _passiveInterfaceList;
  }

  public SortedMap<RoutingProtocol, RipRedistributionPolicy> getRedistributionPolicies() {
    return _redistributionPolicies;
  }

  public void setDefaultInformationMetric(int defaultInformationMetric) {
    _defaultInformationMetric = defaultInformationMetric;
  }

  public void setDefaultInformationOriginate(boolean defaultInformationOriginate) {
    _defaultInformationOriginate = defaultInformationOriginate;
  }

  public void setDefaultInformationOriginateMap(String defaultInformationOriginateMap) {
    _defaultInformationOriginateMap = defaultInformationOriginateMap;
  }

  public void setDistributeListIn(String distributeListIn) {
    _distributeListIn = distributeListIn;
  }

  public void setDistributeListInAcl(boolean distributeListInAcl) {
    _distributeListInAcl = distributeListInAcl;
  }

  public void setDistributeListInLine(int distributeListInLine) {
    _distributeListInLine = distributeListInLine;
  }

  public void setDistributeListOut(String distributeListOut) {
    _distributeListOut = distributeListOut;
  }

  public void setDistributeListOutAcl(boolean distributeListOutAcl) {
    _distributeListOutAcl = distributeListOutAcl;
  }

  public void setDistributeListOutLine(int distributeListOutLine) {
    _distributeListOutLine = distributeListOutLine;
  }

  public void setPassiveInterfaceDefault(boolean passiveInterfaceDefault) {
    _passiveInterfaceDefault = passiveInterfaceDefault;
  }
}
