// ElasticInteraction.java
package com.elasticsearch.application.interactions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.client.indices.GetMappingsResponse;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ParsedValueCount;
import org.elasticsearch.search.aggregations.metrics.Sum;
import org.elasticsearch.search.aggregations.metrics.SumAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ValueCount;
import org.elasticsearch.search.aggregations.metrics.ValueCountAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;

import com.elasticsearch.application.configurations.ApplicationpPropertiesVerarbeiter;
import com.elasticsearch.application.configurations.ElasticsearchConfig;
import com.elasticsearch.application.output.*;
import com.elasticsearch.application.calculator.KostenBerechnungNamespaces;

@SuppressWarnings("deprecation")
@Controller
public class ElasticInteraction {
	
	private final static Logger logger = LogFileWriter.getLogger();
	//private static final String indexname = ApplicationpPropertiesVerarbeiter.getIndexname(); // "richtigerindex";
	private final RestHighLevelClient elasticsearchClient;
	private final ApplicationpPropertiesVerarbeiter propertiesVerarbeiter;
	private final ElasticsearchConfig elasticsearchkonfiguration;
	private static String startTime = "2023-04-01T00:00:00Z"; // YYYY-MM-DDTHH:MM:SSZ;
	private static String endTime = "2023-04-30T23:59:59Z";
	
	private static List<String> listOfAllNamespaces;
	QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();
	private String indexname;
	private String elasticIndex;
	private Double kostenProDokument;
	private Double kostenProByte;
	private Double gesamtKosten;
	private String waehrung;
	private static String timestamp = "@timestamp";
	private static String mappingFeld = "properties";
	private static String ausgewaehltesMappingFeld = "kubernetes_namespace";
	private static String ausgewaehlteAggregrationsname = "namespaces";
	/* 
	 * private String elasticTimeField = "kubernetes_namespace"; private String
	 * elasticFieldTime = "kubernetes_namespace"; private String
	 * elasticFieldNamespace = "kubernetes_namespace"; private String
	 * elasticFieldSize = "_size";
	 */

