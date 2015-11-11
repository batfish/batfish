package org.batfish.coordinator.authorizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.batfish.common.CoordConsts;
import org.batfish.coordinator.Main;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

//An authorizer that is backed by a file
//Useful for testing
public class FileAuthorizer implements Authorizer {

   private File _usersFile;
   private File _permsFile;
   
   public FileAuthorizer() throws FileNotFoundException {
      _usersFile = Paths.get(Main.getSettings().getFileAuthorizerRootDir(), 
            Main.getSettings().getFileAuthorizerUsersFile()).toFile();
      _permsFile = Paths.get(Main.getSettings().getFileAuthorizerRootDir(), 
            Main.getSettings().getFileAuthorizerPermsFile()).toFile();

      if (!_usersFile.exists())
         throw new FileNotFoundException("Users file not found: " + _usersFile.getAbsolutePath());
      
      if (!_permsFile.exists())
         throw new FileNotFoundException("Perms file not found: " + _permsFile.getAbsolutePath());
   }
   
   @Override
   public void authorizeContainer(String apiKey, String containerName) {
      return;
   }

   @Override
   public boolean isAccessibleContainer(String apiKey, String containerName) {
      return true;
   }

   @Override
   public boolean isValidWorkApiKey(String apiKey) throws Exception {
      String allUsers = FileUtils.readFileToString(_usersFile);      
      JSONObject jObj = new JSONObject(allUsers);

      if (!jObj.has("users"))
         throw new Exception("Do not understand the format of users file");
      
      JSONArray usersArray = jObj.getJSONArray("users");
      
      for (int index = 0; index < usersArray.length(); index++) {
         JSONObject jUserObj = usersArray.getJSONObject(index);

         if (apiKey.equals(jUserObj.getString("apikey")))
               return true;   
      }   
      
      return false;
   }
}
