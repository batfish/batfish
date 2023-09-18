package org.batfish.representation.palo_alto;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Configuration of an OSPF interface within an OSPF area {@code network virtual-router NAME
 * protocol ospf area AREA_ID interface INTERFACE_NAME}.
 */
public class OspfInterface implements Serializable {
  public enum LinkType {
    BROADCAST,
    P2P,
    P2MP
  }

  /** From PAN admin UI - sets hello interval to 10 if not set by the user */
  private static final int DEFAULT_HELLO_INTERVAL_SECS = 10;

  /** From PAN admin UI - sets dead counts to 4 if not set by the user */
  private static final int DEFAULT_DEAD_COUNTS = 4;

  /** From PAN admin UI - sets link type to BROADCAST if not explicitly changes by the user. */
  private static final LinkType DEFAULT_LINK_TYPE = LinkType.BROADCAST;

  public OspfInterface(String name) {
    _name = name;
    _linkType = DEFAULT_LINK_TYPE;
    _helloInterval = DEFAULT_HELLO_INTERVAL_SECS;
    _deadCounts = DEFAULT_DEAD_COUNTS;
  }

  public @Nonnull Integer getDeadCounts() {
    return _deadCounts;
  }

  public void setDeadCounts(@Nonnull Integer deadCounts) {
    _deadCounts = deadCounts;
  }

  public @Nullable Boolean getEnable() {
    return _enable;
  }

  public void setEnable(@Nullable Boolean enable) {
    _enable = enable;
  }

  public @Nonnull LinkType getLinkType() {
    return _linkType;
  }

  public void setLinkType(@Nonnull LinkType linkType) {
    _linkType = linkType;
  }

  public @Nonnull Integer getHelloInterval() {
    return _helloInterval;
  }

  public void setHelloInterval(@Nonnull Integer helloInterval) {
    _helloInterval = helloInterval;
  }

  public @Nullable Integer getMetric() {
    return _metric;
  }

  public void setMetric(@Nullable Integer metric) {
    _metric = metric;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable Boolean getPassive() {
    return _passive;
  }

  public void setPassive(@Nullable Boolean passive) {
    _passive = passive;
  }

  /** Priority for the router to be elected as a DR or BDR */
  public @Nullable Integer getPriority() {
    return _priority;
  }

  public void setPriority(@Nullable Integer priority) {
    _priority = priority;
  }

  /**
   * Length of time, in seconds, that OSPF waits to receive a link-state advertisement (LSA) from a
   * neighbor before OSPF retransmits the LSA
   */
  public @Nullable Integer getRetransmitInterval() {
    return _retransmitInterval;
  }

  public void setRetransmitInterval(@Nullable Integer retransmitInterval) {
    _retransmitInterval = retransmitInterval;
  }

  /** Length of time, in seconds, that an LSA is delayed before it is sent out of an interface */
  public @Nullable Integer getTransitDelay() {
    return _transitDelay;
  }

  public void setTransitDelay(@Nullable Integer transitDelay) {
    _transitDelay = transitDelay;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////
  private @Nonnull Integer _deadCounts;
  private @Nullable Boolean _enable;
  private @Nonnull LinkType _linkType;
  private @Nonnull Integer _helloInterval;
  private @Nullable Integer _metric;
  private @Nonnull String _name;
  private @Nullable Boolean _passive;
  private @Nullable Integer _priority;
  private @Nullable Integer _retransmitInterval;
  private @Nullable Integer _transitDelay;
}
