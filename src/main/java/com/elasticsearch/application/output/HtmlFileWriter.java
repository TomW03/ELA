package com.elasticsearch.application.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.elasticsearch.application.calculator.KostenBerechnungNamespaces;
import com.elasticsearch.application.configurations.ApplicationpPropertiesVerarbeiter;
import com.elasticsearch.application.configurations.ElasticsearchConfig;
import com.elasticsearch.application.interactions.ElasticInteraction;

import jakarta.annotation.PostConstruct;

@SuppressWarnings({ "unused", "deprecation" })
public class HtmlFileWriter { 
	
	private RestHighLevelClient elasticsearchClient;
	private final static Logger logger = LogFileWriter.getLogger();
	private ApplicationpPropertiesVerarbeiter propertiesVerarbeiter;
	private ElasticsearchConfig elasticsearchkonfiguration;
	private String elasticIndex;
	private static String htmlDateiPfad;
	private static String htmlDateiPfadAlternative;
	private static String endTime = ElasticInteraction.getendTime();
	private static String startTime = ElasticInteraction.getstartTime();
	private List<String> listOfAllNamespaces = ElasticInteraction.getlistOfAllNamespaces();
	QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();
	private List<Long> documentCount;
	private Long dokumentenGroesse;
	private static double kostenProDokument;
	private static double kostenProByte;
	private static String indexName;
	private static double gesamtkosten;
	private static double kostenproDokumentberechnet;
	
