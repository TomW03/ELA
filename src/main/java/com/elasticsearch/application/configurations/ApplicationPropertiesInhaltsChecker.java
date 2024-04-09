package com.elasticsearch.application.configurations;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.elasticsearch.application.output.LogFileWriter;

/**
* Die Klasse ApplicationPropertiesInhaltsChecker dient zur Überprüfung der Elasticsearch-Kosten, 
* die aus der externen Konfigurationsdatei application.properties gelesen werden.
* Die Klasse verwendet die @Value-Annotation, um die Werte der Eigenschaften elasticsearch.kostenProDokument 
* und elasticsearch.kostenProByte aus der Konfigurationsdatei zu injizieren und dann die Methode 
* areElasticsearchCostsZero() aufzurufen, um zu überprüfen, ob beide Werte gleich 0.0 sind.
*/
//@PropertySource("classpath:/server_config/application.properties")
//@PropertySource("file:H:/Dateien/Informatik/DEVK/HA XI/2.Semester/2.Projekt/Gemeinsamer_Ordner/Dateien/aktueller_Stand/java/ELA/ELA/src/main/resources/server_config/application.properties")
@PropertySource("application.properties")
@Component
public class ApplicationPropertiesInhaltsChecker {
		
		private final static Logger logger = LogFileWriter.getLogger();
	
		public static void main(String[] args) {
			AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ApplicationPropertiesInhaltsChecker.class);
			// Erstelle eine Instanz der ElasticsearchConfigChecker-Klasse
			ApplicationPropertiesInhaltsChecker configChecker = context.getBean(ApplicationPropertiesInhaltsChecker.class);
			
			// Rufe die Methode areElasticsearchCostsZero auf und gib das Ergebnis aus
	        boolean areCostsZero = configChecker.areElasticsearchCostsZero();
	        logger.info("Sind die beiden Variablen fuer die Elasticsearchkosten gleich null:" + areCostsZero);
	        Double areFullCostsZero = configChecker.areElasticsearchGesamtKostenNichtNull();
	        logger.info("Sind die Gesamtkosten richtig ausgelesen worden: " + areFullCostsZero);
	        context.close();
		}
		
		/**
	     * Die Eigenschaft elasticsearch.kostenProDokument, die mit der @Value-Annotation markiert ist,
	     * wird verwendet, um den Wert für die Kosten pro Dokument aus der Konfigurationsdatei 
	     * application.properties zu injizieren.
	     */
	    @Value("${elasticsearch.kostenProDokument}")
	    private double kostenProDokument;
	    
	    /**
	     * Die Eigenschaft elasticsearch.kostenProByte, die mit der @Value-Annotation markiert ist,
	     * wird verwendet, um den Wert für die Kosten pro Byte aus der Konfigurationsdatei 
	     * application.properties zu injizieren.
	     */
	    @Value("${elasticsearch.kostenProByte}")
	    private double kostenProByte;
	    
	    @Value("${elasticsearch.gesamtKosten}")
	    private double gesamtKosten;
	    
	    /**
	     * Die Methode areElasticsearchCostsZero() überprüft, ob die Kosten für Elasticsearch gleich 0.0 sind.
	     * Wenn sowohl kostenProDokument als auch kostenProByte gleich 0.0 sind, wird true zurückgegeben, 
	     * andernfalls wird false zurückgegeben.
	     *
	     * @return true, wenn beide Kostenwerte gleich 0.0 sind, sonst false.
	     */
	    public boolean areElasticsearchCostsZero() {
	        return kostenProDokument == 0.0 && kostenProByte == 0.0;
	    }
	    public double areElasticsearchGesamtKostenNichtNull() {
	    	return gesamtKosten;
	    }
}
