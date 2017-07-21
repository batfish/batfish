package org.batfish.datamodel.vendor_family.cisco;

import org.batfish.common.util.ComparableStructure;

public class User extends ComparableStructure<String> {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String _password;
   
   private String _role;
   
   public String getRole() {
      return _role;
   }

   public void setRole(String role) {
      _role = role;
   }

   public String getPassword() {
      return _password;
   }

   public void setPassword(String password) {
      _password = password;
   }

   public User(String name) {
      super(name);
   }

}
