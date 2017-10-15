package org.batfish.graphviz;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.batfish.common.BatfishException;
import org.batfish.config.Settings;
import org.batfish.datamodel.Prefix;
import org.batfish.job.BatfishJob;

public class GraphvizJob extends BatfishJob<GraphvizResult> {

  private static final String GRAPHVIZ_COMMAND = "sfdp";

  private final Path _graphFile;

  private final Path _htmlFile;

  private final GraphvizInput _input;

  private final Prefix _prefix;

  private final Path _svgFile;

  public GraphvizJob(
      Settings settings,
      GraphvizInput input,
      Path graphFile,
      Path svgFile,
      Path htmlFile,
      Prefix prefix) {
    super(settings);
    _input = input;
    _graphFile = graphFile;
    _htmlFile = htmlFile;
    _svgFile = svgFile;
    _prefix = prefix;
  }

  @Override
  public GraphvizResult callBatfishJob() {
    long startTime = System.currentTimeMillis();
    long elapsedTime;
    Throwable failureCause = null;
    byte[] graphBytes = null;
    byte[] htmlBytes = null;
    byte[] svgBytes = null;
    try {
      graphBytes = computeGraph();
      byte[] mapBytes = computeMap(graphBytes);
      htmlBytes = computeHtml(mapBytes);
      svgBytes = computeSvg(graphBytes);
    } catch (BatfishException e) {
      failureCause = e;
    }
    elapsedTime = System.currentTimeMillis() - startTime;
    if (failureCause != null) {
      return new GraphvizResult(elapsedTime, _logger.getHistory(), _prefix, failureCause);
    } else {
      return new GraphvizResult(
          elapsedTime,
          _logger.getHistory(),
          _graphFile,
          graphBytes,
          _svgFile,
          svgBytes,
          _htmlFile,
          htmlBytes,
          _prefix);
    }
  }

  private byte[] computeGraph() {
    byte[] graphBytes = null;
    try {
      graphBytes = _input.toString().getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new BatfishException("Failed to convert graphviz input to bytes", e);
    }
    return graphBytes;
  }

  private byte[] computeHtml(byte[] mapBytes) {
    String mapText = null;
    try {
      mapText = new String(mapBytes, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new BatfishException("Could not convert map bytes to string", e);
    }
    String graphName = GraphvizDigraph.getGraphName(_prefix);
    StringBuilder sb = new StringBuilder();
    sb.append("<!DOCTYPE html>\n");
    sb.append("<html>\n");
    sb.append("<head>\n");
    sb.append("<script>\n");
    sb.append(
        "window.onload = function() { window.scrollTo( (window.scrollMaxX)/2, "
            + "(window.scrollMaxY)/2 ); }\n");
    sb.append("</script>\n");
    sb.append("</head>\n");
    sb.append("<body>\n");
    sb.append("<img src=\"../svg/" + graphName + ".svg\" usemap=\"#" + graphName + "\" />\n");
    sb.append(mapText);
    sb.append("</body>\n");
    sb.append("</html>\n");
    byte[] htmlBytes = null;
    try {
      htmlBytes = sb.toString().getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new BatfishException("Could not convert map bytes to string", e);
    }
    return htmlBytes;
  }

  private byte[] computeMap(byte[] graphBytes) {
    DefaultExecutor executor = new DefaultExecutor();
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    ByteArrayOutputStream errStream = new ByteArrayOutputStream();
    ByteArrayInputStream inStream = new ByteArrayInputStream(graphBytes);
    executor.setStreamHandler(new PumpStreamHandler(outStream, errStream, inStream));
    executor.setExitValue(0);
    CommandLine cmdLine = new CommandLine(GRAPHVIZ_COMMAND);
    cmdLine.addArgument("-Tcmapx");
    StringBuilder cmdLineSb = new StringBuilder();
    cmdLineSb.append(GRAPHVIZ_COMMAND + " ");
    cmdLineSb.append(String.join(" ", cmdLine.getArguments()));
    String cmdLineString = cmdLineSb.toString();
    boolean failure = false;
    try {
      executor.execute(cmdLine);
    } catch (ExecuteException e) {
      failure = true;
    } catch (IOException e) {
      throw new BatfishException("Unknown error running graphviz", e);
    }
    byte[] mapBytes = outStream.toByteArray();
    byte[] errRaw = errStream.toByteArray();
    String err = null;
    try {
      err = new String(errRaw, "UTF-8");
    } catch (IOException e) {
      throw new BatfishException("Error reading nxnet output", e);
    }
    StringBuilder sb = new StringBuilder();
    if (failure) {
      sb.append("graphviz terminated abnormally:\n");
      sb.append("graphviz command line: " + cmdLineString + "\n");
      sb.append(err);
      throw new BatfishException(sb.toString());
    } else {
      return mapBytes;
    }
  }

  private byte[] computeSvg(byte[] graphBytes) {
    DefaultExecutor executor = new DefaultExecutor();
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    ByteArrayOutputStream errStream = new ByteArrayOutputStream();
    ByteArrayInputStream inStream = new ByteArrayInputStream(graphBytes);
    executor.setStreamHandler(new PumpStreamHandler(outStream, errStream, inStream));
    executor.setExitValue(0);
    CommandLine cmdLine = new CommandLine(GRAPHVIZ_COMMAND);
    cmdLine.addArgument("-Tsvg");
    StringBuilder cmdLineSb = new StringBuilder();
    cmdLineSb.append(GRAPHVIZ_COMMAND + " ");
    cmdLineSb.append(String.join(" ", cmdLine.getArguments()));
    String cmdLineString = cmdLineSb.toString();
    boolean failure = false;
    try {
      executor.execute(cmdLine);
    } catch (ExecuteException e) {
      failure = true;
    } catch (IOException e) {
      throw new BatfishException("Unknown error running graphviz", e);
    }
    byte[] svgBytes = outStream.toByteArray();
    byte[] errRaw = errStream.toByteArray();
    String err = null;
    try {
      err = new String(errRaw, "UTF-8");
    } catch (IOException e) {
      throw new BatfishException("Error reading nxnet output", e);
    }
    StringBuilder sb = new StringBuilder();
    if (failure) {
      sb.append("graphviz terminated abnormally:\n");
      sb.append("graphviz command line: " + cmdLineString + "\n");
      sb.append(err);
      throw new BatfishException(sb.toString());
    } else {
      return svgBytes;
    }
  }

  public GraphvizInput getInput() {
    return _input;
  }
}
