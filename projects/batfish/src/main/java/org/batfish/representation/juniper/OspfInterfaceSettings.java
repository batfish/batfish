package org.batfish.representation.juniper;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

/** Represents OSPF settings for an interface. */
@ParametersAreNonnullByDefault
public class OspfInterfaceSettings implements Serializable {

  /** Represents the type of interface for OSPF */
  public enum OspfInterfaceType {
    /** This is not an explicit type -- assumed by default */
    BROADCAST,
    /** non-broadcast multi-access */
    NBMA,
    /** Point to multipoint */
    P2MP,
    /** Point to multipoint over lan */
    P2MP_OVER_LAN,
    /** Point to point */
    P2P
  }

  @Nonnull private final Ip _ospfArea;
  @Nullable private Integer _ospfCost;
  @Nullable private Integer _ospfDeadInterval;
  @Nullable private Boolean _ospfDisable;
  @Nullable private Integer _ospfHelloInterval;
  @Nullable private OspfInterfaceType _ospfInterfaceType;
  private boolean _ospfPassive;
  @Nonnull private final Set<InterfaceOspfNeighbor> _ospfNeighbors;

  public OspfInterfaceSettings(Ip ospfArea) {
    _ospfArea = ospfArea;
    _ospfNeighbors = new HashSet<>();
  }

  /** Returns the configured or vendor default {@link OspfInterfaceType}. */
  public @Nonnull OspfInterfaceType getOspfInterfaceTypeOrDefault() {
    return firstNonNull(_ospfInterfaceType, OspfInterfaceType.BROADCAST);
  }

  public @Nonnull Ip getOspfArea() {
    return _ospfArea;
  }

  public Integer getOspfCost() {
    return _ospfCost;
  }

  /** Get the time (in seconds) to wait before neighbors are declared dead */
  @Nullable
  public Integer getOspfDeadInterval() {
    return _ospfDeadInterval;
  }

  @Nullable
  public Boolean getOspfDisable() {
    return _ospfDisable;
  }

  /** Get the time (in seconds) between sending hello messages to neighbors */
  @Nullable
  public Integer getOspfHelloInterval() {
    return _ospfHelloInterval;
  }

  @Nullable
  public OspfInterfaceType getOspfInterfaceType() {
    return _ospfInterfaceType;
  }

  public boolean getOspfPassive() {
    return _ospfPassive;
  }

  public @Nonnull Set<InterfaceOspfNeighbor> getOspfNeighbors() {
    return _ospfNeighbors;
  }

  public void setOspfCost(int ospfCost) {
    _ospfCost = ospfCost;
  }

  public void setOspfDeadInterval(int seconds) {
    _ospfDeadInterval = seconds;
  }

  public void setOspfDisable(boolean disable) {
    _ospfDisable = disable;
  }

  public void setOspfHelloInterval(int seconds) {
    _ospfHelloInterval = seconds;
  }

  public void setOspfPassive(boolean ospfPassive) {
    _ospfPassive = true;
  }

  public void setOspfInterfaceType(OspfInterfaceType ospfInterfaceType) {
    _ospfInterfaceType = ospfInterfaceType;
  }
}
