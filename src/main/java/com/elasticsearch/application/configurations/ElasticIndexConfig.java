package com.elasticsearch.application.configurations;

import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.action.support.master.AcknowledgedResponse;


import com.elasticsearch.application.output.LogFileWriter;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Diese Klasse enthält Methoden zum Konfigurieren des Elasticsearch-Index.
 */
@SuppressWarnings("deprecation")
public class ElasticIndexConfig {
	
	private final static Logger logger = LogFileWriter.getLogger();

	 /**
     * Konfiguriert den Elasticsearch-Index, indem das Feld "search_after" mit dem Datentyp "keyword" hinzugefügt wird.
     *
     * @param elasticsearchClient der Elasticsearch-Client
     * @param elasticindex         der Name des Elasticsearch-Index
     * @throws IOException wenn ein Fehler bei der Kommunikation mit Elasticsearch auftritt
     */
	
	static void configureIndex(RestHighLevelClient elasticsearchClient, String elasticindex) throws IOException {
	    PutMappingRequest putMappingRequest = new PutMappingRequest(elasticindex);

	    org.elasticsearch.xcontent.XContentBuilder mappingBuilder = org.elasticsearch.xcontent.XContentFactory.jsonBuilder();
	    mappingBuilder.startObject();
	    mappingBuilder.startObject("properties");
	    mappingBuilder.startObject("search_after");
	    mappingBuilder.field("type", "long");
	    mappingBuilder.field("example", "0"); // Startwert für search_after
	    mappingBuilder.endObject();
	    mappingBuilder.endObject();
	    mappingBuilder.endObject();

	    putMappingRequest.indices(elasticindex);
	    putMappingRequest.source(mappingBuilder);

	    AcknowledgedResponse putMappingResponse = elasticsearchClient.indices().putMapping(putMappingRequest, RequestOptions.DEFAULT);
	    if (putMappingResponse.isAcknowledged()) {
	        logger.info("Das Mapping des nachfolgenden Index wurde erfolgreich aktualisiert: " + elasticindex);
	    } else {
	    	 logger.warning("Das updaten des Mappings, des nachfolgenden Index, ist leider fehlgeschlagen: " + elasticindex);
	    }
	}

}