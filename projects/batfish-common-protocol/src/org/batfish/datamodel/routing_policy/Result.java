package org.batfish.datamodel.routing_policy;

import org.batfish.datamodel.Route;

public class Result {

   private Boolean _action;

   private boolean _booleanValue;

   private boolean _exit;

   private boolean _return;

   private Route _route;

   public Boolean getAction() {
      return _action;
   }

   public boolean getBooleanValue() {
      return _booleanValue;
   }

   public boolean getExit() {
      return _exit;
   }

   public boolean getReturn() {
      return _return;
   }

   public Route getRoute() {
      return _route;
   }

   public void setAction(Boolean action) {
      _action = action;
   }

   public void setBooleanValue(boolean booleanValue) {
      _booleanValue = booleanValue;
   }

   public void setExit(boolean exit) {
      _exit = exit;
   }

   public void setReturn(boolean ret) {
      _return = ret;
   }

   public void setRoute(Route route) {
      _route = route;
   }

}
