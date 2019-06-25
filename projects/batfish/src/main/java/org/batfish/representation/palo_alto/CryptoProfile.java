package org.batfish.representation.palo_alto;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.IkeHashingAlgorithm;
import org.batfish.datamodel.IpsecAuthenticationAlgorithm;

/** Represents a crypto profile for Palo Alto */
public final class CryptoProfile implements Serializable {

  public enum Type {
    GLOBAL_PROTECT_APP,
    IKE,
    IPSEC,
  }

  /** null implies no authentiation */
  @Nullable private IpsecAuthenticationAlgorithm _authAlgorithm;

  private DiffieHellmanGroup _dhGroup;

  private List<EncryptionAlgorithm> _encryptionAlgorithms;

  private IkeHashingAlgorithm _hashAlgorithm;

  private Integer _lifetimeSeconds;

  @Nonnull private final String _name;

  @Nonnull private final Type _type;

  public CryptoProfile(String name, Type cpType) {
    this(name, cpType, null, null, null, null, null);
  }

  public CryptoProfile(
      String name,
      Type cpType,
      IpsecAuthenticationAlgorithm authAlgorithm,
      DiffieHellmanGroup dhGroup,
      List<EncryptionAlgorithm> encryptionAlgorithms,
      IkeHashingAlgorithm hashAlgorithm,
      Integer lifetimeSeconds) {
    _name = name;
    _type = cpType;
    _authAlgorithm = authAlgorithm;
    _encryptionAlgorithms = encryptionAlgorithms;
    _dhGroup = dhGroup;
    _hashAlgorithm = hashAlgorithm;
    _lifetimeSeconds = lifetimeSeconds;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof CryptoProfile)) {
      return false;
    }
    CryptoProfile rhs = (CryptoProfile) o;
    return Objects.equals(_name, rhs._name)
        && Objects.equals(_type, rhs._type)
        && Objects.equals(_authAlgorithm, rhs._authAlgorithm)
        && Objects.equals(_dhGroup, rhs._dhGroup)
        && Objects.equals(_encryptionAlgorithms, rhs._encryptionAlgorithms)
        && Objects.equals(_hashAlgorithm, rhs._hashAlgorithm)
        && Objects.equals(_lifetimeSeconds, rhs._lifetimeSeconds);
  }

  @Nullable
  public IpsecAuthenticationAlgorithm getAuthAlgorithm() {
    return _authAlgorithm;
  }

  public DiffieHellmanGroup getDhGroup() {
    return _dhGroup;
  }

  public List<EncryptionAlgorithm> getEncryptionAlgorithms() {
    return _encryptionAlgorithms;
  }

  public IkeHashingAlgorithm getHashAlgorithm() {
    return _hashAlgorithm;
  }

  public Integer getLifetimeSeconds() {
    return _lifetimeSeconds;
  }

  public String getName() {
    return _name;
  }

  public Type getType() {
    return _type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _name,
        _type,
        _authAlgorithm,
        _dhGroup,
        _encryptionAlgorithms,
        _hashAlgorithm,
        _lifetimeSeconds);
  }

  public void setAuthAlgorithm(@Nullable IpsecAuthenticationAlgorithm authAlgorithm) {
    _authAlgorithm = authAlgorithm;
  }

  public void setDhGroup(DiffieHellmanGroup dhGroup) {
    _dhGroup = dhGroup;
  }

  public void setEncryptionAlgorithms(List<EncryptionAlgorithm> encryptionAlgorithms) {
    _encryptionAlgorithms = encryptionAlgorithms;
  }

  public void setHashAlgorithm(IkeHashingAlgorithm hashAlgorithm) {
    _hashAlgorithm = hashAlgorithm;
  }

  public void setLifetimeSeconds(Integer lifetimeSeconds) {
    _lifetimeSeconds = lifetimeSeconds;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(CryptoProfile.class)
        .omitNullValues()
        .add("name", _name)
        .add("type", _type)
        .add("auth-alog", _authAlgorithm)
        .add("dh-group", _dhGroup)
        .add("encrypt-algos", _encryptionAlgorithms)
        .add("hash-algo", _hashAlgorithm)
        .add("life", _lifetimeSeconds)
        .toString();
  }
}
