package org.batfish.job;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.base.Throwables;
import java.nio.file.Paths;
import java.util.SortedMap;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BatfishLogger.BatfishLoggerHistory;
import org.batfish.common.ErrorDetails;
import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ParseEnvironmentBgpTablesAnswerElement;
import org.batfish.datamodel.answers.ParseStatus;
import org.batfish.datamodel.collections.BgpAdvertisementsByVrf;

public class ParseEnvironmentBgpTableResult
    extends BatfishJobResult<
        SortedMap<String, BgpAdvertisementsByVrf>, ParseEnvironmentBgpTablesAnswerElement> {

  private BgpAdvertisementsByVrf _bgpTable;

  private final String _key;

  private String _name;

  private ParseTreeSentences _parseTree;

  private final ParseStatus _status;

  private Warnings _warnings;

  public ParseEnvironmentBgpTableResult(
      long elapsedTime,
      BatfishLoggerHistory history,
      String key,
      String name,
      BgpAdvertisementsByVrf bgpTable,
      Warnings warnings,
      ParseTreeSentences parseTree) {
    super(elapsedTime, history);
    _key = key;
    _parseTree = parseTree;
    _name = name;
    _bgpTable = bgpTable;
    _warnings = warnings;
    // parse status is determined from other fields
    _status = null;
  }

  public ParseEnvironmentBgpTableResult(
      long elapsedTime, BatfishLoggerHistory history, String key, Throwable failureCause) {
    super(elapsedTime, history, failureCause);
    _key = key;
    _status = ParseStatus.FAILED;
  }

  public ParseEnvironmentBgpTableResult(
      long elapsedTime,
      BatfishLoggerHistory history,
      String key,
      Warnings warnings,
      ParseStatus status) {
    super(elapsedTime, history);
    _key = key;
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
      terseLogLevelPrefix = _key + ": ";
    }
    logger.append(_history, terseLogLevelPrefix);
  }

  @Override
  public void applyTo(
      SortedMap<String, BgpAdvertisementsByVrf> bgpTables,
      BatfishLogger logger,
      ParseEnvironmentBgpTablesAnswerElement answerElement) {
    appendHistory(logger);
    String filename = Paths.get(_key).getFileName().toString();
    if (_bgpTable != null) {
      String hostname = _name;
      if (bgpTables.containsKey(hostname)) {
        throw new BatfishException("Duplicate hostname: " + hostname);
      } else {
        bgpTables.put(hostname, _bgpTable);
        if (!_warnings.isEmpty()) {
          answerElement.getWarnings().put(filename, _warnings);
        }
        if (!_parseTree.isEmpty()) {
          answerElement.getParseTrees().put(filename, _parseTree);
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
        answerElement
            .getErrorDetails()
            .put(
                filename,
                new ErrorDetails(
                    Throwables.getStackTraceAsString(
                        firstNonNull(_failureCause.getCause(), _failureCause))));
      }
    }
  }

  public BgpAdvertisementsByVrf getBgpTable() {
    return _bgpTable;
  }

  public String getKey() {
    return _key;
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
