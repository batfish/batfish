package org.batfish.datamodel.routing_policy;

import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AbstractRoute6;
import org.batfish.datamodel.AbstractRouteBuilder;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Vrf;

public class Environment {

   private boolean _buffered;

   private boolean _callExprContext;

   private boolean _callStatementContext;

   private final Configuration _configuration;

   private boolean _defaultAction;

   private String _defaultPolicy;

   private boolean _error;

   private BgpRoute.Builder _intermediateBgpAttributes;

   private boolean _localDefaultAction;

   private final AbstractRoute _originalRoute;

   private AbstractRoute6 _originalRoute6;

   private final AbstractRouteBuilder<?> _outputRoute;

   private final Ip _peerAddress;

   private boolean _readFromIntermediateBgpAttributes;

   private final boolean _useOutputAttributes;

   private Vrf _vrf;

   private boolean _writeToIntermediateBgpAttributes;

   public Environment(Configuration configuration, String vrf,
         AbstractRoute originalRoute, AbstractRoute6 originalRoute6,
         AbstractRouteBuilder<?> outputRoute, Ip peerAddress) {
      _configuration = configuration;
      _vrf = configuration.getVrfs().get(vrf);
      _originalRoute = originalRoute;
      _outputRoute = outputRoute;
      _peerAddress = peerAddress;
      ConfigurationFormat format = _configuration.getConfigurationFormat();
      _useOutputAttributes = format == ConfigurationFormat.JUNIPER
            || format == ConfigurationFormat.JUNIPER_SWITCH
            || format == ConfigurationFormat.FLAT_JUNIPER;
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

   public String getDefaultPolicy() {
      return _defaultPolicy;
   }

   public boolean getError() {
      return _error;
   }

   public BgpRoute.Builder getIntermediateBgpAttributes() {
      return _intermediateBgpAttributes;
   }

   public boolean getLocalDefaultAction() {
      return _localDefaultAction;
   }

   public AbstractRoute getOriginalRoute() {
      return _originalRoute;
   }

   public AbstractRoute6 getOriginalRoute6() {
      return _originalRoute6;
   }

   public AbstractRouteBuilder<?> getOutputRoute() {
      return _outputRoute;
   }

   public Ip getPeerAddress() {
      return _peerAddress;
   }

   public boolean getReadFromIntermediateBgpAttributes() {
      return _readFromIntermediateBgpAttributes;
   }

   public boolean getUseOutputAttributes() {
      return _useOutputAttributes;
   }

   public Vrf getVrf() {
      return _vrf;
   }

   public boolean getWriteToIntermediateBgpAttributes() {
      return _writeToIntermediateBgpAttributes;
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

   public void setDefaultPolicy(String defaultPolicy) {
      _defaultPolicy = defaultPolicy;
   }

   public void setError(boolean error) {
      _error = error;
   }

   public void setIntermediateBgpAttributes(
         BgpRoute.Builder intermediateBgpAttributes) {
      _intermediateBgpAttributes = intermediateBgpAttributes;
   }

   public void setLocalDefaultAction(boolean localDefaultAction) {
      _localDefaultAction = localDefaultAction;
   }

   public void setReadFromIntermediateBgpAttributes(
         boolean readFromIntermediateBgpAttributes) {
      _readFromIntermediateBgpAttributes = readFromIntermediateBgpAttributes;
   }

   public void setWriteToIntermediateBgpAttributes(
         boolean writeToIntermediateBgpAttributes) {
      _writeToIntermediateBgpAttributes = writeToIntermediateBgpAttributes;
   }

}
