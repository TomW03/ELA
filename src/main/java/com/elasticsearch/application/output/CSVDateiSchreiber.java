package com.elasticsearch.application.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.elasticsearch.application.calculator.KostenBerechnungNamespaces;
import com.elasticsearch.application.configurations.ApplicationpPropertiesVerarbeiter;
import com.elasticsearch.application.configurations.ElasticsearchConfig;
import com.elasticsearch.application.interactions.ElasticInteraction;
import com.elasticsearch.application.views.main.MainView;

/**
 * Diese Klasse dient dazu, eine CSV-Datei zu erstellen und mit Informationen
 * über einen gefilterten Zeitraum, einen Index und die Kosten für verschiedene
 * Aspekte im Zusammenhang mit Elasticsearch-Indizes und Dokumenten zu füllen.
 * Die Klasse verwendet den Elasticsearch-Client, um die erforderlichen Daten
 * abzurufen, und erstellt dann eine CSV-Datei mit den gesammelten
 * Informationen.
 */
@SuppressWarnings({ "unused", "deprecation" })
public class CSVDateiSchreiber {

	private final static Logger logger = LogFileWriter.getLogger();
	/**
	 * Der Elasticsearch-Client, der für die Kommunikation mit dem
	 * Elasticsearch-Server verwendet wird.
	 */
	private final RestHighLevelClient elasticsearchClient;
	/**
	 * Ein Bean für die Verarbeitung von Anwendungseigenschaften.
	 */
	private final ApplicationpPropertiesVerarbeiter propertiesVerarbeiter;
	/**
	 * Die Elasticsearch-Konfiguration, die Informationen über die
	 * Elasticsearch-Verbindung enthält.
	 */
	private final ElasticsearchConfig elasticsearchkonfiguration;
	/**
	 * Der Dateipfad für die CSV-Datei.
	 */
	private String csvDateipfad;
	/**
	 * Der alternativer Dateipfad für den Fall, dass die ursprüngliche Datei bereits
	 * existiert.
	 */
	private static String csvAlternativpfad;
	
	
	public void setCSVDateipfad(String csvDateipfad) {
        this.csvDateipfad = csvDateipfad;
    }
	/**
	 * Konstruktor, der die Abhängigkeiten initialisiert.
	 *
	 * @param elasticsearchClient        Der Elasticsearch-Client.
	 * @param propertiesVerarbeiter      Ein Bean für die Verarbeitung von
	 *                                   Anwendungseigenschaften.
	 * @param elasticsearchkonfiguration Die Elasticsearch-Konfiguration.
	 */
	@Autowired
	public CSVDateiSchreiber(RestHighLevelClient elasticsearchClient,
			ApplicationpPropertiesVerarbeiter propertiesVerarbeiter, ElasticsearchConfig elasticsearchkonfiguration) {
		this.elasticsearchClient = elasticsearchClient;
		this.elasticsearchkonfiguration = elasticsearchkonfiguration;
		this.propertiesVerarbeiter = propertiesVerarbeiter;
		csvDateipfad = propertiesVerarbeiter.getcsvDateiPfad();
		csvAlternativpfad = propertiesVerarbeiter.getcsvAlternativpfad();
	}

