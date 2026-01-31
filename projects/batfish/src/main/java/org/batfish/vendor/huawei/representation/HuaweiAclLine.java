package org.batfish.vendor.huawei.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a single line/entry in a Huawei ACL.
 *
 * <p>This is a stub class for future ACL line implementation.
 */
public class HuaweiAclLine implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Line number/sequence number */
  private int _sequenceNumber;

  /** Action: permit or deny */
  private @Nonnull String _action;

  /** Protocol (tcp, udp, ip, icmp, etc.) */
  private @Nullable String _protocol;

  /** Source IP address/prefix */
  private @Nullable String _source;

  /** Source port(s) */
  private @Nullable String _sourcePort;

  /** Destination IP address/prefix */
  private @Nullable String _destination;

  /** Destination port(s) */
  private @Nullable String _destinationPort;

  public HuaweiAclLine(int sequenceNumber, @Nonnull String action) {
    _sequenceNumber = sequenceNumber;
    _action = action;
  }

  /**
   * Gets the sequence number.
   *
   * @return The sequence number
   */
  public int getSequenceNumber() {
    return _sequenceNumber;
  }

  /**
   * Sets the sequence number.
   *
   * @param sequenceNumber The sequence number to set
   */
  public void setSequenceNumber(int sequenceNumber) {
    _sequenceNumber = sequenceNumber;
  }

  /**
   * Gets the action (permit or deny).
   *
   * @return The action
   */
  @Nonnull
  public String getAction() {
    return _action;
  }

  /**
   * Sets the action.
   *
   * @param action The action to set
   */
  public void setAction(@Nonnull String action) {
    _action = action;
  }

  /**
   * Gets the protocol.
   *
   * @return The protocol (tcp, udp, icmp, ip, etc.)
   */
  @Nullable
  public String getProtocol() {
    return _protocol;
  }

  /**
   * Sets the protocol.
   *
   * @param protocol The protocol to set
   */
  public void setProtocol(@Nullable String protocol) {
    _protocol = protocol;
  }

  /**
   * Gets the source IP address/prefix.
   *
   * @return The source address
   */
  @Nullable
  public String getSource() {
    return _source;
  }

  /**
   * Sets the source IP address/prefix.
   *
   * @param source The source address to set
   */
  public void setSource(@Nullable String source) {
    _source = source;
  }

  /**
   * Gets the source port(s).
   *
   * @return The source port
   */
  @Nullable
  public String getSourcePort() {
    return _sourcePort;
  }

  /**
   * Sets the source port(s).
   *
   * @param sourcePort The source port to set
   */
  public void setSourcePort(@Nullable String sourcePort) {
    _sourcePort = sourcePort;
  }

  /**
   * Gets the destination IP address/prefix.
   *
   * @return The destination address
   */
  @Nullable
  public String getDestination() {
    return _destination;
  }

  /**
   * Sets the destination IP address/prefix.
   *
   * @param destination The destination address to set
   */
  public void setDestination(@Nullable String destination) {
    _destination = destination;
  }

  /**
   * Gets the destination port(s).
   *
   * @return The destination port
   */
  @Nullable
  public String getDestinationPort() {
    return _destinationPort;
  }

  /**
   * Sets the destination port(s).
   *
   * @param destinationPort The destination port to set
   */
  public void setDestinationPort(@Nullable String destinationPort) {
    _destinationPort = destinationPort;
  }
}
