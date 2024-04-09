package com.elasticsearch.application.views.main;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse.FieldMappingMetadata;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentFactory;
//import org.elasticsearch.cluster.metadata.MappingMetadata;
//import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.ParseException;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.elasticsearch.application.configurations.ApplicationpPropertiesVerarbeiter;
import com.elasticsearch.application.configurations.ElasticsearchConfig;
import com.elasticsearch.application.interactions.ElasticInteraction;
import com.elasticsearch.application.output.CSVDateiSchreiber;
import com.elasticsearch.application.output.HTMLDateiSchreiber;
import com.elasticsearch.application.output.HtmlFileWriter;
import com.elasticsearch.application.output.LogFileWriter;
import com.elasticsearch.application.output.MyFileWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@SuppressWarnings({ "unused", "deprecation", "removal", "serial" })
@PageTitle("Startseite")
@Route(value = "")
public class MainView extends VerticalLayout {

	private final static Logger logger = LogFileWriter.getLogger();
	private final RestHighLevelClient elasticsearchClient;
	private final ApplicationpPropertiesVerarbeiter propertiesVerarbeiter;
	private final ElasticsearchConfig elasticsearchkonfiguration;
	private double gesamtkosten;
	private String gewaehlterindex;
	private String gewaehlteStartZeit;
	private String gewaehlteEndZeit;
	private String waehrung;
	List<String> listOfAllNamespaces;
	private String zeitFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	private String htmlDateipfad;
	private String htmlAlternativpfad;
	private String csvDateipfad;
	private String csvAlternativpfad;
	String chromePfad = "C:/Program Files/Google/Chrome/Application/chrome.exe"; // Passe den Pfad zu Chrome an
	String microsoftVisualStudioCodePfad ="C:/Anwendungen/Microsoft VS Code/Code.exe/";

	@Autowired
	public MainView(RestHighLevelClient elasticsearchClient, ApplicationpPropertiesVerarbeiter propertiesVerarbeiter,
			ElasticsearchConfig elasticsearchkonfiguration) {
		this.elasticsearchClient = elasticsearchClient;
		this.elasticsearchkonfiguration = elasticsearchkonfiguration;
		this.propertiesVerarbeiter = propertiesVerarbeiter;
		this.gesamtkosten = propertiesVerarbeiter.getGesamtKosten();
		this.gewaehlterindex = propertiesVerarbeiter.getIndexname();
		htmlDateipfad = propertiesVerarbeiter.gethtmlDateiPfad();
		htmlAlternativpfad = propertiesVerarbeiter.gethtmlDateiPfadAlternative();
		csvDateipfad = propertiesVerarbeiter.getcsvDateiPfad();
		csvAlternativpfad = propertiesVerarbeiter.getcsvAlternativpfad();
		mainViewstart();
	}

	private ComboBox<String> indexNameComboBox;
	private Button erstelleIndex;
	private Button exitButton;
	private Button generiereCSV;
	private Button generiereHTML;
	private DateTimePicker startDatumZeitWaehler;
	private DateTimePicker endDatumZeitWaehler;
	private Label startZeitraumLabel;
	private Label endZeitraumLabel;
	private ComboBox<String> mappingFieldComboBox;
	private Label selectIndexWarning;
	private TextField gesamtkostenTextField;
	private Button bestaetigungsgesamtkostenButton;

	public void sethtmlDateipfad(String htmlDateiPfadInput) {
		this.htmlDateipfad = htmlDateiPfadInput;
	}

	public void setcsvDateipfad(String csvDateiPfadInput) {
		this.csvDateipfad = csvDateiPfadInput;
	}

