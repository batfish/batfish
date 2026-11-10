package org.batfish.vendor.rtx.representation;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;

/**
 * An RTX interface: a physical LAN interface ({@code lan1}), an 802.1Q sub-interface of one
 * ({@code lan3/1}), or a tunnel ({@code tunnel1}).
 *
 * <p>An interface exists as soon as any command names it, even one that assigns no address. On the
 * J57 routers the WAN-side interfaces are referenced only by {@code ip <if> secure filter}.
 */
public final class RtxInterface implements Serializable {

  /** Kind of RTX interface, which determines the vendor-independent interface type. */
  public enum Type {
    LAN,
    LAN_SUBINTERFACE,
    TUNNEL
  }

  private final @Nonnull String _name;
  private final @Nonnull Type _type;
  private final @Nonnull List<ConcreteInterfaceAddress> _addresses;
  private final @Nonnull List<Long> _inboundFilters;
  private final @Nonnull List<Long> _outboundFilters;
  private final @Nonnull Map<Integer, RtxVrrpGroup> _vrrpGroups;
  private final @Nonnull List<Integer> _natDescriptors;
  private final @Nonnull List<Integer> _reverseNatDescriptors;
  private @Nullable Integer _vlanId;
  private @Nullable String _parentInterface;

  public RtxInterface(String name, Type type) {
    _name = name;
    _type = type;
    _addresses = new ArrayList<>();
    _inboundFilters = new ArrayList<>();
    _outboundFilters = new ArrayList<>();
    _vrrpGroups = new LinkedHashMap<>();
    _natDescriptors = new ArrayList<>();
    _reverseNatDescriptors = new ArrayList<>();
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull Type getType() {
    return _type;
  }

  /** Addresses assigned by {@code ip <if> address}, in configuration order. */
  public @Nonnull List<ConcreteInterfaceAddress> getAddresses() {
    return _addresses;
  }

  /** Filter numbers applied by {@code ip <if> secure filter in}, in evaluation order. */
  public @Nonnull List<Long> getInboundFilters() {
    return _inboundFilters;
  }

  /** Filter numbers applied by {@code ip <if> secure filter out}, in evaluation order. */
  public @Nonnull List<Long> getOutboundFilters() {
    return _outboundFilters;
  }

  public void setInboundFilters(List<Long> filters) {
    _inboundFilters.clear();
    _inboundFilters.addAll(filters);
  }

  public void setOutboundFilters(List<Long> filters) {
    _outboundFilters.clear();
    _outboundFilters.addAll(filters);
  }

  public @Nonnull Map<Integer, RtxVrrpGroup> getVrrpGroups() {
    return _vrrpGroups;
  }

  /** NAT descriptors applied by {@code ip <if> nat descriptor}, in configuration order. */
  public @Nonnull List<Integer> getNatDescriptors() {
    return _natDescriptors;
  }

  /**
   * NAT descriptors listed after the {@code reverse} keyword, which translate the opposite
   * direction's addresses and ports. Not modeled.
   */
  public @Nonnull List<Integer> getReverseNatDescriptors() {
    return _reverseNatDescriptors;
  }

  /** VLAN ID declared by {@code vlan <if>/<sub> 802.1q vid=<vid>}. */
  public @Nullable Integer getVlanId() {
    return _vlanId;
  }

  public void setVlanId(int vlanId) {
    _vlanId = vlanId;
  }

  /** The parent LAN interface of an 802.1Q sub-interface. */
  public @Nullable String getParentInterface() {
    return _parentInterface;
  }

  public void setParentInterface(String parentInterface) {
    _parentInterface = parentInterface;
  }

  /** An immutable snapshot of this interface's addresses. */
  public @Nonnull List<ConcreteInterfaceAddress> getAddressesSnapshot() {
    return ImmutableList.copyOf(_addresses);
  }
}
