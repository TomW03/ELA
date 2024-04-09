package com.elasticsearch.application.calculator;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.elasticsearch.application.configurations.ApplicationpPropertiesVerarbeiter;
import com.elasticsearch.application.configurations.ElasticsearchConfig;
import com.elasticsearch.application.interactions.ElasticInteraction;
import com.elasticsearch.application.output.LogFileWriter;

/**
 * Diese Klasse enthält Methoden zur Berechnung von Kosten und anteiligen Kosten für verschiedene Aspekte im Zusammenhang mit Elasticsearch-Indizes und Dokumenten.
 */
public class KostenBerechnungNamespaces {
	private final static Logger logger = LogFileWriter.getLogger();
	private final ElasticsearchConfig elasticsearchkonfiguration;
	private RestHighLevelClient elasticsearchClient;

	@Autowired
	public KostenBerechnungNamespaces(RestHighLevelClient elasticsearchClient,
			ApplicationpPropertiesVerarbeiter propertiesVerarbeiter, ElasticsearchConfig elasticsearchkonfiguration) {
		this.elasticsearchClient = elasticsearchClient;
		this.elasticsearchkonfiguration = elasticsearchkonfiguration;

	}

	/**
	 * Berechnet den prozentualen Anteil eines Werts an einem Gesamtwert.
	 *
	 * @param value Der Wert, dessen Anteil berechnet werden soll
	 * @param total Der Gesamtwert
	 * @return Der prozentuale Anteil des Werts am Gesamtwert
	 */
	public static double calculatePercentage(long value, long total) {
		if (total == 0) {
			return 0.0;
		}
		return (value * 100.0) / total;
	}

	public static long BerechneGroesseDokumenteOhneNamespace(Map<String, Long> documentSize, long IndexsizeInBytes) {
		long summe = 0;
		for (Long size : documentSize.values()) {
			summe += size;
		}
		if (summe >= IndexsizeInBytes) {
			return IndexsizeInBytes - summe;
		} else {
			logger.warning(
					"Fehler! Groesse der Dokumente ist groesser als die Indexgroesse, irgendwo liegt ein Fehler vor!");
			return IndexsizeInBytes - summe;
		}
	}

	// Neu

	public static double gesamtKostenAnhandderDokumentenAnzahl(Long totalDocumentCount, Double kostenProDokument) {
		return totalDocumentCount * kostenProDokument;
	}

	public static double gesamtKostenAnhandderDokumentenGroesse(Long indexSize, Double kostenProByte) {
		return indexSize * kostenProByte;
	}

	public static double anteiligeKostenAnhandderDokumentenGroesse(Map<String, Long> documentSize,
			long IndexsizeInBytes, Double Gesamtkosten) {
		Double summe = 0.0;
		for (Long size : documentSize.values()) {
			summe += size;
		}
		if (summe >= IndexsizeInBytes) {
			return IndexsizeInBytes - summe;
		} else {
			logger.warning(
					"Fehler! Groesse der Dokumente ist groesser als die Indexgroesse, irgendwo liegt ein Fehler vor!");
			return IndexsizeInBytes - summe;
		}
	}

	/**
	 * Berechnet die anteiligen Kosten für jeden Namespace basierend auf den
	 * gegebenen Gesamtkosten und der Anzahl der Dokumente pro Namespace.
	 *
	 * @param gesamtKosten              Die Gesamtkosten für alle Namespaces.
	 * @param namespaceDokumentenAnzahl Eine Map, die die Namen der Namespaces als
	 *                                  Schlüssel und die Anzahl der Dokumente pro
	 *                                  Namespace als Wert enthält.
	 * @return Eine Liste von Strings, die die Zeilen der Tabelle mit den anteiligen
	 *         Kosten für jeden Namespace enthalten. Jeder String in der Liste
	 *         repräsentiert eine Zeile der Tabelle mit dem Format
	 *         "Namespace\t\tKosten in Euro", wobei "\t" einen Tabulator darstellt.
	 */
	public static List<String> berechneNamespaceDokumenteAnteiligeKosten(double gesamtKosten,
			Map<String, Long> namespaceDokumentenAnzahl, long totalDocumentCount) {
		List<String> listeanteiligeKostenproNamespaceDokumente = new ArrayList<>();
		DecimalFormat decimalFormat = new DecimalFormat("#0.00");

		double kostenProDokument = gesamtKosten / totalDocumentCount;

		for (Map.Entry<String, Long> entry : namespaceDokumentenAnzahl.entrySet()) {
			String namespace = entry.getKey();
			Long dokumentenAnzahl = entry.getValue();
			double kostenFürNamespace = kostenProDokument * dokumentenAnzahl;

			String tableRow = namespace + "\t\t\t" + decimalFormat.format(kostenFürNamespace);
			listeanteiligeKostenproNamespaceDokumente.add(tableRow);
		}

		return listeanteiligeKostenproNamespaceDokumente;
	}

	public static void berechneNamespaceDokumenteAnteiligeKostenAusgabe(
	        List<String> listeanteiligeKostenproNamespaceDokumente, String waehrung) {
	    List<String> kostenTabelle = listeanteiligeKostenproNamespaceDokumente;
	    
	    int maxNamespaceLength = kostenTabelle.stream()
	            .mapToInt(row -> row.substring(27).indexOf('\t')) // Find the tab after the namespace
	            .max().orElse(0); // Get the maximum namespace length
	    
	    System.out.println("Namespace" + " ".repeat(maxNamespaceLength) + "\t\tKosten in " + waehrung);
	    System.out.println("-".repeat(50 + maxNamespaceLength));
	    
	    kostenTabelle.forEach(row -> {
	        String namespacePart = row.substring(0, 27); // Extract the namespace part
	        String costPart = row.substring(27); // Extract the cost part
	        String[] parts = costPart.split("\\s+");
	        String euroAmount = parts[0];
	        String formattedCostPart = parts[1];
	        System.out.println(namespacePart + " ".repeat(maxNamespaceLength - namespacePart.length()) +
	                euroAmount + " Euro" + "\t" + formattedCostPart);
	    });
	}

