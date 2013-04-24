package org.autoTdd.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.autoTdd.IEngineStrategy;
import org.autoTdd.builder.internal.Node;
import org.autoTdd.engine.IEngineAsTree;
import org.autoTdd.engine.internal.Engine;
import org.autoTdd.internal.Constraint;
import org.autoTdd.internal.EngineSpecification;
import org.softwarefm.utilities.collections.Sets;
import org.softwarefm.utilities.maps.Maps;
import org.softwarefm.utilities.strings.Strings;

public class AsciiArtBuilder {

	private final static String ifKeyword = "if";
	private final static String thenKeyword = "then";
	private final static String elseKeyword = "else";
	private final static Set<String> keywords = Sets.makeSet(ifKeyword, thenKeyword, elseKeyword);

	private Map<String, Constraint> becauseMap = new HashMap<String, Constraint>();
	private Map<String, Object> resultMap = new HashMap<String, Object>();
	private Map<String, Object> inputMap = new HashMap<String, Object>();

	public void registerInput(String inputString, Object input) {
		Maps.putNoDuplicates(inputMap, inputString, input);
	}

	public void registerBecause(String becauseString, Object becauseObject) {
		Maps.putNoDuplicates(becauseMap, becauseString, new Constraint(null, becauseObject, new Object[0]));
	}

	public void registerResult(String string, Object resultObject) {
		Maps.putNoDuplicates(resultMap, string, resultObject);

	}

	class Context {
		List<String>words;
		AtomicInteger index = new AtomicInteger();
		Node thisNode;
		Node lastMatch;
		Object defaultOutput;

		private Context(List<String> words, AtomicInteger index, Node thisNode, Node lastMatch, Object defaultOutput) {
			this.words = words;
			this.index = index;
			this.thisNode = thisNode;
			this.lastMatch = lastMatch;
			this.defaultOutput = defaultOutput;
		}

		public Context(String words) {
			this.words = Strings.splitIgnoreBlanks(words, "\\s|\\/|,");
		}

		void assertToken(String token) {
			String thisWord = peekWord();
			if (!thisWord.equals(token))
				errorMessage("Expected " + token);
			eatWord();
		}

		String nextWord() {
			return words.get(index.getAndIncrement());
		}

		void eatWord() {
			index.getAndIncrement();
		}

		String peekWord() {
			return words.get(index.get());
		}

		boolean isPeekWordAKeyword() {
			return keywords.contains(peekWord());
		}

		Object nextWordAsResult() {
			String resultWord = nextWord();
			Object result = Maps.getOrException(resultMap, resultWord);
			return result;
		}

		void errorMessage(String string) {
			throw new IllegalArgumentException(string + " at word " + index + " in " + Arrays.asList(words));
		}

		public Context withThisNode(Node thisNode) {
			return new Context(words, index, thisNode, lastMatch, defaultOutput);
		}

		public Context withLastMatch(Node lastMatch) {
			return new Context(words, index, thisNode, lastMatch, defaultOutput);
		}

		public Context withDefaultOutput(Object defaultOutput) {
			return new Context(words, index, thisNode, lastMatch, defaultOutput);
		}

		public Context makeNode(Constraint constraint) {
			return withThisNode(new Node(constraint, lastMatch, null, null));
		}

		public boolean isPeekToken(String string) {
			return peekWord().equals(string);
		}
	}

	public IEngineAsTree build(String string) {
		IEngineAsTree tree = build(string, new NullEngineStrategy(), Object.class);
		return tree;
	}
	public IEngineAsTree build(String string, IEngineStrategy engineStrategy, Class<?> resultClass, Class<?>... parameters) {
		Context context = new Context(string);
		final Context root = parseIf(context);
		return new Engine(new EngineSpecification(resultClass, parameters, engineStrategy, root.defaultOutput), root.thisNode);
	}

	private Context parseIf(Context context) {
		context.assertToken("if");
		Constraint constraint = Maps.getOrException(becauseMap, context.nextWord());
		Context thisContext = context.makeNode(constraint);
		positiveClause(thisContext.withLastMatch(thisContext.thisNode));
		Context negativeContext = negativeClause(thisContext);
		return thisContext.withDefaultOutput(negativeContext.defaultOutput);

	}

	private void positiveClause(Context context) {
		parseAnyInputs(context);
		if (context.isPeekToken("if")) {
			Context match = parseIf(context);
			context.thisNode.match = match.thisNode;
			context.thisNode.constraint = Constraint.withResult(context.thisNode.constraint, match.defaultOutput);
		} else {
			parseThen(context);
		}
	}

	private void parseThen(Context context) {
		context.assertToken("then");
		Object result = context.nextWordAsResult();
		context.thisNode.constraint = Constraint.withResult(context.thisNode.constraint, result);
	}

	private Context negativeClause(Context context) {
		context.assertToken("else");
		if (context.isPeekToken("if")) {
			Context negativeContext = parseIf(context);
			context.thisNode.noMatch = negativeContext.thisNode;
			return negativeContext.withDefaultOutput(negativeContext.defaultOutput);
		} else {
			Object result = context.nextWordAsResult();
			return context.withDefaultOutput(result);
		}
	}

	private void parseAnyInputs(Context context) {
		List<Object> inputs = new ArrayList<Object>();
		while (!context.isPeekWordAKeyword()) {
			String inputString = context.nextWord();
			Object input = Maps.getOrException(inputMap, inputString);
			inputs.add(input);
		}
		context.thisNode.constraint = Constraint.withInputs(context.thisNode.constraint, inputs.toArray());

	}

}
