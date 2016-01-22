package org.batfish.representation.vyos;

import java.io.Serializable;

import org.batfish.collections.RoleSet;

public class VyosConfiguration implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   protected String _hostname;

   protected final RoleSet _roles;

   public VyosConfiguration() {
      _roles = new RoleSet();
   }

   public String getHostname() {
      return _hostname;
   }

   public RoleSet getRoles() {
      return _roles;
   }

   public void setHostname(String hostname) {
      _hostname = hostname;
   }

   public void setRoles(RoleSet roles) {
      _roles.addAll(roles);
   }

}
