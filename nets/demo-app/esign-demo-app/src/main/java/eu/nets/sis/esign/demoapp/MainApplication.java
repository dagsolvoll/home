package eu.nets.sis.esign.demoapp;

import com.github.ulisesbocchio.jar.resources.JarResourceLoader;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.EventListener;

/**
 * Class that starts Spring boot application
 * 
 */
@SpringBootApplication
@PropertySource("classpath:application.properties")
public class MainApplication {
	private static final Logger LOGGER = Logger.getLogger(MainApplication.class);

	@Value("${base.url}")
	private String baseUrl;
	
	@Value("#{systemProperties['user.dir']}#{systemProperties['file.separator']}${logging.file}") 
	private String loggingPath;
	
	/**
	 * Write logs into console and file, after server started successfully
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void doSomethingAfterStartup() {
		
		LOGGER.info("E-Signing demo app started");
		System.out.println("\n\nLog file path: "+ loggingPath);
		System.out.println("\n\nPlease point your preferred browser to " + baseUrl + "/esign");
	}


	public static void main(String[] args) {
		if(args.length < 2 || !args[0].toLowerCase().endsWith("p12")) {
			System.out.println("\n\nERROR: Please provide a valid keystore (PKCS12) path and a password as command line arguments to execute the demo app. "
					+ "Contact Nets support at support.esecurity@nets.eu for details.\n");
		} else {
			//TO avoid ssl handshake exception in some versions of jre
			System.setProperty("jsse.enableSNIExtension", "false");
			new SpringApplicationBuilder()
		        .sources(MainApplication.class)
		        .resourceLoader(new JarResourceLoader())
		        .run(args);
		}
	}
}
