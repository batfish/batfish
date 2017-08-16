package org.batfish.coordinator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.Container;
import org.batfish.coordinator.config.Settings;
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
    String containerPath = _folder.newFolder("container").getPath();
    String testrigPath = _folder.newFolder("testrigPath").getPath();
    SortedSet<String> questions = _manager.listQuestions(containerPath, testrigPath);
    assertThat(questions.isEmpty(), is(true));
  }

  @Test
  public void listQuestionNames() throws IOException {
    String containerPath = _folder.newFolder("container").getPath();
    String testrigPath = _folder.newFolder("testrigPath").getPath();
    Path questionsDir = Paths.get(testrigPath).resolve(BfConsts.RELPATH_QUESTIONS_DIR);
    assertThat(questionsDir.resolve("initinfo").toFile().mkdirs(), is(true));
    SortedSet<String> questions = _manager.listQuestions(containerPath, testrigPath);
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
    String containerPath = _folder.newFolder("container").getPath();
    String testrigPath = _folder.newFolder("testrigPath").getPath();
    Path questionsDir = Paths.get(testrigPath).resolve(BfConsts.RELPATH_QUESTIONS_DIR);
    assertThat(questionsDir.resolve("nodes").toFile().mkdirs(), is(true));
    assertThat(questionsDir.resolve("access").toFile().mkdirs(), is(true));
    assertThat(questionsDir.resolve("initinfo").toFile().mkdirs(), is(true));
    SortedSet<String> questions = _manager.listQuestions(containerPath, testrigPath);
    assertThat(questions.size(), is(3));
    assertThat(questions.toString(), equalTo("[access, initinfo, nodes]"));
  }

  private void initContainerEnvironment(String containerName) throws Exception {
    _folder.newFolder(containerName);
    Main.mainInit(new String[] {"-containerslocation", _folder.getRoot().toString()});
  }

  @Test
  public void getEmptyContainer() throws Exception {
    String containerName = "myContainer";
    initContainerEnvironment(containerName);
    Path containerDir = Paths.get(_folder.getRoot().toPath().resolve(containerName).toString());
    Container container = _manager.getContainer(containerDir);
    assertThat(container.getTestrigs(), equalTo(new TreeSet<String>()));
  }

  @Test
  public void getNonEmptyContainer() throws Exception {
    String containerName = "myContainer";
    initContainerEnvironment(containerName);
    Path containerPath = _folder.getRoot().toPath().resolve(containerName);
    Path testrigPath = containerPath.resolve("testrig1");
    assertThat(testrigPath.toFile().mkdir(), is(true));
    Path testrigPath2 = containerPath.resolve("testrig2");
    assertThat(testrigPath2.toFile().mkdir(), is(true));
    Path containerDir = Paths.get(_folder.getRoot().toPath().resolve(containerName).toString());
    Container container = _manager.getContainer(containerDir);
    SortedSet<String> expectedTestrigs = new TreeSet<>();
    expectedTestrigs.add("testrig1");
    expectedTestrigs.add("testrig2");
    assertThat(container.getTestrigs(), equalTo(expectedTestrigs));
  }

  @Test
  public void getNonExistContainer() {
    String containerName = "myContainer";
    Path containerDir = Paths.get(_folder.getRoot().toPath().resolve(containerName).toString());
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(equalTo("Error listing directory '" + containerDir + "'"));
    _manager.getContainer(containerDir);
  }

  @Test
  public void testDeleteQuestionsFromAnalysis() throws Exception {
    String containerName = "myContainer";
    initContainerEnvironment(containerName);
    Path containerPath = _folder.getRoot().toPath().resolve(containerName);
    Path analysisPath = containerPath.resolve(BfConsts.RELPATH_ANALYSES_DIR).resolve("analysis");
    assertTrue(analysisPath.toFile().mkdirs());
    Path question1Path = analysisPath.resolve(BfConsts.RELPATH_QUESTIONS_DIR).resolve("question1");
    assertTrue(question1Path.toFile().mkdirs());
    Path question2Path = analysisPath.resolve(BfConsts.RELPATH_QUESTIONS_DIR).resolve("question2");
    assertTrue(question2Path.toFile().mkdirs());
    Path question3Path = analysisPath.resolve(BfConsts.RELPATH_QUESTIONS_DIR).resolve("question3");
    assertTrue(question3Path.toFile().mkdirs());
    List<String> questionsToDelete = Lists.newArrayList();
    _manager.configureAnalysis(containerName, false, "analysis", null, questionsToDelete);
    assertTrue(Files.exists(question1Path));
    assertTrue(Files.exists(question2Path));
    assertTrue(Files.exists(question3Path));
    questionsToDelete = Lists.newArrayList("question1", "question2");
    _manager.configureAnalysis(containerName, false, "analysis", null, questionsToDelete);
    assertFalse(Files.exists(question1Path));
    assertFalse(Files.exists(question2Path));
    assertTrue(Files.exists(question3Path));
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(equalTo("Question question1 does not exist for analysis analysis"));
    questionsToDelete = Lists.newArrayList("question1");
    _manager.configureAnalysis(containerName, false, "analysis", null, questionsToDelete);
  }
}
