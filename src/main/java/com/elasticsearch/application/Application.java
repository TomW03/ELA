package com.elasticsearch.application;

//Importe für die Anwendung
import com.elasticsearch.application.output.LogFileWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

import com.elasticsearch.application.configurations.AppKonfigurator;
import com.elasticsearch.application.configurations.ApplicationpPropertiesVerarbeiter;
//STANDARD Imports
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;

//Standard-Imports für die Spring Boot-Anwendung
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Der Einstiegspunkt der Spring Boot-Anwendung.
 *
 * Die @SpringBootApplication-Annotation kombiniert @Configuration, @EnableAutoConfiguration
 * und @ComponentScan, um eine Spring Boot-Anwendung zu erstellen.
 *
 * Die @Theme-Annotation definiert das Standard-Thema für die Vaadin-Oberfläche.
 *
 */
@SuppressWarnings({ "serial", "unused", "deprecation" })
@ComponentScan(basePackages = {"com.elasticsearch.application", "com.elasticsearch.application.configurations", "com.elasticsearch.application.interactions", "com.elasticsearch.application.calculator", "com.elasticsearch.application.output"})
@SpringBootApplication
@Theme(value = "mytodo")
public class Application implements AppShellConfigurator {
	
	// Logger für die Anwendung
	private final static Logger logger = LogFileWriter.getLogger();
	
	// RestHighLevelClient für Elasticsearch
	private RestHighLevelClient elasticsearchClient;
	
	// ApplicationContext für die Spring-Anwendung
	private ApplicationContext applicationContext;

	public static void main(String[] args) {
		//String currentDir = System.getProperty("user.dir");
        //System.out.println("Current project path: " + currentDir);
		
		// Starte die Spring Boot-Anwendung
        ApplicationContext context = SpringApplication.run(Application.class, args);
        
        // Hole die Bean-Instanz von ApplicationpPropertiesVerarbeiter
        ApplicationpPropertiesVerarbeiter propertiesVerarbeiter = context.getBean(ApplicationpPropertiesVerarbeiter.class);
		
        // Eigene Programmlogik
		// LogFileWriter.initializeLogger(); ->Wird ncht mehr benötigt da @Component
		
		// Rufe die Bean AppKonfigurator auf und rufe die Methoden auf
        AppKonfigurator appKonfigurator = context.getBean(AppKonfigurator.class);
		int port;
		try {
			port = appKonfigurator.findeFreienPort();
			appKonfigurator.schreibtDieProzessIDInDieLogDatei(port);
			System.setProperty("server.port", String.valueOf(port));
			//logger.info("Http-Server laeuft auf Port: " + port);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Fehler beim Indices laden: " + e.getMessage(), e);
		}
	}

}
