package com.revolut.web;

import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

/**
 * Utility class to be used in test cases.
 */
public class DelegatingServletInputStream extends ServletInputStream {

  private final InputStream sourceStream;

  /**
   * Create a DelegatingServletInputStream for the given source stream.
   *
   * @param sourceStream the source stream (never {@code null}
   */
  DelegatingServletInputStream(InputStream sourceStream) {
    this.sourceStream = sourceStream;
  }

  @Override
  public int read() throws IOException {
    return sourceStream.read();
  }

  @Override
  public void close() throws IOException {
    super.close();
    sourceStream.close();
  }

  @Override
  public boolean isFinished() {
    return Boolean.FALSE;
  }

  @Override
  public boolean isReady() {
    return Boolean.TRUE;
  }

  @Override
  public void setReadListener(ReadListener readListener) {
    //NOP
  }
}