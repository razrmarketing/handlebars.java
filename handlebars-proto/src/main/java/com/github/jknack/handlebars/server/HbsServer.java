/**
 * Copyright (c) 2012-2013 Edgar Espina
 *
 * This file is part of Handlebars.java.
 *
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
package com.github.jknack.handlebars.server;

import com.github.jknack.handlebars.*;
import com.github.jknack.handlebars.helper.*;
import com.github.jknack.handlebars.io.*;
import com.razr.handlebars.helpers.*;
import org.apache.commons.io.*;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.servlet.*;
import org.eclipse.jetty.util.component.AbstractLifeCycle.*;
import org.eclipse.jetty.util.component.*;
import org.eclipse.jetty.util.resource.*;
import org.eclipse.jetty.webapp.*;
import org.kohsuke.args4j.*;
import org.slf4j.*;

import javax.servlet.http.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * A handlebars web server.
 *
 * @author edgar.espina
 */
public class HbsServer {

  /**
   * The logging system.
   */
  public static final Logger logger = LoggerFactory.getLogger(HbsServer.class);

  public static final String CONTEXT = "/";

  public static final String CONTENT_TYPE = "text/html";

  public static final int PORT = 6780;

  public static String version;

  static {
    InputStream in = null;
    try {
      in = HbsServer.class.getResourceAsStream("/hbs.properties");
      Properties properties = new Properties();
      properties.load(in);
      version = properties.getProperty("version");
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      IOUtils.closeQuietly(in);
    }

  }

  public static class Options {
    @Option(name = "-dir", aliases = "-d", required = true,
        usage = "set the template directory", metaVar = " ")
    public File dir;

    @Option(name = "-suffix", aliases = "-sx",
        usage = "set the template's suffix, default is: "
            + TemplateLoader.DEFAULT_SUFFIX, metaVar = " ")
    public String suffix = TemplateLoader.DEFAULT_SUFFIX;

    @Option(name = "-prefix", aliases = "-px",
        usage = "set the template's prefix, default is: "
            + TemplateLoader.DEFAULT_PREFIX, metaVar = " ")
    public String prefix = TemplateLoader.DEFAULT_PREFIX;

    @Option(name = "-context", aliases = "-c",
        usage = "set the web context's path, default is: " + CONTEXT,
        metaVar = " ")
    public String contextPath = CONTEXT;

    @Option(name = "-port", aliases = "-p",
        usage = "set the port's number, default is: " + PORT, metaVar = " ")
    public int port = PORT;

    @Option(name = "-contentType", aliases = "-ct",
        usage = "set the content's type header, default is: " + CONTENT_TYPE,
        metaVar = " ")
    public String contentType = CONTENT_TYPE;

    @Option(name = "-encoding", aliases = "-e",
        usage = "set the charset used it while rendering, default is: UTF-8",
        metaVar = " ")
    public String encoding = "UTF-8";
  }

  public static void main(final String[] args) throws Exception {
    Options arguments = new Options();
    CmdLineParser parser = new CmdLineParser(arguments);
    try {
      parser.parseArgument(args);
      run(arguments);
    } catch (CmdLineException e) {
      System.err.println(e.getMessage());
      String os = System.getProperty("os.name").toLowerCase();
      System.err.println("Usage:");
      String program = "java -jar handlebars-proto-${version}.jar";
      if (os.contains("win")) {
        program = "handlebars";
      }
      System.err.println("  " + program + " [-option value]");
      System.err.println("Options:");
      parser.printUsage(System.err);
    }
  }

