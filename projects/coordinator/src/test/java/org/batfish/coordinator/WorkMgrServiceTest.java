package org.batfish.coordinator;

import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import com.google.common.collect.Lists;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.CoordConsts;
import org.batfish.common.Version;
import org.batfish.coordinator.config.Settings;
import org.batfish.datamodel.pojo.Container;
import org.batfish.datamodel.pojo.Testrig;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class WorkMgrServiceTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private WorkMgr _manager;

  private WorkMgrService _service;

  private String _containerName = "myContainer";

  private void initContainerEnvironment() throws Exception {
    Settings settings = new Settings(new String[] {});
    BatfishLogger logger = new BatfishLogger("debug", false);
    Main.mainInit(new String[] {});
    _folder.newFolder(_containerName);
    Main.mainInit(new String[] {"-containerslocation", _folder.getRoot().toString()});
    Main.initAuthorizer();
    Main.setLogger(logger);
    _manager = new WorkMgr(settings, logger);
    Main.setWorkMgr(_manager);
    _service = new WorkMgrService();
  }

  @Test
  public void getEmptyContainer() throws Exception {
    initContainerEnvironment();
    Response response =
        _service.getContainer(CoordConsts.DEFAULT_API_KEY, Version.getVersion(), _containerName);
    Container expected = Container.of(_containerName, new ArrayList<>(), new ArrayList<>());
    assertThat(response.getEntity(), equalTo(expected));
  }

  @Test
  public void getNonExistContainer() throws Exception {
    String containerName = "non-existing-folder";
    initContainerEnvironment();
    Response response =
        _service.getContainer(CoordConsts.DEFAULT_API_KEY, Version.getVersion(), containerName);
    String actualMessage = response.getEntity().toString();
    String expected = "Container '" + containerName + "' not found";
    assertThat(actualMessage, equalTo(expected));
  }

  @Test
  public void getContainerWithBadVersion() throws Exception {
    initContainerEnvironment();
    Response response =
        _service.getContainer(CoordConsts.DEFAULT_API_KEY, "invalid version", _containerName);
    String actualMessage = response.getEntity().toString();
    String expected = "Illegal version 'invalid version' for Client";
    assertThat(actualMessage, equalTo(expected));
  }

  @Test
  public void getNonEmptyContainer() throws Exception {
    initContainerEnvironment();
    Path containerPath = _folder.getRoot().toPath().resolve(_containerName);
    Path testrigPath = containerPath.resolve(BfConsts.RELPATH_TESTRIGS_DIR).resolve("testrig");
    assertThat(testrigPath.toFile().mkdirs(), is(true));
    Response response =
        _service.getContainer(CoordConsts.DEFAULT_API_KEY, Version.getVersion(), _containerName);
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    List<Testrig> testrigs = Lists.newArrayList(Testrig.of("testrig"));
    Container expected = Container.of(_containerName, testrigs, new ArrayList<>());
    assertThat(response.getEntity(), equalTo(expected));
  }
}
