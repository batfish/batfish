package org.batfish.representation.cumulus;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.ConcreteInterfaceAddress;

/** Represents an interface defined/extended in FRR file */
@ParametersAreNonnullByDefault
public class FrrInterface implements Serializable {

  private @Nullable String _alias;
  private final @Nonnull List<ConcreteInterfaceAddress> _ipAddresses;
  private final @Nonnull String _name;
  private final @Nullable String _vrfName;
  private boolean _shutdown = false;

  private @Nullable OspfInterface _ospf;

  public FrrInterface(String name) {
    this(name, null);
  }

  public FrrInterface(String name, @Nullable String vrfName) {
    _name = name;
    _vrfName = vrfName;
    _ipAddresses = new LinkedList<>();
  }

  /** Interface alias (description) */
  @Nullable
  public String getAlias() {
    return _alias;
  }

  public @Nonnull List<ConcreteInterfaceAddress> getIpAddresses() {
    return _ipAddresses;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public void setAlias(@Nullable String alias) {
    _alias = alias;
  }

  public @Nullable String getVrfName() {
    return _vrfName;
  }

  public boolean getShutdown() {
    return _shutdown;
  }

  public void setShutdown(@Nonnull Boolean shutdown) {
    _shutdown = shutdown;
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
}