	public String getIndexName() {
		return indexname;
	}
	public static String getstartTime() {
		return startTime;
	}
	public void setstartTime(String neueStartZeit) {
		startTime = neueStartZeit;
	}
	public static String getendTime() {
		return endTime;
	}
	public void setendTime(String neueEndZeit) {
		startTime = neueEndZeit;
	}
	public static List<String> getallNamespaces(RestHighLevelClient elasticsearchClient, String indexname) {
		if (listOfAllNamespaces == null) {
			try {
				listOfAllNamespaces = getAllNamespaces(elasticsearchClient,indexname);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	}
		return listOfAllNamespaces;
	}
	public double getkostenProDokument() {
		return kostenProDokument;
	}
	public void setkostenProDokument(double dokumentkosten) {
		kostenProDokument = dokumentkosten;
	}
	public double getkostenProByte() {
		return kostenProByte;
	}
	public void setkostenProByte(double bytekosten) {
		kostenProByte = bytekosten;
	}
	public static List<String> getlistOfAllNamespaces(){
		return listOfAllNamespaces;
	}
	public void setWaehrung(String waehrungNeu) {
		this.waehrung = waehrungNeu;
	}
	
	@Autowired
	public ElasticInteraction(RestHighLevelClient elasticsearchClient, ApplicationpPropertiesVerarbeiter propertiesVerarbeiter, ElasticsearchConfig elasticsearchkonfiguration) {
		this.elasticsearchClient = elasticsearchClient;
		this.elasticsearchkonfiguration = elasticsearchkonfiguration;
		this.propertiesVerarbeiter = propertiesVerarbeiter;
		elasticIndex = propertiesVerarbeiter.getIndexname();
		kostenProDokument = propertiesVerarbeiter.getkostenProDokument();
		kostenProByte = propertiesVerarbeiter.getkostenProByte();
		gesamtKosten = propertiesVerarbeiter.getGesamtKosten();
		waehrung = propertiesVerarbeiter.getWaehrung();
	}

	/**
	 * Ruft die Gesamtanzahl der Dokumente in einem Elasticsearch-Index ab.
	 *
	 * @param elasticsearchClient Ein Instanz von RestHighLevelClient für die
	 *                            Kommunikation mit Elasticsearch.
	 * @param elasticindex        Der Name des Elasticsearch-Indexes.
	 * @return Die Gesamtanzahl der Dokumente im Index.
	 * @throws IOException Falls ein Fehler bei der Kommunikation mit Elasticsearch
	 *                     auftritt.
	 */
	public static long getTotalDocumentCount(RestHighLevelClient elasticsearchClient, String elasticindex)
			throws IOException {
		SearchRequest countRequest = new SearchRequest(elasticindex);
		countRequest.source(new SearchSourceBuilder().size(0)); // Keine Treffer zurückgeben, nur die Aggregationen
																// berechnen

		ValueCountAggregationBuilder totalCountAggregation = AggregationBuilders.count("total_documents")
				.field("_index");
		countRequest.source().aggregation(totalCountAggregation);

		SearchResponse countResponse = elasticsearchClient.search(countRequest, RequestOptions.DEFAULT);
		ParsedValueCount totalDocumentsAggregation = countResponse.getAggregations().get("total_documents");
		return totalDocumentsAggregation.getValue();
	}

	/**
	 * Ruft die Anzahl der Dokumente in verschiedenen Namespaces innerhalb eines
	 * bestimmten Zeitbereichs ab.
	 *
	 * @param elasticsearchClient Ein Instanz von RestHighLevelClient für die
	 *                            Kommunikation mit Elasticsearch.
	 * @param startTime           Der Startzeitpunkt des Zeitbereichs im Format
	 *                            "yyyy-MM-dd'T'HH:mm:ss'Z'" (z. B.
	 *                            "2023-07-01T00:00:00Z").
	 * @param endTime             Der Endzeitpunkt des Zeitbereichs im Format
	 *                            "yyyy-MM-dd'T'HH:mm:ss'Z'" (z. B.
	 *                            "2023-07-04T23:59:59Z").
	 * @param elasticindex        Der Name des Elasticsearch-Indexes.
	 * @return Eine Map, die die Namespace-Namen als Schlüssel und die Anzahl der
	 *         Dokumente als Werte enthält.
	 * @throws IOException Falls ein Fehler bei der Kommunikation mit Elasticsearch
	 *                     auftritt.
	 */
	public static Map<String, Long> getNamespaceDokumente(RestHighLevelClient elasticsearchClient, String startTime,
			String endTime, String elasticindex) throws IOException {
		Map<String, Long> documentCounts = new HashMap<>();

		long totalDocuments = getTotalDocumentCount(elasticsearchClient, elasticindex);
		logger.info("Gesamtanzahl der Dokumente: " + totalDocuments);

		SearchRequest searchRequest = new SearchRequest(elasticindex);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		RangeQueryBuilder timestampQueryBuilder = QueryBuilders.rangeQuery("@timestamp").gte(startTime).lte(endTime);
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().filter(timestampQueryBuilder);

		searchSourceBuilder.query(boolQueryBuilder);

		TermsAggregationBuilder namespacesAggregationBuilder = AggregationBuilders.terms("namespaces")
				.field(ausgewaehltesMappingFeld).size(100);

		searchSourceBuilder.aggregation(namespacesAggregationBuilder);

		searchRequest.source(searchSourceBuilder);

		SearchResponse searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);

		Terms namespacesAggregation = searchResponse.getAggregations().get("namespaces");
		for (Terms.Bucket bucket : namespacesAggregation.getBuckets()) {
			String namespace = bucket.getKeyAsString();
			long documentCount = bucket.getDocCount();
			documentCounts.put(namespace, documentCount);
			logger.info("Namespace: " + namespace + ", Dokumente: " + documentCount);
		}

		// Abfrage für Dokumente ohne Namespace
		QueryBuilder nullNamespaceQueryBuilder = QueryBuilders.boolQuery()
				.mustNot(QueryBuilders.existsQuery("kubernetes_namespace"));
		long noNamespaceDocumentCount = getDocumentCountforOhneNamespace(elasticsearchClient, elasticindex,
				nullNamespaceQueryBuilder);
		documentCounts.put("Ohne Namespace", noNamespaceDocumentCount);
		logger.info("Namespace: Ohne Namespace, Dokumente: " + noNamespaceDocumentCount);

		// System.out.println("Search response: " + searchResponse.toString());

		return documentCounts;
	}

