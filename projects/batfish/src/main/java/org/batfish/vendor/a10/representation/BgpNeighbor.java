package org.batfish.vendor.a10.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Data model class representing configuration of a BGP neighbor. */
public class BgpNeighbor implements Serializable {
  public enum SendCommunity {
    BOTH,
    EXTENDED,
    NONE,
    STANDARD,
  }

  public boolean isActivate() {
    return _activate;
  }

  public void setActivate(boolean activate) {
    _activate = activate;
  }

  @Nullable
  public String getDescription() {
    return _description;
  }

  public void setDescription(String description) {
    _description = description;
  }

  @Nonnull
  public BgpNeighborId getId() {
    return _id;
  }

  @Nullable
  public Integer getMaximumPrefix() {
    return _maximumPrefix;
  }

  public void setMaximumPrefix(Integer maximumPrefix) {
    _maximumPrefix = maximumPrefix;
  }

  @Nullable
  public Integer getMaximumPrefixThreshold() {
    return _maximumPrefixThreshold;
  }

  public void setMaximumPrefixThreshold(Integer maximumPrefixThreshold) {
    _maximumPrefixThreshold = maximumPrefixThreshold;
  }

  @Nullable
  public Long getRemoteAs() {
    return _remoteAs;
  }

  public void setRemoteAs(Long remoteAs) {
    _remoteAs = remoteAs;
  }

  @Nullable
  public SendCommunity getSendCommunity() {
    return _sendCommunity;
  }

  public void setSendCommunity(SendCommunity sendCommunity) {
    _sendCommunity = sendCommunity;
  }

  @Nullable
  public Integer getWeight() {
    return _weight;
  }

  public void setWeight(Integer weight) {
    _weight = weight;
  }

  @Nullable
  public BgpNeighborUpdateSource getUpdateSource() {
    return _updateSource;
  }

  public void setUpdateSource(BgpNeighborUpdateSource updateSource) {
    _updateSource = updateSource;
  }

  public BgpNeighbor(BgpNeighborId id) {
    _id = id;
  }

  private boolean _activate;
  @Nullable private String _description;
  @Nonnull private final BgpNeighborId _id;
  @Nullable private Integer _maximumPrefix;
  @Nullable private Integer _maximumPrefixThreshold;
  @Nullable private Long _remoteAs;
  @Nullable private SendCommunity _sendCommunity;
  @Nullable private Integer _weight;
  @Nullable private BgpNeighborUpdateSource _updateSource;
}
