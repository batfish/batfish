package org.batfish.representation.huawei;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents an Access Control List (ACL) on a Huawei VRP device.
 *
 * <p>This is a stub class for future ACL implementation. It will store ACL configuration including
 * ACL name, type (basic, advanced, or layer-2), and ACL entries.
 */
public class HuaweiAcl implements Serializable {

  private static final long serialVersionUID = 1L;

  /** ACL type enumeration */
  public enum AclType {
    BASIC, // Basic ACL (source IP only)
    ADVANCED, // Advanced ACL (source, destination, protocol, ports)
    L2 // Layer-2 ACL (MAC addresses)
  }

  /** ACL name or number */
  @Nonnull private String _name;

  /** ACL type */
  @Nonnull private AclType _type;

  /** ACL entries/lines */
  @Nonnull private List<HuaweiAclLine> _lines;

  /** Whether this is an IPv6 ACL */
  private boolean _ipv6;

  /** VRF name for this ACL (if applicable) */
  @Nullable private String _vrfName;

  public HuaweiAcl(@Nonnull String name, @Nonnull AclType type) {
    _name = name;
    _type = type;
    _lines = new ArrayList<>();
    _ipv6 = false;
  }

  /**
   * Gets the ACL name.
   *
   * @return The ACL name
   */
  @Nonnull
  public String getName() {
    return _name;
  }

  /**
   * Sets the ACL name.
   *
   * @param name The ACL name to set
   */
  public void setName(@Nonnull String name) {
    _name = name;
  }

  /**
   * Gets the ACL type.
   *
   * @return The ACL type
   */
  @Nonnull
  public AclType getType() {
    return _type;
  }

  /**
   * Sets the ACL type.
   *
   * @param type The ACL type to set
   */
  public void setType(@Nonnull AclType type) {
    _type = type;
  }

  /**
   * Gets the ACL lines/entries.
   *
   * @return A list of ACL lines
   */
  @Nonnull
  public List<HuaweiAclLine> getLines() {
    return _lines;
  }

  /**
   * Sets the ACL lines/entries.
   *
   * @param lines The list of ACL lines to set
   */
  public void setLines(@Nonnull List<HuaweiAclLine> lines) {
    _lines = lines;
  }

  /**
   * Adds an ACL line/entry.
   *
   * @param line The ACL line to add
   */
  public void addLine(@Nonnull HuaweiAclLine line) {
    _lines.add(line);
  }

  /**
   * Checks if this is an IPv6 ACL.
   *
   * @return true if this is an IPv6 ACL, false otherwise
   */
  public boolean isIpv6() {
    return _ipv6;
  }

  /**
   * Sets whether this is an IPv6 ACL.
   *
   * @param ipv6 true if this is an IPv6 ACL, false otherwise
   */
  public void setIpv6(boolean ipv6) {
    _ipv6 = ipv6;
  }

  /**
   * Gets the VRF name for this ACL.
   *
   * @return The VRF name, or null if not applicable
   */
  @Nullable
  public String getVrfName() {
    return _vrfName;
  }

  /**
   * Sets the VRF name for this ACL.
   *
   * @param vrfName The VRF name to set
   */
  public void setVrfName(@Nullable String vrfName) {
    _vrfName = vrfName;
  }
}

/**
 * Represents a single line/entry in a Huawei ACL.
 *
 * <p>This is a stub class for future ACL line implementation.
 */
class HuaweiAclLine implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Line number/sequence number */
  private int _sequenceNumber;

  /** Action: permit or deny */
  @Nonnull private String _action;

  /** Protocol (tcp, udp, ip, icmp, etc.) */
  @Nullable private String _protocol;

  /** Source IP address/prefix */
  @Nullable private String _source;

  /** Source port(s) */
  @Nullable private String _sourcePort;

  /** Destination IP address/prefix */
  @Nullable private String _destination;

  /** Destination port(s) */
  @Nullable private String _destinationPort;

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

  // TODO: Add getters and setters for other fields
}
