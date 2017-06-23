package org.batfish.datamodel;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SourceNat implements Serializable {

   private static final String ACL_VAR = "acl";

   private static final String POOL_IP_FIRST_VAR = "poolIpFirst";

   private static final String POOL_IP_LAST_VAR = "poolIpLast";

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private IpAccessList _acl;

   private Ip _poolIpFirst;

   private Ip _poolIpLast;

   @JsonProperty(ACL_VAR)
   public IpAccessList getAcl() {
      return _acl;
   }

   @JsonProperty(POOL_IP_FIRST_VAR)
   public Ip getPoolIpFirst() {
      return _poolIpFirst;
   }

   @JsonProperty(POOL_IP_LAST_VAR)
   public Ip getPoolIpLast() {
      return _poolIpLast;
   }

   @JsonProperty(ACL_VAR)
   public void setAcl(IpAccessList acl) {
      _acl = acl;
   }

   @JsonProperty(POOL_IP_FIRST_VAR)
   public void setPoolIpFirst(Ip poolIpFirst) {
      _poolIpFirst = poolIpFirst;
   }

   @JsonProperty(POOL_IP_LAST_VAR)
   public void setPoolIpLast(Ip poolIpLast) {
      _poolIpLast = poolIpLast;
   }

}
