package org.batfish.coordinator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SortedSet;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.coordinator.config.Settings;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link WorkMgr}. */
public class WorkMgrTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private WorkMgr _manager;

  @Rule public ExpectedException _thrown = ExpectedException.none();

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
}
