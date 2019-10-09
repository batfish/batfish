package org.batfish.representation.palo_alto;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * /** Configuration of an OSPF interface within an OSPF area {@code network virtual-router NAME
 * protocol ospf area AREA_ID interface INTERFACE_NAME}.
 */
public class OspfInterface {
  public enum LinkType {
    BROADCAST,
    P2P,
    P2MP
  }

  public OspfInterface(String name) {
    _name = name;
  }

  @Nullable
  public Integer getDeadCounts() {
    return _deadCounts;
  }

  public void setDeadCounts(@Nullable Integer deadCounts) {
    _deadCounts = deadCounts;
  }

  @Nullable
  public Boolean getEnable() {
    return _enable;
  }

  public void setEnable(@Nullable Boolean enable) {
    _enable = enable;
  }

  @Nullable
  public LinkType getLinkType() {
    return _linkType;
  }

  public void setLinkType(@Nullable LinkType linkType) {
    _linkType = linkType;
  }

  @Nullable
  public Integer getHelloInterval() {
    return _helloInterval;
  }

  public void setHelloInterval(@Nullable Integer helloInterval) {
    _helloInterval = helloInterval;
  }

  @Nullable
  public Integer getMetric() {
    return _metric;
  }

  public void setMetric(@Nullable Integer metric) {
    _metric = metric;
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  @Nullable
  public Boolean getPassive() {
    return _passive;
  }

  public void setPassive(@Nullable Boolean passive) {
    _passive = passive;
  }

  /** Priority for the router to be elected as a DR or BDR */
  @Nullable
  public Integer getPriority() {
    return _priority;
  }

  public void setPriority(@Nullable Integer priority) {
    _priority = priority;
  }

  /**
   * Length of time, in seconds, that OSPF waits to receive a link-state advertisement (LSA) from a
   * neighbor before OSPF retransmits the LSA
   */
  @Nullable
  public Integer getRetransmitInterval() {
    return _retransmitInterval;
  }

  public void setRetransmitInterval(@Nullable Integer retransmitInterval) {
    _retransmitInterval = retransmitInterval;
  }

  /** Length of time, in seconds, that an LSA is delayed before it is sent out of an interface */
  @Nullable
  public Integer getTransitDelay() {
    return _transitDelay;
  }

  public void setTransitDelay(@Nullable Integer transitDelay) {
    _transitDelay = transitDelay;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////
  @Nullable private Integer _deadCounts;
  @Nullable private Boolean _enable;
  @Nullable private LinkType _linkType;
  @Nullable private Integer _helloInterval;
  @Nullable private Integer _metric;
  @Nonnull private String _name;
  @Nullable private Boolean _passive;
  @Nullable private Integer _priority;
  @Nullable private Integer _retransmitInterval;
  @Nullable private Integer _transitDelay;
}
