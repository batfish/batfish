package org.batfish.representation.palo_alto;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/**
 * Configuration of a BGP Peer {@code network virtual-router NAME protocol bgp peer-group NAME peer
 * NAME}.
 */
public class BgpPeer implements Serializable {
  public enum ReflectorClient {
    CLIENT,
    MESHED_CLIENT,
    NON_CLIENT,
  }

  /** TODO From PAN admin UI - only shows in running config if checked (as yes). */
  private static final boolean DEFAULT_ENABLE = false;

  public BgpPeer(String name) {
    _enable = DEFAULT_ENABLE;
    _name = name;
  }

  public BgpConnectionOptions getConnectionOptions() {
    return _connectionOptions;
  }

  public boolean getEnable() {
    return _enable;
  }

  public void setEnable(boolean enable) {
    _enable = enable;
  }

  public @Nullable Boolean getEnableSenderSideLoopDetection() {
    return _enableSenderSideLoopDetection;
  }

  public void setEnableSenderSideLoopDetection(boolean enableSenderSideLoopDetection) {
    _enableSenderSideLoopDetection = enableSenderSideLoopDetection;
  }

  public @Nullable Ip getLocalAddress() {
    return _localAddress;
  }

  public void setLocalAddress(@Nullable Ip localAddress) {
    _localAddress = localAddress;
  }

  public @Nullable String getLocalInterface() {
    return _localInterface;
  }

  public void setLocalInterface(@Nullable String localInterface) {
    _localInterface = localInterface;
  }

  public @Nullable Integer getMultihop() {
    return _multihop;
  }

  public void setMultihop(@Nullable Integer multihop) {
    _multihop = multihop;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable Ip getPeerAddress() {
    return _peerAddress;
  }

  public void setPeerAddress(@Nullable Ip peerAddress) {
    _peerAddress = peerAddress;
  }

  public @Nullable Long getPeerAs() {
    return _peerAs;
  }

  public void setPeerAs(@Nullable Long peerAs) {
    _peerAs = peerAs;
  }

  public @Nullable ReflectorClient getReflectorClient() {
    return _reflectorClient;
  }

  public void setReflectorClient(@Nullable ReflectorClient reflectorClient) {
    _reflectorClient = reflectorClient;
  }

  public @Nullable String getBfdProfile() {
    return _bfdProfile;
  }

  public void setBfdProfile(@Nullable String bfdProfile) {
    _bfdProfile = bfdProfile;
  }

  // private implementation details

  private BgpConnectionOptions _connectionOptions = new BgpConnectionOptions();
  private @Nullable String _bfdProfile;
  private boolean _enable;
  private @Nullable Boolean _enableSenderSideLoopDetection;
  private @Nullable Ip _localAddress;
  private @Nullable String _localInterface;
  private final @Nonnull String _name;
  private @Nullable Ip _peerAddress;
  private @Nullable Long _peerAs;
  private @Nullable ReflectorClient _reflectorClient;
  private @Nullable Integer _multihop;
}
