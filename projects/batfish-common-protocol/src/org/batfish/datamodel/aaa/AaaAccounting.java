package org.batfish.datamodel.aaa;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;

public class AaaAccounting implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private SortedMap<Integer, AaaAccountingCommands> _commands;

   private AaaAccountingCommands _defaultCommands;

   public AaaAccounting() {
      _commands = new TreeMap<>();
   }

   public SortedMap<Integer, AaaAccountingCommands> getCommands() {
      return _commands;
   }

   public AaaAccountingCommands getDefaultCommands() {
      return _defaultCommands;
   }

   public void setCommands(SortedMap<Integer, AaaAccountingCommands> commands) {
      _commands = commands;
   }

   public void setDefaultCommands(AaaAccountingCommands defaultCommands) {
      _defaultCommands = defaultCommands;
   }

}
