package org.batfish.coordinator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

  @Before
  public void initManager() throws Exception {
    Settings settings = new Settings(new String[] {});
    BatfishLogger logger = new BatfishLogger("debug", false);
    Main.mainInit(new String[] {"-containerslocation", _folder.getRoot().toString()});
    Main.setLogger(logger);
    _manager = new WorkMgr(settings, logger);
  }

  @Test
  public void initContainerWithContainerName() {
    String initResult = _manager.initContainer("container", null);
    assertThat(initResult, equalTo("container"));
  }

  @Test
  public void initContainerWithContainerPrefix() {
    String initResult = _manager.initContainer(null, "containerPrefix");
    assertThat(initResult, startsWith("containerPrefix"));
  }

  @Test
  public void initContainerWithNullInput() {
    String initResult = _manager.initContainer(null, null);
    assertThat(initResult, startsWith("null_"));
  }

  @Test
  public void initExistingContainer() {
    _manager.initContainer("container", null);
    String expectedMessage = "Container 'container' already exists!";
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(equalTo(expectedMessage));
    _manager.initContainer("container", null);
  }

  @Test
  public void listEmptyQuestion() {
    _manager.initContainer("container", null);
    Path containerDir =
        Main.getSettings().getContainersLocation().resolve("container").toAbsolutePath();
    Path testrigPath = containerDir.resolve(BfConsts.RELPATH_TESTRIGS_DIR).resolve("testrig");
    assertThat(testrigPath.toFile().mkdirs(), is(true));
    SortedSet<String> questions = _manager.listQuestions("container", "testrig");
    assertThat(questions.isEmpty(), is(true));
  }

  @Test
  public void listQuestionNames() {
    _manager.initContainer("container", null);
    Path containerDir =
        Main.getSettings().getContainersLocation().resolve("container").toAbsolutePath();
    Path testrigPath = containerDir.resolve(BfConsts.RELPATH_TESTRIGS_DIR).resolve("testrig");
    assertThat(testrigPath.toFile().mkdirs(), is(true));
    Path questionsDir = testrigPath.resolve(BfConsts.RELPATH_QUESTIONS_DIR);
    assertThat(questionsDir.resolve("initinfo").toFile().mkdirs(), is(true));
    SortedSet<String> questions = _manager.listQuestions("container", "testrig");
    assertThat(questions.size(), is(1));
    assertThat(questions.first(), equalTo("initinfo"));
  }

  @Test
  public void listQuestionWithNonExistContainer() {
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(equalTo("Container 'container' does not exist"));
    _manager.listQuestions("container", "testrig");
  }

  @Test
  public void listQuestionWithNonExistTestrig() {
    _manager.initContainer("container", null);
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(equalTo("Testrig 'testrig' does not exist"));
    _manager.listQuestions("container", "testrig");
  }

  @Test
  public void listSortedQuestionNames() {
    _manager.initContainer("container", null);
    Path containerDir =
        Main.getSettings().getContainersLocation().resolve("container").toAbsolutePath();
    Path testrigPath = containerDir.resolve(BfConsts.RELPATH_TESTRIGS_DIR).resolve("testrig");
    assertThat(testrigPath.toFile().mkdirs(), is(true));
    Path questionsDir = testrigPath.resolve(BfConsts.RELPATH_QUESTIONS_DIR);
    assertTrue(questionsDir.resolve("nodes").toFile().mkdirs());
    assertTrue(questionsDir.resolve("access").toFile().mkdirs());
    assertTrue(questionsDir.resolve("initinfo").toFile().mkdirs());
    SortedSet<String> questions = _manager.listQuestions("container", "testrig");
    assertThat(questions.size(), is(3));
    assertThat(questions.toString(), equalTo("[access, initinfo, nodes]"));
  }

  @Test
  public void getEmptyContainer() {
    _manager.initContainer("container", null);
    Container container = _manager.getContainer("container");
    assertThat(container, equalTo(Container.of("container", new TreeSet<>())));
  }

  @Test
  public void getNonEmptyContainer() {
    _manager.initContainer("container", null);
    Path containerDir =
        Main.getSettings().getContainersLocation().resolve("container").toAbsolutePath();
    Path testrigPath = containerDir.resolve(BfConsts.RELPATH_TESTRIGS_DIR).resolve("testrig");
    assertThat(testrigPath.toFile().mkdirs(), is(true));
    Container container = _manager.getContainer("container");
    assertThat(
        container,
        equalTo(Container.of("container", Sets.newTreeSet(Collections.singleton("testrig")))));
  }

  @Test
  public void getNonExistContainer() {
    _thrown.expect(Exception.class);
    _thrown.expectMessage(equalTo("Container 'container' does not exist"));
    _manager.getContainer("container");
  }

  @Test
  public void testConfigureAnalysis() throws Exception {
    String containerName = "myContainer";
    _manager.initContainer(containerName, null);
    // test init and add questions to analysis
    Map<String, String> questionsToAdd =
        Maps.newHashMap(Collections.singletonMap("question1", "question1Content"));
    _manager.configureAnalysis(
        containerName, true, "analysis", questionsToAdd, Lists.newArrayList());
    questionsToAdd = Maps.newHashMap(Collections.singletonMap("question2", "question2Content"));
    questionsToAdd.put("question3", "question3Content");
    _manager.configureAnalysis(
        containerName, false, "analysis", questionsToAdd, Lists.newArrayList());
    Path questionPath =
        _folder
            .getRoot()
            .toPath()
            .resolve(
                Paths.get(
                    containerName,
                    BfConsts.RELPATH_ANALYSES_DIR,
                    "analysis",
                    BfConsts.RELPATH_QUESTIONS_DIR));
    Path qFile = questionPath.resolve(Paths.get("question1", BfConsts.RELPATH_QUESTION_FILE));
    Path otherQFile = questionPath.resolve(Paths.get("question2", BfConsts.RELPATH_QUESTION_FILE));
    try {
      String actual = new String(Files.readAllBytes(qFile));
      assertThat(actual, equalTo("question1Content"));
      actual = new String(Files.readAllBytes(otherQFile));
      assertThat(actual, equalTo("question2Content"));
    } catch (IOException e) {
      throw new BatfishException("Failed to read question content", e);
    }

    // test delete questions
    List<String> questionsToDelete = Lists.newArrayList();
    _manager.configureAnalysis(
        containerName, false, "analysis", Maps.newHashMap(), questionsToDelete);
    assertTrue(
        Files.exists(questionPath.resolve("question1"))
            && Files.exists(questionPath.resolve("question2"))
            && Files.exists(questionPath.resolve("question3")));
    questionsToDelete = Lists.newArrayList("question1", "question2");
    _manager.configureAnalysis(
        containerName, false, "analysis", Maps.newHashMap(), questionsToDelete);
    assertFalse(Files.exists(questionPath.resolve("question1")));
    assertFalse(Files.exists(questionPath.resolve("question2")));
    assertTrue(Files.exists(questionPath.resolve("question3")));
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(equalTo("Question question1 does not exist for analysis analysis"));
    questionsToDelete = Lists.newArrayList("question1");
    _manager.configureAnalysis(
        containerName, false, "analysis", Maps.newHashMap(), questionsToDelete);
  }
}