  /**
   * Start a Handlebars server.
   *
   * @param args The command line arguments.
   * @throws Exception If something goes wrong.
   */
  public static void run(final Options args) throws Exception {
    if (!args.dir.exists()) {
      System.out.println("File not found: " + args.dir);
    }
    logger.info("Welcome to the Handlebars.java server v" + version);

    URLTemplateLoader loader = new FileTemplateLoader(args.dir);
    loader.setPrefix(new File(args.dir, args.prefix).getAbsolutePath());
    loader.setSuffix(args.suffix);
    Handlebars handlebars = new Handlebars(loader);
      handlebars.handlebarsJsFile("/handlebars-v2.0.0.js");

    /**
     * Helper wont work in the stand-alone version, so we add a default helper
     * that render the plain text.
     */
    handlebars.registerHelper(HelperRegistry.HELPER_MISSING, new Helper<Object>() {
      @Override
      public CharSequence apply(final Object context,
          final com.github.jknack.handlebars.Options options)
          throws IOException {
        return new Handlebars.SafeString(options.fn.text());
      }
    });
    //handlebars.registerHelper("json", Jackson2Helper.INSTANCE);
    //handlebars.registerHelper("md", new MarkdownHelper());
      DateHelpers.register(handlebars);
      PhoneHelpers.register(handlebars);
      SortHelpers.register(handlebars);

      handlebars.registerHelper("math", new MathHelper());

      // Array Helpers
      ArrayHelpers.register(handlebars);

      // String helpers
      StringHelpers.register(handlebars);
      HtmlHelpers.register(handlebars);
      handlebars.registerHelpers(new IfEqualsHelpers());
      handlebars.registerHelpers(new IfInListHelpers());
    try {
        handlebars.registerHelpers("everyNth.js", HbsServer.class.getClassLoader().getResourceAsStream("jshelpers/everyNth.js"));
    } catch (Exception e) {
        logger.error("Could not load javascript handlebars helper files: "+e);
    }

    // Humanize helpers
    //HumanizeHelper.register(handlebars);

    final Server server = new Server(args.port);
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      @Override
      public void run() {
        logger.info("Hope you enjoy it! bye!");
        try {
          server.stop();
        } catch (Exception ex) {
          logger.info("Cann't stop the server", ex);
        }
      }
    }));

    server.addLifeCycleListener(new AbstractLifeCycleListener() {
      @Override
      public void lifeCycleStarted(final LifeCycle event) {
        logger.info("Open a browser and type:");
        logger.info("  http://localhost:{}{}/[page]{}", new Object[]{
            args.port,
            args.contextPath.equals(CONTEXT) ? "" : args.contextPath,
            args.suffix });
      }
    });

    WebAppContext root = new WebAppContext();
    ErrorHandler errorHandler = new ErrorHandler() {
      @Override
      protected void writeErrorPageHead(final HttpServletRequest request, final Writer writer,
          final int code, final String message) throws IOException {
        writer
            .write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"/>\n");
        writer.write("<title>{{");
        writer.write(Integer.toString(code));
        writer.write("}}");
        writer.write("</title>\n");
        writer.write("<style>body{font-family: monospace;}</style>");
      }

      @Override
      protected void writeErrorPageMessage(final HttpServletRequest request, final Writer writer,
          final int code,
          final String message, final String uri) throws IOException {
        writer.write("<div align=\"center\">");
        writer
            .write("<p><span style=\"font-size: 48px;\">{{</span><span style=\"font-size: 36px; color:#999;\">");
        writer.write(Integer.toString(code));
        writer.write("</span><span style=\"font-size: 48px;\">}}</span></p>");
        writer.write("</h2>\n<p>Problem accessing ");
        write(writer, uri);
        writer.write(". Reason:\n<pre>    ");
        write(writer, message);
        writer.write("</pre></p>");
        writer.write("</div>");
        writer.write("<hr />");
      }

      @Override
      protected void writeErrorPageBody(final HttpServletRequest request, final Writer writer,
          final int code,
          final String message, final boolean showStacks) throws IOException {
        String uri = request.getRequestURI();

        writeErrorPageMessage(request, writer, code, message, uri);
      }
    };
    root.setErrorHandler(errorHandler);
    root.setContextPath(args.contextPath);
    root.setResourceBase(args.dir.getAbsolutePath());
    root.addServlet(new ServletHolder(new HbsServlet(handlebars, args)),
        "*" + args.suffix);

    root.setParentLoaderPriority(true);

    // prevent jetty from loading the webapp web.xml
    root.setConfigurations(new Configuration[]{new WebXmlConfiguration() {
      @Override
      protected Resource findWebXml(final WebAppContext context)
          throws IOException, MalformedURLException {
        return null;
      }
    } });

    server.setHandler(root);

    server.start();
    server.join();
  }

}
