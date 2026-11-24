/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.literature.export;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvWriterTest {

  @Test
  void escapeFormula_prefixesFormulaValues() {
    assertEquals("'=HYPERLINK(\"x\")", CsvWriter.escapeFormula("=HYPERLINK(\"x\")"));
    assertEquals("'+1", CsvWriter.escapeFormula("+1"));
    assertEquals("'-1", CsvWriter.escapeFormula("-1"));
    assertEquals("'@SUM(A1)", CsvWriter.escapeFormula("@SUM(A1)"));
    assertEquals("Plain title", CsvWriter.escapeFormula("Plain title"));
    assertEquals("", CsvWriter.escapeFormula(""));
    assertEquals("' =HYPERLINK(\"x\")", CsvWriter.escapeFormula(" =HYPERLINK(\"x\")"));
    assertEquals("   ", CsvWriter.escapeFormula("   "));
  }

  @Test
  void localDateProcessor_formatsIsoDate() {
    CsvWriter.LocalDateProcessor processor = new CsvWriter.LocalDateProcessor();
    assertEquals("2024-07-14", processor.execute(LocalDate.of(2024, 7, 14), null));
    assertEquals("", processor.execute(null, null));
  }
}
