package com.elasticsearch.application.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticsearch.application.configurations.ApplicationpPropertiesVerarbeiter;
import com.elasticsearch.application.configurations.ElasticsearchConfig;
import com.elasticsearch.application.interactions.ElasticInteraction;
import com.elasticsearch.application.calculator.KostenBerechnungNamespaces;

/**
 * Diese Klasse für das Schreiben und Generieren der CSV-Datei verantwortlich.
 */
public class MyFileWriter {

	private final RestHighLevelClient elasticsearchClient;
	private final ApplicationpPropertiesVerarbeiter propertiesVerarbeiter;
	private final ElasticsearchConfig elasticsearchkonfiguration;
	
	private static String startTime = ElasticInteraction.getstartTime();
	private static String csvDateipfad;
	private static String csvAlternativpfad;
	private static String endTime = ElasticInteraction.getendTime();
	
	private static String indexName = "fantasticelastic";
	private List<String> listOfAllNamespaces = ElasticInteraction.getlistOfAllNamespaces();
	private static double kostenProDokument;
	private static double kostenProByte;
	private String elasticIndex;
	private Double gesamtkosten;
	private String waehrung;

	public void setElasticIndex(String elasticIndexNeu) {
		this.elasticIndex = elasticIndexNeu;
	}

	public void setGesamtkosten(Double gesamtKostenNeu) {
		this.gesamtkosten = gesamtKostenNeu;
	}

	public void setWaehrung(String waehrungNeu) {
		this.waehrung = waehrungNeu;
	}

	@Autowired
	public MyFileWriter(RestHighLevelClient elasticsearchClient,
			ApplicationpPropertiesVerarbeiter propertiesVerarbeiter, ElasticsearchConfig elasticsearchkonfiguration,
			List<String> namespaces) {
		this.elasticsearchClient = elasticsearchClient;
		this.elasticsearchkonfiguration = elasticsearchkonfiguration;
		this.propertiesVerarbeiter = propertiesVerarbeiter;
		elasticIndex = propertiesVerarbeiter.getIndexname();
		System.out.println("Elasticsearch Index: " + elasticIndex);
		kostenProDokument = propertiesVerarbeiter.getkostenProDokument();
		System.out.println("Kosten pro Dokument: " + kostenProDokument);
		kostenProByte = propertiesVerarbeiter.getkostenProByte();
		System.out.println("Kosten pro Byte: " + kostenProByte);
		csvDateipfad = propertiesVerarbeiter.getcsvDateiPfad();
		System.out.println("CSV Dateipfad: " + csvDateipfad);
		csvAlternativpfad = propertiesVerarbeiter.getcsvAlternativpfad();
		System.out.println("CSV Alternativpfad: " + csvAlternativpfad);
		try {
			writeDataToCSV(listOfAllNamespaces, elasticsearchClient);
		} catch (IOException e) {
		}
	}

