package org.batfish.coordinator.authorizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.coordinator.Main;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

//An authorizer that is backed by a file
//Useful for testing
public class FileAuthorizer implements Authorizer {

   private static final String APIKEY_KEY = "apikey";
   private static final String CONTAINER_KEY = "container";
   private static final String PERMS_KEY = "perms";
   private static final String USERS_KEY = "users";

   private BatfishLogger _logger;
   private File _permsFile;
   private File _usersFile;

   public FileAuthorizer() throws FileNotFoundException {
      _logger = Main.getLogger();

      _usersFile = Paths.get(Main.getSettings().getFileAuthorizerRootDir(),
            Main.getSettings().getFileAuthorizerUsersFile()).toFile();
      _permsFile = Paths.get(Main.getSettings().getFileAuthorizerRootDir(),
            Main.getSettings().getFileAuthorizerPermsFile()).toFile();

      if (!_usersFile.exists()) {
         throw new FileNotFoundException("Users file not found: "
               + _usersFile.getAbsolutePath());
      }

      if (!_permsFile.exists()) {
         throw new FileNotFoundException("Perms file not found: "
               + _permsFile.getAbsolutePath());
      }
   }

   @Override
   public synchronized void authorizeContainer(String apiKey,
         String containerName) throws Exception {

      _logger.infof("Authorizing %s for %s\n", apiKey, containerName);
      String allPerms = FileUtils.readFileToString(_permsFile);
      JSONObject jObj = new JSONObject(allPerms);

      if (!jObj.has(PERMS_KEY)) {
         throw new Exception("Do not understand the format of perms file");
      }

      JSONArray permsArray = jObj.getJSONArray(PERMS_KEY);

      JSONObject jPermsObj = new JSONObject();
      jPermsObj.put(APIKEY_KEY, apiKey);
      jPermsObj.put(CONTAINER_KEY, containerName);

      permsArray.put(jPermsObj);
      jObj.put("perms", permsArray);

      String newAllPerms = jObj.toString();

      FileUtils.write(_permsFile, newAllPerms);
   }

   @Override
   public boolean isAccessibleContainer(String apiKey, String containerName)
         throws Exception {
      String allPerms = FileUtils.readFileToString(_permsFile);
      JSONObject jObj = new JSONObject(allPerms);

      if (!jObj.has(PERMS_KEY)) {
         throw new Exception("Do not understand the format of perms file");
      }

      JSONArray permsArray = jObj.getJSONArray(PERMS_KEY);

      for (int index = 0; index < permsArray.length(); index++) {
         JSONObject jPermsObj = permsArray.getJSONObject(index);

         if (apiKey.equals(jPermsObj.getString(APIKEY_KEY))
               && containerName.equals(jPermsObj.getString(CONTAINER_KEY))) {
            _logger.infof("Authorizer: %s is allowed to access %s\n", apiKey,
                  containerName);
            return true;
         }
      }

      _logger.infof("Authorizer: %s is NOT allowed to access %s\n", apiKey,
            containerName);
      return false;
   }

   @Override
   public boolean isValidWorkApiKey(String apiKey) throws Exception {
      String allUsers = FileUtils.readFileToString(_usersFile);
      JSONObject jObj = new JSONObject(allUsers);

      if (!jObj.has(USERS_KEY)) {
         throw new Exception("Do not understand the format of users file");
      }

      JSONArray usersArray = jObj.getJSONArray(USERS_KEY);

      for (int index = 0; index < usersArray.length(); index++) {
         JSONObject jUserObj = usersArray.getJSONObject(index);

         if (apiKey.equals(jUserObj.getString(APIKEY_KEY))) {
            _logger.infof("Authorizer: %s is a valid key\n", apiKey);
            return true;
         }
      }

      _logger.infof("Authorizer: %s is NOT a valid key\n", apiKey);
      return false;
   }
}