	/**
	 * Diese Methode erstellt eine CSV-Datei mit Informationen über den gefilterten
	 * Zeitraum, den Index, die Gesamtkosten und die anteiligen Kosten für
	 * verschiedene Aspekte im Zusammenhang mit Elasticsearch-Indizes und
	 * Dokumenten.
	 *
	 * @param indexName    Der Name des Indexes.
	 * @param startZeit    Der Startzeitpunkt des gefilterten Zeitraums.
	 * @param endZeit      Der Endzeitpunkt des gefilterten Zeitraums.
	 * @param gesamtkosten Die Gesamtkosten.
	 * @param waehrung     Die Währungseinheit.
	 */
	public void writeCSV(String indexName, String startZeit, String endZeit, Double gesamtkosten, String waehrung) {
	/*	int fileSuffix = 1;
		File file = new File(csvDateipfad);

		// Überprüfen, ob bereits eine Datei mit dem Namen vorhanden ist
		while (file.exists()) {
			csvDateipfad = csvAlternativpfad + fileSuffix + ".csv";
			file = new File(csvDateipfad);
			fileSuffix++;
		}

		// Erstelle das Verzeichnis, falls es noch nicht vorhanden ist
		File directory = file.getParentFile();
		if (!directory.exists()) {
			directory.mkdirs();
		}
		*/
		try (FileWriter writer = new FileWriter(csvDateipfad)) {
			double kostenProDokument = gesamtkosten
					/ ElasticInteraction.getTotalDocumentCount(elasticsearchClient, indexName);
			Long gesamtAnzahlDerDokumenteImIndex = ElasticInteraction.getTotalDocumentCount(elasticsearchClient,
					indexName);
			Long gesamtGroesseDesIndex = ElasticInteraction.getIndexSize(elasticsearchClient, indexName);
			Long gesamtGroesseDesIndexKB = gesamtGroesseDesIndex / 1024;
			Long gesamtGroesseDesIndexMB = gesamtGroesseDesIndex / 1000000;
			Double kostenDesIndexproKB = gesamtkosten / (gesamtGroesseDesIndex / 1024);
			Double kostenDesIndexproMB = gesamtkosten / (gesamtGroesseDesIndex / (1024 ^ 2));

			writer.append("Gefilterter Zeitraum: ").append(startZeit).append(" bis ").append(endZeit).append("\n");
			writer.append("Gefilteter Index: ").append(indexName).append("\n");
			writer.append("Gesamtzahl der gefundenen Dokumente im Namespace: ")
					.append(gesamtAnzahlDerDokumenteImIndex + " Dokumente").append("\n");
			writer.append("Gesamtgröße des Index in Bytes: ").append(gesamtGroesseDesIndex + " Bytes oder: "
					+ gesamtGroesseDesIndexKB + " KB" + " oder ca.: " + gesamtGroesseDesIndexMB + " MB").append("\n");
			writer.append("Gesamtkosten des Indices (Dokumentenanzahl): ")
					.append(String.valueOf(gesamtkosten) + " (" + kostenProDokument + " " + waehrung + " pro Dokument)")
					.append("\n");
			writer.append("Gesamtkosten des Indices (Dokumentengröße): ")
					.append(String.valueOf(gesamtkosten) + " " + waehrung + " (ca. "
							+ gesamtGroesseDesIndex / kostenProDokument + " " + waehrung + " pro Byte" + "   oder: "
							+ kostenDesIndexproKB + waehrung + "  pro KB" + " oder ca.: " + kostenDesIndexproMB
							+ waehrung + " pro MB )")
					.append("\n").append("\n");

			writer.append("Namespace").append(getSpaces(15 - "Namespace".length())).append("Dok. im Namespace")
		      .append(getSpaces(20 - "Dok. im Namespace".length())).append("% Anteil")
		      .append(getSpaces(15 - "% Anteil".length())).append("Kosten (Dok. Anzahl)")
		      .append(getSpaces(25 - "Kosten (Dok. Anzahl)".length())).append("Namespace Größe")
		      .append(getSpaces(20 - "Namespace Größe".length())).append("Kosten (Dok. Größe)")
		      .append(getSpaces(20 - "Kosten (Dok. Größe)".length())).append("\n");

		// Retrieve namespace documents
		Map<String, Long> namespaceDokumente = ElasticInteraction.getNamespaceDokumente(elasticsearchClient,
		      startZeit, endZeit, indexName);

		// Write namespace details
		for (Map.Entry<String, Long> entry : namespaceDokumente.entrySet()) {
		    String namespace = entry.getKey();
		    Long dokumentenAnzahl = entry.getValue();
		    double prozentualerAnteil = KostenBerechnungNamespaces.calculatePercentage(dokumentenAnzahl,
		            ElasticInteraction.getTotalDocumentCount(elasticsearchClient, indexName));
		    double kostenDokumentenanzahl = dokumentenAnzahl * kostenProDokument;

		    writer.append(namespace).append(getSpaces(15 - namespace.length()))
		            .append(String.valueOf(dokumentenAnzahl))
		            .append(getSpaces(20 - String.valueOf(dokumentenAnzahl).length()))
		            .append(String.format("%.2f%%", prozentualerAnteil))
		            .append(getSpaces(15 - String.format("%.2f%%", prozentualerAnteil).length()))
		            .append(String.format("%.2f", kostenDokumentenanzahl))
		            .append(getSpaces(25 - String.format("%.2f", kostenDokumentenanzahl).length()))
		            .append(getSpaces(20)).append("\n");
		}
			logger.info("Daten wurden erfolgreich in die CSV-Datei geschrieben: " + csvDateipfad);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Das schreiben der CSV-Datei ist fehlgeschlagen! Möglicher Grund: ",
					e.getMessage());
		}
	}

	private static String getSpaces(int count) {
		StringBuilder spaces = new StringBuilder();
		for (int i = 0; i < count; i++) {
			spaces.append(" ");
		}
		return spaces.toString();
	}
}

/**
 * Die Hauptmethode, die das Erstellen und Schreiben der CSV-Datei auslöst.
 *
 * @param args Die Befehlszeilenargumente (nicht verwendet).
 */
/*public static void main(String[] args) {
	String indexName = "fantasticelastic";
	String startZeit = "2023-04-01T00:00:00Z";
	String endZeit = "2023-04-30T23:59:00Z";
	Double gesamtkosten = 10000.00;
	String waehrung = "€";

	try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
			ElasticsearchConfig.class)) {
		// Hole das ElasticsearchConfig bean
		ElasticsearchConfig elasticsearchkonfiguration = context.getBean(ElasticsearchConfig.class);
		ApplicationpPropertiesVerarbeiter propertiesVerarbeiter = context
				.getBean(ApplicationpPropertiesVerarbeiter.class);
		// Hole den RestHighLevelClient von dem ElasticsearchConfig bean
		RestHighLevelClient elasticsearchClient = elasticsearchkonfiguration.getElasticsearchClient();
		// Stelle die nötigen Ressourcen zur Verfügung
		CSVDateiSchreiber csvDateiSchreiber = new CSVDateiSchreiber(elasticsearchClient, propertiesVerarbeiter,
				elasticsearchkonfiguration);
		csvDateiSchreiber.writeCSV(indexName, startZeit, endZeit, gesamtkosten, waehrung);
	} catch (BeansException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}*/