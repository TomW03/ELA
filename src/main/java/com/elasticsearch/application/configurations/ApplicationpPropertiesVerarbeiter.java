package com.elasticsearch.application.configurations;

//import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;



@PropertySource("classpath:application.properties")

//@PropertySource("classpath:/server_config/application.properties") //@PropertySource("application.properties") 
//@PropertySource("classpath:/application.properties")
//@PropertySource("application.properties")
//@PropertySource("classpath:/server_config/application.properties")
@Component  // Füge diese Annotation hinzu, damit Spring die Klasse als Bean verwaltet
@SuppressWarnings("unused")
public class ApplicationpPropertiesVerarbeiter {
	

	private static final Logger logger = Logger.getLogger(FileWriter.class.getName());
    private Integer applicationPropertiesDefaultPort;
    private Integer elasticsearchPort;
    private Double kostenProDokument;
    private Double kostenProByte;
	private Double gesamtKosten;
	
	
	@Value ("${anwendung.logDirectory")
	private String logDirectory;
	
    @Value("${anwendung.logDateiPfad}")
    private String logDateiPfad; // Pfad zum log Verzeichnis

    @Value("${anwendung.logDateiName}")
    private String logDateiName;

    @Value("${anwendung.logDateiNameZeitFormat}")
    private String logDateiNameZeitFormat;

    @Value("${anwendung.htmlDateiPfad}")
    private String htmlDateiPfad;

    @Value("${anwendung.htmlDateiPfadAlternative}")
    private String htmlDateiPfadAlternative;

    @Value("${anwendung.applicationPropertiesDefaultPort}")
    private String applicationPropertiesDefaultPortString;

    @Value("${elasticsearch.host}")
    private String elasticsearchHost;

    @Value("${elasticsearch.port}")
    private String elasticsearchPortString;

    @Value("${elasticsearch.username}")
    private String elasticsearchUsername;

    @Value("${elasticsearch.password}")
    private String elasticsearchPassword;

    @Value("${elasticsearch.apiKey}")
    private String elasticsearchApiKey;

    @Value("${elasticsearch.TruststorePath}")
    private String elasticsearchTruststorePath;

    @Value("${elasticsearch.Truststorepassword}")
    private String elasticsearchTruststorepassword;

    @Value("${elasticsearch.certAlias}")
    private String elasticsearchcertAlias;

    @Value("${elasticsearch.CertCN}")
    private String elasticsearchCertCN;

    @Value("${elasticsearch.indexname}")
    private String indexname;

    @Value("${elasticsearch.kostenProDokument}")
    private String kostenproDokumentString;

    @Value("${elasticsearch.kostenProByte}")
    private String kostenProByteString;

    @Value("${elasticsearch.gesamtKosten}")
    private String gesamtKostenString;

    @Value("${elasticsearch.startTime}")
    private String elasticsearchstartTime;

    @Value("${elasticsearch.endTime}")
    private String elasticsearchendTime;

    @Value("${anwendung.csvDateiPfad}")
    private String csvDateiPfad;

    @Value("${anwendung.csvAlternativPfad}")
    private String csvAlternativPfad;

    @Value("${anwendung.waehrung}")
    private String waehrung;
		
	/**
	 * Get-Methode für den Log-Dateipfad
	 * @return logDateiPfad -> Pfad zur Logging-Datei
	 */
	public String getlogDateiPfad() {
		return logDateiPfad;
	}
	
	/**
	 * Get-Methode für den Logdatei-Namen
	 * @return logDateiName -> Name der Logging-Datei (Präfix)
	 */
	public String getlogDateiName() {
		return logDateiName;
	}
	
	/**
	 * Get-Methode für das Zeitformat des Logdateinamen-Zeit-Formats
	 * @return logDateiNameZeitFormat -> Pfad zur Logging-Datei
	 */
	public String getlogDateiNameZeitFormat() {
		return logDateiNameZeitFormat;
	}
	
