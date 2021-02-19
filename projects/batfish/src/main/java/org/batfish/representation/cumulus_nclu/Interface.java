package org.batfish.representation.cumulus_nclu;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.ConcreteInterfaceAddress;

/** A physical or logical interface */
@ParametersAreNonnullByDefault
public class Interface implements Serializable {

  static final Pattern NULL_INTERFACE_PATTERN =
      Pattern.compile("Null0|blackhole|reject", Pattern.CASE_INSENSITIVE);

  private @Nullable String _alias;
  private @Nullable InterfaceBridgeSettings _bridge;
  private @Nullable InterfaceClagSettings _clag;
  private boolean _disabled;
  private final @Nullable Integer _encapsulationVlan;
  private final @Nonnull List<ConcreteInterfaceAddress> _ipAddresses;
  private final @Nonnull String _name;
  private @Nullable Integer _speed;
  private final @Nullable String _superInterfaceName;
  private final @Nonnull CumulusInterfaceType _type;
  private @Nullable String _vrf;
  private @Nonnull List<StaticRoute> _postUpIpRoutes;

  private @Nullable OspfInterface _ospf;

  /**
   * Construct an Interface
   *
   * @param name Name of the interface
   * @param type Type of the interface
   * @param superInterfaceName Name of the super interface if this is a subinterface, or else {@code
   *     null}
   * @param encapsulationVlan Encapsulation VLAN number for this interface if this is a
   *     subinterface, or else {@code null}
   */
  public Interface(
      String name,
      CumulusInterfaceType type,
      @Nullable String superInterfaceName,
      @Nullable Integer encapsulationVlan) {
    _name = name;
    _ipAddresses = new LinkedList<>();
    _type = type;
    _superInterfaceName = superInterfaceName;
    _encapsulationVlan = encapsulationVlan;
    _postUpIpRoutes = ImmutableList.of();
  }

  /** Interface alias (description) */
  @Nullable
  public String getAlias() {
    return _alias;
  }

  public @Nonnull InterfaceBridgeSettings getBridge() {
    if (_bridge == null) {
      _bridge = new InterfaceBridgeSettings();
    }
    return _bridge;
  }

  public @Nullable InterfaceClagSettings getClag() {
    return _clag;
  }

  public boolean isDisabled() {
    return _disabled;
  }

  public void setDisabled(boolean disabled) {
    _disabled = disabled;
  }

  public @Nullable Integer getEncapsulationVlan() {
    return _encapsulationVlan;
  }

  public @Nonnull List<ConcreteInterfaceAddress> getIpAddresses() {
    return _ipAddresses;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull InterfaceClagSettings getOrInitClag() {
    if (_clag == null) {
      _clag = new InterfaceClagSettings();
    }
    return _clag;
  }

  public void setAlias(@Nullable String alias) {
    _alias = alias;
  }

  /** Speed in Mbps */
  @Nullable
  public Integer getSpeed() {
    return _speed;
  }

  /** Set speed (assumed to be in Mbps) */
  public void setSpeed(@Nullable Integer speed) {
    _speed = speed;
  }

  public @Nullable String getSuperInterfaceName() {
    return _superInterfaceName;
  }

  public @Nonnull CumulusInterfaceType getType() {
    return _type;
  }

  public @Nullable String getVrf() {
    return _vrf;
  }

  public void setBridgeSettings(@Nullable InterfaceBridgeSettings bridgeSettings) {
    _bridge = bridgeSettings;
  }

  public void setClagSettings(@Nullable InterfaceClagSettings clagSettings) {
    _clag = clagSettings;
  }

  public void setVrf(@Nullable String vrf) {
    _vrf = vrf;
  }

  @Nullable
  public OspfInterface getOspf() {
    return _ospf;
  }

  public @Nonnull OspfInterface getOrCreateOspf() {
    if (_ospf == null) {
      _ospf = new OspfInterface();
    }
    return _ospf;
  }

  public void setOspf(@Nullable OspfInterface ospf) {
    _ospf = ospf;
  }

  @Nonnull
  public List<StaticRoute> getPostUpIpRoutes() {
    return _postUpIpRoutes;
  }

  public void addPostUpIpRoute(StaticRoute sr) {
    _postUpIpRoutes = ImmutableList.<StaticRoute>builder().addAll(_postUpIpRoutes).add(sr).build();
  }
}
