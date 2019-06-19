package org.batfish.coordinator.authorizer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.batfish.common.BatfishLogger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link FileAuthorizer}. */
@RunWith(JUnit4.class)
public class FileAuthorizerTest {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  @Rule public ExpectedException _exception = ExpectedException.none();
  private BatfishLogger _logger = new BatfishLogger("fatal", false);

  @Test
  public void testUserMissing() throws Exception {
    Path root = _folder.getRoot().toPath();

    _exception.expect(FileNotFoundException.class);
    _exception.expectMessage("missing_users");
    new FileAuthorizer(root.resolve("missing_users"), root.resolve("missing_perms"), _logger);
  }

  @Test
  public void testPermsMissing() throws Exception {
    Path root = _folder.getRoot().toPath();
    File users = _folder.newFile("present_users");

    _exception.expect(FileNotFoundException.class);
    _exception.expectMessage("missing_perms");
    new FileAuthorizer(users.toPath(), root.resolve("missing_perms"), _logger);
  }

  @Test
  public void testEmptyAuthorizer() throws Exception {
    Path users = _folder.newFile("users").toPath();
    Path perms = _folder.newFile("perms").toPath();
    Files.write(users, "{\"users\": []}".getBytes());
    Files.write(perms, "{\"perms\": []}".getBytes());

    // Rejects any key.
    Authorizer authorizer = new FileAuthorizer(users, perms, _logger);
    assertFalse(authorizer.isAccessibleNetwork("nokey", "nocontainer", false));
    assertFalse(authorizer.isAccessibleNetwork("", "", false));
  }

  @Test
  public void testAuthorizerCorrectness() throws Exception {
    Path users = _folder.newFile("users").toPath();
    Path perms = _folder.newFile("perms").toPath();
    Files.write(users, "{\"users\": []}".getBytes());
    Files.write(perms, "{\"perms\": [{\"apikey\": \"key\", \"container\": \"cont\"}]}".getBytes());

    // Accepts initial key and initial container.
    Authorizer authorizer = new FileAuthorizer(users, perms, _logger);
    assertTrue(authorizer.isAccessibleNetwork("key", "cont", false));

    // Rejects right key wrong container, and vice versa.
    assertFalse(authorizer.isAccessibleNetwork("key", "nocont", false));
    assertFalse(authorizer.isAccessibleNetwork("nokey", "cont", false));

    // Is case-sensitive
    assertFalse(authorizer.isAccessibleNetwork("kEy", "cont", false));
    assertFalse(authorizer.isAccessibleNetwork("key", "CONT", false));
  }

  @Test
  public void testAddingPermissions() throws Exception {
    final String KEY = "key";
    final String KEY2 = "key2";
    final String CONT = "cont";
    final String CONT2 = "cont2";
    Path users = _folder.newFile("users").toPath();
    Path perms = _folder.newFile("perms").toPath();
    Files.write(users, "{\"users\": []}".getBytes());
    Files.write(perms, "{\"perms\": [{\"apikey\": \"key\", \"container\": \"cont\"}]}".getBytes());

    // Accepts initial key and initial container, but not second key or container.
    Authorizer authorizer = new FileAuthorizer(users, perms, _logger);
    assertTrue(authorizer.isAccessibleNetwork(KEY, CONT, false));
    assertFalse(authorizer.isAccessibleNetwork(KEY, CONT2, false));
    assertFalse(authorizer.isAccessibleNetwork(KEY2, CONT, false));
    assertFalse(authorizer.isAccessibleNetwork(KEY2, CONT2, false));

    // Authorize key for second container, ensure only 1 new interaction is valid.
    authorizer.authorizeContainer(KEY, CONT2);
    assertTrue(authorizer.isAccessibleNetwork(KEY, CONT, false));
    assertTrue(authorizer.isAccessibleNetwork(KEY, CONT2, false));
    assertFalse(authorizer.isAccessibleNetwork(KEY2, CONT, false));
    assertFalse(authorizer.isAccessibleNetwork(KEY2, CONT2, false));

    // Authorize key2 for second container, ensure only 1 new interaction is valid.
    authorizer.authorizeContainer(KEY2, CONT2);
    assertTrue(authorizer.isAccessibleNetwork(KEY, CONT, false));
    assertTrue(authorizer.isAccessibleNetwork(KEY, CONT2, false));
    assertFalse(authorizer.isAccessibleNetwork(KEY2, CONT, false));
    assertTrue(authorizer.isAccessibleNetwork(KEY2, CONT2, false));
  }

  private static String generateUsers(String... keys) {
    StringBuilder sb = new StringBuilder("{\"users\":[");
    int i = 0;
    for (String key : keys) {
      if (i++ > 0) {
        sb.append(',');
      }
      sb.append("{\"apikey\":\"").append(key).append("\"}");
    }
    return sb.append("]}").toString();
  }

  @Test
  public void testApiKeyValidity() throws Exception {
    Path users = _folder.newFile("users").toPath();
    Path perms = _folder.newFile("perms").toPath();
    Files.write(users, generateUsers("key1").getBytes());
    Files.write(perms, "{\"perms\": []}".getBytes());

    // Accepts initial key and initial container, but not second key or container.
    Authorizer authorizer = new FileAuthorizer(users, perms, _logger);
    assertTrue(authorizer.isValidWorkApiKey("key1"));
    assertFalse(authorizer.isValidWorkApiKey("key2"));

    Files.write(users, generateUsers("key1", "key2").getBytes());
    assertTrue(authorizer.isValidWorkApiKey("key1"));
    assertTrue(authorizer.isValidWorkApiKey("key2"));
    assertFalse(authorizer.isValidWorkApiKey("key3"));
    assertFalse(authorizer.isValidWorkApiKey("KEY1"));
  }
}
