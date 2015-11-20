package org.batfish.representation;

import org.batfish.representation.DiffieHellmanGroup;
import org.batfish.representation.EncryptionAlgorithm;
import org.batfish.representation.IkeAuthenticationAlgorithm;
import org.batfish.representation.IkeAuthenticationMethod;
import org.batfish.util.NamedStructure;

public final class IkeProposal extends NamedStructure {

   public static final IkeProposal PSK_3DES_DH2_MD5 = initPSK_3DES_DH2_MD5();

   public static final IkeProposal PSK_3DES_DH2_SHA1 = initPSK_3DES_DH2_SHA1();

   public static final IkeProposal PSK_AES128_DH2_SHA1 = initPSK_AES128_DH2_SHA1();

   public static final IkeProposal PSK_DES_DH1_MD5 = initPSK_DES_DH1_MD5();

   public static final IkeProposal PSK_DES_DH1_SHA1 = initPSK_DES_DH1_SHA1();

   public static final IkeProposal PSK_DES_DH2_MD5 = initPSK_DES_DH2_MD5();

   public static final IkeProposal PSK_DES_DH2_SHA1 = initPSK_DES_DH2_SHA1();

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private static IkeProposal initPSK_3DES_DH2_MD5() {
      IkeProposal p = new IkeProposal("PSK_3DES_DH2_MD5");
      p.setAuthenticationMethod(IkeAuthenticationMethod.PRE_SHARED_KEYS);
      p.setEncryptionAlgorithm(EncryptionAlgorithm.THREEDES_CBC);
      p.setDiffieHellmanGroup(DiffieHellmanGroup.GROUP2);
      p.setAuthenticationAlgorithm(IkeAuthenticationAlgorithm.MD5);
      return p;
   }

   private static IkeProposal initPSK_3DES_DH2_SHA1() {
      IkeProposal p = new IkeProposal("PSK_3DES_DH2_SHA1");
      p.setAuthenticationMethod(IkeAuthenticationMethod.PRE_SHARED_KEYS);
      p.setEncryptionAlgorithm(EncryptionAlgorithm.THREEDES_CBC);
      p.setDiffieHellmanGroup(DiffieHellmanGroup.GROUP2);
      p.setAuthenticationAlgorithm(IkeAuthenticationAlgorithm.SHA1);
      return p;
   }

   private static IkeProposal initPSK_AES128_DH2_SHA1() {
      IkeProposal p = new IkeProposal("PSK_AES128_DH2_SHA1");
      p.setAuthenticationMethod(IkeAuthenticationMethod.PRE_SHARED_KEYS);
      p.setEncryptionAlgorithm(EncryptionAlgorithm.AES_128_CBC);
      p.setDiffieHellmanGroup(DiffieHellmanGroup.GROUP2);
      p.setAuthenticationAlgorithm(IkeAuthenticationAlgorithm.SHA1);
      return p;
   }

   private static IkeProposal initPSK_DES_DH1_MD5() {
      IkeProposal p = new IkeProposal("PSK_DES_DH1_MD5");
      p.setAuthenticationMethod(IkeAuthenticationMethod.PRE_SHARED_KEYS);
      p.setEncryptionAlgorithm(EncryptionAlgorithm.DES_CBC);
      p.setDiffieHellmanGroup(DiffieHellmanGroup.GROUP1);
      p.setAuthenticationAlgorithm(IkeAuthenticationAlgorithm.MD5);
      return p;
   }

   private static IkeProposal initPSK_DES_DH1_SHA1() {
      IkeProposal p = new IkeProposal("PSK_DES_DH1_SHA1");
      p.setAuthenticationMethod(IkeAuthenticationMethod.PRE_SHARED_KEYS);
      p.setEncryptionAlgorithm(EncryptionAlgorithm.DES_CBC);
      p.setDiffieHellmanGroup(DiffieHellmanGroup.GROUP1);
      p.setAuthenticationAlgorithm(IkeAuthenticationAlgorithm.SHA1);
      return p;
   }

   private static IkeProposal initPSK_DES_DH2_MD5() {
      IkeProposal p = new IkeProposal("PSK_DES_DH2_MD5");
      p.setAuthenticationMethod(IkeAuthenticationMethod.PRE_SHARED_KEYS);
      p.setEncryptionAlgorithm(EncryptionAlgorithm.DES_CBC);
      p.setDiffieHellmanGroup(DiffieHellmanGroup.GROUP2);
      p.setAuthenticationAlgorithm(IkeAuthenticationAlgorithm.MD5);
      return p;
   }

   private static IkeProposal initPSK_DES_DH2_SHA1() {
      IkeProposal p = new IkeProposal("PSK_DES_DH2_SHA1");
      p.setAuthenticationMethod(IkeAuthenticationMethod.PRE_SHARED_KEYS);
      p.setEncryptionAlgorithm(EncryptionAlgorithm.DES_CBC);
      p.setDiffieHellmanGroup(DiffieHellmanGroup.GROUP2);
      p.setAuthenticationAlgorithm(IkeAuthenticationAlgorithm.SHA1);
      return p;
   }

   private IkeAuthenticationAlgorithm _authenticationAlgorithm;

   private IkeAuthenticationMethod _authenticationMethod;

   private DiffieHellmanGroup _diffieHellmanGroup;

   private EncryptionAlgorithm _encryptionAlgorithm;

   private Integer _lifetimeSeconds;

   public IkeProposal(String name) {
      super(name);
   }

   public boolean compatibleWith(IkeProposal rhs) {
      return _authenticationAlgorithm == rhs._authenticationAlgorithm
            && _authenticationMethod == rhs._authenticationMethod
            && _diffieHellmanGroup == rhs._diffieHellmanGroup
            && _encryptionAlgorithm == rhs._encryptionAlgorithm;
   }

   public IkeAuthenticationAlgorithm getAuthenticationAlgorithm() {
      return _authenticationAlgorithm;
   }

   public IkeAuthenticationMethod getAuthenticationMethod() {
      return _authenticationMethod;
   }

   public DiffieHellmanGroup getDiffieHellmanGroup() {
      return _diffieHellmanGroup;
   }

   public EncryptionAlgorithm getEncryptionAlgorithm() {
      return _encryptionAlgorithm;
   }

   public Integer getLifetimeSeconds() {
      return _lifetimeSeconds;
   }

   public void setAuthenticationAlgorithm(
         IkeAuthenticationAlgorithm authenticationAlgorithm) {
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

   public void setLifetimeSeconds(int lifetimeSeconds) {
      _lifetimeSeconds = lifetimeSeconds;
   }

}
