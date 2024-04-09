package com.elasticsearch.application.configurations;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.elasticsearch.application.output.LogFileWriter;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Diese Klasse konfiguriert den Elasticsearch-Client und stellt eine Instanz
 * des RestHighLevelClient bereit.
 */

@PropertySource("application.properties")
@ConfigurationProperties(prefix = "elasticsearch")
@ComponentScan("")
@SuppressWarnings({ "unused", "deprecation" })
@Configuration
public class ElasticsearchConfig {

	private final static Logger logger = LogFileWriter.getLogger();

	// @Autowired
	private RestHighLevelClient elasticsearchClient;

	/*
	 * Folgend werden die Klassenvariablen initialisiert
	 * 
	 */
	@Value("${elasticsearch.host}")
	private String host;
	@Value("${elasticsearch.port}")
	private int port;
	@Value("${elasticsearch.username}")
	private String username;
	@Value("${elasticsearch.password}")
	private String password;
	@Value("${elasticsearch.apiKey}")
	private String apiKey;
	@Value("${elasticsearch.Truststorepassword}")
	private String truststorePassword;
	@Value("${elasticsearch.TruststorePath}")
	private String truststorePath;
	@Value("${elasticsearch.CertCN}")
	private String certCN;
	@Value("${elasticsearch.certAlias}")
	private String certAlias;
	
	//private int port = Integer.parseInt(portString);

	// Getters und Setters für die Klassenvariablen

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getTruststorePassword() {
		return truststorePassword;
	}

	public void setTruststorePassword(String truststorePassword) {
		this.truststorePassword = truststorePassword;
	}

	public String getTruststorePath() {
		return truststorePath;
	}

	public void setTruststorePath(String truststorePath) {
		this.truststorePath = truststorePath;
	}

	public String getCertCN() {
		return certCN;
	}

	public void setCertCN(String certCN) {
		this.certCN = certCN;
	}

	public String getCertAlias() {
		return certAlias;
	}

	public void setCertAlias(String certAlias) {
		this.certAlias = certAlias;
	}

	/**
	 * @return the elasticsearchClient
	 */
	public RestHighLevelClient getElasticsearchClient() {
		return elasticsearchClient();

	}

	public String getElasticsearchClientAlsString() {
		return "https://" + host + ":" + port;
	}

	/**
	 * @param elasticsearchClient the elasticsearchClient to set
	 */
	public void setElasticsearchClient(RestHighLevelClient elasticsearchClientInput) {
		elasticsearchClient = elasticsearchClientInput;
	}

	/**
	 * Erstellt und konfiguriert einen SSLContext für die HTTPS-Verbindung mit
	 * Elasticsearch.
	 *
	 * @return Der SSLContext für die sichere Verbindung.
	 */
	public SSLContext createSSLContext() {
		try {
			String truststorePassword = this.truststorePassword;// "03012003";
			String truststorePath = this.truststorePath;// "C:/Users/Luise/Desktop/Projekt_02/Elasticsearch/Certs/truststore_03.p12";

			KeyStore truststore = KeyStore.getInstance("pkcs12");
			char[] passwordChars = truststorePassword.toCharArray();
			FileInputStream truststream = new FileInputStream(new File(truststorePath));
			truststore.load(truststream, passwordChars);

			SSLContextBuilder sslBuilder = SSLContexts.custom().loadTrustMaterial(truststore, null);
			return sslBuilder.build();
		} catch (Exception e) {
			throw new RuntimeException("Fehler beim Erstellen des SSL-Kontexts für Elasticsearch", e);
		}
	}
	// @Bean
	// public ElasticsearchRestTemplate elasticsearchOperations() {
	// return new ElasticsearchRestTemplate(getElasticsearchClient());
	// }

	/**
	 * Erstellt und konfiguriert einen RestHighLevelClient für die Kommunikation mit
	 * Elasticsearch.
	 *
	 * @return Der RestHighLevelClient für Elasticsearch.
	 */
	@Primary
	@Bean
	public RestHighLevelClient elasticsearchClient() {
		try {
			// int elasticsearchPortCast = Integer.parseInt(elasticsearchPort);
			String certAlias = this.certAlias; // Setze den Alias je nach Zertifikat, das du verwenden möchtest
			HttpHost httpHost = new HttpHost(host, port, "https");

			String truststorePassword = this.truststorePassword;
			String truststorePath = this.truststorePath;

			KeyStore truststore = KeyStore.getInstance("pkcs12");
			char[] passwordChars = truststorePassword.toCharArray();
			FileInputStream truststream = new FileInputStream(new File(truststorePath));
			truststore.load(truststream, passwordChars);

			// Erstelle SSLContext mit benutzerdefinierter Hostnamen-Verifikation
			SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(truststore, null).setProtocol("TLS").build();

			// Hostnamen-Verifikation, die immer true zurückgibt (ohne
			// Hostnamen-Überprüfung)
			HostnameVerifier allowAllHosts = new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};

			// Setze die benutzerdefinierte Hostnamen-Verifikation
			SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts);

			// Erstelle den RestClientBuilder
			RestClientBuilder restClientBuilder = RestClient.builder(httpHost)
					.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
						@Override
						public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
							// Setze die SSLContext und die Hostnamen-Verifikation
							httpClientBuilder.setSSLContext(sslContext);
							httpClientBuilder.setSSLHostnameVerifier(allowAllHosts);