	/**
	 * Holt die Namespaces und Dokumentenzahlen aus Elasticsearch und speichert sie
	 * in einer CSV-Datei.
	 *
	 * @param startTime    Der Startzeitpunkt des gefilterten Zeitraums
	 * @param endTime      Der Endzeitpunkt des gefilterten Zeitraums
	 * @param elasticindex Der Elasticsearch Index
	 */
	private static Long getDocumentCountforOhneNamespace(RestHighLevelClient elasticsearchClient, String elasticindex,
			QueryBuilder queryBuilder) throws IOException {
		SearchRequest countRequest = new SearchRequest(elasticindex);
		countRequest.source(new SearchSourceBuilder().query(queryBuilder).size(0)); // Keine Treffer zurückgeben, nur
																					// die Aggregationen berechnen

		SearchResponse countResponse = elasticsearchClient.search(countRequest, RequestOptions.DEFAULT);
		return countResponse.getHits().getTotalHits().value;
	}

	public static List<Long> getDocumentCount(RestHighLevelClient elasticsearchClient, String elasticindex,
			QueryBuilder queryBuilder) throws IOException {
		TermsAggregationBuilder namespacesAggregationBuilder = AggregationBuilders.terms("namespaces")
				.field(ausgewaehltesMappingFeld).size(100);

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(queryBuilder)
				.aggregation(namespacesAggregationBuilder).size(0); // Keine Treffer zurückgeben, nur die Aggregationen
																	// berechnen

		SearchRequest countRequest = new SearchRequest(elasticindex);
		countRequest.source(searchSourceBuilder);

		SearchResponse countResponse = elasticsearchClient.search(countRequest, RequestOptions.DEFAULT);

		Terms namespacesAggregation = countResponse.getAggregations().get("namespaces");
		List<Long> documentCounts = new ArrayList<>();

		for (Terms.Bucket bucket : namespacesAggregation.getBuckets()) {
			long docCount = bucket.getDocCount();
			documentCounts.add(docCount);
		}

		return documentCounts;
	}

	/**
	 * Ruft die Größe der Dokumente in verschiedenen Namespaces innerhalb eines
	 * bestimmten Zeitbereichs ab.
	 *
	 * @param elasticsearchClient Ein Instanz von RestHighLevelClient für die
	 *                            Kommunikation mit Elasticsearch.
	 * @param startTime           Der Startzeitpunkt des Zeitbereichs im Format
	 *                            "yyyy-MM-dd'T'HH:mm:ss'Z'" (z. B.
	 *                            "2023-07-01T00:00:00Z").
	 * @param endTime             Der Endzeitpunkt des Zeitbereichs im Format
	 *                            "yyyy-MM-dd'T'HH:mm:ss'Z'" (z. B.
	 *                            "2023-07-04T23:59:59Z").
	 * @param elasticindex        Der Name des Elasticsearch-Indexes.
	 * @return Eine Map, die die Namespace-Namen als Schlüssel und die Größe der
	 *         Dokumente in Bytes als Werte enthält.
	 * @throws IOException Falls ein Fehler bei der Kommunikation mit Elasticsearch
	 *                     auftritt.
	 */

	public static Map<String, Long> getNamespaceBytes(RestHighLevelClient elasticsearchClient, String startTime,
			String endTime, String elasticindex) throws IOException {
		Map<String, Long> documentSize = new HashMap<>();

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		RangeQueryBuilder timestampQueryBuilder = QueryBuilders.rangeQuery("@timestamp").gte(startTime).lte(endTime);
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().filter(timestampQueryBuilder);

		searchSourceBuilder.query(boolQueryBuilder);

		TermsAggregationBuilder namespacesAggregationBuilder = AggregationBuilders.terms("namespaces")
				.field("kubernetes_namespace").size(10);

		//SumAggregationBuilder sizeAggregationBuilder = AggregationBuilders.sum("size_sum")
			//	.script(new Script("params['_source'].toString().length()"));
		SumAggregationBuilder sizeAggregationBuilder = AggregationBuilders.sum("size_sum").field("_size");
		namespacesAggregationBuilder.subAggregation(sizeAggregationBuilder);

		searchSourceBuilder.aggregation(namespacesAggregationBuilder);
		searchSourceBuilder.size(0); // Setze die Anzahl der Treffer pro Seite auf 0, um nur Aggregationsergebnisse
										// zu erhalten

		SearchRequest searchRequest = new SearchRequest(elasticindex);
		searchRequest.source(searchSourceBuilder);

		SearchResponse searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);