	public static double anteiligeKostenderDokumenteAnhandderIndexGroesse(Map<String, Long> namespaceBytes,
			Long indexGroesseInBytes, Double gesamtkosten) {
		Double einzelkostenDokumentengroesse = indexGroesseInBytes / gesamtkosten;
		return einzelkostenDokumentengroesse;
	}

	public static double einzelkostenproByteAnhandDerIndexgroesse(Double gesamtkosten, Long indexGroesseInBytes) {
		double einzelkostenproByte = gesamtkosten / indexGroesseInBytes;
		return einzelkostenproByte;
	}

	public static List<String> einzelkostenproDokumentAnhandDerAnzahlDerGesamtAnzahlDerDokumente(
			Map<String, Long> namespaceDokumente, Double gesamtkosten, Long gesamtanzahlderDokumente) {

		List<String> kostenProNamespaceDokumente = new ArrayList<>();
		DecimalFormat decimalFormat = new DecimalFormat("#0.00");

		double kostenProDokument = gesamtkosten / gesamtanzahlderDokumente;

		for (Map.Entry<String, Long> entry : namespaceDokumente.entrySet()) {
			String namespace = entry.getKey();
			Long dokumentenAnzahl = entry.getValue();
			double kostenFürNamespace = kostenProDokument * dokumentenAnzahl;

			String tableRow = namespace + "\t\t\t" + decimalFormat.format(kostenFürNamespace);
			kostenProNamespaceDokumente.add(tableRow);
		}

		return kostenProNamespaceDokumente;
	}

	public static void main(String[] args) {

		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ElasticsearchConfig.class);

		// Get the ElasticsearchConfig bean
		ElasticsearchConfig elasticsearchConfig = context.getBean(ElasticsearchConfig.class);

		ApplicationpPropertiesVerarbeiter propertiesVerarbeiter = context
				.getBean(ApplicationpPropertiesVerarbeiter.class);

		// Get the RestHighLevelClient from the ElasticsearchConfig bean
		RestHighLevelClient elasticsearchClient = elasticsearchConfig.getElasticsearchClient();

		// Provide the necessary dependencies
		KostenBerechnungNamespaces neu = new KostenBerechnungNamespaces(elasticsearchClient, propertiesVerarbeiter,
				elasticsearchConfig);
		String indexName = "fantasticelastic";
		String startTime = "2023-04-01T00:00:00Z";
		String endTime = "2023-04-30T23:59:59Z";
		Double gesamtkosten = 10000.00;
		String waehrung = "Euro";

		Map<String, Long> namespaceBytes = null;
		try {
			namespaceBytes = ElasticInteraction.getNamespaceBytes(elasticsearchClient, startTime, endTime, indexName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Long anzahlAllerDokumente = null;
		try {
			anzahlAllerDokumente = ElasticInteraction.getTotalDocumentCount(elasticsearchClient, indexName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Long indexGroesseInBytes = null;
		try {
			indexGroesseInBytes = ElasticInteraction.getIndexSize(elasticsearchClient, indexName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Test methods
		double einzelkostenproByte = einzelkostenproByteAnhandDerIndexgroesse(gesamtkosten, indexGroesseInBytes);
		double einzelkostenproDokument = anzahlAllerDokumente / gesamtkosten;

		Map<String, Long> namespaceDokumente = null;
		try {
			namespaceDokumente = ElasticInteraction.getNamespaceDokumente(elasticsearchClient, startTime, endTime,
					indexName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<String> einzelkostenNamespacesproDokument = einzelkostenproDokumentAnhandDerAnzahlDerGesamtAnzahlDerDokumente(
				namespaceDokumente, gesamtkosten, anzahlAllerDokumente);
		Double IndexgroesseInMB = (double) (indexGroesseInBytes / 1000000);
		// Display results
		System.out.println("Gesamtkosten: " + gesamtkosten);
		System.out.println("Gesamtanzahl der Dokumente im Index: " + anzahlAllerDokumente);
		System.out.println("Gesamtgroesse des Index: " + indexGroesseInBytes + " Bytes");
		System.out.println("Anteilige Kosten der Dokumente anhand der Dokumentenanzahl: " + einzelkostenproDokument
				+ " " + waehrung);
		System.out.println("Einzelkosten pro Byte anhand der Indexgroesse: " + einzelkostenproByte + " " + waehrung);
		System.out.println(
				"Einzelkosten pro MB anhand der Groesse des Index: " + einzelkostenproByte / 1000000 + " " + waehrung);

		for (Map.Entry<String, Long> entry : namespaceDokumente.entrySet()) {
			String namespace = entry.getKey();
			Long dokumentenAnzahl = entry.getValue();
			System.out.println("Namespace: " + namespace + ", Dokumenten Anzahl: " + dokumentenAnzahl);
		}
		for (String row : einzelkostenNamespacesproDokument) {
			System.out.println("(Berechnung auf grundlage der Dokumente) " + row + " " + waehrung);
		}
	}

}