	public void mainViewstart() {
		// Erstelle das ComboBox-Feld
		indexNameComboBox = new ComboBox<>();
		indexNameComboBox.setLabel("Wähle einen Index aus oder erstelle einen Neuen!");
		indexNameComboBox.setPlaceholder("Gebe hier einen Indexnamen für einen neuen Index ein");

		// Fülle das ComboBox-Feld mit den vorhandenen Indexnamen aus dem
		// Elasticsearch-Container
		try {
			fillIndexNames();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Fehler beim Indices laden: " + e.getMessage(), e);
		}
		
		
		indexNameComboBox.addValueChangeListener(event -> {
			String ausgewaehlterIndexComboBox = event.getValue();
			logger.info("Der ausgewaehlte Index lautet: " + ausgewaehlterIndexComboBox);
			gewaehlterindex = ausgewaehlterIndexComboBox;
		});
		erstelleIndex = new Button("Erstelle den Index");
		erstelleIndex.addClickListener(e -> {
			 //gewaehlterindex = indexNameComboBox.getValue();
			if (gewaehlterindex == null || gewaehlterindex.isEmpty()) {
				Notification.show("Bitte wählen Sie einen Indexnamen aus der Drop-Down-Liste aus oder gebe einen Namen ein.");
			} else {
				erstelleIndex(gewaehlterindex);
				Notification.show("Index: " + gewaehlterindex + "wurde erfolgreich erstellt");
			}
		});
		erstelleIndex.addClickShortcut(Key.ENTER);
		


		// Create the dropdown field for selecting the mapping field
		mappingFieldComboBox = new ComboBox<>();
		mappingFieldComboBox.setLabel("Wähle ein Mapping-Feld aus");
		mappingFieldComboBox.setPlaceholder("Wähle ein Feld");
		mappingFieldComboBox.setEnabled(false); // Initially, disable the combo box

		// Create a label for displaying a warning message
		selectIndexWarning = new Label("Bitte wählen Sie zuerst einen Index aus");
		selectIndexWarning.getStyle().set("color", "red");
		selectIndexWarning.setVisible(false); // Initially, hide the warning message

		indexNameComboBox.addValueChangeListener(event -> {
			String selectedIndexOfComboBox = event.getValue();
			if (selectedIndexOfComboBox != null && !selectedIndexOfComboBox.isEmpty()) {
				try {
					List<String> mappingFields = getMappingFelder(selectedIndexOfComboBox);
					if (!mappingFields.isEmpty()) {
						mappingFieldComboBox.setEnabled(true); // Enable the combo box
						mappingFieldComboBox.setItems(mappingFields);
						selectIndexWarning.setVisible(false); // Hide the warning message
					} else {
						mappingFieldComboBox.clear(); // Clear the combo box options if no mapping fields found
						mappingFieldComboBox.setEnabled(false); // Disable the combo box
						selectIndexWarning.setVisible(true); // Show the warning message
					}
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Fehler beim Laden der Mapping-Felder: " + e.getMessage(), e);
				}
			} else {
				mappingFieldComboBox.clear(); // Clear the combo box options if no index is selected
				mappingFieldComboBox.setEnabled(false); // Disable the combo box
				selectIndexWarning.setVisible(true); // Show the warning message
			}
		});

		// Erstelle das Textfeld für die Gesamtkosten
		gesamtkostenTextField = new TextField();
		gesamtkostenTextField.setLabel("Trage hier die Gesamtkosten ein");
		gesamtkostenTextField.setPlaceholder("z.B.: 100.00€ o. 9.99$");
		bestaetigungsgesamtkostenButton = new Button("Bestätigen");
		bestaetigungsgesamtkostenButton.addClickListener(e -> {
			String eingabe = gesamtkostenTextField.getValue();
			if (eingabe != null && !eingabe.isEmpty()) {
				String[] teile = eingabe.split("\\s+");
				if (teile.length == 2) {
					String zahlStr = teile[0];
					String waehrung = teile[1];

					// Entferne das Währungszeichen
					String zahlOhneWaehrung = zahlStr.replace(waehrung, "");

					try {
						// Setze das Trennzeichen für Dezimalstellen auf Punkt
						DecimalFormatSymbols symbols = new DecimalFormatSymbols();
						symbols.setDecimalSeparator('.');
						NumberFormat formatter = new DecimalFormat("0.00", symbols);
						this.gesamtkosten = formatter.parse(zahlOhneWaehrung).doubleValue();
						this.waehrung = waehrung;
						logger.info("Die eingegebene Waehrung ist: " + waehrung);

						// Aktualisiere die gesamtkosten in der ApplicationpPropertiesVerarbeiter-Klasse
						Double neuegesamtkosten = gesamtkosten;
						Double altegesamtkosten = propertiesVerarbeiter.getGesamtKosten();
						propertiesVerarbeiter.setGesamtkosten(neuegesamtkosten);
						logger.info("Die eingegebenen Gesamtkosten sind: " + neuegesamtkosten + waehrung);
						logger.info("Die Gesamtkosten wurden von: " + altegesamtkosten +"€" + "auf: " + neuegesamtkosten
								+ waehrung + " geändert.");
						this.gesamtkosten = neuegesamtkosten;
						this.waehrung = waehrung;
					} catch (java.text.ParseException ex) {
						logger.log(Level.WARNING,
								"Das umwandeln/extrahieren der eingegebenen Summe und der Währung ist fehlgeschlagen!",
								ex);
						ex.printStackTrace(); // Handle die ParseException, wenn die Konvertierung fehlschlägt

					}
				}
			}
		});

		// Add a button to perform an action using the selected mapping field
		Button useMappingFieldButton = new Button("Verwende Mapping-Feld");
		useMappingFieldButton.addClickListener(e -> {
			String selectedMappingField = mappingFieldComboBox.getValue();
			if (selectedMappingField != null && !selectedMappingField.isEmpty()) {
				// Perform your action using the selected mapping field here
				showSelectedMappingField(selectedMappingField);
			} else {
				Notification.show("Bitte wählen Sie ein Mapping-Feld aus der Dropdown-Liste aus.");
			}
		});
		useMappingFieldButton.addClickShortcut(Key.ENTER);

		// Erstellung des Exit Buttons
		exitButton = new Button("Exit");
		exitButton.addClickListener(e -> {
			// Rufe die Methode zum Beenden der Elasticsearch-Verbindung auf

			// Zeige die "goodbye.html"-Seite an
			UI.getCurrent().getPage().executeJs("window.location.href = 'goodbye';");
		});
		// Weise dem Button die benutzerdefinierte CSS-Klasse zu
		exitButton.addClassName("red-button");
		exitButton.addClickShortcut(Key.ENTER);

		generiereCSV = new Button("Generiere CSV Datei");
		generiereCSV.addClickListener(e -> {
			// Rufe die Methode zum Beenden der Elasticsearch-Verbindung auf
			generiereCSV();
			if (generiereCSV() == false) {
				Notification
						.show("Fehler beim erstellen der CSV-Datei, schaue in die Logs um mehr details zu erhalten");
			} else {
				Notification.show("Die CSV-Datei wurde im Pfad:" + csvDateipfad
						+ "(wird in der application.property angegeben), erfolgreich gespeichert!");

			}
		});
		// Weise dem Button die benutzerdefinierte CSS-Klasse zu
		generiereCSV.addClassName("pink-button");
		generiereCSV.addClickShortcut(Key.ENTER);

		generiereHTML = new Button("Generiere HTML Datei");
		generiereHTML.addClickListener(e -> {
			// Rufe die Methode zum Beenden der Elasticsearch-Verbindung auf
			generiereHTML();
			if (generiereHTML() == false) {
				Notification
						.show("Fehler beim erstellen der CSV-Datei, schaue in die Logs um mehr details zu erhalten");
			} else {
				ErgebnisView ergebnisView = new ErgebnisView();
				ergebnisView.sethtmlDateipfad(htmlDateipfad);
				ergebnisView.setzeWerte(htmlDateipfad);
				/*Notification.show("Die HTML-Datei wurde im Pfad: " + this.htmlDateipfad
						+ " (wird in der application.property angegeben), erfolgreich gespeichert und wird in 5 Sekunden automatisch geöffnet");
				UI.getCurrent().getPage().executeJs("window.location.href = 'Ergebnis';");*/
			}
		});

		// Erstelle das Start-Zeitraumfilter-Feld
		startDatumZeitWaehler = new DateTimePicker("Startdatum und Startzeit");
		startDatumZeitWaehler.setLabel("Start des gefilterten Zeitraums:");
		startDatumZeitWaehler.setStep(Duration.ofSeconds(1));
		// startDatumZeitWaehler.setValue(LocalDateTime.of(2020, 6, 12, 15, 45, 8));
		startDatumZeitWaehler.setValue(LocalDateTime.now());

		// Erstelle das End-Zeitraumfilter-Feld
		endDatumZeitWaehler = new DateTimePicker("Enddatum und Enduhrzeit");
		endDatumZeitWaehler.setLabel("Ende des gefilterten Zeitraums:");
		endDatumZeitWaehler.setStep(Duration.ofSeconds(1));
		// endDatumZeitWaehler.setValue(LocalDateTime.of(2020, 6, 12, 15, 45, 8));
		endDatumZeitWaehler.setValue(LocalDateTime.now());

		startDatumZeitWaehler.addValueChangeListener(e -> endDatumZeitWaehler.setMin(e.getValue()));

		// Add onChange handler to startDatumZeitWaehler
		startDatumZeitWaehler.addValueChangeListener(event -> {
			LocalDateTime ausgewaehltestartDatumZeitWaehlerevent = event.getValue();
			logger.info("Die/Das ausgewaehlte Startzeit/Startdatum lautet: " + ausgewaehltestartDatumZeitWaehlerevent);
			// Aktualisiere die Startzeit in der ApplicationpPropertiesVerarbeiter-Klasse
			DateTimeFormatter formatierer = DateTimeFormatter.ofPattern(zeitFormat);
			String neueStartZeit = ausgewaehltestartDatumZeitWaehlerevent.format(formatierer);
			String alteStartZeit = propertiesVerarbeiter.getelasticsearchstartTime();
			gewaehlteStartZeit = ausgewaehltestartDatumZeitWaehlerevent.format(formatierer);
			propertiesVerarbeiter.setelasticsearchstartTime(neueStartZeit);

			logger.info("Startzeit wurde von :" + alteStartZeit + "auf: " + neueStartZeit + " geändert.");
		});
		endDatumZeitWaehler.addValueChangeListener(event -> {
			LocalDateTime ausgewaehlteendDatumZeitWaehlerevent = event.getValue();
			logger.info("Die/Das ausgewaehlte Startzeit/Startdatum lautet: " + ausgewaehlteendDatumZeitWaehlerevent);

			// Aktualisiere die Startzeit in der ApplicationpPropertiesVerarbeiter-Klasse
			DateTimeFormatter formatierer = DateTimeFormatter.ofPattern(zeitFormat);
			String neueEndZeit = ausgewaehlteendDatumZeitWaehlerevent.format(formatierer);
			String alteEndZeit = propertiesVerarbeiter.getelasticsearchendTime();
			gewaehlteEndZeit = ausgewaehlteendDatumZeitWaehlerevent.format(formatierer);
			propertiesVerarbeiter.setelasticsearchstartTime(neueEndZeit);

			logger.info("Endzeit wurde von :" + alteEndZeit + "auf: " + neueEndZeit + " geändert.");
		});

		// Setze das Label für den Startdatum und Startzeit filter
		startZeitraumLabel = new Label("Startzeitraumfilter");
		startZeitraumLabel.getStyle().set("font-weight", "bold");

		// Set labels for End Date and Time filters
		endZeitraumLabel = new Label("Endzeitraumfilter");
		endZeitraumLabel.getStyle().set("font-weight", "bold");

		HorizontalLayout mappingFieldLayout = new HorizontalLayout();
		mappingFieldLayout.setPadding(true);
		mappingFieldLayout.setJustifyContentMode(JustifyContentMode.CENTER);
		mappingFieldLayout.setAlignItems(Alignment.BASELINE);
		mappingFieldLayout.add(mappingFieldComboBox);
		mappingFieldLayout.add(useMappingFieldButton);
		// Add the warning message below the combo box
		mappingFieldLayout.add(selectIndexWarning);

		// Füge die Komponenten zum Layout hinzu
		HorizontalLayout indexoptionsleiste = new HorizontalLayout();
		indexoptionsleiste.setPadding(true);
		indexoptionsleiste.setJustifyContentMode(JustifyContentMode.CENTER);
		indexoptionsleiste.setAlignItems(Alignment.BASELINE); // Align items vertically at the baseline
		indexoptionsleiste.add(indexNameComboBox);
		indexoptionsleiste.add(erstelleIndex);
		indexoptionsleiste.add(mappingFieldLayout);
		// add(indexoptionsleiste);

		HorizontalLayout zeitraumWaehlerMittig = new HorizontalLayout();
		zeitraumWaehlerMittig.setPadding(true);
		zeitraumWaehlerMittig.setJustifyContentMode(JustifyContentMode.CENTER);
		zeitraumWaehlerMittig.add(startZeitraumLabel);
		zeitraumWaehlerMittig.add(startDatumZeitWaehler);
		zeitraumWaehlerMittig.add(endZeitraumLabel);
		zeitraumWaehlerMittig.add(endDatumZeitWaehler);
		// add(zeitraumWaehlerMittig);

		HorizontalLayout unterZeitraumwaehler = new HorizontalLayout();
		unterZeitraumwaehler.setPadding(true);
		unterZeitraumwaehler.setJustifyContentMode(JustifyContentMode.CENTER);
		unterZeitraumwaehler.setAlignItems(Alignment.BASELINE);
		unterZeitraumwaehler.add(gesamtkostenTextField);
		unterZeitraumwaehler.add(bestaetigungsgesamtkostenButton);

		HorizontalLayout knopfLeisteUnten = new HorizontalLayout();
		knopfLeisteUnten.setPadding(true);
		knopfLeisteUnten.setJustifyContentMode(JustifyContentMode.CENTER);
		knopfLeisteUnten.add(generiereCSV);
		knopfLeisteUnten.add(generiereHTML);
		knopfLeisteUnten.add(exitButton);
		// add(knopfLeisteUnten);

		setMargin(true);
		// setAlignItems(Alignment.START);
		// Center the layouts on the main vertical layout (MainView)
		setHorizontalComponentAlignment(Alignment.CENTER, indexoptionsleiste, zeitraumWaehlerMittig,
				unterZeitraumwaehler, knopfLeisteUnten);
		// Add the layouts to the main vertical layout (MainView)
		add(indexoptionsleiste, zeitraumWaehlerMittig, unterZeitraumwaehler, knopfLeisteUnten);

	}

