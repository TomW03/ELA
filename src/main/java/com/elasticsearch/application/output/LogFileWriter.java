package com.elasticsearch.application.output;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.elasticsearch.application.configurations.*;
import jakarta.annotation.PostConstruct;

/**
 * Die FileWriter Klasse ist zum initialisieren und konfigurieren des Loggers
 * für die Anwendung verantwortlich. Sie enthält Methoden zum erstellen des
 * logging-Verzeichnisses und der Datei, zum einrichten der log handler und
 * generieren eines Log-Dateinamen.
 */
@Component
public class LogFileWriter {	
	// Generiert den LogdateiNamen mit Datum und Zeit
	private String logDateiPfad;
	private String logDateiName;
	private String logDateiNameZeitFormat; //keine Klassenvariable, sondern eine Instanzvariable, daher kein 'static'
	private String logDateiNamevollstaendig;
	private static final Logger logger = Logger.getLogger(FileWriter.class.getName());
	private final ApplicationpPropertiesVerarbeiter propertiesVerarbeiter;
	 
	@Autowired
	public LogFileWriter(ApplicationpPropertiesVerarbeiter propertiesVerarbeiter) {
		this.propertiesVerarbeiter = propertiesVerarbeiter;
		init();
	}
	private void init() {
        // Initialisiere die Instanzvariablen mit Werten aus dem Bean
        logDateiPfad = propertiesVerarbeiter.getlogDateiPfad();
        logDateiName = propertiesVerarbeiter.getlogDateiName();
        logDateiNameZeitFormat = propertiesVerarbeiter.getlogDateiNameZeitFormat();
        logDateiNamevollstaendig = generateLogFileName();
        
    }

	/**
	 * Initialisiert den Logger für das Logging von Informationen, Warnungen und
	 * Fehlern in der Java-Anwendung. Der Logger wird so konfiguriert, dass die
	 * Log-Daten sowohl in einer Log-Datei als auch auf der Konsole ausgegeben
	 * werden. Die Log-Datei wird im angegebenen Log-Verzeichnis erstellt und erhält
	 * einen Namen basierend auf dem aktuellen Zeitstempel.
	 *
	 * <p>
	 * Die Methode führt die folgenden Schritte aus: 1. Generiere den Namen der
	 * Log-Datei basierend auf dem aktuellen Zeitstempel. 2. Überprüfe, ob das
	 * Log-Verzeichnis vorhanden ist, und erstelle es gegebenenfalls. 3. Überprüfe,
	 * ob die Log-Datei vorhanden ist, und erstelle sie gegebenenfalls. 4.
	 * Konfiguriere den Logger, um Log-Nachrichten auf verschiedenen Ebenen zu
	 * protokollieren. 5. Erstelle einen FileHandler, um Log-Daten in die Log-Datei
	 * zu schreiben, und setze den benutzerdefinierten LogFileFormatter. 6. Erstelle
	 * einen KonsolenHandler, um Log-Daten auf der Konsole auszugeben, und setze
	 * einen einfachen Formatter. 7. Füge die Handler zum Logger hinzu. 8. Schreibe
	 * eine Startnachricht in die Log-Datei.
	 *
	 * @throws IOException Wenn ein Fehler beim Erstellen der Log-Datei oder des
	 *                     Log-Verzeichnisses auftritt.
	 */
	@PostConstruct
	public void initializeLogger() {
		
		
		// Überprüfe ob der Log-Datei-Pfad existiert und erstellt ihn, falls er nicht existieren sollte
		File logDir = new File(logDateiPfad);
		if (!logDir.exists()) {
			System.out.println(
					"Das Log-Verzeichnis existiert noch nicht und wird nun, auf dem folgenden lokalen Pfad, erstellt: "
							+ logDateiPfad);
			if (!logDir.mkdirs()) {
				System.out.println("Fehler beim Erstellen des Log-Verzeichnisses unter: " + logDateiPfad);
				return;
			}
		}

		// Check and create the log file
		File logFile = new File(logDateiNamevollstaendig);
		if (!logFile.exists()) {
			System.out.println(
					"Die Log-Datei existiert noch nicht und wird nun, auf dem folgenden lokalen Pfad, erstellt: "
					+ logDateiPfad + logDateiNamevollstaendig);
			System.out.println("Vollständiger Pfad der Log-Datei: " + logDateiNamevollstaendig);

			try {
				if (!logFile.createNewFile()) {
					System.out.println("Fehler beim Erstellen der Log-Datei auf dem lokalen Pfad: " + logDateiPfad + logDateiNamevollstaendig);
					return;
				}
			} catch (IOException e) {
				System.out.println("Fehler beim Erstellen der Log-Datei: " + e.getMessage());
				return;
			}
		}

		try {
			// Configure the logger
			logger.setLevel(Level.ALL);
			
			 
			
		        


			// Create a FileHandler to write log data to the file
			FileHandler fileHandler = new FileHandler(logDateiPfad + File.separator + logDateiNamevollstaendig);
			fileHandler.setLevel(Level.ALL);
			fileHandler.setFormatter(new LogFileFormatter());

			// Erstelle einen KonsolenHandler fuer den Konsolen-output
			//ConsoleHandler consoleHandler = new ConsoleHandler();
			// consoleHandler.setLevel(Level.ALL);
			//consoleHandler.setLevel(Level.WARNING);
			//consoleHandler.setFormatter(new SimpleFormatter());

			// Add the handlers to the logger
			logger.addHandler(fileHandler);
			logger.setUseParentHandlers(false); // Entfernen Sie den ConsoleHandler aus dem Logger
			 // Entfernen Sie alle vorhandenen ConsoleHandler, falls vorhanden
	        Handler[] handlers = logger.getHandlers();
	        for (Handler handler : handlers) {
	            if (handler instanceof ConsoleHandler) {
	                logger.removeHandler(handler);
	            }
	        }

			// Write log messages
			logger.info("Anwendung gestartet.");
			System.out.println("Anwendung erfolgreich intialisiert, ab jetzt sind alle log.Nachrichten ausschließlich in der Log-Datei zu finden!");
		} catch (IOException e) {
			// Error Meldung über die Fehler, welche während der Initialisierung des Loggers auftraten
			System.out.println("Fehler beim Initialisieren des Loggers: " + e.getMessage());
		}
	}