	/**
	 * Get-Methode für den HTML-Dateipfad
	 * @return htmlDateiPfad -> Pfad zur HTML-Datei
	 */
	public String gethtmlDateiPfad() {
		return htmlDateiPfad;
	}
	
	/** 
	 * Get-Methode für den aternativen HTML-Dateipfad
	 * @return htmlDateiPfadAlternative -> Alternativ-Pfad zur HTML-Datei
	 */
	public String gethtmlDateiPfadAlternative() {
		return htmlDateiPfadAlternative;
	}
	
	/** 
	 * Get-Methode für den in der Application.Properties Datei gesetzten Port für die Anwendung
	 * @return applicationPropertiesDefaultPort -> Standard Port in der Application.Properties
	 */
	public int getApplicationPropertiesDefaultPort() {
		return applicationPropertiesDefaultPort;
	}
	
	/** 
	 * Get-Methode für den in der Application.Properties Datei gesetzten Port fixen Preis pro Dokument
	 * @return kostenProDokument -> gesetzter fixer Preis pro Dokument in der Application.Properties
	 */
	public double getkostenProDokument() {
	return kostenProDokument;
	}
	
	/** 
	 * Get-Methode für den in der Application.Properties Datei gesetzten Port fixen Preis pro Byte
	 * @return kostenProByte -> gesetzter fixer Preis pro Byte in der Application.Properties
	 */
	public double getkostenProByte() {
		return kostenProByte;
		}
	
	/**
	 * Get-Methode für den Indexnamen, welcher in der application.properties Datei gesetzt wurde
	 * @return indexname -> Name des Indices
	 */
	public String getIndexname() {
		return indexname;
	}
	/**
	 * Get-Methode für den Wert der Gesamtkosten, welcher in der application.properties Datei gesetzt wurde
	 * @return gesamtKosten -> Gesamtkosten für alle Namespaces
	 */
	public double getGesamtKosten() {
		return gesamtKosten;
	}
	
	/**
	 * Get-Methode für den Start, des Zeitraums, für den Filters, welcher in der application.properties Datei gesetzt wurde
	 * @return elasticsearchstartTime -> Start des Zeitraums für den Filter
	 */
	public String getelasticsearchstartTime() {
		return this.elasticsearchstartTime;
	}
	
	/**
	 * Get-Methode für das Ende, des Zeitraums,für den Filter, welcher in der application.properties Datei gesetzt wurde
	 * @return elasticsearchstartTime -> Ende des Zeitraums für den Filter
	 */
	public String getelasticsearchendTime() {
		return this.elasticsearchendTime;
	}
	public String getWaehrung() {
		return waehrung;
	}
	public int getelasticsearchPort() {
		return this.elasticsearchPort;
	}
	public double kostenproDokument() {
		return this.kostenproDokument();
	}
	
	public void setlogDateiPfad(String logDirectoryInput) {
		logDateiPfad = logDirectoryInput;
	}
	public void setlogDateiName(String logDateiNameInput) {
		logDateiName = logDateiNameInput;
	}
	public void setlogDateiNameZeitFormat(String logDateiNameZeitFormatInput) {
		logDateiNameZeitFormat = logDateiNameZeitFormatInput;
	}
	public void sethtmlDateiPfad(String htmlDateiPfadInput) {
		htmlDateiPfad = htmlDateiPfadInput;
	}
	public void sethtmlDateiPfadAlternative(String htmlDateiPfadAlternativeInput) {
		htmlDateiPfadAlternative = htmlDateiPfadAlternativeInput;
	}

	public void setkostenProDokument(Double kostenProDokumentInput) {
		this.kostenProDokument = kostenProDokumentInput;
	}
	public void setkostenProByte(Double kostenproByteInput) {
		kostenProByte = kostenproByteInput;
	}
	public void setindexname(String indexNameInput) {
		indexname = indexNameInput;
	}
	public void setGesamtkosten(Double gesamtKostenInput) {
		gesamtKosten = gesamtKostenInput;
	}
	public void  setWaehrung(String waehrungNeu) {
		waehrung = waehrungNeu;
	}
	
