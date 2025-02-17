/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2022 SonarSource SA
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
package org.sonar.plugins.php.phpunit;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.plugins.php.PhpTestUtils;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.phpunit.xml.TestCase;

import static org.assertj.core.api.Assertions.assertThat;

public class TestFileReportTest {

  private String componentKey;
  private String testFileName;
  private SensorContextTester context;
  private Set<String> unresolvedInputFiles;

  @Before
  public void setUp() throws Exception {
    testFileName = "testfile.php";
    DefaultInputFile testFile = TestInputFileBuilder.create("moduleKey", testFileName).setType(InputFile.Type.TEST).setLanguage(Php.KEY).build();
    context = SensorContextTester.create(new File("src/test/resources"));
    context.fileSystem().add(testFile);
    componentKey = testFile.key();
    unresolvedInputFiles = new LinkedHashSet<>();
  }

  @Test
  public void shouldReportStatusCounts() throws Exception {
    final TestFileReport report = new TestFileReport(testFileName, 3d);
    report.addTestCase(new TestCase(TestCase.Status.SKIPPED));
    report.addTestCase(new TestCase(TestCase.Status.ERROR));
    report.addTestCase(new TestCase(TestCase.Status.FAILURE));
    report.addTestCase(new TestCase(TestCase.Status.FAILURE));
    report.saveTestMeasures(context, unresolvedInputFiles);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.TEST_EXECUTION_TIME, 3000l);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.SKIPPED_TESTS, 1);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.TEST_ERRORS, 1);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.TEST_FAILURES, 2);
    assertThat(unresolvedInputFiles).isEmpty();
  }

  @Test
  public void shouldReportZeroTestsIfEmpty() throws Exception {
    final TestFileReport report = new TestFileReport(testFileName, 0d);
    report.saveTestMeasures(context, unresolvedInputFiles);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.TESTS, 0);
    assertThat(unresolvedInputFiles).isEmpty();
  }

  @Test
  public void shouldNotCountSkippedTests() throws Exception {
    final TestFileReport report = new TestFileReport(testFileName, 1d);
    report.addTestCase(new TestCase(null, null, null, null, null));
    report.addTestCase(new TestCase(TestCase.Status.SKIPPED));
    report.addTestCase(new TestCase(TestCase.Status.FAILURE));
    report.addTestCase(new TestCase(TestCase.Status.ERROR));
    report.saveTestMeasures(context, unresolvedInputFiles);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.TESTS, 3);
    assertThat(unresolvedInputFiles).isEmpty();
  }

}
