package org.batfish.coordinator;

import static org.hamcrest.CoreMatchers.containsString;
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

/**
 * Tests for {@link WorkMgr}.
 */
public class WorkMgrTest {

   private WorkMgr manager;

   @Rule
   public TemporaryFolder folder = new TemporaryFolder();

   @Rule
   public ExpectedException thrown = ExpectedException.none();

   @Before
   public void initManager() throws Exception{
      Settings settings = new Settings(new String[]{});
      BatfishLogger logger = new BatfishLogger("debug", false);
      Main.mainInit(new String[]{});
      manager = new WorkMgr(settings, logger);
   }

   @Test
   public void listQuestionWithNonExistContainer() {
      String nonExistingPath = folder.getRoot().toPath().resolve("non-existing").toString();
      thrown.expect(BatfishException.class);
      thrown.expectMessage(equalTo("Container '" + nonExistingPath + "' does not exist"));
      manager.listQuestions(nonExistingPath, "non-existing");
   }

   @Test
   public void listQuestionWithNonExistTestrig() throws IOException {
      String nonExistingPath = folder.getRoot().toPath().resolve("non-existing").toString();
      String containerPath = folder.newFolder("container").getPath();
      thrown.expect(BatfishException.class);
      thrown.expectMessage(equalTo("Testrig '" + nonExistingPath + "' does not exist"));
      manager.listQuestions(containerPath, nonExistingPath);
   }

   @Test
   public void listEmptyQuestion() throws IOException {
      String containerPath = folder.newFolder("container").getPath();
      String testrigPath = folder.newFolder("testrigPath").getPath();
      SortedSet<String> questions = manager.listQuestions(containerPath, testrigPath);
      assertThat(questions.isEmpty(), is(true));
   }

   @Test
   public void listQuestionNames() throws IOException {
      String containerPath = folder.newFolder("container").getPath();
      String testrigPath = folder.newFolder("testrigPath").getPath();
      Path questionsDir = Paths.get(testrigPath).resolve(BfConsts.RELPATH_QUESTIONS_DIR);
      assertThat(questionsDir.resolve("initinfo").toFile().mkdirs(), is(true));
      SortedSet<String> questions = manager.listQuestions(containerPath, testrigPath);
      assertThat(questions.size(), is(1));
      assertThat(questions.first(), equalTo("initinfo"));
   }

   @Test
   public void listSortedQuestionNames() throws IOException {
      String containerPath = folder.newFolder("container").getPath();
      String testrigPath = folder.newFolder("testrigPath").getPath();
      Path questionsDir = Paths.get(testrigPath).resolve(BfConsts.RELPATH_QUESTIONS_DIR);
      assertThat(questionsDir.resolve("nodes").toFile().mkdirs(), is(true));
      assertThat(questionsDir.resolve("access").toFile().mkdirs(), is(true));
      assertThat(questionsDir.resolve("initinfo").toFile().mkdirs(), is(true));
      SortedSet<String> questions = manager.listQuestions(containerPath, testrigPath);
      assertThat(questions.size(), is(3));
      assertThat(questions.toString(), equalTo("[access, initinfo, nodes]"));
   }

   // Tests for getContainer method
   @Test
   public void getNonExistContainer() {
      String containerName = "myContainer";
      Main.mainInit(new String[]{"-containerslocation", folder.getRoot().toString()});
      String containerInfo = manager.existContainer(containerName);
      String expectedInfo = String
            .format("Container %s does not exist\n", containerName);
      assertThat(containerInfo, equalTo(expectedInfo));
   }

   @Test
   public void getEmptyContainer() throws IOException {
      String containerName = "myContainer";
      folder.newFolder(containerName);
      Main.mainInit(new String[]{"-containerslocation", folder.getRoot().toString()});
      String containerInfo = manager.existContainer(containerName);
      String expectedInfo = String
            .format("Container %s created at:", containerName);
      assertThat(containerInfo, containsString(expectedInfo));
   }

   @Test
   public void getNonEmptyContainer() throws IOException {
      String containerName = "myContainer";
      Path containerPath = folder.newFolder(containerName).toPath();
      Path testrigPath = containerPath.resolve("testrig");
      assertThat(testrigPath.toFile().mkdir(), is(true));
      Main.mainInit(new String[] { "-containerslocation", folder.getRoot().toString() });
      String containerInfo = manager.existContainer(containerName);
      String expectedInfo = String.format("Container %s created at:", containerName);
      assertThat(containerInfo, containsString(expectedInfo));
   }

   @Test
   public void initExistingContainer() throws IOException {
      String containerName = "myContainer";
      folder.newFolder(containerName);
      Main.mainInit(new String[]{"-containerslocation", folder.getRoot().toString()});
      String expectedMessage = String
            .format("Container '%s' already exists!", containerName);
      thrown.expect(BatfishException.class);
      thrown.expectMessage(equalTo(expectedMessage));
      manager.initContainer(containerName, null);
   }

   @Test
   public void initContainerWithContainerName() throws IOException {
      String containerName = "myContainer";
      Main.mainInit(new String[]{"-containerslocation", folder.getRoot().toString()});
      String initResult = manager.initContainer(containerName, null);
      assertThat(initResult, equalTo(containerName));
   }

   @Test
   public void initContainerWithcontainerPrefix() throws IOException {
      String containerPrefix = "myContainerPrefix";
      Main.mainInit(new String[]{"-containerslocation", folder.getRoot().toString()});
      String initResult = manager.initContainer(null, containerPrefix);
      assertThat(initResult, startsWith(containerPrefix));
   }

   @Test
   public void initContainerWithNullInput() throws IOException {
      Main.mainInit(new String[]{"-containerslocation", folder.getRoot().toString()});
      String initResult = manager.initContainer(null, null);
      assertThat(initResult, startsWith("null_"));
   }

}
