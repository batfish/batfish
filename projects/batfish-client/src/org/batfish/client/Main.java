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
         if (args.length == 10) {
            workMgr = args[1];
            poolMgr = args[2];
            testrigName = args[3];
            testrigZipfileName = args[4];
            envName = args[5];
            envZipfileName = args[6];
            questionName = args[7];
            questionFilename = args[8];
            workerToAdd = args[9];
         }

         new BatchClient(workMgr, poolMgr, testrigName, testrigZipfileName,
               envName, envZipfileName, questionName, questionFilename,
               workerToAdd);
      }
      else {
         String workMgr = "localhost:" + CoordConsts.SVC_WORK_PORT;
         String poolMgr = "localhost:" + CoordConsts.SVC_POOL_PORT;

         if (args.length == 3) {
            workMgr = args[1];
            poolMgr = args[2];
         }

         new InteractiveClient(workMgr, poolMgr);
      }
   }
}