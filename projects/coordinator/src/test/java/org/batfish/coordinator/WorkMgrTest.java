package org.batfish.coordinator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.CoordConsts;
import org.batfish.coordinator.config.Settings;
import org.batfish.datamodel.pojo.AccessLevel;
import org.batfish.datamodel.pojo.Container;
import org.batfish.datamodel.pojo.Testrig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link WorkMgr}. */
public class WorkMgrTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private WorkMgr _manager;

  @Test
  public void initContainerWithContainerName() throws IOException {
    String containerName = "myContainer";
    Main.mainInit(new String[] {"-containerslocation", _folder.getRoot().toString()});
    String initResult = _manager.initContainer(containerName, null);
    assertThat(initResult, equalTo(containerName));
  }

  @Test
  public void initContainerWithcontainerPrefix() throws IOException {
    String containerPrefix = "myContainerPrefix";
    Main.mainInit(new String[] {"-containerslocation", _folder.getRoot().toString()});
    String initResult = _manager.initContainer(null, containerPrefix);
    assertThat(initResult, startsWith(containerPrefix));
  }

  @Test
  public void initContainerWithNullInput() throws IOException {
    Main.mainInit(new String[] {"-containerslocation", _folder.getRoot().toString()});
    String initResult = _manager.initContainer(null, null);
    assertThat(initResult, startsWith("null_"));
  }

  @Test
  public void initExistingContainer() throws IOException {
    String containerName = "myContainer";
    _folder.newFolder(containerName);
    Main.mainInit(new String[] {"-containerslocation", _folder.getRoot().toString()});
    String expectedMessage = String.format("Container '%s' already exists!", containerName);
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(equalTo(expectedMessage));
    _manager.initContainer(containerName, null);
  }

  @Before
  public void initManager() throws Exception {
    Settings settings = new Settings(new String[] {});
    BatfishLogger logger = new BatfishLogger("debug", false);
    Main.mainInit(new String[] {});
    Main.setLogger(logger);
    _manager = new WorkMgr(settings, logger);
  }

  @Test
  public void listEmptyQuestion() throws IOException {
    Main.mainInit(new String[] {"-containerslocation", _folder.getRoot().toString()});
    String containerPath = _folder.newFolder("container").getPath();
    Path testrigPath =
        Paths.get(containerPath).resolve(BfConsts.RELPATH_TESTRIGS_DIR).resolve("testrig");
    assertThat(testrigPath.toFile().mkdirs(), is(true));
    SortedSet<String> questions = _manager.listQuestions("container", "testrig");
    assertThat(questions.isEmpty(), is(true));
  }

  @Test
  public void listQuestionNames() throws IOException {
    Main.mainInit(new String[] {"-containerslocation", _folder.getRoot().toString()});
    String containerPath = _folder.newFolder("container").getPath();
    Path testrigPath =
        Paths.get(containerPath).resolve(BfConsts.RELPATH_TESTRIGS_DIR).resolve("testrig");
    assertThat(testrigPath.toFile().mkdirs(), is(true));
    Path questionsDir = testrigPath.resolve(BfConsts.RELPATH_QUESTIONS_DIR);
    assertThat(questionsDir.resolve("initinfo").toFile().mkdirs(), is(true));
    SortedSet<String> questions = _manager.listQuestions("container", "testrig");
    assertThat(questions.size(), is(1));
    assertThat(questions.first(), equalTo("initinfo"));
  }

  @Test
  public void listQuestionWithNonExistContainer() {
    String nonExistingPath = _folder.getRoot().toPath().resolve("non-existing").toString();
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(equalTo("Container '" + nonExistingPath + "' does not exist"));
    _manager.listQuestions(nonExistingPath, "non-existing");
  }

  @Test
  public void listQuestionWithNonExistTestrig() throws IOException {
    String nonExistingPath = _folder.getRoot().toPath().resolve("non-existing").toString();
    String containerPath = _folder.newFolder("container").getPath();
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(equalTo("Testrig '" + nonExistingPath + "' does not exist"));
    _manager.listQuestions(containerPath, nonExistingPath);
  }

  @Test
  public void listSortedQuestionNames() throws IOException {
    Main.mainInit(new String[] {"-containerslocation", _folder.getRoot().toString()});
    String containerPath = _folder.newFolder("container").getPath();
    Path testrigPath =
        Paths.get(containerPath).resolve(BfConsts.RELPATH_TESTRIGS_DIR).resolve("testrig");
    assertThat(testrigPath.toFile().mkdirs(), is(true));
    Path questionsDir = testrigPath.resolve(BfConsts.RELPATH_QUESTIONS_DIR);
    assertThat(questionsDir.resolve("nodes").toFile().mkdirs(), is(true));
    assertThat(questionsDir.resolve("access").toFile().mkdirs(), is(true));
    assertThat(questionsDir.resolve("initinfo").toFile().mkdirs(), is(true));
    SortedSet<String> questions = _manager.listQuestions("container", "testrig");
    assertThat(questions.size(), is(3));
    assertThat(questions.toString(), equalTo("[access, initinfo, nodes]"));
  }

  private void initContainerEnvironment(String containerName) throws Exception {
    _folder.newFolder(containerName);
    Main.mainInit(new String[] {"-containerslocation", _folder.getRoot().toString()});
  }

  @Test
  public void testCheckContainerExists() throws Exception {
    String containerName = "myContainer";
    initContainerEnvironment(containerName);
    assertTrue(_manager.checkContainerExists(containerName));
    assertFalse(_manager.checkContainerExists("nonExistingContainer"));
  }

  @Test
  public void getNonExistContainer() {
    String containerName = "myContainer";
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(equalTo("Container '" + containerName + "' does not exist"));
    _manager.getContainer(containerName, AccessLevel.FULL);
  }

  @Test
  public void getEmptyContainerDiffAccessLevel() throws Exception {
    String containerName = "myContainer";
    initContainerEnvironment(containerName);
    Container container = _manager.getContainer(containerName, AccessLevel.SUMMARY);
    Container expected = Container.of(containerName, new ArrayList<>(), new ArrayList<>());
    assertThat(container, equalTo(expected));
    container = _manager.getContainer(containerName, AccessLevel.ONELINE);
    expected = Container.of(containerName);
    assertThat(container, equalTo(expected));
  }

  @Test
  public void getNonEmptyContainer() throws Exception {
    String containerName = "myContainer";
    initContainerEnvironment(containerName);
    Path containerPath = _folder.getRoot().toPath().resolve(containerName);
    Path testrigPath = containerPath.resolve(BfConsts.RELPATH_TESTRIGS_DIR).resolve("testrig");
    assertThat(testrigPath.toFile().mkdirs(), is(true));
    Container container = _manager.getContainer(containerName, AccessLevel.SUMMARY);
    List<Testrig> expectedTestrigs = Lists.newArrayList(Testrig.of("testrig"));
    Container expected = Container.of(containerName, expectedTestrigs, new ArrayList<>());
    assertThat(container, equalTo(expected));
  }

  @Test
  public void testListContainers() throws Exception {
    Main.mainInit(new String[] {"-containerslocation", _folder.getRoot().toString()});
    Main.initAuthorizer();
    List<Container> containers =
        _manager.listContainers(CoordConsts.DEFAULT_API_KEY, AccessLevel.SUMMARY);
    List<Container> expected = new ArrayList<>();
    assertThat(containers, equalTo(expected));
    String containerName = "myContainer";
    String initResult = _manager.initContainer(containerName, null);
    assertThat(initResult, equalTo(containerName));
    expected.add(Container.of(containerName, new ArrayList<>(), new ArrayList<>()));
    containerName = "myContainer2";
    initResult = _manager.initContainer(containerName, null);
    assertThat(initResult, equalTo(containerName));
    containers = _manager.listContainers(CoordConsts.DEFAULT_API_KEY, AccessLevel.SUMMARY);
    expected.add(Container.of(containerName, new ArrayList<>(), new ArrayList<>()));
    assertThat(containers, equalTo(expected));
    expected.clear();
    expected.add(Container.of("myContainer"));
    expected.add(Container.of("myContainer2"));
    containers = _manager.listContainers(CoordConsts.DEFAULT_API_KEY, AccessLevel.ONELINE);
    assertThat(containers, equalTo(expected));
  }

  @Test
  public void testCheckTestrigsExists() throws Exception {
    String containerName = "myContainer";
    initContainerEnvironment(containerName);
    Path containerPath = _folder.getRoot().toPath().resolve(containerName);
    Path testrigPath = containerPath.resolve(BfConsts.RELPATH_TESTRIGS_DIR).resolve("testrig");
    assertThat(testrigPath.toFile().mkdirs(), is(true));
    assertTrue(_manager.checkTestrigExists(containerName, "testrig"));
    assertFalse(_manager.checkTestrigExists(containerName, "Non-existing-testrig"));
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(equalTo("Container 'nonExist' does not exist"));
    _manager.checkTestrigExists("nonExist", "testrig");
  }

  @Test
  public void getNonExistingTestrig() throws Exception {
    String containerName = "myContainer";
    initContainerEnvironment(containerName);
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(equalTo("Testrig 'non-exist' does not exist in container 'myContainer'"));
    _manager.getTestrig(containerName, "non-exist", AccessLevel.FULL);
  }

  @Test
  public void getEmptyTestrigDiffAccessLevel() throws Exception {
    String containerName = "myContainer";
    initContainerEnvironment(containerName);
    Path containerPath = _folder.getRoot().toPath().resolve(containerName);
    Path testrigPath = containerPath.resolve(BfConsts.RELPATH_TESTRIGS_DIR).resolve("testrig");
    assertTrue(testrigPath.toFile().mkdirs());
    Testrig testrig = _manager.getTestrig(containerName, "testrig", AccessLevel.SUMMARY);
    Testrig expected =
        Testrig.of("testrig", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    assertThat(testrig, equalTo(expected));
    testrig = _manager.getTestrig(containerName, "testrig", AccessLevel.ONELINE);
    expected = Testrig.of("testrig");
    assertThat(testrig, equalTo(expected));
  }

  @Test
  public void getNonEmptyTestrig() throws Exception {
    String containerName = "myContainer";
    initContainerEnvironment(containerName);
    Path containerPath = _folder.getRoot().toPath().resolve(containerName);
    Path testrigPath = containerPath.resolve(BfConsts.RELPATH_TESTRIGS_DIR).resolve("testrig");
    assertThat(testrigPath.toFile().mkdirs(), is(true));
    Path environment =
        testrigPath.resolve(BfConsts.RELPATH_ENVIRONMENTS_DIR).resolve("environment");
    assertThat(environment.toFile().mkdirs(), is(true));
    Testrig testrig = _manager.getTestrig(containerName, "testrig", AccessLevel.SUMMARY);
    Testrig expected =
        Testrig.of(
            "testrig", new ArrayList<>(), Lists.newArrayList("environment"), new ArrayList<>());
    assertThat(testrig, equalTo(expected));
  }

  @Test
  public void testListTestrigs() throws Exception {
    String containerName = "myContainer";
    initContainerEnvironment(containerName);
    List<Testrig> testrigs = _manager.listTestrigs(containerName, AccessLevel.SUMMARY);
    List<Testrig> expected = new ArrayList<>();
    assertThat(testrigs, equalTo(expected));
    Path containerPath = _folder.getRoot().toPath().resolve(containerName);
    Path testrigPath = containerPath.resolve(BfConsts.RELPATH_TESTRIGS_DIR).resolve("testrig");
    assertThat(testrigPath.toFile().mkdirs(), is(true));
    expected.add(Testrig.of("testrig", new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
    Path testrig1Path = containerPath.resolve(BfConsts.RELPATH_TESTRIGS_DIR).resolve("testrig1");
    assertThat(testrig1Path.toFile().mkdirs(), is(true));
    expected.add(Testrig.of("testrig1", new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
    testrigs = _manager.listTestrigs(containerName, AccessLevel.SUMMARY);
    assertThat(testrigs, equalTo(expected));
    expected.clear();
    expected.add(Testrig.of("testrig"));
    expected.add(Testrig.of("testrig1"));
    testrigs = _manager.listTestrigs(containerName, AccessLevel.ONELINE);
    assertThat(testrigs, equalTo(expected));
  }
}
