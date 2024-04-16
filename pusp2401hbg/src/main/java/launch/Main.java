package launch;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.catalina.Server;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.WebResourceSet;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.EmptyResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.util.logging.*;

public class Main {

    private static File getRootFolder() {
        try {
            File root;
            String runningJarPath = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().replaceAll("\\\\", "/");
            int lastIndexOf = runningJarPath.lastIndexOf("/target/");
            if (lastIndexOf < 0) {
                root = new File("");
            } else {
                root = new File(runningJarPath.substring(0, lastIndexOf));
            }
            System.out.println("application resolved root folder: " + root.getAbsolutePath());
            return root;
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void configureLogging() throws SecurityException, UnsupportedEncodingException {
        Level level = Level.INFO;
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger("");
        logger.setLevel(level);
        Handler[] handlers = logger.getHandlers();
        Handler handler;
        if (handlers.length == 1 && handlers[0] instanceof ConsoleHandler) {
            handler = handlers[0];
        } else {
            handler = new ConsoleHandler();
        }
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(level);
        handler.setEncoding("UTF-8");
        logger.addHandler(handler);
    }

    public static void main(String[] args) throws Exception {
        configureLogging();

        File root = getRootFolder();
        System.setProperty("org.apache.catalina.startup.EXIT_ON_INIT_FAILURE", "true");
        Tomcat tomcat = new Tomcat();

        Path tempPath = Files.createTempDirectory("tomcat-base-dir");
        tomcat.setBaseDir(tempPath.toString());

        //The port that we should run on can be set into an environment variable
        //Look for that variable and default to 8888 if it isn't there.
        String webPort = System.getenv("PORT");
        if (webPort == null || webPort.isEmpty()) {
            webPort = "8888";
        }

        tomcat.setHostname("localhost");
        tomcat.setPort(Integer.parseInt(webPort));

        File webContentFolder = new File(root.getAbsolutePath(), "src/main/resources/webapp");
        if (!webContentFolder.exists()) {
            webContentFolder = Files.createTempDirectory("default-doc-base").toFile();
        }
        StandardContext ctx = (StandardContext) tomcat.addWebapp("", webContentFolder.getAbsolutePath());
        ctx.setDocBase(webContentFolder.getAbsolutePath() + File.separator + "static");
        //Set execution independent of current thread context classloader (compatibility with exec:java mojo)
        ctx.setParentClassLoader(Main.class.getClassLoader());

        System.out.println("configuring app with basedir: " + webContentFolder.getAbsolutePath());

        // Declare an alternative location for your "WEB-INF/classes" dir
        // Servlet 3.0 annotation will work
        File additionWebInfClassesFolder = new File(root.getAbsolutePath(), "target/classes");
        WebResourceRoot resources = new StandardRoot(ctx);

        WebResourceSet resourceSet;
        if (additionWebInfClassesFolder.exists()) {
            resourceSet = new DirResourceSet(resources, "/WEB-INF/classes", additionWebInfClassesFolder.getAbsolutePath(), "/");
            System.out.println("loading WEB-INF resources from as '" + additionWebInfClassesFolder.getAbsolutePath() + "'");
        } else {
            resourceSet = new EmptyResourceSet(resources);
        }
        resources.addPreResources(resourceSet);
        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/jsp", webContentFolder.getAbsolutePath() + File.separator + "jsp", "/"));
        ctx.setResources(resources);
        tomcat.getConnector();
    
        tomcat.start();
        System.out.println("Listening to port " + webPort);

        Server server = tomcat.getServer();
        server.await();
    }
}