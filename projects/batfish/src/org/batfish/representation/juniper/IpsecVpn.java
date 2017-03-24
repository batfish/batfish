package org.batfish.representation.juniper;

import org.batfish.common.util.ComparableStructure;

public final class IpsecVpn extends ComparableStructure<String> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private Interface _bindInterface;

   private int _bindInterfaceLine;

   private String _gateway;

   private int _gatewayLine;

   private String _ipsecPolicy;

   private int _ipsecPolicyLine;

   public IpsecVpn(String name) {
      super(name);
   }

   public Interface getBindInterface() {
      return _bindInterface;
   }

   public int getBindInterfaceLine() {
      return _bindInterfaceLine;
   }

   public String getGateway() {
      return _gateway;
   }

   public int getGatewayLine() {
      return _gatewayLine;
   }

   public String getIpsecPolicy() {
      return _ipsecPolicy;
   }

   public int getIpsecPolicyLine() {
      return _ipsecPolicyLine;
   }

   public void setBindInterface(Interface iface) {
      _bindInterface = iface;
   }

   public void setBindInterfaceLine(int bindInterfaceLine) {
      _bindInterfaceLine = bindInterfaceLine;
   }

   public void setGateway(String gateway) {
      _gateway = gateway;
   }

   public void setGatewayLine(int gatewayLine) {
      _gatewayLine = gatewayLine;
   }

   public void setIpsecPolicy(String name) {
      _ipsecPolicy = name;
   }

   public void setIpsecPolicyLine(int ipsecPolicyLine) {
      _ipsecPolicyLine = ipsecPolicyLine;
   }

}