		Terms namespacesTerms = searchResponse.getAggregations().get("namespaces");

		for (Bucket bucket : namespacesTerms.getBuckets()) {
			String namespace = bucket.getKeyAsString();
			Sum sizeSum = bucket.getAggregations().get("size_sum");
			long bytes = (long) sizeSum.getValue(); // Änderung: von double zu long casten
			documentSize.put(namespace, bytes); // Änderung: bytes als long-Wert speichern
		}

		for (Map.Entry<String, Long> entry : documentSize.entrySet()) {
			String namespace = entry.getKey();
			Long size = entry.getValue();
			logger.info("Namespace: " + namespace + ", Dokumentengroesse: " + size + " bytes");
		}

		logger.info("Antwort der Suchanfrage: " + searchResponse.toString());
		return documentSize;
	}

	/**
	 * Ruft die Größe eines Elasticsearch-Indexes in Bytes ab.
	 *
	 * @param elasticsearchClient Ein Instanz von RestHighLevelClient für die
	 *                            Kommunikation mit Elasticsearch.
	 * @param indexName           Der Name des Elasticsearch-Indexes.
	 * @return Die Größe des Indexes in Bytes.
	 * @throws IOException Falls ein Fehler bei der Kommunikation mit Elasticsearch
	 *                     auftritt.
	 */
	public static long getIndexSize(RestHighLevelClient elasticsearchClient, String indexName) throws IOException {
		Request request = new Request("GET", "/" + indexName + "/_stats");
		Response response = elasticsearchClient.getLowLevelClient().performRequest(request);

		// Verarbeite die Antwort und extrahiere die Indexgröße
		String responseBody = EntityUtils.toString(response.getEntity());
		JSONObject json = new JSONObject(responseBody);
		JSONObject indexStats = json.getJSONObject("_all").getJSONObject("primaries");
		long sizeInBytes = indexStats.getJSONObject("store").getLong("size_in_bytes");
		return sizeInBytes;
	}

	public static List<String> getAllNamespaces(RestHighLevelClient elasticsearchClient, String elasticindex)
			throws IOException {

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		TermsAggregationBuilder namespacesAggregationBuilder = AggregationBuilders.terms("namespaces")
				.field("kubernetes_namespace").size(10000);

		searchSourceBuilder.aggregation(namespacesAggregationBuilder);
		searchSourceBuilder.size(0);

		SearchRequest searchRequest = new SearchRequest(elasticindex);
		searchRequest.source(searchSourceBuilder);

		SearchResponse searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);

		Terms namespacesAggregation = searchResponse.getAggregations().get("namespaces");

		List<String> namespaces = new ArrayList<>();
		for (Terms.Bucket bucket : namespacesAggregation.getBuckets()) {
			String namespace = bucket.getKeyAsString();
			namespaces.add(namespace);
		}

		return namespaces;
	}

	public static Map<String, Map<String, Long>> getNamespaceDocsandBytes(RestHighLevelClient elasticsearchClient,
			String startTime, String endTime, String elasticindex) throws IOException {
		Map<String, Map<String, Long>> namespaceData = new HashMap<>();

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		RangeQueryBuilder timestampQueryBuilder = QueryBuilders.rangeQuery("@timestamp").gte(startTime).lte(endTime);
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().filter(timestampQueryBuilder);

		searchSourceBuilder.query(boolQueryBuilder);

		TermsAggregationBuilder namespacesAggregationBuilder = AggregationBuilders.terms("namespaces")
				.field("kubernetes_namespace").size(Integer.MAX_VALUE);

		SumAggregationBuilder sizeAggregationBuilder = AggregationBuilders.sum("total_size")
				.script(new Script("doc['total_size.value'].length()"));
		ValueCountAggregationBuilder docCountAggregationBuilder = AggregationBuilders.count("doc_count")
				.field("doc_count");

		namespacesAggregationBuilder.subAggregation(sizeAggregationBuilder);
		namespacesAggregationBuilder.subAggregation(docCountAggregationBuilder);

		searchSourceBuilder.aggregation(namespacesAggregationBuilder);
		searchSourceBuilder.size(0);

		SearchRequest searchRequest = new SearchRequest(elasticindex);
		searchRequest.source(searchSourceBuilder);

		SearchResponse searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);

		Terms namespacesTerms = searchResponse.getAggregations().get("namespaces");

		for (Terms.Bucket bucket : namespacesTerms.getBuckets()) {
			String namespace = bucket.getKeyAsString();
			Sum sizeSum = bucket.getAggregations().get("total_size");
			ValueCount docCount = bucket.getAggregations().get("doc_count");
			double bytes = sizeSum.getValue();
			long count = docCount.getValue();

			Map<String, Long> namespaceInfo = new HashMap<>();
			namespaceInfo.put("doc_count", count);
			namespaceInfo.put("total_size", (long) bytes);

			namespaceData.put(namespace, namespaceInfo);
		}

		for (Map.Entry<String, Map<String, Long>> entry : namespaceData.entrySet()) {
			String namespace = entry.getKey();
			Map<String, Long> info = entry.getValue();
			Long docCount = info.get("doc_count");
			Long totalSize = info.get("total_size");
			logger.info(
					"Namespace: " + namespace + ", gezaehlte Dokumente: " + docCount + ", Totale Groeße: " + totalSize + " bytes");
		}

		logger.info("Antwort der Suchanfrage ist: " + searchResponse.toString());
		return namespaceData;
	}
	 /**
     * Get a list of all existing indices on the Elasticsearch client.
     *
     * @param elasticsearchClient The Elasticsearch client.
     * @return List of existing index names.
     */
	public List<String> getVorhandeneIndexNamen() {
		List<String> indexNamen = new ArrayList<>();

	    try {
	        // Send a GetIndexRequest to the Elasticsearch client to get all index names
	        GetIndexRequest getIndexRequest = new GetIndexRequest("*"); // Use "*" to get all indices
	        GetIndexResponse getIndexResponse = elasticsearchClient.indices().get(getIndexRequest, RequestOptions.DEFAULT);

	        // Get the array of index names from the response
	        String[] indices = getIndexResponse.getIndices();

	        // Add each index name to the list, excluding those starting with "."
	        for (String index : indices) {
	            if (!index.startsWith(".")) {
	                indexNamen.add(index);
	            }
	        }
	    } catch (IOException e) {
	        // Handle the exception if an error occurs while communicating with Elasticsearch
	        e.printStackTrace();
	    }

	    return indexNamen;
	}
	public Map<String, Object> getIndexMapping(String indexName) throws IOException {
	    Map<String, Object> indexMapping = new HashMap<>();
	    GetMappingsRequest request = new GetMappingsRequest().indices(indexName);
	    org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse response = elasticsearchClient.indices().getMapping(request, RequestOptions.DEFAULT);

	    ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetadata>> mappings = response.mappings();
	    ImmutableOpenMap<String, MappingMetadata> indexMappings = mappings.get(indexName);
	    MappingMetadata mappingMetadata = indexMappings.get("_doc");
	    Map<String, Object> sourceAsMap = mappingMetadata.sourceAsMap();

	    indexMapping.put("properties", sourceAsMap.get("properties"));
	    return indexMapping;
	}
}