	/**
	 * Diese Methode generiert den Namen der Log-Datei basierend auf dem aktuellen
	 * Zeitstempel.
	 *
	 * @return Der Name der Log-Datei mit dem aktuellen Zeitstempel.
	 */
	private String generateLogFileName() {
		LocalDateTime currentTime = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(logDateiNameZeitFormat);
		return logDateiName + currentTime.format(formatter) + ".log";
	}

	/**
	 * Ein interner Formatter, der von der Logger-Klasse verwendet wird, um
	 * Log-Einträge in einem spezifischen Format zu formatieren, bevor sie in eine
	 * Log-Datei geschrieben werden.
	 */
	private class LogFileFormatter extends Formatter {

		/**
		 * Diese Methode formatiert einen einzelnen Log-Eintrag in das gewünschte
		 * Format.
		 *
		 * @param record Der LogRecord, der formatiert werden soll.
		 * @return Ein String, der den formatierten Log-Eintrag repräsentiert.
		 */
		@Override
		public String format(LogRecord record) {
			SimpleDateFormat dateFormat = new SimpleDateFormat(logDateiNameZeitFormat); // Definiere ein SimpleDateFormat-Objekt mit dem gewünschten Datumsformat "yyyy-MM-dd HH:mm:ss" 
			String timestamp = dateFormat.format(new Date(record.getMillis())); 		// Formatieren des Zeitstempels (Datum und Uhrzeit) des Log-Eintrags
			String level = record.getLevel().toString();								// Extrahiere das Log-Level des Eintrags (z. B. INFO, WARNING,ERROR) als String
			String message = record.getMessage(); 										// Extrahiere die Nachricht des Log-Eintrags
			return "[" + timestamp + "] " + level + ": " + message + "\n"; 				// Baue den formatierten Log-Eintrag zusammen, der das Datum, das Log-Level und die Nachricht enthält // Beispiel: "[2023-07-31 08:30:15] INFO: Dies ist eine log Nachricht."
		}
	}

	/**
	 * Gibt die logger instanz zurück.
	 * 
	 * @return die logger instanz.
	 */
	public static Logger getLogger() {
		return logger;
	}
}

//Konfiguriere einen neuen ConsoleHandler, der nur Meldungen mit Level niedriger als WARNING anzeigt
//ConsoleHandler consoleHandler = new ConsoleHandler();
//consoleHandler.setLevel(Level.WARNING);
//consoleHandler.setFormatter(new SimpleFormatter());

// Füge den neuen ConsoleHandler zum Logger hinzu
// logger.addHandler(consoleHandler);