	public HtmlFileWriter(RestHighLevelClient elasticsearchClient, ApplicationpPropertiesVerarbeiter propertiesVerarbeiter, ElasticsearchConfig elasticsearchkonfiguration,List<String> namespaces) {
		this.elasticsearchClient = elasticsearchClient;
		this.elasticsearchkonfiguration = elasticsearchkonfiguration;
		this.propertiesVerarbeiter = propertiesVerarbeiter;
		this.elasticsearchClient = elasticsearchClient;
		this.elasticsearchkonfiguration = elasticsearchkonfiguration;
		this.propertiesVerarbeiter = propertiesVerarbeiter;
		elasticIndex = propertiesVerarbeiter.getIndexname();
		indexName = propertiesVerarbeiter.getIndexname();
		kostenProDokument = propertiesVerarbeiter.getkostenProDokument();
		htmlDateiPfad = propertiesVerarbeiter.gethtmlDateiPfad();
		htmlDateiPfadAlternative = propertiesVerarbeiter.gethtmlDateiPfadAlternative();
		gesamtkosten = propertiesVerarbeiter.getGesamtKosten();
		try {
			documentCount = ElasticInteraction.getDocumentCount(elasticsearchClient, elasticIndex, queryBuilder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			dokumentenGroesse = ElasticInteraction.getTotalDocumentCount(elasticsearchClient, elasticIndex);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			writeDataToHTML(startTime, endTime,listOfAllNamespaces, documentCount, dokumentenGroesse ,elasticsearchClient, elasticIndex, gesamtkosten);
		} catch(IOException e) {}
	}
	//@SuppressWarnings({ "unused" })
	@PostConstruct
	public static void writeDataToHTML(String startTime, String endTime, List<String> namespaces,
	        List<Long> documentCounts, Long dokumentenGroesse, RestHighLevelClient elasticsearchClient,
	        String elasticindex, Double gesamtkosten) throws IOException {
	    String htmlFilePath = htmlDateiPfad;
	    int fileSuffix = 1;
	    File file = new File(htmlFilePath);
	    
	    // Überprüfe ob der Log-Datei-Pfad existiert und erstellt ihn, falls er nicht existieren sollte
	 		File logDir = new File(htmlDateiPfad);
	 		if (!logDir.exists()) {
	 			System.out.println(
	 					"Das Log-Verzeichnis existiert noch nicht und wird nun, auf dem folgenden lokalen Pfad, erstellt: "
	 							+ htmlDateiPfad);
	 			if (!logDir.mkdirs()) {
	 				System.out.println("Fehler beim Erstellen des Log-Verzeichnisses unter: " + htmlDateiPfad);
	 				return;
	 			}
	 		}
	    
	    // Generiere einen eindeutigen Dateinamen, falls eine Datei mit dem gleichen Namen bereits vorhanden ist
	    while (file.exists()) {
	    	htmlDateiPfad = htmlDateiPfadAlternative + fileSuffix + ".html";
	        file = new File(htmlDateiPfad);
	        fileSuffix++;
	    }

	    try (FileWriter writer = new FileWriter(htmlDateiPfad)) {
	        writer.append("<html>\n");
	        writer.append("<head>\n");
	        writer.append("<title>Index-Auswertung</title>\n");
	        writer.append("<style>\n");
	        writer.append("body {\n");
	        writer.append("    font-family: Arial, sans-serif;\n");
	        writer.append("    background-color: #f2f2f2;\n");
	        writer.append("    margin: 0;\n");
	        writer.append("    padding: 0;\n");
	        writer.append("    color: #333333;\n");
	        writer.append("}\n");
	        writer.append("h1 {\n");
	        writer.append("    color: #008746;\n");
	        writer.append("    margin-top: 20px;\n");
	        writer.append("}\n");
	        writer.append("p {\n");
	        writer.append("    color: #666666;\n");
	        writer.append("}\n");
	        writer.append("table {\n");
	        writer.append("    width: 100%;\n");
	        writer.append("    border-collapse: collapse;\n");
	        writer.append("}\n");
	        writer.append("th {\n");
	        writer.append("    background-color: #008746;\n");
	        writer.append("    color: #fff;\n");
	        writer.append("    padding: 10px;\n");
	        writer.append("}\n");
	        writer.append("td {\n");
	        writer.append("    padding: 10px;\n");
	        writer.append("    text-align: center;\n");
	        writer.append("}\n");
	        writer.append("tr:nth-child(even) {\n");
	        writer.append("    background-color: #f2f2f2;\n");
	        writer.append("}\n");
	        writer.append("tr:hover {\n");
	        writer.append("    background-color: #fdc75f;\n");
	        writer.append("}\n");
	        writer.append(".namespace {\n");
	        writer.append("    background-color: #fff;\n");
	        writer.append("}\n");
	        writer.append(".document-count {\n");
	        writer.append("    background-color: #d8f1a0;\n");
	        writer.append("}\n");
	        writer.append(".percentage {\n");
	        writer.append("    background-color: #b5e7b2;\n");
	        writer.append("}\n");
	        writer.append(".namespace-size {\n");
	        writer.append("    background-color: #ffd79d;\n");
	        writer.append("}\n");
	        writer.append(".cost-by-size {\n");
	        writer.append("    background-color: #f8cfd8;\n");
	        writer.append("}\n");
	        writer.append(".cost-by-count {\n");
	        writer.append("    background-color: #e4c8f3;\n");
	        writer.append("}\n");
	        writer.append("</style>\n");
	        writer.append("</head>\n");
	        writer.append("<body>\n");
	        writer.append("<h1>Gefilterter Zeitraum: ").append(startTime).append(" bis ").append(endTime)
	                .append("</h1>\n");
	        writer.append("<p>Gesamtzahl der gefundenen Dokumente im Namespace: ")
	                .append(String.valueOf(dokumentenGroesse)).append(" Dokumente</p>\n");
	        writer.append("<p>Gesamtgröße der gefundenen Dokumente im Namespace: ")
	                .append(String.valueOf(ElasticInteraction.getIndexSize(elasticsearchClient, elasticindex)))
	                .append("</p>\n");
	        writer.append("<table>\n");
	        writer.append("<tr>\n");
	        writer.append("<th class=\"namespace\">Namespace</th>\n");
	        writer.append("<th class=\"document-count\">Anzahl Dokumente im Namespace</th>\n");
	        writer.append("<th class=\"percentage\">Prozentualer Anteil an allen Dokumenten im Index</th>\n");
	        writer.append("<th class=\"namespace-size\">Namespace Größe (Gesamtgröße aller Dokumente)</th>\n");
	        writer.append("<th class=\"cost-by-size\">Kosten des Namespaces (anhand der Dokumentengröße)</th>\n");
	        writer.append("<th class=\"cost-by-count\">Kosten des Namespaces (anhand der Dokumentenanzahl)</th>\n");
	        writer.append("</tr>\n");

	        Long GesamtAnzahlderDokumenteImIndex = ElasticInteraction.getTotalDocumentCount(elasticsearchClient, indexName);
	        Long indexGroesseInBytes = ElasticInteraction.getIndexSize(elasticsearchClient, indexName);

	        
	        Map<String, Long> namespaceDocumentCount = ElasticInteraction.getNamespaceDokumente(elasticsearchClient,
	                startTime, endTime, elasticindex);

	        double gesamtkostenDokumentenAnzahl = KostenBerechnungNamespaces.gesamtKostenAnhandderDokumentenAnzahl(GesamtAnzahlderDokumenteImIndex, kostenProDokument);
	        double gesamtkostenDokumentengroesse = KostenBerechnungNamespaces.gesamtKostenAnhandderDokumentenGroesse(indexGroesseInBytes, kostenProByte);

	        for (String namespace : namespaces) {
	            Long DokumentenAnzahlListe = namespaceDocumentCount.getOrDefault(namespace, 0L);
	            double percentage = (double) DokumentenAnzahlListe / GesamtAnzahlderDokumenteImIndex * 100;
	            
	            if(gesamtkosten != null) {
	            kostenproDokumentberechnet = gesamtkosten / (double) DokumentenAnzahlListe; 
	            }else {
	            kostenproDokumentberechnet = kostenProDokument * DokumentenAnzahlListe;
	            }
	            writer.append("<tr>\n");
	            writer.append("<td class=\"namespace\">").append(namespace).append("</td>\n");
	            writer.append("<td class=\"document-count\">").append(String.valueOf(DokumentenAnzahlListe)).append(" stk.</td>\n");
	            writer.append("<td class=\"percentage\">").append(String.valueOf(percentage)).append(" %</td>\n");
	            writer.append("<td class=\"cost-by-count\">").append(String.valueOf(kostenproDokumentberechnet)).append(" €</td>\n");
	            writer.append("</tr>\n");
	        }
	        
	        Map<String, Long> anzahlDokumenteAllerNamespaces = ElasticInteraction.getNamespaceDokumente(elasticsearchClient, startTime, endTime, elasticindex);
	        Long anzahlDokumenteOhneNamespace = ElasticInteraction.getNamespaceDokumente(elasticsearchClient, startTime, endTime, elasticindex).values().stream()
	                .reduce(0L, Long::sum);
	        Long ohneNamespaceGesamt = anzahlDokumenteAllerNamespaces.values().stream()
	                .reduce(0L, Long::sum) - anzahlDokumenteOhneNamespace;
	      
	       
	       // Dokumente ohne Namespace
	        if (ohneNamespaceGesamt > 0) {
	            Long withoutNamespacePercentage = ohneNamespaceGesamt / GesamtAnzahlderDokumenteImIndex * 100;
	            double withoutNamespaceCostByCount = ohneNamespaceGesamt * kostenProDokument;

	            writer.append("<tr>\n");
	            writer.append("<td class=\"namespace\">Kein Namespace</td>\n");
	            writer.append("<td class=\"document-count\">").append(String.valueOf(ohneNamespaceGesamt)).append(" stk.</td>\n");
	            writer.append("<td class=\"percentage\">").append(String.valueOf(withoutNamespacePercentage)).append(" %</td>\n");
	            writer.append("<td class=\"cost-by-count\">").append(String.valueOf(withoutNamespaceCostByCount)).append(" €</td>\n");
	            writer.append("</tr>\n");
	        }

	        writer.append("</table>\n");
	        writer.append("</body>\n");
	        writer.append("</html>");

	        logger.info("Daten wurden erfolgreich in die HTML-Datei geschrieben: " + htmlDateiPfad);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
}