//Reduandante Methoden:

/*
public static Map<String, Long> getNamespaceDokumenteOhneOhneNamespace(RestHighLevelClient elasticsearchClient, String startTime,
		String endTime, String elasticindex) throws IOException {
	Map<String, Long> documentCounts = new HashMap<>();

	long totalDocuments = getTotalDocumentCount(elasticsearchClient, elasticindex);
	logger.info("Gesamtanzahl der Dokumente: " + totalDocuments);

	SearchRequest searchRequest = new SearchRequest(elasticindex);
	SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

	RangeQueryBuilder timestampQueryBuilder = QueryBuilders.rangeQuery(timestamp).gte(startTime).lte(endTime);
	BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().filter(timestampQueryBuilder);

	searchSourceBuilder.query(boolQueryBuilder);

	TermsAggregationBuilder namespacesAggregationBuilder = AggregationBuilders.terms(ausgewaehlteAggregrationsname)
			.field("kubernetes_namespace").size(100);

	searchSourceBuilder.aggregation(namespacesAggregationBuilder);

	searchRequest.source(searchSourceBuilder);

	SearchResponse searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);

	Terms namespacesAggregation = searchResponse.getAggregations().get(ausgewaehlteAggregrationsname);
	for (Terms.Bucket bucket : namespacesAggregation.getBuckets()) {
		String namespace = bucket.getKeyAsString();
		long documentCount = bucket.getDocCount();
		documentCounts.put(namespace, documentCount);
		logger.info("Namespace: " + namespace + ", Dokumente: " + documentCount);
	}

	return documentCounts;
}*/
/**
 * Behandelt die Anfrage für den Startpunkt ("/") und ruft Informationen über
 * Elasticsearch-Indizes ab.
 *
 * @param model Das Model-Objekt für die Darstellung der Daten in der View.
 * @return Der Name der View, die angezeigt werden soll.
 */