	public static void writeDataToCSV(List<String> namespaces, RestHighLevelClient elasticsearchClient)
			throws IOException {

		int fileSuffix = 1;
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

		Long totalDocumentCount = ElasticInteraction.getTotalDocumentCount(elasticsearchClient, indexName);
		Long indexSize = ElasticInteraction.getIndexSize(elasticsearchClient, indexName);

		double gesamtkostenDokumentenAnzahl = KostenBerechnungNamespaces
				.gesamtKostenAnhandderDokumentenAnzahl(totalDocumentCount, kostenProDokument);
		double gesamtkostenDokumentengroesse = KostenBerechnungNamespaces
				.gesamtKostenAnhandderDokumentenGroesse(indexSize, kostenProByte);

		Map<String, Long> namespaceBytes = ElasticInteraction.getNamespaceBytes(elasticsearchClient, startTime, endTime,
				indexName);
		Map<String, Long> namespaceDocumentCount = ElasticInteraction.getNamespaceDokumente(elasticsearchClient,
				startTime, endTime, indexName);

		try (FileWriter writer = new FileWriter(csvDateipfad)) {
			writer.append("Gefilterter Zeitraum: ").append(startTime).append(" bis ").append(endTime).append("\n");
			writer.append("Gesamtzahl der gefundenen Dokumente im Namespace: ")
					.append(String.valueOf(totalDocumentCount)).append(" Dokumente").append("\n");
			writer.append("Gesamtgröße der gefundenen Dokumente im Namespace: ").append(String.valueOf(indexSize))
					.append(" Bytes").append("\n");
			writer.append("Gesamtkosten des Namespace (Dokumentenanzahl) :  ")
					.append(String.format("%.2f", gesamtkostenDokumentenAnzahl))
					.append(" € " + "(" + kostenProDokument + "€ pro Dokument)")
					.append("\n");
			writer.append("Gesamtkosten des Namespace (Dokumentengröße): ")
					.append(String.format("%.2f", gesamtkostenDokumentengroesse))
					.append(" € " + "(" + kostenProByte + "€ pro Byte)")
					.append("\n");
			writer.append(
					"Namespace                Dokumenten im Namespace          Prozentualer Anteil an allen Dokumenten im Namespace          Namespace Größe (Gesamtgröße aller Dokumente)          Kosten des Namespace (anhand der Dokumentengröße)          Kosten des Namespace (anhand der Dokumentenanzahl)\n");

			for (String namespace : namespaces) {
				Long documentCount = namespaceDocumentCount.getOrDefault(namespace, 0L);
				Long byteSize = namespaceBytes.getOrDefault(namespace, 0L);
				double percentage = (double) documentCount / totalDocumentCount * 100;
				double costBySize = byteSize * kostenProByte;
				double costByCount = documentCount * kostenProDokument;

				writer.append(namespace).append(getSpaces(28 - namespace.length()))
						.append(String.valueOf(documentCount))
						.append(getSpaces(31 - String.valueOf(documentCount).length()))
						.append(String.valueOf(percentage)).append(getSpaces(59 - String.valueOf(percentage).length()))
						.append(String.valueOf(byteSize)).append(getSpaces(95 - String.valueOf(byteSize).length()))
						.append(String.valueOf(costBySize)).append(getSpaces(121 - String.valueOf(costBySize).length()))
						.append(String.valueOf(costByCount)).append("\n");
			}

			System.out.println("Daten wurden erfolgreich in die CSV-Datei geschrieben: " + csvDateipfad);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getSpaces(int count) {
		StringBuilder spaces = new StringBuilder();
		for (int i = 0; i < count; i++) {
			spaces.append(" ");
		}
		return spaces.toString();
	}

	public static void erstelledieCSVanhandderGesamtkosten(List<String> namespaces,
			RestHighLevelClient elasticsearchClient,
			Double gesamtkosten, String waehrung)
			throws IOException {

		int fileSuffix = 1;
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

		Map<String, Long> namespaceBytes = ElasticInteraction.getNamespaceBytes(elasticsearchClient, startTime, endTime,
				indexName);
		Map<String, Long> namespaceDocumentCount = ElasticInteraction.getNamespaceDokumente(elasticsearchClient,
				startTime, endTime, indexName);

		Long anzahlAllerDokumente = ElasticInteraction.getTotalDocumentCount(elasticsearchClient, indexName);
		Long indexGroesseInBytes = ElasticInteraction.getIndexSize(elasticsearchClient, indexName);

		double gesamtkostenDokumentenAnzahl = KostenBerechnungNamespaces
				.gesamtKostenAnhandderDokumentenAnzahl(anzahlAllerDokumente, kostenProDokument);
		double einzelkostenDokumentengroesse = KostenBerechnungNamespaces
				.anteiligeKostenderDokumenteAnhandderIndexGroesse(namespaceBytes, anzahlAllerDokumente, gesamtkosten);
		double einzelkostenproByte = KostenBerechnungNamespaces.einzelkostenproByteAnhandDerIndexgroesse(gesamtkosten, indexGroesseInBytes);
		
		//Map<String, Long> einzelkostenproNamespaceAnhandDerAnzahlDerGesamtAnzahlDerDokumente = KostenBerechnungNamespaces.einzelkostenproDokumentAnhandDerAnzahlDerGesamtAnzahlDerDokumente(namespaceBytes, gesamtkosten, indexGroesseInBytes);
		
		Map<String, Long> dokumentengroeßeproNamespace = ElasticInteraction.getNamespaceBytes(elasticsearchClient, startTime, endTime,
				indexName);
		Map<String, Long> dokumenteproNamespace = ElasticInteraction.getNamespaceDokumente(elasticsearchClient,
				startTime, endTime, indexName);

		try (FileWriter writer = new FileWriter(csvDateipfad)) {
			writer.append("Gefilterter Zeitraum: ").append(startTime).append(" bis ").append(endTime).append("\n");
			writer.append("Gesamtzahl der gefundenen Dokumente im Namespace: ")
					.append(String.valueOf(anzahlAllerDokumente)).append(" Dokumente").append("\n");
			writer.append("Gesamtgröße der gefundenen Dokumente im Namespace: ").append(String.valueOf(indexGroesseInBytes))
					.append(" Bytes").append("\n");
			writer.append("Gesamtkosten des Namespace (Gesamtkostenwert) :  ")
					.append(String.format("%.2f", gesamtkosten))
					.append(" " + waehrung + " (" + einzelkostenDokumentengroesse + waehrung + " pro Dokument)")
					.append("\n");
			writer.append("Gesamtkosten des Namespace (anteilige Dokumentengröße): ")
					.append(String.format("%.2f", einzelkostenproByte))
					.append(" " + waehrung + " (" + gesamtkosten + waehrung + " insgesamt)")
					.append("\n");
			writer.append(
					"Namespace                Dokumenten im Namespace          Prozentualer Anteil an allen Dokumenten im Namespace          Namespace Größe (Gesamtgröße aller Dokumente)          Kosten des Namespace (anhand der Dokumentengröße)          Kosten des Namespace (anhand der Dokumentenanzahl)\n");

			for (String namespace : namespaces) {
				Long documentCount = namespaceDocumentCount.getOrDefault(namespace, 0L);
				Long byteSize = namespaceBytes.getOrDefault(namespace, 0L);
				double percentage = (double) documentCount / anzahlAllerDokumente * 100;
				double costBySize = KostenBerechnungNamespaces.anteiligeKostenAnhandderDokumentenGroesse(namespaceBytes,
						indexGroesseInBytes, gesamtkosten);
				List <String> listeanteiligeKostenproNamespaceDokumente = KostenBerechnungNamespaces.berechneNamespaceDokumenteAnteiligeKosten(gesamtkosten,
						namespaceDocumentCount, anzahlAllerDokumente);
				KostenBerechnungNamespaces.berechneNamespaceDokumenteAnteiligeKostenAusgabe(listeanteiligeKostenproNamespaceDokumente, waehrung);

				String formattedCostBySize = String.format("%.2f", costBySize) + " " + waehrung;
				String formattedCostByCount = String.format("%.2f", listeanteiligeKostenproNamespaceDokumente) + " " + waehrung;

				writer.append(namespace).append(getSpaces(28 - namespace.length()))
						.append(String.valueOf(documentCount))
						.append(getSpaces(31 - String.valueOf(documentCount).length()))
						.append(String.valueOf(percentage)).append(getSpaces(59 - String.valueOf(percentage).length()))
						.append(String.valueOf(byteSize)).append(getSpaces(95 - String.valueOf(byteSize).length()))
						.append(formattedCostBySize).append(getSpaces(121 - formattedCostBySize.length()))
						.append(formattedCostByCount).append("\n");
			}

			System.out.println("Daten wurden erfolgreich in die CSV-Datei geschrieben: " + csvDateipfad);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}