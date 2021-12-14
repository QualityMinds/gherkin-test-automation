package de.qualityminds.gta.driver.utils.reflection.expressions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import lombok.AllArgsConstructor;
import lombok.Data;

import de.qualityminds.gta.driver.utils.exceptions.ResolveException;
import de.qualityminds.gta.driver.utils.reflection.ObjectInspectionUtil;
import de.qualityminds.gta.driver.utils.reflection.setfield.override.FieldAliases;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ExpressionUtil extends TestParameters {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final Pattern PATTERN_ACCESS_CHILD = Pattern.compile("^([^\\.\\(]+(?:\\(.*\\))?)\\.(.+[^\\.])$");
	public static final Pattern PATTERN_METHOD_CALL = Pattern.compile("(.+)\\((.*)\\)$");
	public static final Pattern PATTERN_METHOD_CALL_AFTER_CHILD_ACCESS = Pattern.compile("(.+)\\.(.+)\\((.*)\\)$");
	public static final Pattern PATTERN_METHOD_CALL_ADDTL_PARAMS = Pattern.compile("(.+)\\((.*)\\)(.*),(.*)$");
	public static final Pattern PATTERN_LIST_ACCESS = Pattern.compile("^([^\\[.]*)\\[([0-9]+)\\](.*)$");

	public static final Pattern PATTERN_TRAILING_SINGLEQUOTATIONS = Pattern.compile("^'(.*)'$", Pattern.DOTALL);

	@Autowired
	ObjectInspectionUtil objectInspectionUtil;

	@Autowired(required = false)
	@Lazy // circumvent ring dependency so ExpressionUtil can be used in GlobalGherkinMethodProvider classes
	private List<? extends GlobalGherkinMethodProvider> globalMethodProviders;

	@Override
	public void setParameter(String key, Object value) {
		Object resolved = value;
		try { // resolve possible expressions in value first
			ResolveResult result = resolveExpressions(value);
			if (result.isExpression() || result.getResolvedObject() instanceof String) {
				resolved = result.getResolvedObject();
			}
		} catch (ResolveException e) {
			throw new RuntimeException(e);
		}
		super.setParameter(key, resolved);
	}

	/**
	 * executes the resolveExpression(val, provider) with provider = null to search globally provided methods
	 *
	 * @param value usually a string that should be evaluated as an expression
	 * @return a ResolveResult if no exception occurred
	 */
	public ResolveResult resolveExpressions(Object value) throws ResolveException {
		return resolveExpressions(value, null);
	}

	/**
	 * @param value        usually a string that should be evaluated as an expression
	 * @param providingObj an object providing the expression to evaluate, or null to search globally
	 * @return a ResolveResult if no exception occurred
	 */
	public ResolveResult resolveExpressions(Object value, Object providingObj) throws ResolveException {
		if (value instanceof String) {
			String valueStr = (String) value;
			List<MatchResult> matches = new ArrayList<>();

			if (valueStr.contains("${") && valueStr.contains("}")) {
				String[] splits = splitGherkin(valueStr);
				splits = Arrays.stream(splits).filter(s -> s.length() > 0).toArray(String[]::new);

				int lastCheckedIndex = 0;
				for (String s : splits) {
					int start = valueStr.indexOf(s, lastCheckedIndex);
					lastCheckedIndex = start;
					int length = s.length();
					if (s.startsWith("${")) {
						matches.add(new MatchResult(length, start, start + length, s.substring(2, s.length() - 1)));
					}
				}

				return resolveAllExpressionsInString(valueStr, matches, providingObj);
			}
		}

		return new ResolveResult(false, null);
	}

	/**
	 * Splits input string whenever a gherkin expression begins or ends.
	 * New levels are everything between open and close chars
	 *
	 * @param s : string that is split
	 * @return array of partitions
	 */
	public String[] splitGherkin(String s) {
		String[] params = new String[]{""};
		int bracketDepth = 0;
		int paramIdx = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);

			if (c == '{') {
				bracketDepth++;
				params[paramIdx] += String.valueOf(c);
			} else if (c == '}') {
				bracketDepth--;
				params[paramIdx] += String.valueOf(c);

				if (bracketDepth == 0 && i != s.length() - 1 && s.charAt(i + 1) != '$') {
					paramIdx++;
					String[] enlargedParams = Arrays.copyOf(params, paramIdx + 1);
					enlargedParams[paramIdx] = "";
					params = enlargedParams;
				}
			} else if (c == '$') {
				if (bracketDepth == 0) {
					paramIdx++;
					String[] enlargedParams = Arrays.copyOf(params, paramIdx + 1);
					enlargedParams[paramIdx] = "";
					params = enlargedParams;
				}
				params[paramIdx] += String.valueOf(c);
			} else {
				params[paramIdx] += String.valueOf(c);
			}

		}
		return params;
	}

	private ResolveResult resolveAllExpressionsInString(String expressions, List<MatchResult> matches, Object providingObj)
			throws ResolveException {
		Object resolvedObject;
		if (isSingleExpression(expressions, matches)) {
			resolvedObject = resolveSingleExpression(matches.get(0).getMatchContent(), providingObj);
		} else { // concat
			resolvedObject = concatIntoResolvedObject(expressions, matches, providingObj);
		}
		return new ResolveResult(true, resolvedObject);
	}

	private Object concatIntoResolvedObject(String expressions, List<MatchResult> matches, Object providingObj)
			throws ResolveException {
		StringBuilder sb = new StringBuilder();
		Integer currentStringPosition = 0;
		for (MatchResult match : matches) {
			sb.append(expressions, currentStringPosition, match.getStart());
			sb.append(resolveSingleExpression(match.getMatchContent(), providingObj));
			currentStringPosition = match.getEnd();
		}
		sb.append(expressions.substring(currentStringPosition));
		return sb.toString();
	}

	private static boolean isSingleExpression(String expressions, List<MatchResult> matches) {
		return (matches.size() == 1) && (matches.get(0).getFullLength() == expressions.length());
	}

	private Object resolveSingleExpression(String expression, Object providingObj) throws ResolveException {
		if (StringUtils.isBlank(expression)) {
			throw new ResolveException("Could not resolve blank expression.");
		}

		Matcher accessChildMatcher = PATTERN_ACCESS_CHILD.matcher(expression);
		Matcher methodCall = PATTERN_METHOD_CALL.matcher(expression);
		Matcher methodCallWithParams = PATTERN_METHOD_CALL_ADDTL_PARAMS.matcher(expression);
		Matcher methodCallAfterChildAccess = PATTERN_METHOD_CALL_AFTER_CHILD_ACCESS.matcher(expression);
		Matcher listAccessMatcher = PATTERN_LIST_ACCESS.matcher(expression);

		String toResolveString;
		Object resolvedValue;
		String path;
		boolean isListAccess = listAccessMatcher.matches();
		boolean isChildAccess = accessChildMatcher.matches();
		boolean isMethodCallAfterChildAccess = methodCallAfterChildAccess.matches();
		boolean isSimpleMethodCall = methodCall.matches();
		boolean isMethodCallWithParams = methodCallWithParams.matches();

		if (!isListAccess && isChildAccess && (isMethodCallAfterChildAccess || (!isSimpleMethodCall && !isMethodCallWithParams))) {
			toResolveString = accessChildMatcher.group(1);
			resolvedValue = resolveMethodOrParameter(toResolveString, providingObj);
			path = accessChildMatcher.group(2);
		} else if (isListAccess) {
			toResolveString = listAccessMatcher.group(1);
			resolvedValue = resolveMethodOrParameter(toResolveString, providingObj);
			return resolvePath(resolvedValue, StringUtils.removeStart(expression, toResolveString));
		} else {
			return resolveMethodOrParameter(expression, providingObj);
		}
		return resolvePath(resolvedValue, path);
	}

	private Object resolveMethodOrParameter(String parameterOrMethodName, Object providingObject) throws ResolveException {
		if (isItMethodName(parameterOrMethodName)) { // the input follows the method regex so we assume it is a method
			String[] methodNameAndParams = getMethodNameAndParams(parameterOrMethodName);
			String methodName = methodNameAndParams[0];
			String methodParams = methodNameAndParams[1];
			Object[] params = constructParams(methodParams);
			if (providingObject != null) {
				// Invoke method on given object
				try {
					return invokeMethodOnObj(methodName, params, providingObject);
				} catch (NoSuchMethodException e) {
					// The object does not provide this method. Let's see if it's a global one.
				}
			}
			try {
				return invokeGlobalMethod(methodName, params);
			} catch (NoSuchMethodException e) {
				throw new ResolveException(getExceptionMessage(methodName, methodParams), e);
			}
		} else { // the input does not follow the method regex, so we assume it is a parameter
			if (hasParameter(parameterOrMethodName)) {
				return getParameterValue(parameterOrMethodName);
			} else {
				return invokeMethodInstead(parameterOrMethodName);
			}
		}
	}

	private boolean isItMethodName(String parameterOrMethodName) {
		Matcher methodCallMatcher = PATTERN_METHOD_CALL.matcher(parameterOrMethodName);
		return methodCallMatcher.matches();
	}

	private Object invokeMethodInstead(String parameterOrMethodName) throws ResolveException {
		if (properties.isFallBackFromUnknownParamToMethodEnabled()) {
			try {
				return invokeGlobalMethod(parameterOrMethodName, new Object[0]);
			} catch (NoSuchMethodException | ResolveException e) {
				throw new ResolveException("No such parameter or method: " + parameterOrMethodName);
			}
		}
		throw new ResolveException("No such parameter: " + parameterOrMethodName);
	}

	private String getExceptionMessage(String methodName, String methodParams) {
		if (StringUtils.isNotBlank(methodParams)) {
			return "No such method: " + methodName + " that supports the given parameters";
		} else {
			return "No such method: " + methodName;
		}
	}

	private Object invokeGlobalMethod(String methodName, Object[] methodParameters) throws NoSuchMethodException, ResolveException {

		for (GlobalGherkinMethodProvider globalMethodProviderClass : globalMethodProviders) {
			try {
				return invokeMethodOnObj(methodName, methodParameters, globalMethodProviderClass);
			} catch (NoSuchMethodException e) {
				// The current GlobalMethod class does not provide it. Just continue with the next.
			}
		}
		throw new NoSuchMethodException("No global method found with name " + methodName);
	}

	private Object invokeMethodOnObj(String methodName, Object[] methodParameters, Object obj)
			throws ResolveException, NoSuchMethodException {

		if (obj == null) {
			throw new NoSuchMethodException("Trying to invoke a method without object providing this method.");
		}
		try {
			return invokeMethodWithParams(methodName, obj, methodParameters);

		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.info("{}: {}", e.getClass().getSimpleName(), e.getMessage());
			throw new ResolveException(createExceptionMessage(e, methodName, methodParameters));
		}
	}

	private Object[] constructParams(String methodParameters) {
		Object[] params = new Object[0];
		if (methodParameters.isEmpty()) {
			return params;
		}
		Matcher methodCallMatcher = PATTERN_METHOD_CALL_ADDTL_PARAMS.matcher(methodParameters);
		if (methodCallMatcher.matches()) { // the parameterstring follows the method regex so we assume it contains a method
			params = getParams(methodParameters);
		} else if (StringUtils.isNotBlank(methodParameters)) {
			params = Arrays.stream(methodParameters.split(",")).map(String::trim).toArray();
		}
		for (int i = 0; i < params.length; i++) {
			Object paramValue = extractValueFromParam(params[i]);
			if (paramValue != null) {
				params[i] = paramValue;
			}
		}
		return params;
	}

	private Object extractValueFromParam(Object param) {
		if (param instanceof String) {
			String paramString = (String) param;
			if (!paramString.matches("\\d*(\\.\\d*)?")) {    // not just a number
				if (paramString.startsWith("\"")) {    //if it's a string for sure remove the quotes
					return paramString.substring(1, paramString.length() - 1);
				} else { // we try to resolve it!
					if (paramString.startsWith("${")) {
						//we need to remove the special chars around it
						paramString = paramString.substring(2, paramString.length() - 1);
					}
					return tryResolvingIt(paramString);
				}
			}
		}
		return null;
	}

	private Object invokeMethodWithParams(String methodName, Object obj, Object[] params)
			throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		try {
			Method method = objectInspectionUtil.getDeclaredMethod(obj.getClass(), methodName + "_" + params.length);
			return method.invoke(obj, params);
		} catch (NoSuchMethodException | IllegalAccessException e) {
			Method method = objectInspectionUtil.getDeclaredMethod(obj.getClass(), methodName + ObjectInspectionUtil.SUFFIX_VARARGS);
			Object[] paramsNeedNewArray = new Object[]{params};
			return method.invoke(obj, paramsNeedNewArray);
		}
	}

	private Object tryResolvingIt(String paramString) {
		try {
			Object r = resolveSingleExpression(paramString, null);
			if (r instanceof Integer) {
				return r.toString();
			} else {
				return r;
			}
		} catch (ResolveException ignored) {
			//Guess the param is just a string after all
		}
		return null;
	}

	private String createExceptionMessage(Exception e, String methodName, Object[] methodParameters) {
		String msg = e.getMessage();

		if (msg == null && e instanceof InvocationTargetException) {
			Throwable targetException = ((InvocationTargetException) e).getTargetException();
			if (targetException instanceof NullPointerException) {
				msg = "NullPointerException in method " + methodName;
			} else {
				msg = targetException.getMessage();
			}
		}
		return "Error while trying to invoke " + methodName + "(" + Arrays.stream(methodParameters).map(String::valueOf).collect(Collectors.joining(", ")) + "): \n" + msg;
	}

	public Object resolvePath(Object obj, String path) throws ResolveException {
		path = StringUtils.removeStart(path, ".");
		if (isPathBlankOrObjIncorrect(path, obj)) {
			return obj;
		}

		// Preparation: determine pathToResolveNow and remainingPath
		// if a child should be accessed as in foo.bar, resolve foo now and bar later
		Matcher matcherHasChildObject = PATTERN_ACCESS_CHILD.matcher(path);
		String pathToResolveNow = matcherHasChildObject.matches() ? matcherHasChildObject.group(1) : path;
		// a list access like foo[int1], first resolves list foo and then gets index int1
		Matcher matcherAccessListIndex = PATTERN_LIST_ACCESS.matcher(pathToResolveNow);

		String remainingPathFromListAccess = null;
		if (matcherAccessListIndex.matches()) { // foo[int1][int2]
			// resolve foo
			pathToResolveNow = matcherAccessListIndex.group(1);
			remainingPathFromListAccess = getRemainingPathFromListAccess(pathToResolveNow, matcherAccessListIndex);
		}

		String remainingPath = extractRemainingPath(matcherHasChildObject, remainingPathFromListAccess);

		// Resolve path on obj (access list index, resolve method, get map key, get object field)
		try {
			if (StringUtils.isBlank(pathToResolveNow)) {
				// no pathToResolveNow -> access List index [int1]
				return manageBlankPath(matcherAccessListIndex, obj, remainingPath);
			} else {
				// resolve method on path
				Matcher matcherMethodAccess = PATTERN_METHOD_CALL.matcher(pathToResolveNow);
				if (matcherMethodAccess.matches()) {
					return matcherMethodAccessMatches(obj, matcherMethodAccess, matcherHasChildObject);
				}
				if (obj instanceof String) {
					throw new ResolveException("Could not resolve a path on an object of type " + obj.getClass() + "\n(Value of String was \"" + obj + "\")");

				}
				// resolve pathToResolve...
				if (obj instanceof Map) { // ... in Map
					return objInstanceofMap(obj, path);
				} else { // ... in Object
					return objNotInstanceofMap(obj, pathToResolveNow, remainingPath);
				}
			}
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException e) {
			logger.info("{}: {}", e.getClass().getSimpleName(), e.getMessage());
			throw new ResolveException("Error while trying to resolve path: " + path + "\n" + e.getMessage(), e);
		}
	}

	private static boolean isPathBlankOrObjIncorrect(String path, Object obj) throws ResolveException {
		if (StringUtils.isBlank(path)) {
			return true;
		} else if (obj == null) {
			throw new ResolveException("Could not resolve a path on a NULL object.");
		} else if (obj instanceof Integer || obj instanceof Boolean) {
			throw new ResolveException("Could not resolve a path on an object of type " + obj.getClass());
		}
		return false;
	}

	private static String getRemainingPathFromListAccess(String pathToResolveNow, Matcher matcherAccessListIndex) {
		if (StringUtils.isBlank(pathToResolveNow)) {
			// no foo, will resolve [int1] this step
			return matcherAccessListIndex.group(3);
		} else {
			// add [int1] and [int2] to remaining path
			return "[" + matcherAccessListIndex.group(2) + "]"
					+ matcherAccessListIndex.group(3);
		}
	}

	private static String extractRemainingPath(Matcher matcherHasChildObject, String remainingPathFromListAccess) {
		String remainingPath = matcherHasChildObject.matches() ? matcherHasChildObject.group(2) : "";
		if (!StringUtils.isBlank(remainingPathFromListAccess)) {
			remainingPath = StringUtils.isBlank(remainingPath) ? remainingPathFromListAccess
					: remainingPathFromListAccess + "." + remainingPath;
		}
		return remainingPath;
	}

	private Object manageBlankPath(Matcher matcherAccessListIndex, Object nextObj, String remainingPath)
			throws ResolveException {
		int idx = Integer.parseInt(matcherAccessListIndex.group(2)) - properties.getListAccessOffset();
		List<?> list = (List<?>) nextObj;
		if (idx >= list.size()) {
			throw new ResolveException("Could not resolve element " + (idx + 1) + " in a list with " + list.size() + " element(s).");
		} else {
			nextObj = list.get(idx);
			return resolvePath(nextObj, remainingPath);
		}
	}

	private Object matcherMethodAccessMatches(Object obj, Matcher matcherMethodAccess, Matcher matcherHasChildObject)
			throws NoSuchMethodException, ResolveException {
		String methodName = matcherMethodAccess.group(1);
		if (obj instanceof String) {
			Object[] methodParameters = constructParams(obj + "," + matcherMethodAccess.group(2));
			return invokeGlobalMethod(methodName, methodParameters);
		}
		Object[] methodParameters = constructParams(matcherMethodAccess.group(2));
		return resolvePath(invokeMethodOnObj(methodName, methodParameters, obj),
				matcherHasChildObject.matches() ? matcherHasChildObject.group(2) : ".");
	}

	private Object objInstanceofMap(Object obj, String path) throws ResolveException {
		@SuppressWarnings("unchecked")
		Map<String, Object> mapObj = (Map<String, Object>) obj;
		String mapKey = findBestMapKey(mapObj, path);
		if (!mapObj.containsKey(mapKey)) {
			throw new ResolveException("Could not resolve path in map: No element with name \"" + path + "\" available.");
		}
		Object nextObj = mapObj.get(mapKey);
		String remainingPath = path.substring(mapKey.length()); // may resolve more than one hierarchy
		return resolvePath(nextObj, remainingPath);
	}

	private Object objNotInstanceofMap(Object obj, String pathToResolveNow, String remainingPath)
			throws NoSuchFieldException, IllegalAccessException, ResolveException {
		if (objectInspectionUtil.implementsInterface(obj.getClass(), FieldAliases.class)) {
			Map<String, String> fieldAliases = ((FieldAliases) obj).getFieldAliases();
			String pathInLower = pathToResolveNow.toLowerCase().trim();
			if (fieldAliases.containsKey(pathInLower)) {
				return resolvePath(obj, fieldAliases.get(pathInLower));
			}
		}
		Object nextObj = objectInspectionUtil.getFieldValue(obj, pathToResolveNow);
		return resolvePath(nextObj, remainingPath);
	}

	private static String findBestMapKey(Map<String, Object> mapObj, String fullPath) {
		String bestKey = "";
		for (String key : mapObj.keySet()) {
			if (fullPath.regionMatches(true, 0, key, 0, key.length()) && key.length() > bestKey.length()) {
				bestKey = key;
			}
		}
		return bestKey.length() > 0 ? bestKey : fullPath;
	}

	/**
	 * Method will split a string in
	 * - text before the first (
	 * - text between first ( and last )
	 * <p>
	 * Method removes everything after the last )
	 *
	 * @param parameterOrMethodName String containing a ( and )
	 * @return [0] contains the name, [1] contains a string with all params
	 */
	private static String[] getMethodNameAndParams(String parameterOrMethodName) {
		int paramsStart = parameterOrMethodName.indexOf('(');
		int paramsEnd = parameterOrMethodName.lastIndexOf(')');

		String name = parameterOrMethodName.substring(0, paramsStart);
		String params = parameterOrMethodName.substring(paramsStart + 1, paramsEnd);

		return new String[]{name, params};
	}

	public static String removeQuoteOnStartAndEnd(String input) {
		if (input == null) return null;
		int length = input.length();
		if (length < 2) return input;
		if (hasQuoteOnStartAndEnd(input)) {
			return input.substring(1, length - 1);
		} else {
			return input;
		}
	}

	public static boolean hasQuoteOnStartAndEnd(String input) {
		if (input == null) return false;
		int length = input.length();
		if (length < 2) return false;
		return (input.charAt(0) == '"' && input.charAt(length - 1) == '"');
	}

	/**
	 * @param s : string that is split at all toplevel comma
	 * @return array of params
	 */
	private static String[] getParams(String s) {
		String[] params = new String[]{""};
		int bracketDepth = 0;
		int paramIdx = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
				case '(':
					bracketDepth++;
					break;
				case ')':
					bracketDepth--;
					break;
				case ',': {
					if (bracketDepth == 0) {
						paramIdx++;
						String[] enlargedParams = Arrays.copyOf(params, paramIdx + 1);
						enlargedParams[paramIdx] = "";
						params = enlargedParams;
						continue;
					}
					break;
				}
				default:
					break;
			}

			params[paramIdx] += String.valueOf(c);
		}
		return Arrays.stream(params).map(String::trim).toArray(String[]::new);
	}


	@Data
	@AllArgsConstructor
	private static class MatchResult {
		private Integer fullLength;
		private Integer start;
		private Integer end;
		private String matchContent;
	}

	@Data
	@AllArgsConstructor
	public static class ResolveResult {
		private boolean isExpression;
		private Object resolvedObject;
	}
}
