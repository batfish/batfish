package org.batfish.datamodel.routing_policy;

import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AbstractRouteBuilder;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;

public class Environment {

   private boolean _buffered;

   private boolean _callExprContext;

   private boolean _callStatementContext;

   private final Configuration _configuration;

   private boolean _defaultAction;

   private boolean _error;

   private boolean _localDefaultAction;

   private final AbstractRoute _originalRoute;

   private final AbstractRouteBuilder<?> _outputRoute;

   private final Ip _peerAddress;

   public Environment(Configuration configuration, AbstractRoute originalRoute,
         AbstractRouteBuilder<?> outputRoute, Ip peerAddress) {
      _configuration = configuration;
      _originalRoute = originalRoute;
      _outputRoute = outputRoute;
      _peerAddress = peerAddress;
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

   public AbstractRoute getOriginalRoute() {
      return _originalRoute;
   }

   public AbstractRouteBuilder<?> getOutputRoute() {
      return _outputRoute;
   }

   public Ip getPeerAddress() {
      return _peerAddress;
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
