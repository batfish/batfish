package org.batfish.datamodel.vendor_family.cumulus;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.MacAddress;

/** CLAG settings for a layer-3 interface */
public final class InterfaceClagSettings implements Serializable {

  public static final class Builder {

    private @Nullable Ip _backupIp;
    private @Nullable String _backupIpVrf;
    private @Nullable Ip _peerIp;
    private boolean _peerIpLinkLocal;
    private @Nullable Integer _priority;
    private @Nullable MacAddress _sysMac;

    private Builder() {}

    public @Nonnull InterfaceClagSettings build() {
      return new InterfaceClagSettings(
          _backupIp, _backupIpVrf, _peerIp, _peerIpLinkLocal, _priority, _sysMac);
    }

    public @Nonnull Builder setBackupIp(@Nullable Ip backupIp) {
      _backupIp = backupIp;
      return this;
    }

    public @Nonnull Builder setBackupIpVrf(@Nullable String backupIpVrf) {
      _backupIpVrf = backupIpVrf;
      return this;
    }

    public @Nonnull Builder setPeerIp(@Nullable Ip peerIp) {
      _peerIp = peerIp;
      return this;
    }

    public @Nonnull Builder setPeerLinkLocal(boolean peerIpLinkLocal) {
      _peerIpLinkLocal = peerIpLinkLocal;
      return this;
    }

    public @Nonnull Builder setPriority(@Nullable Integer priority) {
      _priority = priority;
      return this;
    }

    public @Nonnull Builder setSysMac(@Nullable MacAddress sysMac) {
      _sysMac = sysMac;
      return this;
    }
  }

  private static final String PROP_BACKUP_IP = "backupIp";
  private static final String PROP_BACKUP_IP_VRF = "backupIpVrf";
  private static final String PROP_PEER_IP = "peerIp";
  private static final String PROP_PEER_IP_LINK_LOCAL = "peerIpLinkLocal";
  private static final String PROP_PRIORITY = "priority";
  private static final String PROP_SYS_MAC = "sysMac";

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  private final @Nullable Ip _backupIp;
  private final @Nullable String _backupIpVrf;
  private final @Nullable Ip _peerIp;
  private final boolean _peerIpLinkLocal;
  private final @Nullable Integer _priority;
  private final @Nullable MacAddress _sysMac;

  // no need for static @JsonCreator since all fields are @Nullable.
  @JsonCreator
  private InterfaceClagSettings(
      @JsonProperty(PROP_BACKUP_IP) @Nullable Ip backupIp,
      @JsonProperty(PROP_BACKUP_IP_VRF) @Nullable String backupIpVrf,
      @JsonProperty(PROP_PEER_IP) @Nullable Ip peerIp,
      @JsonProperty(PROP_PEER_IP_LINK_LOCAL) boolean peerIpLinkLocal,
      @JsonProperty(PROP_PRIORITY) @Nullable Integer priority,
      @JsonProperty(PROP_SYS_MAC) @Nullable MacAddress sysMac) {
    _backupIp = backupIp;
    _backupIpVrf = backupIpVrf;
    _peerIp = peerIp;
    _peerIpLinkLocal = peerIpLinkLocal;
    _priority = priority;
    _sysMac = sysMac;
  }

  @JsonProperty(PROP_BACKUP_IP)
  public @Nullable Ip getBackupIp() {
    return _backupIp;
  }

  @JsonProperty(PROP_BACKUP_IP_VRF)
  public @Nullable String getBackupIpVrf() {
    return _backupIpVrf;
  }

  @JsonProperty(PROP_PEER_IP)
  public @Nullable Ip getPeerIp() {
    return _peerIp;
  }

  @JsonProperty(PROP_PEER_IP_LINK_LOCAL)
  public boolean isPeerIpLinkLocal() {
    return _peerIpLinkLocal;
  }

  @JsonProperty(PROP_PRIORITY)
  public @Nullable Integer getPriority() {
    return _priority;
  }

  @JsonProperty(PROP_SYS_MAC)
  public @Nullable MacAddress getSysMac() {
    return _sysMac;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof InterfaceClagSettings)) {
      return false;
    }
    InterfaceClagSettings rhs = (InterfaceClagSettings) obj;
    return Objects.equals(_backupIp, rhs._backupIp)
        && Objects.equals(_backupIpVrf, rhs._backupIpVrf)
        && Objects.equals(_peerIp, rhs._peerIp)
        && _peerIpLinkLocal == rhs._peerIpLinkLocal
        && Objects.equals(_priority, rhs._priority)
        && Objects.equals(_sysMac, rhs._sysMac);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_backupIp, _backupIpVrf, _peerIp, _peerIpLinkLocal, _priority, _sysMac);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .omitNullValues()
        .add(PROP_BACKUP_IP, _backupIp)
        .add(PROP_BACKUP_IP_VRF, _backupIpVrf)
        .add(PROP_PEER_IP, _peerIp)
        .add(PROP_PEER_IP_LINK_LOCAL, _peerIp)
        .add(PROP_PRIORITY, _priority)
        .add(PROP_SYS_MAC, _sysMac)
        .toString();
  }
}
