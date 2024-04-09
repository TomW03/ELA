import static org.junit.jupiter.api.Assertions.*;

import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import com.elasticsearch.application.configurations.ElasticsearchConfig;
import com.elasticsearch.application.output.LogFileWriter;

/**
 * Diese Klasse enthält Tests für die Konfigurationsklasse ElasticsearchConfig.
 */
@SuppressWarnings("deprecation")
@SpringBootTest
@ContextConfiguration(classes = ElasticsearchConfigTest.TestConfig.class)
class ElasticsearchConfigTest {
	private final static Logger logger = LogFileWriter.getLogger();
	private static String elasticsearchTestUsername = "test";
	private static String elasticsearchTestPassword = "password";
	private static String elasticsearchTestAPIKey = "apikey";
	private static String elasticsearchTesterwarteterHostString = "IP-Adress";
	private static Integer elasticsearchTesterwarteterHostPort = Port;
	private static String elasticsearchTesterwarteterTruststorePfad = "pfad/zum/trustore";
	private static String elasticsearchTesterwartetesTruststorePasswort = "";
	private static String elasticsearchTesterwartetescertAlias = "";
	private static String elasticsearchTesterwartetecertCN = "";
	
	@Autowired
	private ElasticsearchConfig elasticsearchConfig;

	@Mock
	private RestHighLevelClient mockElasticsearchClient;

	/**
	 * Eine Konfiguration für Testzwecke, die eine Instanz der
	 * ElasticsearchConfig-Klasse erstellt.
	 */
	@Configuration
	public static class TestConfig {

		@Bean
		public ElasticsearchConfig elasticsearchConfig() {
			return new ElasticsearchConfig();
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	/**
	 * Vor der Testausführung werden hier die notwendigen Vorbereitungen getroffen.
	 * 
	 * @throws Exception: Wirft Exception wenn was fehlschlägt
	 */
	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);

		// Setze die Werte für die Eigenschaften manuell
		elasticsearchConfig.setHost("IP");
		elasticsearchConfig.setPort(Port);
		elasticsearchConfig.setTruststorePath(
				"/pfad/zum/Truststore");
		elasticsearchConfig.setTruststorePassword("TruststorePassword");
		elasticsearchConfig.setCertCN("CertCN");
		elasticsearchConfig.setApiKey("ApiKey");
		elasticsearchConfig.setCertAlias("http/https");
	}

	/**
	 * Testet die Erstellung des Elasticsearch-Clients.
	 */
	@Test
	void testElasticsearchClientCreation() {
		logger.info(
				"Starte mit dem Test der testElasticsearchClientCreation() Methode, der Klasse: ElasticsearchConfig");
		RestHighLevelClient client = elasticsearchConfig.elasticsearchClient();
		assertNotNull(client);
		logger.info(
				"Der Test, der testElasticsearchClientCreation() Methode, der Klasse: ElasticsearchConfig, wurde erfolgreich ausgeführt!");
	}

	/**
	 * Testet die Methode zum Abrufen des Elasticsearch-Clients als String.
	 */
	@Test
	void testElasticsearchClientAlsString() {
		logger.info("Starte mit dem Test der ElasticsearchClientAlsString() Methode, der Klasse: ElasticsearchConfig");
		String erwartet = "https://" + elasticsearchTesterwarteterHostString + ":" + elasticsearchTesterwarteterHostPort;
		String clientString = elasticsearchConfig.getElasticsearchClientAlsString();
		assertEquals(erwartet, clientString);
		logger.info(
				"Der Test der ElasticsearchClientAlsString() Methode, der Klasse: ElasticsearchConfig, wurde erfolgreich ausgeführt!");
	}

