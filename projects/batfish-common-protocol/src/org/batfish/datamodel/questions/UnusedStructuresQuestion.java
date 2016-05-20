package org.batfish.datamodel.questions;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UnusedStructuresQuestion extends Question {

	private static final String NODE_REGEX_VAR = "nodeRegex";

	private String _nodeRegex;

	public UnusedStructuresQuestion() {
		super(QuestionType.UNUSED_STRUCTURES);
	}

	@Override
	public boolean getDataPlane() {
		return false;
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
