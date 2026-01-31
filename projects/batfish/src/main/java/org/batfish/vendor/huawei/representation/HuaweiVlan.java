package org.batfish.vendor.huawei.representation;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a VLAN on a Huawei VRP device.
 *
 * <p>This class stores the configuration of a VLAN including its ID, name, description, and member
 * interfaces.
 */
public class HuaweiVlan implements Serializable {

  private static final long serialVersionUID = 1L;

  /** VLAN ID (1-4094) */
  private final int _vlanId;

  /** VLAN name */
  @Nullable private String _name;

  /** VLAN description */
  @Nullable private String _description;

  /** Set of interface names that are members of this VLAN */
  @Nonnull private SortedSet<String> _interfaces;

  /** Name of the associated VLANIF interface (if any) */
  @Nullable private String _vlanifInterface;

  /**
   * Creates a new VLAN with the specified ID.
   *
   * @param vlanId The VLAN ID (1-4094)
   */
  public HuaweiVlan(int vlanId) {
    _vlanId = vlanId;
    _interfaces = new TreeSet<>();
  }

  /**
   * Gets the VLAN ID.
   *
   * @return The VLAN ID
   */
  public int getVlanId() {
    return _vlanId;
  }

  /**
   * Gets the VLAN name.
   *
   * @return The VLAN name, or null if not set
   */
  @Nullable
  public String getName() {
    return _name;
  }

  /**
   * Sets the VLAN name.
   *
   * @param name The VLAN name to set
   */
  public void setName(@Nullable String name) {
    _name = name;
  }

  /**
   * Gets the VLAN description.
   *
   * @return The VLAN description, or null if not set
   */
  @Nullable
  public String getDescription() {
    return _description;
  }

  /**
   * Sets the VLAN description.
   *
   * @param description The VLAN description to set
   */
  public void setDescription(@Nullable String description) {
    _description = description;
  }

  /**
   * Gets the set of interface names that are members of this VLAN.
   *
   * @return A sorted set of interface names
   */
  @Nonnull
  public SortedSet<String> getInterfaces() {
    return _interfaces;
  }

  /**
   * Sets the set of interface names that are members of this VLAN.
   *
   * @param interfaces The sorted set of interface names
   */
  public void setInterfaces(@Nonnull SortedSet<String> interfaces) {
    _interfaces = interfaces;
  }

  /**
   * Adds an interface to this VLAN.
   *
   * @param interfaceName The interface name to add
   */
  public void addInterface(String interfaceName) {
    _interfaces.add(interfaceName);
  }

  /**
   * Gets the name of the associated VLANIF interface.
   *
   * @return The VLANIF interface name (e.g., "Vlanif100"), or null if not set
   */
  @Nullable
  public String getVlanifInterface() {
    return _vlanifInterface;
  }

  /**
   * Sets the name of the associated VLANIF interface.
   *
   * @param vlanifInterface The VLANIF interface name
   */
  public void setVlanifInterface(@Nullable String vlanifInterface) {
    _vlanifInterface = vlanifInterface;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add("vlanId", _vlanId)
        .add("name", _name)
        .add("description", _description)
        .add("interfaces", _interfaces)
        .add("vlanifInterface", _vlanifInterface)
        .toString();
  }
}
