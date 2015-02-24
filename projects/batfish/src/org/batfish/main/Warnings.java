package org.batfish.main;

import java.util.ArrayList;
import java.util.List;

public class Warnings {

   private final boolean _pedanticAsError;

   private final boolean _pedanticRecord;

   private final List<String> _pedanticWarnings;

   private final boolean _redFlagAsError;

   private final boolean _redFlagRecord;

   private final List<String> _redFlagWarnings;

   private final boolean _unimplementedAsError;

   private final boolean _unimplementedRecord;

   private final List<String> _unimplementedWarnings;

   public Warnings(boolean pedanticAsError, boolean pedanticRecord,
         boolean redFlagAsError, boolean redFlagRecord,
         boolean unimplementedAsError, boolean unimplementedRecord) {
      _pedanticAsError = pedanticAsError;
      _pedanticWarnings = new ArrayList<String>();
      _pedanticRecord = pedanticRecord;
      _redFlagAsError = redFlagAsError;
      _redFlagRecord = redFlagRecord;
      _redFlagWarnings = new ArrayList<String>();
      _unimplementedAsError = unimplementedAsError;
      _unimplementedRecord = unimplementedRecord;
      _unimplementedWarnings = new ArrayList<String>();
   }

   public List<String> getPedanticWarnings() {
      return _pedanticWarnings;
   }

   public List<String> getRedFlagWarnings() {
      return _redFlagWarnings;
   }

   public List<String> getUnimplementedWarnings() {
      return _unimplementedWarnings;
   }

   public void pedantic(String msg) {
      if (_pedanticAsError) {
         throw new PedanticBatfishException(msg);
      }
      else if (_pedanticRecord) {
         String prefix = "WARNING " + (_pedanticWarnings.size() + 1)
               + ": PEDANTIC: ";
         String warning = prefix + msg + "\n";
         _pedanticWarnings.add(warning);
      }
   }

   public void redFlag(String msg) {
      if (_redFlagAsError) {
         throw new RedFlagBatfishException(msg);
      }
      else if (_redFlagRecord) {
         String prefix = "WARNING " + (_redFlagWarnings.size() + 1)
               + ": RED FLAG: ";
         String warning = prefix + msg + "\n";
         _redFlagWarnings.add(warning);
      }
   }

   public void unimplemented(String msg) {
      if (_unimplementedAsError) {
         throw new UnimplementedBatfishException(msg);
      }
      else if (_unimplementedRecord) {
         String prefix = "WARNING " + (_unimplementedWarnings.size() + 1)
               + ": UNIMPLEMENTED: ";
         String warning = prefix + msg + "\n";
         _unimplementedWarnings.add(warning);
      }
   }

}
