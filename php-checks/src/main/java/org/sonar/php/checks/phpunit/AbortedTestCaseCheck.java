/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.checks.phpunit;

import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.PhpUnitCheck;
import org.sonar.php.utils.collections.SetUtils;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

import static org.sonar.php.checks.utils.CheckUtils.lowerCaseFunctionName;

@Rule(key = "S1607")
public class AbortedTestCaseCheck extends PhpUnitCheck {

  private static final String MESSAGE = "Either remove this call or add an explanation about why the test is aborted.";
  private static final Set<String> ABORT_FUNCTIONS = SetUtils.immutableSetOf("marktestskipped", "marktestincomplete");

  @Override
  public void visitFunctionCall(FunctionCallTree fct) {
    if (!isPhpUnitTestCase()) {
      return;
    }

    if (isAbortFunctionWithoutMessage(fct)) {
      newIssue(fct, MESSAGE);
    }

    super.visitFunctionCall(fct);
  }

  private static boolean isAbortFunctionWithoutMessage(FunctionCallTree fct) {
    return ABORT_FUNCTIONS.contains(lowerCaseFunctionName(fct)) && fct.arguments().isEmpty();
  }
}