	public List<String> getMappingFelder(String indexName) {
		List<String> mappingFelder = new ArrayList<>();
		SearchRequest suchAnfrage = new SearchRequest(indexName);
		SearchSourceBuilder quellBuilder = new SearchSourceBuilder();
		quellBuilder.query(QueryBuilders.matchAllQuery());
		suchAnfrage.source(quellBuilder);
		RestHighLevelClient elasticsearchClient = elasticsearchkonfiguration.getElasticsearchClient();
		try {
			SearchResponse antwort = elasticsearchClient.search(suchAnfrage, RequestOptions.DEFAULT);

			for (SearchHit treffer : antwort.getHits().getHits()) {
				Map<String, Object> quelleAlsMap = treffer.getSourceAsMap();
				for (String feld : quelleAlsMap.keySet()) {
					if (!mappingFelder.contains(feld)) {
						mappingFelder.add(feld);
					}
				}
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE,
					"Es ist ein Fehler bei der Abfrage der Mapping Felder aufgetreten! Der mögliche Grund könnte sein: ",
					e.getMessage());
		}
		return mappingFelder;
	}

	public void erstelleIndex(String selectedIndexName) {
        try {
            XContentBuilder mappingBuilder = createIndexMapping();

            CreateIndexRequest request = new CreateIndexRequest(selectedIndexName)
                .settings(Settings.builder()
                    .put("number_of_shards", 1)
                    .put("number_of_replicas", 0))
                .mapping("doc", mappingBuilder);

            CreateIndexResponse response = elasticsearchClient.indices().create(request, RequestOptions.DEFAULT);

            if (response.isAcknowledged()) {
                System.out.println("Index erfolgreich erstellt: " + selectedIndexName);
            } else {
                System.err.println("Index konnte nicht erstellt werden: " + selectedIndexName);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Fehler beim Erstellen des Index: " + selectedIndexName);
        }
    }
            
	private XContentBuilder createIndexMapping() throws IOException {
        return XContentFactory.jsonBuilder()
            .startObject()
                .startObject("_meta")
                    .field("created_by", "file-data-visualizer")
                .endObject()
                .startObject("properties")
                    .startObject("@timestamp")
                        .field("type", "date")
                    .endObject()
                    .startObject("bytes")
                        .field("type", "keyword")
                    .endObject()
                    .startObject("document_size")
                        .field("type", "long")
                        .field("store", true)
                    .endObject()
                    .startObject("host")
                        .field("type", "ip")
                    .endObject()
                    .startObject("kubernetes_namespace")
                        .field("type", "keyword")
                    .endObject()
                    .startObject("message")
                        .field("type", "keyword")
                    .endObject()
                    .startObject("method")
                        .field("type", "keyword")
                    .endObject()
                    .startObject("protocol")
                        .field("type", "keyword")
                    .endObject()
                    .startObject("referer")
                        .field("type", "keyword")
                    .endObject()
                    .startObject("request")
                        .field("type", "keyword")
                    .endObject()
                    .startObject("status")
                        .field("type", "keyword")
                    .endObject()
                    .startObject("user-identifier")
                        .field("type", "keyword")
                    .endObject()
                .endObject()
            .endObject();
    }

	private boolean generiereHTML() {
		boolean rueckgabe = false;
		try {
			HTMLDateiSchreiber htmlDateiSchreiber = new HTMLDateiSchreiber(elasticsearchClient, propertiesVerarbeiter,
					elasticsearchkonfiguration);
			int fileSuffix = 1;
			File file = new File(htmlDateipfad);

			while (file.exists()) {
				htmlDateipfad = htmlAlternativpfad + fileSuffix + ".html";
				file = new File(htmlDateipfad);
				fileSuffix++;
			}
			htmlDateiSchreiber.setHTMLDateipfad(htmlDateipfad);
			htmlDateiSchreiber.writeHTML(gewaehlterindex, gewaehlteStartZeit, gewaehlteEndZeit, gesamtkosten, waehrung);
			rueckgabe = true;
			logger.info("Öffnet die HTML-Datei im Google Chrome Browser");
			openHtmlInChrome(htmlDateipfad);
		} catch (Exception e) {
			rueckgabe = false;
			logger.log(Level.SEVERE,
					"Es ist ein Fehler bei der Erstellung der CSV Datei aufgetreten! Der mögliche Grund könnte sein: ",
					e.getMessage());
		}
		return rueckgabe;
	}
	
	public void openHtmlInChrome(String htmlDateipfad) {
        try {
             // Passe den Pfad zu Chrome an
            String url = "file:///" + htmlDateipfad.replace("\\", "/");
            Runtime.getRuntime().exec(new String[]{this.chromePfad, url});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public void openCSVWithDefaultProgram(String csvDateipfad) {
		try {
            
           String url = csvDateipfad.replace("\\", "/");
           Runtime.getRuntime().exec(new String[]{this.microsoftVisualStudioCodePfad, url});
       } catch (IOException e) {
           e.printStackTrace();
       }
   }
	
	private boolean generiereCSV() {
		boolean rueckgabe = false;
		try {
			CSVDateiSchreiber csvDateiSchreiber = new CSVDateiSchreiber(elasticsearchClient, propertiesVerarbeiter,
					elasticsearchkonfiguration);
			int fileSuffix = 1;
			File file = new File(csvDateipfad);

			while (file.exists()) {
				csvDateipfad = csvAlternativpfad + fileSuffix + ".csv";
				file = new File(csvDateipfad);
				fileSuffix++;
			}
			csvDateiSchreiber.setCSVDateipfad(csvDateipfad); // Setzen Sie den Pfad hier
			csvDateiSchreiber.writeCSV(gewaehlterindex, gewaehlteStartZeit, gewaehlteEndZeit, gesamtkosten, waehrung);
			rueckgabe = true;
			openCSVWithDefaultProgram(csvDateipfad);
		} catch (Exception e) {
			rueckgabe = false;
			logger.log(Level.SEVERE,
					"Es ist ein Fehler bei der Erstellung der CSV Datei aufgetreten! Der mögliche Grund könnte sein: ",
					e.getMessage());
		}
		return rueckgabe;
	}

	// Diese Methode füllt das ComboBox-Feld mit den vorhandenen Indexnamen aus dem
	// Elasticsearch-Container
	private void fillIndexNames() {
		try {
			// Create an instance of the ElasticsearchUtils class with the
			// elasticsearchClient
			ElasticInteraction elasticInteraction = new ElasticInteraction(elasticsearchClient, propertiesVerarbeiter,
					elasticsearchkonfiguration);

			// Call the getVorhandeneIndexNamen() method to get the existing index names
			List<String> existingIndexNames = elasticInteraction.getVorhandeneIndexNamen();

			// Set the existing index names to the ComboBox
			indexNameComboBox.setItems(existingIndexNames);
		} catch (Exception e) {
			// Handle the exception if an error occurs while getting the index names
			e.printStackTrace();
		}
	}

	private void showSelectedMappingField(String mappingField) {
		// Show the selected mapping field in a dialog or perform any other action as
		// needed
		Dialog dialog = new Dialog();
		dialog.add(new Label("Ausgewähltes Mapping Feld: " + mappingField));
		dialog.open();
	}

	public static void closeApplication() {
		// Get the ApplicationContext
		ApplicationContext context;
		try {
			context = (ApplicationContext) UI.getCurrent().getSession().getService().getContext();
			if (context != null) {
				// Close the application gracefully
				SpringApplication.exit(context, () -> 0);
			} else {
				// If the ApplicationContext is not available, use System.exit as a fallback
				System.exit(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/*
	 * public static void main(String[] args) throws IOException { MainView mainView
	 * = new MainView(elasticsearchClient, propertiesVerarbeiter,
	 * elasticsearchkonfiguration); String indexName = "fantasticelastic";
	 * List<String> mappingFields = mainView.getMappingFields(indexName);
	 * System.out.println("Mapping fields for index " + indexName + ":"); for
	 * (String field : mappingFields) { System.out.println(field); } }
	 */
	// MyFileWriter neuCSVFileWriter = new MyFileWriter (elasticsearchClient,
	// propertiesVerarbeiter, elasticsearchkonfiguration,listOfAllNamespaces);
	// logger.info("Die Instanz von dem CSVFileWriter wurde soeben erstellt!");
	// HtmlFileWriter neuHTMLFileWriter = new HtmlFileWriter(elasticsearchClient,
	// propertiesVerarbeiter, elasticsearchkonfiguration, listOfAllNamespaces);
	// logger.info("Die Instanz von dem HTMLFileWriter wurde soeben erstellt!");
	//
	// ElasticInteraction.getallNamespaces(elasticsearchClient, gewaehlterindex);

}
