package org.batfish.vendor.nec_ix.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;

/**
 * An NEC IX interface.
 *
 * <p>IX names a logical interface {@code <device>.<n>}, e.g. {@code GigaEthernet1.10}: the
 * Ethernet port {@code GigaEthernet1} (or switch port group {@code GigaEthernet2:7}) carries
 * {@code .0} untagged and the other sub-interfaces tagged with their {@code encapsulation dot1q}
 * VLAN.
 */
public final class NecIxInterface implements Serializable {

  /** Kinds of interfaces, determined by the interface name. */
  public enum Type {
    /** An Ethernet port. Never configured directly; the parent of Ethernet sub-interfaces. */
    ETHERNET_PORT,
    /** {@code <ethernet-device>.<n>}. */
    ETHERNET,
    LOOPBACK,
    NULL,
    TUNNEL,
    /** Bridge virtual interface: the routed interface of a bridge group. */
    BVI,
    /** ISDN, USB modem, ... */
    OTHER,
  }

  private static final Pattern ETHERNET_SUBINTERFACE =
      Pattern.compile("^((?:\\w*Ethernet)\\d+(?::\\d+)?)\\.\\d+$");

  /** The type of the interface named {@code name}. */
  public static @Nonnull Type computeType(String name) {
    if (name.startsWith("Loopback")) {
      return Type.LOOPBACK;
    } else if (name.startsWith("Null")) {
      return Type.NULL;
    } else if (name.startsWith("Tunnel")) {
      return Type.TUNNEL;
    } else if (name.startsWith("BVI")) {
      return Type.BVI;
    } else if (ETHERNET_SUBINTERFACE.matcher(name).matches()) {
      return Type.ETHERNET;
    }
    return Type.OTHER;
  }

  /**
   * The Ethernet port ({@code GigaEthernet1}, {@code GigaEthernet2:7}) that the Ethernet
   * sub-interface {@code name} runs on, or {@code null} if {@code name} is not one.
   */
  public static @Nullable String computeParentPort(String name) {
    Matcher m = ETHERNET_SUBINTERFACE.matcher(name);
    return m.matches() ? m.group(1) : null;
  }

  private static final Pattern USB_SERIAL = Pattern.compile("^USB-Serial(\\d+)\\..*$");

  /**
   * The device ({@code GigaEthernet2} for {@code GigaEthernet2:7.0}) that the interface belongs
   * to, i.e. what {@code device <name>} configures.
   */
  public static @Nonnull String computeDevice(String name) {
    // The modem interface USB-Serial<n>.<m> runs on the USB device USB<n>.
    Matcher usb = USB_SERIAL.matcher(name);
    if (usb.matches()) {
      return "USB" + usb.group(1);
    }
    int end = name.length();
    int colon = name.indexOf(':');
    int dot = name.indexOf('.');
    if (colon >= 0) {
      end = colon;
    } else if (dot >= 0) {
      end = dot;
    }
    return name.substring(0, end);
  }

  /** {@code ip napt inside list <acl> [outside <address>]}. */
  public static final class NaptInside implements Serializable {
    private final @Nonnull String _accessList;
    private final @Nullable Ip _outsideAddress;

    public NaptInside(String accessList, @Nullable Ip outsideAddress) {
      _accessList = accessList;
      _outsideAddress = outsideAddress;
    }

    public @Nonnull String getAccessList() {
      return _accessList;
    }

    public @Nullable Ip getOutsideAddress() {
      return _outsideAddress;
    }
  }

  /** {@code ip nat static <inside> <outside>}: a one-to-one mapping of equal-length prefixes. */
  public static final class StaticNat implements Serializable {
    private final @Nonnull Prefix _inside;
    private final @Nonnull Prefix _outside;

    public StaticNat(Prefix inside, Prefix outside) {
      _inside = inside;
      _outside = outside;
    }

    public @Nonnull Prefix getInside() {
      return _inside;
    }

    public @Nonnull Prefix getOutside() {
      return _outside;
    }
  }

