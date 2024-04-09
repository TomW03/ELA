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

@SuppressWarnings({ "unused", "deprecation" })
public class HTMLDateiSchreiber {

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
	 * Der Dateipfad für die CSV-Datei.
	 */
	private String htmlDateipfad;
	/**
	 * Der alternativer Dateipfad für den Fall, dass die ursprüngliche Datei bereits
	 * existiert.
	 */
	private static String htmlAlternativpfad;
	/**
	 * Die Elasticsearch-Konfiguration, die Informationen über die
	 * Elasticsearch-Verbindung enthält.
	 */
	private final ElasticsearchConfig elasticsearchkonfiguration;
	
	
	   public void setHTMLDateipfad(String htmlDateipfad) {
	        this.htmlDateipfad = htmlDateipfad;
	    }
	@Autowired
	public HTMLDateiSchreiber(RestHighLevelClient elasticsearchClient,
			ApplicationpPropertiesVerarbeiter propertiesVerarbeiter, ElasticsearchConfig elasticsearchkonfiguration) {
		this.elasticsearchClient = elasticsearchClient;
		this.propertiesVerarbeiter = propertiesVerarbeiter;
		this.elasticsearchkonfiguration = elasticsearchkonfiguration;
		htmlDateipfad = propertiesVerarbeiter.gethtmlDateiPfad();
		htmlAlternativpfad = propertiesVerarbeiter.gethtmlDateiPfadAlternative();
	}

	public void writeHTML(String indexName, String startZeit, String endZeit, Double gesamtkosten, String waehrung) {
		/*int fileSuffix = 1;
		File file = new File(htmlDateipfad);

		while (file.exists()) {
			htmlDateipfad = htmlAlternativpfad + fileSuffix + ".html";
			file = new File(htmlDateipfad);
			fileSuffix++;
		}*/
		try (FileWriter writer = new FileWriter(htmlDateipfad)) {
			double kostenProDokument = gesamtkosten
					/ ElasticInteraction.getTotalDocumentCount(elasticsearchClient, indexName);
			Long gesamtAnzahlDerDokumenteImIndex = ElasticInteraction.getTotalDocumentCount(elasticsearchClient,
					indexName);
			Long gesamtGroesseDesIndex = ElasticInteraction.getIndexSize(elasticsearchClient, indexName);
			Long gesamtGroesseDesIndexKB = gesamtGroesseDesIndex / 1024;
			Long gesamtGroesseDesIndexMB = gesamtGroesseDesIndex / 1000000;
			Double kostenDesIndexproKB = gesamtkosten / (gesamtGroesseDesIndex / 1024);
			Double kostenDesIndexproMB = gesamtkosten / (gesamtGroesseDesIndex / (1024 ^ 2));

			writer.write("<html><head><title>Ergebnisse</title></head><body>");
			writer.write("<h2>Gefilterter Zeitraum: " + startZeit + " bis " + endZeit + "</h2>");
			writer.write("<h2>Gefilteter Index: " + indexName + "</h2>");
			writer.write("<p>Gesamtzahl der gefundenen Dokumente im Namespace: " + gesamtAnzahlDerDokumenteImIndex
					+ " Dokumente</p>");
			writer.write("<p>Gesamtgröße des Index in Bytes: " + gesamtGroesseDesIndex + " Bytes oder: "
					+ gesamtGroesseDesIndexKB + " KB" + " oder ca.: " + gesamtGroesseDesIndexMB + " MB</p>");
			writer.write("<p>Gesamtkosten des Indices (Dokumentenanzahl): " + gesamtkosten + " (" + kostenProDokument
					+ " " + waehrung + " pro Dokument)</p>");
			writer.write("<p>Gesamtkosten des Indices (Dokumentengröße): " + gesamtkosten + " " + waehrung + " (ca. "
					+ gesamtGroesseDesIndex / kostenProDokument + " " + waehrung + " pro Byte" + "   oder: "
					+ kostenDesIndexproKB + waehrung + "  pro KB" + " oder ca.: " + kostenDesIndexproMB + waehrung
					+ " pro MB )</p>");
			writer.write("<br><br>");
			writer.write("<table border='1' cellpadding='5'>");
			writer.write(
					"<tr><th>Namespace</th><th>Dok. im Namespace</th><th>% Anteil</th><th>Kosten (Dok. Anzahl)</th><th>Namespace Größe</th><th>Kosten (Dok. Größe)</th></tr>");
			Map<String, Long> namespaceDokumente = ElasticInteraction.getNamespaceDokumente(elasticsearchClient,
					startZeit, endZeit, indexName);

			for (Map.Entry<String, Long> entry : namespaceDokumente.entrySet()) {
				String namespace = entry.getKey();
				Long dokumentenAnzahl = entry.getValue();
				double prozentualerAnteil = KostenBerechnungNamespaces.calculatePercentage(dokumentenAnzahl,
						ElasticInteraction.getTotalDocumentCount(elasticsearchClient, indexName));
				double kostenDokumentenanzahl = dokumentenAnzahl * kostenProDokument;

				writer.write("<tr>");
				writer.write("<td>" + namespace + "</td>");
				writer.write("<td>" + dokumentenAnzahl + "</td>");
				writer.write("<td>" + String.format("%.2f%%", prozentualerAnteil) + "</td>");
				writer.write("<td>" + String.format("%.2f", kostenDokumentenanzahl) + "</td>");
				writer.write("<td></td>");
				writer.write("<td></td>");
				writer.write("</tr>");
			}

			writer.write("</table>");
			writer.write("</body></html>");

			logger.info("Daten wurden erfolgreich in die HTML-Datei geschrieben: " + htmlDateipfad);
			
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Das Schreiben der HTML-Datei ist fehlgeschlagen! Möglicher Grund: ",
					e.getMessage());
		}
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
		HTMLDateiSchreiber htmlDateiSchreiber = new HTMLDateiSchreiber(elasticsearchClient, propertiesVerarbeiter,
				elasticsearchkonfiguration);
		htmlDateiSchreiber.writeHTML(indexName, startZeit, endZeit, gesamtkosten, waehrung);
	} catch (BeansException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}*/