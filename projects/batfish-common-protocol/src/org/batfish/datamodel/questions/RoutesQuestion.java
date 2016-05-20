package org.batfish.datamodel.questions;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RoutesQuestion extends Question {

	private static final String NODE_REGEX_VAR = "nodeRegex";

	private String _nodeRegex;

	public RoutesQuestion() {
		super(QuestionType.ROUTES);
	}

	@Override
	public boolean getDataPlane() {
		return true;
	}

	@Override
	public boolean getDifferential() {
		return false;
	}

	@JsonProperty(NODE_REGEX_VAR)
	public String getNodeRegex() {
		return _nodeRegex;
	}

	@Override
	public boolean getTraffic() {
		return false;
	}

	public void setNodeRegex(String nodeRegex) {
		_nodeRegex = nodeRegex;
	}

}
