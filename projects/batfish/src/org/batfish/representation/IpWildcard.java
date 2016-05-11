package org.batfish.representation;

import org.batfish.common.BatfishException;
import org.batfish.common.Pair;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

public class IpWildcard extends Pair<Ip, Ip> {

   public static final IpWildcard ANY = new IpWildcard(Ip.ZERO, Ip.MAX);

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public IpWildcard(Ip t1, Ip t2) {
      super(t1, t2);
   }

   public IpWildcard(Prefix prefix) {
      this(prefix.getAddress(), prefix.getPrefixWildcard());
   }

   public Ip getIp() {
      return _first;
   }

   public Ip getWildcard() {
      return _second;
   }

   public boolean isPrefix() {
      return CommonUtil.isValidWildcard(_second);
   }

   public Prefix toPrefix() {
      if (isPrefix()) {
         return new Prefix(_first, _second.inverted());
      }
      else {
         throw new BatfishException(
               "Invalid wildcard format for conversion to prefix: " + _second);
      }
   }

}