//@GetMapping("/")
/*	public String index(Model model) {
	try {
		getNamespaceDokumente(elasticsearchClient, startTime, endTime, elasticIndex);
		// getNamespaceBytes(elasticsearchClient, startTime, endTime, elasticIndex);
		logger.info("Indexgröße in Bytes: " + getIndexSize(elasticsearchClient, elasticIndex));
		logger.info("Groesse der Dokumente ohne Namespace:"
				+ KostenBerechnungNamespaces.BerechneGroesseDokumenteOhneNamespace(
						getNamespaceBytes(elasticsearchClient, startTime, endTime, elasticIndex),
						getIndexSize(elasticsearchClient, elasticIndex)));

		listOfAllNamespaces = getAllNamespaces(elasticsearchClient, elasticIndex); // Liste von Allen Namespaces
		QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();
		List<Long> documentCounts = getDocumentCount(elasticsearchClient, elasticIndex, queryBuilder); // Liste von
																										// Dokumentenzählung
		Long documentSize = getTotalDocumentCount(elasticsearchClient, elasticIndex);

		//MyFileWriter.writeDataToCSV(listOfAllNamespaces, elasticsearchClient);
		MyFileWriter neuCSVFileWriter = new MyFileWriter (elasticsearchClient, propertiesVerarbeiter, elasticsearchkonfiguration,listOfAllNamespaces);
		logger.info("Die Instanz von dem CSVFileWriter wurde soeben erstellt!");
		//HtmlFileWriter.writeDataToHTML(startTime, endTime, listOfAllNamespaces,documentCounts, documentSize, elasticsearchClient, indexname);
		HtmlFileWriter neuHTMLFileWriter = new HtmlFileWriter(elasticsearchClient, propertiesVerarbeiter, elasticsearchkonfiguration, listOfAllNamespaces);
		logger.info("Die Instanz von dem HTMLFileWriter wurde soeben erstellt!");
		//Neue Methode!!!
		List<String> listeanteiligeKostenproNamespaceDokumente = KostenBerechnungNamespaces.berechneNamespaceDokumenteAnteiligeKosten(gesamtKosten, getNamespaceDokumente(elasticsearchClient, startTime, endTime, elasticIndex), getTotalDocumentCount(elasticsearchClient, elasticIndex));
		//Testausgabe Methode für die neue Methode
		KostenBerechnungNamespaces.berechneNamespaceDokumenteAnteiligeKostenAusgabe(listeanteiligeKostenproNamespaceDokumente, waehrung);
		
		// Index Request
		GetIndexRequest getIndexRequest = new GetIndexRequest("*");

		// Get index names
		GetIndexResponse getIndexResponse = elasticsearchClient.indices().get(getIndexRequest,
				RequestOptions.DEFAULT);
		String[] indices = getIndexResponse.getIndices();

		List<String> indexList = Arrays.asList(indices);

		model.addAttribute("indices", indexList);

		return "index";
	} catch (IOException e) {
		// Handle exception
		e.printStackTrace();
		model.addAttribute("error", "Failed to retrieve indices");
		return "error";
	}
}
*/