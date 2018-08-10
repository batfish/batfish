package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.common.util.ComparableStructure;

public class IpsecProposal extends ComparableStructure<String> {

  public static final IpsecProposal G2_ESP_3DES_SHA = initG2_ESP_3DES_SHA();

  public static final IpsecProposal G2_ESP_AES128_SHA = initG2_ESP_AES128_SHA();

  public static final IpsecProposal NOPFS_ESP_3DES_MD5 = initNOPFS_ESP_3DES_MD5();

  public static final IpsecProposal NOPFS_ESP_3DES_SHA = initNOPFS_ESP_3DES_SHA();

  public static final IpsecProposal NOPFS_ESP_DES_MD5 = initNOPFS_ESP_DES_MD5();

  public static final IpsecProposal NOPFS_ESP_DES_SHA = initNOPFS_ESP_DES_SHA();

  /** */
  private static final long serialVersionUID = 1L;

  private static IpsecProposal initG2_ESP_3DES_SHA() {
    IpsecProposal p = new IpsecProposal("G2_ESP_3DES_SHA");
    p.getProtocols().add(IpsecProtocol.ESP);
    p.setAuthenticationAlgorithm(IpsecAuthenticationAlgorithm.HMAC_SHA1_96);
    p.setEncryptionAlgorithm(EncryptionAlgorithm.THREEDES_CBC);
    return p;
  }

  private static IpsecProposal initG2_ESP_AES128_SHA() {
    IpsecProposal p = new IpsecProposal("G2_ESP_AES128_SHA");
    p.getProtocols().add(IpsecProtocol.ESP);
    p.setAuthenticationAlgorithm(IpsecAuthenticationAlgorithm.HMAC_SHA1_96);
    p.setEncryptionAlgorithm(EncryptionAlgorithm.AES_128_CBC);
    return p;
  }

  private static IpsecProposal initNOPFS_ESP_3DES_MD5() {
    IpsecProposal p = new IpsecProposal("NOPFS_ESP_3DES_MD5");
    p.getProtocols().add(IpsecProtocol.ESP);
    p.setAuthenticationAlgorithm(IpsecAuthenticationAlgorithm.HMAC_MD5_96);
    p.setEncryptionAlgorithm(EncryptionAlgorithm.THREEDES_CBC);
    return p;
  }

  private static IpsecProposal initNOPFS_ESP_3DES_SHA() {
    IpsecProposal p = new IpsecProposal("NOPFS_ESP_3DES_SHA");
    p.getProtocols().add(IpsecProtocol.ESP);
    p.setAuthenticationAlgorithm(IpsecAuthenticationAlgorithm.HMAC_SHA1_96);
    p.setEncryptionAlgorithm(EncryptionAlgorithm.THREEDES_CBC);
    return p;
  }

  private static IpsecProposal initNOPFS_ESP_DES_MD5() {
    IpsecProposal p = new IpsecProposal("NOPFS_ESP_DES_MD5");
    p.getProtocols().add(IpsecProtocol.ESP);
    p.setAuthenticationAlgorithm(IpsecAuthenticationAlgorithm.HMAC_MD5_96);
    p.setEncryptionAlgorithm(EncryptionAlgorithm.DES_CBC);
    return p;
  }

  private static IpsecProposal initNOPFS_ESP_DES_SHA() {
    IpsecProposal p = new IpsecProposal("NOPFS_ESP_DES_SHA");
    p.getProtocols().add(IpsecProtocol.ESP);
    p.setAuthenticationAlgorithm(IpsecAuthenticationAlgorithm.HMAC_SHA1_96);
    p.setEncryptionAlgorithm(EncryptionAlgorithm.DES_CBC);
    return p;
  }

  private IpsecAuthenticationAlgorithm _authenticationAlgorithm;

  private EncryptionAlgorithm _encryptionAlgorithm;

  private Integer _lifetimeKilobytes;

  private Integer _lifetimeSeconds;

  private SortedSet<IpsecProtocol> _protocols;

  @JsonCreator
  public IpsecProposal(@JsonProperty(PROP_NAME) String name) {
    super(name);
    _protocols = new TreeSet<>();
  }

  public boolean compatibleWith(IpsecProposal rhs) {
    return (_authenticationAlgorithm == rhs._authenticationAlgorithm
        && _encryptionAlgorithm == rhs._encryptionAlgorithm
        && _protocols.equals(rhs._protocols));
  }

  public IpsecAuthenticationAlgorithm getAuthenticationAlgorithm() {
    return _authenticationAlgorithm;
  }

  public EncryptionAlgorithm getEncryptionAlgorithm() {
    return _encryptionAlgorithm;
  }

  public Integer getLifetimeKilobytes() {
    return _lifetimeKilobytes;
  }

  public Integer getLifetimeSeconds() {
    return _lifetimeSeconds;
  }

  public SortedSet<IpsecProtocol> getProtocols() {
    return _protocols;
  }

  public void setAuthenticationAlgorithm(IpsecAuthenticationAlgorithm alg) {
    _authenticationAlgorithm = alg;
  }

  public void setEncryptionAlgorithm(EncryptionAlgorithm alg) {
    _encryptionAlgorithm = alg;
  }

  public void setLifetimeKilobytes(int lifetimeKilobytes) {
    _lifetimeKilobytes = lifetimeKilobytes;
  }

  public void setLifetimeSeconds(int lifetimeSeconds) {
    _lifetimeSeconds = lifetimeSeconds;
  }

  /** @deprecated Use {@link #setProtocols(SortedSet)} instead. */
  @Deprecated
  public void setProtocol(IpsecProtocol protocol) {
    setProtocols(ImmutableSortedSet.of(protocol));
  }

  public void setProtocols(SortedSet<IpsecProtocol> protocols) {
    _protocols = protocols;
  }
}
