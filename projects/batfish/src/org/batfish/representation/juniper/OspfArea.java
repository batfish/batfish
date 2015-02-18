package org.batfish.representation.juniper;

import java.io.Serializable;

import org.batfish.representation.Ip;

public class OspfArea implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private Ip _areaIp;

   public OspfArea(Ip areaIp) {
      _areaIp = areaIp;
   }

   public Ip getAreaIp() {
      return _areaIp;
   }

}
