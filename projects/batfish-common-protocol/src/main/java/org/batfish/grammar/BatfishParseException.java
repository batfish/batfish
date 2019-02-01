package org.batfish.grammar;

import org.batfish.common.BatfishException;

public class BatfishParseException extends BatfishException {

  private static final long serialVersionUID = 1L;

  private final Integer _line;

  private final String _lineText;

  /**
   * Constructs a BatfishParseException with a detail message, line number, and line text
   *
   * @param msg The detail message
   * @param line The line number where the exception occurred
   * @param lineText The text of the line where the exception occurred
   */
  public BatfishParseException(String msg, Integer line, String lineText) {
    super(msg);
    _line = line;
    _lineText = lineText;
  }

  /**
   * Constructs a BatfishParseException with a detail message, cause, line number, and line text
   *
   * @param msg The detail message
   * @param cause The cause of this exception
   * @param line The line number where the exception occurred
   * @param lineText The text of the line where the exception occurred
   */
  public BatfishParseException(String msg, Throwable cause, Integer line, String lineText) {
    super(msg, cause);
    _line = line;
    _lineText = lineText;
  }

  public Integer getLine() {
    return _line;
  }

  public String getLineText() {
    return _lineText;
  }
}
