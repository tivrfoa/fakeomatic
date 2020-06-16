/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Marcelo Guimarães <ataxexe@backpackcloud.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.backpackcloud.fakeomatic.infra;

import io.backpackcloud.fakeomatic.spi.Config;
import io.backpackcloud.fakeomatic.UnbelievableException;
import io.backpackcloud.fakeomatic.spi.FakeData;
import io.backpackcloud.fakeomatic.spi.PayloadGenerator;
import io.quarkus.qute.Engine;
import io.quarkus.qute.TemplateExtension;
import io.quarkus.qute.TemplateInstance;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@ApplicationScoped
public class PayloadProducer {

  private final Config config;

  private final FakeData fakeData;

  private final Engine templateEngine;

  public PayloadProducer(Config config, FakeData fakeData, Engine templateEngine) {
    this.config = config;
    this.fakeData = fakeData;
    this.templateEngine = templateEngine;
  }

  @Produces
  public PayloadGenerator produce() {
    try {
      // read all bytes
      byte[] bytes = Files.readAllBytes(Paths.get(config.templatePath()));
      // convert bytes to string
      String content = new String(bytes, Charset.forName(config.charset()));

      TemplateInstance template = templateEngine
          .parse(content)
          .data(fakeData);

      return new PayloadGenerator() {
        @Override
        public String contentType() {
          return config.templateType();
        }

        @Override
        public String generate() {
          return template.render();
        }
      };
    } catch (IOException e) {
      throw new UnbelievableException(e);
    }
  }

  @TemplateExtension
  public static String env(String name) {
    return System.getenv(name);
  }

  @TemplateExtension
  public static String today(String format) {
    DateFormat dateFormat = new SimpleDateFormat(format);
    return dateFormat.format(new Date());
  }

}
