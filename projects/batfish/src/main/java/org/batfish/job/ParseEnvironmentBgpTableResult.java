package org.batfish.job;

import java.nio.file.Path;
import java.util.SortedMap;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BatfishLogger.BatfishLoggerHistory;
import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ParseEnvironmentBgpTablesAnswerElement;
import org.batfish.datamodel.answers.ParseStatus;
import org.batfish.datamodel.collections.BgpAdvertisementsByVrf;

public class ParseEnvironmentBgpTableResult
    extends BatfishJobResult<
        SortedMap<String, BgpAdvertisementsByVrf>, ParseEnvironmentBgpTablesAnswerElement> {

  private BgpAdvertisementsByVrf _bgpTable;

  private final Path _file;

  private String _name;

  private ParseTreeSentences _parseTree;

  private final ParseStatus _status;

  private Warnings _warnings;

  public ParseEnvironmentBgpTableResult(
      long elapsedTime,
      BatfishLoggerHistory history,
      Path file,
      String name,
      BgpAdvertisementsByVrf bgpTable,
      Warnings warnings,
      ParseTreeSentences parseTree) {
    super(elapsedTime, history);
    _file = file;
    _parseTree = parseTree;
    _name = name;
    _bgpTable = bgpTable;
    _warnings = warnings;
    // parse status is determined from other fields
    _status = null;
  }

  public ParseEnvironmentBgpTableResult(
      long elapsedTime, BatfishLoggerHistory history, Path file, Throwable failureCause) {
    super(elapsedTime, history, failureCause);
    _file = file;
    _status = ParseStatus.FAILED;
  }

  public ParseEnvironmentBgpTableResult(
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
    } else if (_bgpTable != null) {
      terseLogLevelPrefix = _name + ": ";
    } else {
      terseLogLevelPrefix = _file + ": ";
    }
    logger.append(_history, terseLogLevelPrefix);
  }

  @Override
  public void applyTo(
      SortedMap<String, BgpAdvertisementsByVrf> bgpTables,
      BatfishLogger logger,
      ParseEnvironmentBgpTablesAnswerElement answerElement) {
    appendHistory(logger);
    String filename = _file.getFileName().toString();
    if (_bgpTable != null) {
      String hostname = _name;
      if (bgpTables.containsKey(hostname)) {
        throw new BatfishException("Duplicate hostname: " + hostname);
      } else {
        bgpTables.put(hostname, _bgpTable);
        if (!_warnings.isEmpty()) {
          answerElement.getWarnings().put(hostname, _warnings);
        }
        if (!_parseTree.isEmpty()) {
          answerElement.getParseTrees().put(hostname, _parseTree);
        }
        if (_bgpTable.getUnrecognized()) {
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

  public BgpAdvertisementsByVrf getBgpTable() {
    return _bgpTable;
  }

  public Path getFile() {
    return _file;
  }

  @Override
  public BatfishLoggerHistory getHistory() {
    return _history;
  }

  @Override
  public String toString() {
    if (_bgpTable == null) {
      return "<EMPTY OR UNSUPPORTED FORMAT>";
    } else {
      return "<" + _name + ">";
    }
  }
}
