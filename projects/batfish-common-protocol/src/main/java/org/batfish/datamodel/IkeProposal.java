package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.batfish.common.util.ComparableStructure;

public final class IkeProposal extends ComparableStructure<String> {

  public static final IkeProposal PSK_3DES_DH2_MD5 = initPSK_3DES_DH2_MD5();

  public static final IkeProposal PSK_3DES_DH2_SHA1 = initPSK_3DES_DH2_SHA1();

  public static final IkeProposal PSK_AES128_DH2_SHA1 = initPSK_AES128_DH2_SHA1();

  public static final IkeProposal PSK_DES_DH1_MD5 = initPSK_DES_DH1_MD5();

  public static final IkeProposal PSK_DES_DH1_SHA1 = initPSK_DES_DH1_SHA1();

  public static final IkeProposal PSK_DES_DH2_MD5 = initPSK_DES_DH2_MD5();

  public static final IkeProposal PSK_DES_DH2_SHA1 = initPSK_DES_DH2_SHA1();

  /** */
  private static final long serialVersionUID = 1L;

  private static IkeProposal initPSK_3DES_DH2_MD5() {
    IkeProposal p = new IkeProposal("PSK_3DES_DH2_MD5");
    p.setAuthenticationMethod(IkeAuthenticationMethod.PRE_SHARED_KEYS);
    p.setEncryptionAlgorithm(EncryptionAlgorithm.THREEDES_CBC);
    p.setDiffieHellmanGroup(DiffieHellmanGroup.GROUP2);
    p.setAuthenticationAlgorithm(IkeHashingAlgorithm.MD5);
    return p;
  }

  private static IkeProposal initPSK_3DES_DH2_SHA1() {
    IkeProposal p = new IkeProposal("PSK_3DES_DH2_SHA1");
    p.setAuthenticationMethod(IkeAuthenticationMethod.PRE_SHARED_KEYS);
    p.setEncryptionAlgorithm(EncryptionAlgorithm.THREEDES_CBC);
    p.setDiffieHellmanGroup(DiffieHellmanGroup.GROUP2);
    p.setAuthenticationAlgorithm(IkeHashingAlgorithm.SHA1);
    return p;
  }

  private static IkeProposal initPSK_AES128_DH2_SHA1() {
    IkeProposal p = new IkeProposal("PSK_AES128_DH2_SHA1");
    p.setAuthenticationMethod(IkeAuthenticationMethod.PRE_SHARED_KEYS);
    p.setEncryptionAlgorithm(EncryptionAlgorithm.AES_128_CBC);
    p.setDiffieHellmanGroup(DiffieHellmanGroup.GROUP2);
    p.setAuthenticationAlgorithm(IkeHashingAlgorithm.SHA1);
    return p;
  }

  private static IkeProposal initPSK_DES_DH1_MD5() {
    IkeProposal p = new IkeProposal("PSK_DES_DH1_MD5");
    p.setAuthenticationMethod(IkeAuthenticationMethod.PRE_SHARED_KEYS);
    p.setEncryptionAlgorithm(EncryptionAlgorithm.DES_CBC);
    p.setDiffieHellmanGroup(DiffieHellmanGroup.GROUP1);
    p.setAuthenticationAlgorithm(IkeHashingAlgorithm.MD5);
    return p;
  }

  private static IkeProposal initPSK_DES_DH1_SHA1() {
    IkeProposal p = new IkeProposal("PSK_DES_DH1_SHA1");
    p.setAuthenticationMethod(IkeAuthenticationMethod.PRE_SHARED_KEYS);
    p.setEncryptionAlgorithm(EncryptionAlgorithm.DES_CBC);
    p.setDiffieHellmanGroup(DiffieHellmanGroup.GROUP1);
    p.setAuthenticationAlgorithm(IkeHashingAlgorithm.SHA1);
    return p;
  }

  private static IkeProposal initPSK_DES_DH2_MD5() {
    IkeProposal p = new IkeProposal("PSK_DES_DH2_MD5");
    p.setAuthenticationMethod(IkeAuthenticationMethod.PRE_SHARED_KEYS);
    p.setEncryptionAlgorithm(EncryptionAlgorithm.DES_CBC);
    p.setDiffieHellmanGroup(DiffieHellmanGroup.GROUP2);
    p.setAuthenticationAlgorithm(IkeHashingAlgorithm.MD5);
    return p;
  }

  private static IkeProposal initPSK_DES_DH2_SHA1() {
    IkeProposal p = new IkeProposal("PSK_DES_DH2_SHA1");
    p.setAuthenticationMethod(IkeAuthenticationMethod.PRE_SHARED_KEYS);
    p.setEncryptionAlgorithm(EncryptionAlgorithm.DES_CBC);
    p.setDiffieHellmanGroup(DiffieHellmanGroup.GROUP2);
    p.setAuthenticationAlgorithm(IkeHashingAlgorithm.SHA1);
    return p;
  }

  private IkeHashingAlgorithm _authenticationAlgorithm;

  private IkeAuthenticationMethod _authenticationMethod;

  private DiffieHellmanGroup _diffieHellmanGroup;

  private EncryptionAlgorithm _encryptionAlgorithm;

  private Integer _lifetimeSeconds;

  @JsonCreator
  public IkeProposal(@JsonProperty(PROP_NAME) String name) {
    super(name);
  }

  public boolean compatibleWith(IkeProposal rhs) {
    return _authenticationAlgorithm == rhs._authenticationAlgorithm
        && _authenticationMethod == rhs._authenticationMethod
        && _diffieHellmanGroup == rhs._diffieHellmanGroup
        && _encryptionAlgorithm == rhs._encryptionAlgorithm;
  }

  @JsonPropertyDescription("Authentication algorithm to use for connection to IKE gateway")
  public IkeHashingAlgorithm getAuthenticationAlgorithm() {
    return _authenticationAlgorithm;
  }

  @JsonPropertyDescription("Authentication method to use for connection to IKE gateway")
  public IkeAuthenticationMethod getAuthenticationMethod() {
    return _authenticationMethod;
  }

  @JsonPropertyDescription("Diffie-Hellman group to use for key exchange")
  public DiffieHellmanGroup getDiffieHellmanGroup() {
    return _diffieHellmanGroup;
  }

  @JsonPropertyDescription("Encryption algorithm to use for IKE traffic")
  public EncryptionAlgorithm getEncryptionAlgorithm() {
    return _encryptionAlgorithm;
  }

  @JsonPropertyDescription("Lifetime in seconds of connection to IKE gateway")
  public Integer getLifetimeSeconds() {
    return _lifetimeSeconds;
  }

  public void setAuthenticationAlgorithm(IkeHashingAlgorithm authenticationAlgorithm) {
    _authenticationAlgorithm = authenticationAlgorithm;
  }

  public void setAuthenticationMethod(IkeAuthenticationMethod method) {
    _authenticationMethod = method;
  }

  public void setDiffieHellmanGroup(DiffieHellmanGroup diffieHellmanGroup) {
    _diffieHellmanGroup = diffieHellmanGroup;
  }

  public void setEncryptionAlgorithm(EncryptionAlgorithm encryptionAlgorithm) {
    _encryptionAlgorithm = encryptionAlgorithm;
  }

  public void setLifetimeSeconds(Integer lifetimeSeconds) {
    _lifetimeSeconds = lifetimeSeconds;
  }
}