	@Test
	void testGettersAndSetters() {
		logger.info("Starte mit dem Test der Getter und Setter Methoden, der Klasse: ElasticsearchConfig");
		assertEquals(elasticsearchTesterwarteterHostString, elasticsearchConfig.getHost());
		assertEquals(elasticsearchTesterwarteterHostPort, elasticsearchConfig.getPort());
		assertEquals(elasticsearchTesterwarteterTruststorePfad,
				elasticsearchConfig.getTruststorePath());
		assertEquals(elasticsearchTesterwartetesTruststorePasswort, elasticsearchConfig.getTruststorePassword());
		assertEquals(elasticsearchTesterwartetescertAlias, elasticsearchConfig.getCertAlias());
		assertEquals(elasticsearchTesterwartetecertCN, elasticsearchConfig.getCertCN());
		assertEquals(elasticsearchTestAPIKey,
				elasticsearchConfig.getApiKey());

		elasticsearchConfig.setHost("newHost");
		assertEquals("newHost", elasticsearchConfig.getHost());

		logger.info(
				"Der Test der Getter und Setter Methoden, der Klasse: ElasticsearchConfig, wurde erfolgreich ausgeführt!");
	}

	/**
	 * Testet die Erstellung des SSL-Kontexts für die sichere Verbindung.
	 */
	@Test
	void testSSLContextCreation() {
		logger.info("Starte mit dem Test der Methode zum erstellen des SSL Kontextes, der Klasse: ElasticsearchConfig");
		SSLContext sslContext = elasticsearchConfig.createSSLContext();
		assertNotNull(sslContext);
		logger.info(
				"Der Test, der Methode zum erstellen des SSL Kontextes, der Klasse: ElasticsearchConfig, wurde erfolgreich ausgeführt!");
	}
	
	@Test
    void testElasticsearchClientUsingApiKey() {
        logger.info("Starte mit dem Test der Methode testElasticsearchClientUsingApiKey(), der Klasse: ElasticsearchConfig");
        // Test, wenn ein API-key bereitgestellt wird
        elasticsearchConfig.setApiKey(elasticsearchTestAPIKey);
        RestHighLevelClient client = elasticsearchConfig.elasticsearchClient();
        assertNotNull(client);

        logger.info("Der Test der Methode testElasticsearchClientUsingApiKey(), der Klasse: ElasticsearchConfig, wurde erfolgreich ausgeführt!");
    }

    @Test
    void testElasticsearchClientNutztUsernameUndPassword() {
        logger.info("Starte mit dem Test der Methode testElasticsearchClientUsingUsernameAndPassword(), der Klasse: ElasticsearchConfig");
        elasticsearchConfig.setApiKey(null);
        elasticsearchConfig.setUsername(elasticsearchTestUsername);
        elasticsearchConfig.setPassword(elasticsearchTestPassword);
        // Setze den APIKey zu null um die Verwendung von Benutzername und Passwort für die Anmeldung zu erzwingen
        elasticsearchConfig.setApiKey(null);

        // Test Wenn ein Benutzername und Passwort bereitgestellt wird
        RestHighLevelClient client = elasticsearchConfig.elasticsearchClient();
        assertNotNull(client);

        logger.info("Der Test der Methode testElasticsearchClientUsingUsernameAndPassword(), der Klasse: ElasticsearchConfig, wurde erfolgreich ausgeführt!");
    }

	/**
	 * Führt alle Testmethoden nacheinander aus und prüft auf mögliche Ausnahmen.
	 */
	@Test
	void testAll() {
		try {
			testElasticsearchClientCreation();
			testElasticsearchClientAlsString();
			testGettersAndSetters();
			testSSLContextCreation();
			testElasticsearchClientUsingApiKey();
			testElasticsearchClientNutztUsernameUndPassword();
			logger.info("Alle Tests wurden erfolgreich ausgeführt!");
		} catch (Exception e) {
			logger.info("Ein oder mehrere Tests sind leider fehlgeschlagen, der Grund dafür ist: " + e.getMessage());
		}
	}

	/**
	 * Nach der Testausführung werden hier Aufräumarbeiten durchgeführt.
	 */
	@AfterEach
	void tearDown() throws Exception {
	}

	/**
	 * Wird einmal nach der gesamten Testklasse ausgeführt, um abschließende
	 * Aufräumarbeiten durchzuführen.
	 */
	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

}
