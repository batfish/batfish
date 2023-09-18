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

  public @Nullable Boolean getActivate() {
    return _activate;
  }

  public void setActivate(boolean activate) {
    _activate = activate;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public @Nonnull BgpNeighborId getId() {
    return _id;
  }

  public @Nullable Integer getMaximumPrefix() {
    return _maximumPrefix;
  }

  public void setMaximumPrefix(Integer maximumPrefix) {
    _maximumPrefix = maximumPrefix;
  }

  public @Nullable Integer getMaximumPrefixThreshold() {
    return _maximumPrefixThreshold;
  }

  public void setMaximumPrefixThreshold(Integer maximumPrefixThreshold) {
    _maximumPrefixThreshold = maximumPrefixThreshold;
  }

  public @Nullable Long getRemoteAs() {
    return _remoteAs;
  }

  public void setRemoteAs(Long remoteAs) {
    _remoteAs = remoteAs;
  }

  public @Nullable SendCommunity getSendCommunity() {
    return _sendCommunity;
  }

  public void setSendCommunity(SendCommunity sendCommunity) {
    _sendCommunity = sendCommunity;
  }

  public @Nullable Integer getWeight() {
    return _weight;
  }

  public void setWeight(Integer weight) {
    _weight = weight;
  }

  public @Nullable BgpNeighborUpdateSource getUpdateSource() {
    return _updateSource;
  }

  public void setUpdateSource(BgpNeighborUpdateSource updateSource) {
    _updateSource = updateSource;
  }

  public BgpNeighbor(BgpNeighborId id) {
    _id = id;
  }

  private @Nullable Boolean _activate;
  private @Nullable String _description;
  private final @Nonnull BgpNeighborId _id;
  private @Nullable Integer _maximumPrefix;
  private @Nullable Integer _maximumPrefixThreshold;
  private @Nullable Long _remoteAs;
  private @Nullable SendCommunity _sendCommunity;
  private @Nullable Integer _weight;
  private @Nullable BgpNeighborUpdateSource _updateSource;
}
