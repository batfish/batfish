package org.batfish.question;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.batfish.collections.AdvertisementSet;
import org.batfish.collections.RouteSet;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.question.statement.Statement;
import org.batfish.representation.Configuration;

public class VerifyProgram {

   private boolean _dataPlane;

   private boolean _dataPlaneBgpAdvertisements;

   private boolean _dataPlaneRoutes;

   private Environment _environment;

   private List<Statement> _statements;

   public VerifyProgram(QuestionParameters parameters) {
      _environment = new Environment();
      _environment.applyParameters(parameters);
      _statements = new ArrayList<Statement>();
   }

   public void execute(Map<String, Configuration> configurations,
         BatfishLogger logger, Settings settings) {
      _environment.setConfigurations(configurations);
      try {
         for (Statement statement : _statements) {
            statement.execute(_environment, logger, settings);
         }
      }
      catch (BatfishException e) {
         throw new BatfishException("Question failed", e);
      }
   }

   public boolean getAssertions() {
      return _environment.getAssertions();
   }

   public boolean getDataPlane() {
      return _dataPlane;
   }

   public boolean getDataPlaneBgpAdvertisements() {
      return _dataPlaneBgpAdvertisements;
   }

   public boolean getDataPlaneRoutes() {
      return _dataPlaneRoutes;
   }

   public int getFailedAssertions() {
      return _environment.getFailedAssertions();
   }

   public String getJson() {
      QMap query = _environment.getQuery();
      return query.toString();
   }

   public List<Statement> getStatements() {
      return _statements;
   }

   public int getTotalAssertions() {
      return _environment.getTotalAssertions();
   }

   public boolean getUnsafe() {
      return _environment.getUnsafe();
   }

   public void setBgpAdvertisements(AdvertisementSet bgpAdvertisements) {
      _environment.setGlobalBgpAdvertisements(bgpAdvertisements);
   }

   public void setDataPlane(boolean dataPlane) {
      _dataPlane = dataPlane;
   }

   public void setDataPlaneBgpAdvertisements(boolean dataPlaneBgpAdvertisements) {
      _dataPlaneBgpAdvertisements = dataPlaneBgpAdvertisements;
   }

   public void setDataPlaneRoutes(boolean dataPlaneRoutes) {
      _dataPlaneRoutes = dataPlaneRoutes;
   }

   public void setRoutes(RouteSet routes) {
      _environment.setGlobalRoutes(routes);
   }

}