							// Hier können weitere Konfigurationen für den HttpClient vorgenommen werden
							// Zum Beispiel die Verbindungs-Timeouts oder Proxy-Einstellungen
							httpClientBuilder.setDefaultRequestConfig(
									RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(60000).build());

							// Versuche, den API-Schlüssel zu verwenden
							try {
								List<Header> headers = new ArrayList<>();
								headers.add(new BasicHeader("Authorization", "ApiKey " + apiKey));
								httpClientBuilder.setDefaultHeaders(headers);
							} catch (Exception apiKeyException) {
								// Wenn der API-Schlüssel nicht funktioniert, verwende Benutzername und Passwort
								CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
								credentialsProvider.setCredentials(AuthScope.ANY,
										new UsernamePasswordCredentials(username, password));
								httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
							}

							return httpClientBuilder;
						}
					});

			this.elasticsearchClient = new RestHighLevelClient(restClientBuilder);
			return new RestHighLevelClient(restClientBuilder);

		} catch (Exception e) {
			throw new RuntimeException("Fehler beim Erstellen des Elasticsearch-Clients", e);
		}
	}

}

	// @Override
/*	@Primary
	@Bean
	public RestHighLevelClient elasticsearchClient() {
		try {
		*/
			/*logger.info("Der Truststorepfad lautet: " + this.truststorePath);
			logger.info("Das Truststorepasswort lautet: " + this.truststorePassword);
			logger.info("Der Vertrauenswuerdige CN-Name lautet: " + this.certCN);*/
/*
			String certAlias = this.certAlias; // Setze den Alias je nach Zertifikat, das du verwenden möchtest

			HttpHost httpHost = new HttpHost(elasticsearchHost, elasticsearchPort, "https");
			System.out.println(httpHost);

			String truststorePassword = this.truststorePassword;
			String truststorePath = this.truststorePath;

			KeyStore truststore = KeyStore.getInstance("pkcs12");
			char[] passwordChars = truststorePassword.toCharArray();
			FileInputStream truststream = new FileInputStream(new File(truststorePath));
			truststore.load(truststream, passwordChars);

			// Überprüfe, ob das Zertifikat im Truststore vorhanden ist
			if (!truststore.containsAlias(certAlias)) {
				throw new RuntimeException(
						"Das Zertifikat mit dem Alias '" + certAlias + "' wurde nicht im Truststore gefunden.");
			}

			// Überprüfe, ob der CN im Zertifikat mit dem gespeicherten CN übereinstimmt
			X509Certificate certificate = (X509Certificate) truststore.getCertificate(certAlias);
			String certificateCN = certificate.getSubjectX500Principal().getName();
			String expectedCN = "CN=" + this.certCN;
			if (!certificateCN.equalsIgnoreCase(expectedCN)) {
				throw new RuntimeException(
						"Der CN im Zertifikat stimmt nicht mit dem in der Konfiguration gespeicherten CN überein. Erwartet: "
								+ expectedCN + ", gefunden: " + certificateCN);
			}

			// Erstelle SSLContext mit benutzerdefinierter Hostnamen-Verifikation
			SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(truststore, null).setProtocol("TLS").build();

			// Hostnamen-Verifikation, die immer true zurückgibt (ohne
			// Hostnamen-Überprüfung)
			HostnameVerifier allowAllHosts = new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};

			// Setze die benutzerdefinierte Hostnamen-Verifikation
			SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts);

			// Erstelle den RestClientBuilder
			RestClientBuilder restClientBuilder = RestClient.builder(httpHost)
					.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
						@Override
						public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
							// Setze die SSLContext und die Hostnamen-Verifikation
							httpClientBuilder.setSSLContext(sslContext);
							httpClientBuilder.setSSLHostnameVerifier(allowAllHosts);

							// Hier können weitere Konfigurationen für den HttpClient vorgenommen werden
							// Zum Beispiel die Verbindungs-Timeouts oder Proxy-Einstellungen
							httpClientBuilder.setDefaultRequestConfig(
									RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(60000).build());

							return httpClientBuilder;
						}
					}).setDefaultHeaders(
							new Header[] { new BasicHeader("Authorization", "ApiKey " + elasticsearchApiKey) });

			this.elasticsearchClient = new RestHighLevelClient(restClientBuilder);
			return new RestHighLevelClient(restClientBuilder);

		} catch (Exception e) {
			throw new RuntimeException("Fehler beim Erstellen des Elasticsearch-Clients", e);
		}
	}
*/
/*	@Value("${elasticsearch.host}")
private String elasticsearchHost;
@Value("${elasticsearch.port}")
private int elasticsearchPort;
@Value("${elasticsearch.username}")
private String elasticsearchUsername;
@Value("${elasticsearch.password}")
private String elasticsearchPassword;
@Value("${elasticsearch.apiKey}")
private String elasticsearchApiKey;
@Value("${elasticsearch.Truststorepassword}")
private String truststorePassword;
@Value("${elasticsearch.TruststorePath}")
private String truststorePath;
@Value("${elasticsearch.CertCN}")
private String certCN;
@Value("${elasticsearch.certAlias}")
private String certAlias;*/
