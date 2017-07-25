package org.batfish.graphviz;

import java.nio.file.Path;
import java.util.Map;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BatfishLogger.BatfishLoggerHistory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.GraphvizAnswerElement;
import org.batfish.job.BatfishJobResult;

public final class GraphvizResult
    extends BatfishJobResult<Map<Path, byte[]>, GraphvizAnswerElement> {

  private final byte[] _graphBytes;

  private final Path _graphFile;

  private final byte[] _htmlBytes;

  private final Path _htmlFile;

  private final Prefix _prefix;

  private final byte[] _svgBytes;

  private final Path _svgFile;

  public GraphvizResult(
      long elapsedTime,
      BatfishLoggerHistory history,
      Path graphFile,
      byte[] graphBytes,
      Path svgFile,
      byte[] svgBytes,
      Path htmlFile,
      byte[] htmlBytes,
      Prefix prefix) {
    super(elapsedTime, history);
    _graphBytes = graphBytes;
    _graphFile = graphFile;
    _htmlBytes = htmlBytes;
    _htmlFile = htmlFile;
    _prefix = prefix;
    _svgBytes = svgBytes;
    _svgFile = svgFile;
  }

  public GraphvizResult(
      long elapsedTime, BatfishLoggerHistory history, Prefix prefix, Throwable failureCause) {
    super(elapsedTime, history, failureCause);
    _graphBytes = null;
    _graphFile = null;
    _htmlBytes = null;
    _htmlFile = null;
    _prefix = prefix;
    _svgBytes = null;
    _svgFile = null;
  }

  @Override
  public void appendHistory(BatfishLogger logger) {
    logger.append(_history);
  }

  @Override
  public void applyTo(
      Map<Path, byte[]> output, BatfishLogger logger, GraphvizAnswerElement answerElement) {
    output.put(_graphFile, _graphBytes);
    output.put(_svgFile, _svgBytes);
    output.put(_htmlFile, _htmlBytes);
  }

  @Override
  public String toString() {
    return "<Computed graph for prefix: " + _prefix.toString() + ">";
  }
}
