package org.batfish.datamodel.routing_policy;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Route;

public class Environment {

   private boolean _buffered;

   private boolean _callExprContext;

   private boolean _callStatementContext;

   private Configuration _configuration;

   private boolean _defaultAction;

   private boolean _error;

   private boolean _localDefaultAction;

   private final Route _originalRoute;

   public Environment(Route originalRoute) {
      _originalRoute = originalRoute;
   }

   public boolean getBuffered() {
      return _buffered;
   }

   public boolean getCallExprContext() {
      return _callExprContext;
   }

   public boolean getCallStatementContext() {
      return _callStatementContext;
   }

   public Configuration getConfiguration() {
      return _configuration;
   }

   public boolean getDefaultAction() {
      return _defaultAction;
   }

   public boolean getError() {
      return _error;
   }

   public boolean getLocalDefaultAction() {
      return _localDefaultAction;
   }

   public Route getOriginalRoute() {
      return _originalRoute;
   }

   public void setBuffered(boolean buffered) {
      _buffered = buffered;
   }

   public void setCallExprContext(boolean callExprContext) {
      _callExprContext = callExprContext;
   }

   public void setCallStatementContext(boolean callStatementContext) {
      _callStatementContext = callStatementContext;
   }

   public void setConfiguration(Configuration configuration) {
      _configuration = configuration;
   }

   public void setDefaultAction(boolean defaultAction) {
      _defaultAction = defaultAction;
   }

   public void setError(boolean error) {
      _error = error;
   }

   public void setLocalDefaultAction(boolean localDefaultAction) {
      _localDefaultAction = localDefaultAction;
   }

}
