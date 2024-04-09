package com.elasticsearch.application.configurations;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

import com.elasticsearch.application.output.LogFileWriter;

/**
 * Die Klasse AppKonfigurator ist für die Konfiguration und Verwaltung von Anwendungseinstellungen verantwortlich.
 * Sie bietet Methoden zum Setzen und Abfragen des freien Ports sowie zur Ausgabe von Informationen und Prozess-IDs in das Log.
 */
@Component
public class AppKonfigurator {
	
	private static int freierPort;
	private final Logger logger = LogFileWriter.getLogger();
	private int ApplicationPropertiesDefaultPort;
	private final ApplicationpPropertiesVerarbeiter propertiesVerarbeiter;
	
	/**
     * Konstruktor für die AppKonfigurator-Klasse.
     *
     * @param propertiesVerarbeiter Ein Objekt von ApplicationpPropertiesVerarbeiter zur Verarbeitung von Anwendungseigenschaften.
     */
	@Autowired
	public AppKonfigurator(ApplicationpPropertiesVerarbeiter propertiesVerarbeiter) {
		this.propertiesVerarbeiter = propertiesVerarbeiter;
	   // this.propertiesVerarbeiter.loadProperties(); // Lade die Properties zuerst
		init();
		try {
		findeFreienPort();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Kritischer Fehler beim Finden eines freien Ports: " + e.getMessage(), e);
		}
	}
	
	private void init() {
        // Initialisiere die Instanzvariablen mit Werten aus dem Bean
		ApplicationPropertiesDefaultPort = propertiesVerarbeiter.getApplicationPropertiesDefaultPort();
    }
	
	
	/**
	 * Get-Methode für den aktuell gesetzten freien Port[freierPort->Klassenvariable]
	 * @return freierPort -> aktuell gesetzter freier Port [freierPort->Klassenvariable]
	 */
	public static int getFreienPort() {
		return freierPort;
	}
	
	/**
     * Setzt den gewünschten Port als freien Port.
     *
     * @param portInput Der zu setzende freie Port.
     */
	public void setPort(int portInput) {
		freierPort = portInput;
	}
	
	/**
     * Verwendet den Standardport aus den Anwendungseigenschaften als freien Port.
     */
	public void useApplicationPropertiesDefaultPort() {
		freierPort = ApplicationPropertiesDefaultPort;
	}
	@Autowired
    private ConfigurableEnvironment environment;
	 
	/**
     * Schreibt die Prozess-ID und den gewählten Port in die Log-Datei.
     *
     * @param port Der gewählte Port.
     * @throws IOException Wenn ein Fehler beim Schreiben in die Log-Datei auftritt.
     */
	 public void schreibtDieProzessIDInDieLogDatei(int port) throws IOException {
	        // Log-Nachrichten schreiben
	        logger.info("Gewählter Port: " + port);
	        logger.info("Prozess-ID: " + ProcessHandle.current().pid());
	        
	        // Setzen des Ports für den Tomcat-Server
	        environment.getPropertySources().addFirst(new PropertySource<String>("customProperties") {
	            @Override
	            public String getProperty(String name) {
	                if ("server.port".equals(name)) {
	                    return String.valueOf(port);
	                }
	                return null;
	            }
	        });
	        
	        // Testausgabe, um zu prüfen, ob die benutzerdefinierte Eigenschaft gesetzt ist
	        logger.info("Custom property 'server.port' value: " + environment.getProperty("server.port"));
	    }
	 
	/**
     * Findet einen freien Port und weist ihn der Klassenvariablen freierPort zu.
     *
     * @return Der gefundene freie Port.
     * @throws IOException Wenn ein Fehler beim Finden des freien Ports auftritt.
     */
	public int findeFreienPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            freierPort = socket.getLocalPort();
            return socket.getLocalPort();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Fehler beim Finden eines freien Ports: " + e.getMessage(), e);
            return -1;
        }
    }
}
