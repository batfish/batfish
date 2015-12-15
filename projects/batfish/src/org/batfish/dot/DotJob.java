package org.batfish.dot;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;

public class DotJob {

   private static final String DOT_COMMAND = "dot";

   private DotInput _input;

   private final BatfishLogger _logger;

   public DotJob(BatfishLogger logger) {
      _logger = logger;
   }

   public DotInput getInput() {
      return _input;
   }

   public void setInput(DotInput input) {
      _input = input;
   }

   public void writeSvg(File inputFile, File outputFile) {
      byte[] inBytes;
      try {
         inBytes = _input.toString().getBytes("UTF-8");
      }
      catch (UnsupportedEncodingException e) {
         throw new BatfishException("Failed to convert dot input to bytes", e);
      }
      DefaultExecutor executor = new DefaultExecutor();
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      ByteArrayOutputStream errStream = new ByteArrayOutputStream();
      ByteArrayInputStream inStream = new ByteArrayInputStream(inBytes);
      executor.setStreamHandler(new PumpStreamHandler(outStream, errStream,
            inStream));
      executor.setExitValue(0);
      CommandLine cmdLine = new CommandLine(DOT_COMMAND);
      cmdLine.addArgument("-Tsvg");
      StringBuilder cmdLineSb = new StringBuilder();
      cmdLineSb.append(DOT_COMMAND + " ");
      cmdLineSb.append(org.batfish.common.Util.joinStrings(" ",
            cmdLine.getArguments()));
      String cmdLineString = cmdLineSb.toString();
      boolean failure = false;
      _logger.info("Command line: " + cmdLineString + " \n");
      try {
         executor.execute(cmdLine);
      }
      catch (ExecuteException e) {
         failure = true;
      }
      catch (IOException e) {
         throw new BatfishException("Unknown error running dot", e);
      }
      finally {
         byte[] outRaw = outStream.toByteArray();
         byte[] errRaw = errStream.toByteArray();
         String err = null;
         try {
            err = new String(errRaw, "UTF-8");
         }
         catch (IOException e) {
            throw new BatfishException("Error reading nxnet output", e);
         }
         StringBuilder sb = new StringBuilder();
         if (failure) {
            sb.append("dot terminated abnormally:\n");
            sb.append("dot command line: " + cmdLine.toString() + "\n");
            sb.append(err);
            throw new BatfishException(sb.toString());
         }
         else {
            sb.append("writing svg to \"" + outputFile.toString() + "\"\n");
            try {
               FileUtils.writeByteArrayToFile(inputFile, inBytes);
               FileUtils.writeByteArrayToFile(outputFile, outRaw);
            }
            catch (IOException e) {
               throw new BatfishException("Failed to write dot output to file",
                     e);
            }
            _logger.debug(sb.toString());
            _logger.info("dot completed successfully\n");
         }
      }
   }

}
