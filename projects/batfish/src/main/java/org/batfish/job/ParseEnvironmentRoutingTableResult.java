package org.batfish.job;

import java.nio.file.Path;
import java.util.SortedMap;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BatfishLogger.BatfishLoggerHistory;
import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ParseEnvironmentRoutingTablesAnswerElement;
import org.batfish.datamodel.answers.ParseStatus;
import org.batfish.datamodel.collections.RoutesByVrf;

public class ParseEnvironmentRoutingTableResult
    extends BatfishJobResult<
        SortedMap<String, RoutesByVrf>, ParseEnvironmentRoutingTablesAnswerElement> {

  private final Path _file;

  private String _name;

  private ParseTreeSentences _parseTree;

  private RoutesByVrf _routingTable;

  private final ParseStatus _status;

  private Warnings _warnings;

  public ParseEnvironmentRoutingTableResult(
      long elapsedTime,
      BatfishLoggerHistory history,
      Path file,
      String name,
      RoutesByVrf routingTable,
      Warnings warnings,
      ParseTreeSentences parseTree) {
    super(elapsedTime, history);
    _file = file;
    _parseTree = parseTree;
    _name = name;
    _routingTable = routingTable;
    _warnings = warnings;
    // parse status is determined from other fields
    _status = null;
  }

  public ParseEnvironmentRoutingTableResult(
      long elapsedTime, BatfishLoggerHistory history, Path file, Throwable failureCause) {
    super(elapsedTime, history, failureCause);
    _file = file;
    _status = ParseStatus.FAILED;
  }

  public ParseEnvironmentRoutingTableResult(
      long elapsedTime,
      BatfishLoggerHistory history,
      Path file,
      Warnings warnings,
      ParseStatus status) {
    super(elapsedTime, history);
    _file = file;
    _status = status;
    _warnings = warnings;
  }

  @Override
  public void appendHistory(BatfishLogger logger) {
    String terseLogLevelPrefix;
    if (logger.isActive(BatfishLogger.LEVEL_INFO)) {
      terseLogLevelPrefix = "";
    } else if (_routingTable != null) {
      terseLogLevelPrefix = _name + ": ";
    } else {
      terseLogLevelPrefix = _file + ": ";
    }
    logger.append(_history, terseLogLevelPrefix);
  }

  @Override
  public void applyTo(
      SortedMap<String, RoutesByVrf> routingTables,
      BatfishLogger logger,
      ParseEnvironmentRoutingTablesAnswerElement answerElement) {
    appendHistory(logger);
    String filename = _file.getFileName().toString();
    if (_routingTable != null) {
      String hostname = _name;
      if (routingTables.containsKey(hostname)) {
        throw new BatfishException("Duplicate hostname: " + hostname);
      } else {
        routingTables.put(hostname, _routingTable);
        if (!_warnings.isEmpty()) {
          answerElement.getWarnings().put(hostname, _warnings);
        }
        if (!_parseTree.isEmpty()) {
          answerElement.getParseTrees().put(hostname, _parseTree);
        }
        if (_routingTable.getUnrecognized()) {
          answerElement.getParseStatus().put(filename, ParseStatus.PARTIALLY_UNRECOGNIZED);
        } else {
          answerElement.getParseStatus().put(filename, ParseStatus.PASSED);
        }
      }
    } else {
      answerElement.getParseStatus().put(filename, _status);
      if (_status == ParseStatus.FAILED) {
        answerElement
            .getErrors()
            .put(filename, ((BatfishException) _failureCause).getBatfishStackTrace());
      }
    }
  }

  public Path getFile() {
    return _file;
  }

  @Override
  public BatfishLoggerHistory getHistory() {
    return _history;
  }

  public RoutesByVrf getRoutingTable() {
    return _routingTable;
  }

  @Override
  public String toString() {
    if (_routingTable == null) {
      return "<EMPTY OR UNSUPPORTED FORMAT>";
    } else {
      return "<" + _name + ">";
    }
  }
}
