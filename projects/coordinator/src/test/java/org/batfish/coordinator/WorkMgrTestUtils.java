package org.batfish.coordinator;

import org.batfish.common.BatfishLogger;
import org.batfish.coordinator.config.Settings;
import org.junit.rules.TemporaryFolder;

public final class WorkMgrTestUtils {

  private WorkMgrTestUtils() {}

  public static void initWorkManager(TemporaryFolder folder) throws Exception {
    BatfishLogger logger = new BatfishLogger("debug", false);
    Settings settings = new Settings(new String[] {});
    Main.mainInit(new String[] {"-containerslocation", folder.getRoot().toString()});
    Main.setLogger(logger);
    Main.initAuthorizer();
    Main.setWorkMgr(new WorkMgr(settings, logger));
  }
}
