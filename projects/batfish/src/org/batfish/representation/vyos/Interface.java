package org.batfish.representation.vyos;

import org.batfish.common.BatfishException;
import org.batfish.representation.Prefix;
import org.batfish.util.ComparableStructure;

public class Interface extends ComparableStructure<String> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public static double getDefaultBandwidth(InterfaceType type) {
      // TODO: update with correct values
      switch (type) {
      case ETHERNET:
      case LOOPBACK:
      case VTI:
         return 1E12d;

      case BONDING:
      case BRIDGE:
      case DUMMY:
      case INPUT:
      case L2TPV3:
      case OPENVPN:
      case PSEUDO_ETHERNET:
      case TUNNEL:
      case VXLAN:
      case WIRELESS:
      case WIRELESSMODEM:
      default:
         throw new BatfishException("unsupported interface type");
      }
   }

   private double _bandwidth;

   private String _description;

   private Prefix _prefix;

   public Interface(String name) {
      super(name);
   }

   public double getBandwidth() {
      return _bandwidth;
   }

   public String getDescription() {
      return _description;
   }

   public Prefix getPrefix() {
      return _prefix;
   }

   public void setBandwidth(double bandwidth) {
      _bandwidth = bandwidth;
   }

   public void setDescription(String description) {
      _description = description;
   }

   public void setPrefix(Prefix prefix) {
      _prefix = prefix;
   }

}
