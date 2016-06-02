package org.batfish.datamodel;

import org.batfish.common.BatfishException;
import org.batfish.common.Pair;
import org.batfish.common.util.CommonUtil;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IpWildcard extends Pair<Ip, Ip> {

   public static final IpWildcard ANY = new IpWildcard(Ip.ZERO, Ip.MAX);

   private static final String IP_VAR = "ip";
   private static final String WILDCARD_VAR = "wildcard";

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   @JsonCreator
   public IpWildcard(@JsonProperty(IP_VAR) Ip address,
         @JsonProperty(WILDCARD_VAR) Ip wildcardMask) {
      super(address, wildcardMask);
      if (!wildcardMask.valid()) {
         throw new BatfishException("Invalid wildcard: "
               + wildcardMask.toString());
      }
   }

   public IpWildcard(Prefix prefix) {
      this(prefix.getAddress(), prefix.getPrefixWildcard());
   }

   @JsonProperty(IP_VAR)
   public Ip getIp() {
      return _first;
   }

   @JsonProperty(WILDCARD_VAR)
   public Ip getWildcard() {
      return _second;
   }

   @JsonIgnore
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
