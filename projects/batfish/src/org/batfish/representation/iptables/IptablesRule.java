package org.batfish.representation.iptables;

import java.io.Serializable;

public class IptablesRule implements Serializable {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   List<IptablesMatch> _matchList;   
   IptablesAction _action;
   
   
}
