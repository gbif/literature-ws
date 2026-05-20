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

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ByteCountingWriterTest {

  @Test
  void throwsWhenByteLimitExceeded() throws IOException {
    StringWriter target = new StringWriter();
    ByteCountingWriter writer = new ByteCountingWriter(target, 5, StandardCharsets.UTF_8);

    writer.write("12345");

    ExportLimitExceededException ex =
        assertThrows(ExportLimitExceededException.class, () -> writer.write("6"));
    assertTrue(ex.getMessage().contains("5"));
  }
}