  private final @Nonnull String _name;
  private final @Nonnull Type _type;
  private @Nullable String _description;
  private @Nullable ConcreteInterfaceAddress _address;
  private final @Nonnull List<ConcreteInterfaceAddress> _secondaryAddresses;
  private @Nullable Integer _mtu;
  private @Nullable Boolean _shutdown;
  private @Nullable Integer _vlan;
  private @Nullable String _vrf;
  private @Nullable Integer _bridgeGroup;
  private final @Nonnull SortedMap<Integer, String> _inboundFilters;
  private final @Nonnull SortedMap<Integer, String> _outboundFilters;
  private boolean _naptEnabled;
  private @Nullable Ip _naptAddress;
  private @Nullable String _naptAddressInterface;
  private final @Nonnull List<NaptInside> _naptInsides;
  private @Nullable List<SubRange> _naptPortRanges;
  private boolean _natEnabled;
  private final @Nonnull List<StaticNat> _staticNats;

  public NecIxInterface(String name) {
    this(name, computeType(name));
  }

  public NecIxInterface(String name, Type type) {
    _name = name;
    _type = type;
    _secondaryAddresses = new ArrayList<>();
    _inboundFilters = new TreeMap<>();
    _outboundFilters = new TreeMap<>();
    _naptInsides = new ArrayList<>();
    _staticNats = new ArrayList<>();
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull Type getType() {
    return _type;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public void setDescription(@Nullable String description) {
    _description = description;
  }

  /** The primary address. */
  public @Nullable ConcreteInterfaceAddress getAddress() {
    return _address;
  }

  public void setAddress(@Nullable ConcreteInterfaceAddress address) {
    _address = address;
  }

  public @Nonnull List<ConcreteInterfaceAddress> getSecondaryAddresses() {
    return _secondaryAddresses;
  }

  /** The IPv4 MTU from {@code ip mtu}. */
  public @Nullable Integer getMtu() {
    return _mtu;
  }

  public void setMtu(@Nullable Integer mtu) {
    _mtu = mtu;
  }

  /** {@code true} for {@code shutdown}, {@code false} for {@code no shutdown}, else unset. */
  public @Nullable Boolean getShutdown() {
    return _shutdown;
  }

  public void setShutdown(@Nullable Boolean shutdown) {
    _shutdown = shutdown;
  }

  /** The VLAN from {@code encapsulation dot1q}. */
  public @Nullable Integer getVlan() {
    return _vlan;
  }

  public void setVlan(@Nullable Integer vlan) {
    _vlan = vlan;
  }

  /** The VRF from {@code ip vrf forwarding}, or {@code null} for the default VRF. */
  public @Nullable String getVrf() {
    return _vrf;
  }

  public void setVrf(@Nullable String vrf) {
    _vrf = vrf;
  }

  public @Nullable Integer getBridgeGroup() {
    return _bridgeGroup;
  }

  public void setBridgeGroup(@Nullable Integer bridgeGroup) {
    _bridgeGroup = bridgeGroup;
  }

  /** {@code ip filter <acl> <seq> in}: access-list names keyed by filter sequence number. */
  public @Nonnull SortedMap<Integer, String> getInboundFilters() {
    return _inboundFilters;
  }

  /** {@code ip filter <acl> <seq> out}: access-list names keyed by filter sequence number. */
  public @Nonnull SortedMap<Integer, String> getOutboundFilters() {
    return _outboundFilters;
  }

  public boolean getNaptEnabled() {
    return _naptEnabled;
  }

  public void setNaptEnabled(boolean naptEnabled) {
    _naptEnabled = naptEnabled;
  }

  /** The NAPT address from {@code ip napt address <address>}. */
  public @Nullable Ip getNaptAddress() {
    return _naptAddress;
  }

  public void setNaptAddress(@Nullable Ip naptAddress) {
    _naptAddress = naptAddress;
  }

  /** The interface from {@code ip napt address <interface>}, whose address NAPT uses. */
  public @Nullable String getNaptAddressInterface() {
    return _naptAddressInterface;
  }

  public void setNaptAddressInterface(@Nullable String naptAddressInterface) {
    _naptAddressInterface = naptAddressInterface;
  }

  public @Nonnull List<NaptInside> getNaptInsides() {
    return _naptInsides;
  }

  /** The ports from {@code ip napt translation port-range}, or {@code null} for the default. */
  public @Nullable List<SubRange> getNaptPortRanges() {
    return _naptPortRanges;
  }

  public void setNaptPortRanges(@Nullable List<SubRange> naptPortRanges) {
    _naptPortRanges = naptPortRanges;
  }

  public boolean getNatEnabled() {
    return _natEnabled;
  }

  public void setNatEnabled(boolean natEnabled) {
    _natEnabled = natEnabled;
  }

  public @Nonnull List<StaticNat> getStaticNats() {
    return _staticNats;
  }
}
