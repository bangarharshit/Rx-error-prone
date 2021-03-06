package com.rx.errorprone;

import static com.google.errorprone.BugPattern.Category.JDK;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.rx.errorprone.utils.MatcherUtils;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;

/** @author harshit.bangar@gmail.com (Harshit Bangar) */
@AutoService(BugChecker.class)
@BugPattern(
  name = "CacheCheck",
  summary = "Don't use cache. Use replay() instead.",
  category = JDK,
  severity = WARNING
)
public class CacheCheck extends BugChecker implements BugChecker.MethodInvocationTreeMatcher {

  private static final Matcher<ExpressionTree> CACHE = MatcherUtils.cache();

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (CACHE.matches(tree, state)) {
      return describeMatch(tree);
    } else {
      return Description.NO_MATCH;
    }
  }

  @Override
  public String linkUrl() {
    return "https://speakerdeck.com/dlew/common-rxjava-mistakes";
  }
}
