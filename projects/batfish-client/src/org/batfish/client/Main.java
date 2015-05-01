package org.batfish.client;

import org.batfish.common.BfConsts;
import org.batfish.common.CoordConsts;

public class Main {

   public static void main(String[] args) {

      if (args.length == 0 || (!args[0].equals("-b") && !args[0].equals("-i"))) {
         System.err
               .println("Specify either '-b' (batch mode) or '-i' (interactive mode) as first argument");
         System.exit(1);
      }

      if (args[0].equals("-b")) {

         // default values
         String workMgr = "localhost:" + CoordConsts.SVC_WORK_PORT;
         String poolMgr = "localhost:" + CoordConsts.SVC_POOL_PORT;
         String testrigName = "example";
         String testrigZipfileName = "example.zip";
         String envName = "default";
         String envZipfileName = "default.zip";
         String questionName = "multipath";
         String questionFilename = "multipath.q";
         String workerToAdd = "localhost:" + BfConsts.SVC_PORT;

         // if arguments are supplied
         if (args.length == 9) {
            workMgr = args[0];
            poolMgr = args[1];
            testrigName = args[2];
            testrigZipfileName = args[3];
            envName = args[4];
            envZipfileName = args[5];
            questionName = args[6];
            questionFilename = args[7];
            workerToAdd = args[8];
         }

         new BatchClient(workMgr, poolMgr, testrigName, testrigZipfileName,
               envName, envZipfileName, questionName, questionFilename,
               workerToAdd);
      }
      else {
         new InteractiveClient(new String[] { "simple" });
      }
   }
}