	public String getcsvDateiPfad() {
		return csvDateiPfad;
	}

	public String getcsvAlternativpfad() {
		return csvAlternativPfad;
	}
	
	public void setelasticsearchstartTime(String elasticsearchstartTimeNeu) {
		this.elasticsearchstartTime = elasticsearchstartTimeNeu;
	}
	
	public void setelasticsearchendTime(String elasticsearchendTimeNeu) {
		this.elasticsearchendTime = elasticsearchendTimeNeu;
	}
	public void setelasticsearchPort(int elasticsearchPortInput) {
		this.elasticsearchPort = elasticsearchPortInput;
	}
	
	
	
	@PostConstruct
    public void initialize() {
        //loadProperties();
		// Wandle die Strings in Integer um
		this.applicationPropertiesDefaultPort = Integer.parseInt(applicationPropertiesDefaultPortString);
	    this.elasticsearchPort = Integer.parseInt(elasticsearchPortString);
	    this.kostenProDokument = Double.valueOf(kostenproDokumentString);
	    this.kostenProByte = Double.valueOf(kostenProByteString);
	    this.gesamtKosten = Double.valueOf(gesamtKostenString);
	}
	/*
	public void loadProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getResourceAsStream("/application.properties")) {
            properties.load(inputStream);

            // Print out the properties read from the file
            //properties.forEach((key, value) -> System.out.println(key + " = " + value));
            // Get values from the properties file and set the corresponding variables
            logDateiPfad = properties.getProperty("anwendung.logDateiPfad", "C:/Users/Luise/Desktop/Projekt_02/Elasticsearch/generierte_Daten/Logs");
            logDateiName = properties.getProperty("anwendung.logDateiName", "FantasticElasticApp_");
            logDateiNameZeitFormat = properties.getProperty("anwendung.logDateiNameZeitFormat", "yyyyMMdd_HHmmss");
            htmlDateiPfad = properties.getProperty("anwendung.htmlDateiPfad", "C:/Users/Luise/Desktop/Projekt_02/Elasticsearch/generierte_Daten/HTML_Dateien/namespace_hello.html");
            htmlDateiPfadAlternative = properties.getProperty("anwendung.htmlDateiPfadAlternative", "C:/Users/Luise/Desktop/Projekt_02/Elasticsearch/generierte_Daten/HTML_Dateien/namespace_hello_");
            applicationPropertiesDefaultPort = Integer.parseInt(properties.getProperty("anwendung.applicationPropertiesDefaultPort", "8080"));
            kostenProDokument = Double.parseDouble(properties.getProperty("elasticsearch.kostenProDokument", "0.2"));
            kostenProByte = Double.parseDouble(properties.getProperty("elasticsearch.kostenProByte", "0.25"));
            indexname = properties.getProperty("elasticsearch.indexname", "fantasticelastic");
            csvDateiPfad = properties.getProperty("anwendung.csvDateiPfad", "C:/Users/Luise/Desktop/Projekt_02/Elasticsearch/generierte_Daten/CSV_Dateien/namespace_hello.csv");
            csvAlternativPfad = properties.getProperty("anwendung.csvAlternativPfad", "C:/Users/Luise/Desktop/Projekt_02/Elasticsearch/generierte_Daten/CSV_Dateien/namespace_hello_");
            gesamtKosten = Double.parseDouble(properties.getProperty("elasticsearch.gesamtKosten", "100000.98"));

        } catch (IOException e) {
            // Handle IOException if the file cannot be read
        	logger.log(Level.SEVERE, "Fehler: Application.properties Datei kann nicht gelesen werden" + e.getMessage(), e );
        } catch (NumberFormatException e) {
            // Handle NumberFormatException if property values cannot be parsed as numbers
        	logger.log(Level.SEVERE, "Fehler: Ungueltige numerische Werte in der Application.properties Datei", e);
        }
    }*/	
}
