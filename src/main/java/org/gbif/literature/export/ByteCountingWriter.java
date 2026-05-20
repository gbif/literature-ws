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
import java.io.Writer;
import java.nio.charset.Charset;

/** Writer that tracks UTF-8 bytes written and enforces a maximum. */
public class ByteCountingWriter extends Writer {

  private final Writer delegate;
  private final long maxBytes;
  private final Charset charset;
  private long bytesWritten;

  public ByteCountingWriter(Writer delegate, long maxBytes) {
    this(delegate, maxBytes, Charset.defaultCharset());
  }

  public ByteCountingWriter(Writer delegate, long maxBytes, Charset charset) {
    this.delegate = delegate;
    this.maxBytes = maxBytes;
    this.charset = charset;
    this.bytesWritten = 0;
  }

  public long getBytesWritten() {
    return bytesWritten;
  }

  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    if (maxBytes > 0) {
      long additional = new String(cbuf, off, len).getBytes(charset).length;
      if (bytesWritten + additional > maxBytes) {
        throw new ExportLimitExceededException(
            "Export exceeds maximum size of " + maxBytes + " bytes");
      }
      bytesWritten += additional;
    }
    delegate.write(cbuf, off, len);
  }

  @Override
  public void flush() throws IOException {
    delegate.flush();
  }

  @Override
  public void close() throws IOException {
    delegate.close();
  }
}
