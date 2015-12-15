package org.batfish.graphviz;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.batfish.common.BatfishException;
import org.batfish.job.BatfishJob;
import org.batfish.representation.Prefix;

public class GraphvizJob extends BatfishJob<GraphvizResult> {

   private static final String GRAPHVIZ_COMMAND = "sfdp";

   private final String _graphFile;

   private final GraphvizInput _input;

   private final Prefix _prefix;

   private final String _svgFile;

   public GraphvizJob(GraphvizInput input, String graphFile, String svgFile,
         Prefix prefix) {
      _input = input;
      _graphFile = graphFile;
      _svgFile = svgFile;
      _prefix = prefix;
   }

   @Override
   public GraphvizResult call() throws Exception {
      long startTime = System.currentTimeMillis();
      long elapsedTime;
      byte[] graphBytes;
      try {
         graphBytes = _input.toString().getBytes("UTF-8");
      }
      catch (UnsupportedEncodingException e) {
         elapsedTime = System.currentTimeMillis() - startTime;
         return new GraphvizResult(elapsedTime, _graphFile, _svgFile, _prefix,
               new BatfishException("Failed to convert dot input to bytes", e));
      }
      DefaultExecutor executor = new DefaultExecutor();
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      ByteArrayOutputStream errStream = new ByteArrayOutputStream();
      ByteArrayInputStream inStream = new ByteArrayInputStream(graphBytes);
      executor.setStreamHandler(new PumpStreamHandler(outStream, errStream,
            inStream));
      executor.setExitValue(0);
      CommandLine cmdLine = new CommandLine(GRAPHVIZ_COMMAND);
      cmdLine.addArgument("-Tsvg");
      StringBuilder cmdLineSb = new StringBuilder();
      cmdLineSb.append(GRAPHVIZ_COMMAND + " ");
      cmdLineSb.append(org.batfish.common.Util.joinStrings(" ",
            cmdLine.getArguments()));
      String cmdLineString = cmdLineSb.toString();
      boolean failure = false;
      try {
         executor.execute(cmdLine);
      }
      catch (ExecuteException e) {
         failure = true;
      }
      catch (IOException e) {
         elapsedTime = System.currentTimeMillis() - startTime;
         return new GraphvizResult(elapsedTime, _graphFile, _svgFile, _prefix,
               new BatfishException("Unknown error running graphviz", e));
      }
      byte[] svgBytes = outStream.toByteArray();
      byte[] errRaw = errStream.toByteArray();
      String err = null;
      try {
         err = new String(errRaw, "UTF-8");
      }
      catch (IOException e) {
         elapsedTime = System.currentTimeMillis() - startTime;
         return new GraphvizResult(elapsedTime, _graphFile, _svgFile, _prefix,
               new BatfishException("Error reading nxnet output", e));
      }
      StringBuilder sb = new StringBuilder();
      if (failure) {
         sb.append("graphviz terminated abnormally:\n");
         sb.append("graphviz command line: " + cmdLineString + "\n");
         sb.append(err);
         elapsedTime = System.currentTimeMillis() - startTime;
         return new GraphvizResult(elapsedTime, _graphFile, _svgFile, _prefix,
               new BatfishException(sb.toString()));
      }
      else {
         elapsedTime = System.currentTimeMillis() - startTime;
         return new GraphvizResult(elapsedTime, _graphFile, graphBytes,
               _svgFile, svgBytes, _prefix);
      }
   }

   public GraphvizInput getInput() {
      return _input;
   }

}
