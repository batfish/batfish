#!/usr/bin/env python3
import re
from glob import glob
from json import load
from os import pardir, path

import pytest

REPO = path.abspath(path.join(path.dirname(__file__), pardir, pardir))
QUESTIONS = glob(REPO + '/questions*/**/*.json', recursive=True)


@pytest.fixture(scope='module', params=QUESTIONS)
def question_path(request):
    yield path.relpath(request.param, REPO)


@pytest.fixture(scope='module')
def question_text(question_path):
    with open(path.join(REPO, question_path), 'r') as qfile:
        return qfile.read()


@pytest.fixture(scope='module')
def question(question_path):
    with open(path.join(REPO, question_path), 'r') as qfile:
        q = load(qfile)  # Throws if the question is not valid JSON.

    assert 'class' in q
    assert 'instance' in q
    return q


def test_name_and_filename_match(question_path, question):
    """Tests that all questions have filenames that match their instance name."""
    assert 'instanceName' in question['instance']
    name = question['instance']['instanceName']
    filename = path.splitext(path.basename(question_path))[0]
    assert name == filename


def test_instance_vars_present(question, question_text):
    """Tests that all questions with instance variables use those variables."""
    instance = question['instance']
    for v in instance.get('variables', {}):
        v_pattern = '${' + v + '}'
        assert v_pattern in question_text


def test_types(question):
    """Tests (partially) that instance variable properties have the correct types."""
    instance = question['instance']
    for name, data in instance.get('variables', {}).items():
        assert 'optional' not in data or isinstance(data['optional'], bool)
        if data.get('type') == 'boolean':
            assert 'value' not in data or isinstance(data['value'], bool)
        elif data.get('type') in ['integer', 'long']:
            assert 'value' not in data or isinstance(data['value'], int)


def test_indented_with_spaces(question_text):
    """Tests that JSON is indented with spaces, and not tabs."""
    pattern = re.compile(r"\t+")
    for line in question_text:
        if re.search(pattern, line) is not None:
            raise ValueError(
                "Found tab indentation in question {}. Please run \"sed -i '' 's/\\\\t/    /g' {}\" to switch to spaces.".format(
                    question, path.join(REPO, question)))


if __name__ == '__main__':
    pytest.main()
