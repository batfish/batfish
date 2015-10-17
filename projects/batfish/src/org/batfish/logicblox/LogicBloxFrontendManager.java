package org.batfish.logicblox;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.main.Settings.EnvironmentSettings;
import org.batfish.util.Util;

public class LogicBloxFrontendManager {

   private EnvironmentSettings _activeEnvSettings;
   private Map<EnvironmentSettings, LogicBloxFrontend> _lbFrontends;
   private BatfishLogger _logger;
   private Settings _settings;

   public LogicBloxFrontendManager(Settings settings, BatfishLogger logger,
         EnvironmentSettings activeEnvSettings) {
      _settings = settings;
      _logger = logger;
      _activeEnvSettings = activeEnvSettings;
      _lbFrontends = new HashMap<EnvironmentSettings, LogicBloxFrontend>();
   }

   public void close() {
      for (LogicBloxFrontend lbFrontend : _lbFrontends.values()) {
         // Close backend threads
         if (lbFrontend != null && lbFrontend.connected()) {
            lbFrontend.close();
         }
      }
   }

   public LogicBloxFrontend connect() {
      return connect(_activeEnvSettings);
   }

   public LogicBloxFrontend connect(EnvironmentSettings envSettings) {
      LogicBloxFrontend lbFrontend = _lbFrontends.get(envSettings);
      if (lbFrontend != null) {
         return lbFrontend;
      }
      boolean assumedToExist = !_settings.createWorkspace();
      String workspaceMaster = envSettings.getWorkspaceName();
      String connectBloxHost = null;
      if (assumedToExist) {
         String jobLogicBloxHostnamePath = envSettings
               .getJobLogicBloxHostnamePath();
         if (jobLogicBloxHostnamePath != null) {
            String lbHostname = Util
                  .readFile(new File(jobLogicBloxHostnamePath));
            connectBloxHost = lbHostname;
         }
      }
      else {
         String serviceLogicBloxHostname = _settings
               .getServiceLogicBloxHostname();
         if (serviceLogicBloxHostname != null) {
            connectBloxHost = serviceLogicBloxHostname;
         }
      }
      if (connectBloxHost == null) {
         connectBloxHost = _settings.getConnectBloxHost();
      }
      try {
         lbFrontend = initFrontend(envSettings, assumedToExist,
               workspaceMaster, connectBloxHost, _settings.getConnectBloxPort());
      }
      catch (LBInitializationException e) {
         throw new BatfishException("Failed to connect to LogicBlox", e);
      }
      return lbFrontend;
   }

   private LogicBloxFrontend initFrontend(EnvironmentSettings envSettings,
         boolean assumedToExist, String workspace, String connectBloxHost,
         int connectBloxPort) throws LBInitializationException {
      _logger.info("\n*** STARTING CONNECTBLOX SESSION ***\n");
      LogicBloxFrontend lbFrontend = new LogicBloxFrontend(connectBloxHost,
            connectBloxPort, _settings.getLbWebPort(),
            _settings.getLbWebAdminPort(), workspace, assumedToExist, _logger);
      lbFrontend.initialize();
      if (!lbFrontend.connected()) {
         throw new BatfishException(
               "Error connecting to ConnectBlox service. Please make sure service is running and try again.");
      }
      _logger.info("SUCCESS\n");
      _lbFrontends.put(envSettings, lbFrontend);
      return lbFrontend;

   }

}
