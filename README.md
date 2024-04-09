# Meine Elastic-API-App

Dieses Projekt ist eine Beispiel-Implementierung einer REST-API, die CRUD-Operationen (Create, Read, Update, Delete) für Elasticsearch ausführt. Die Daten werden dabei zunächst in einem Elasticsearch-Container/Datenbank gespeichert. Hierfür wurde die Kibana-Oberfläche verwendet. Die REST-API wurde mit Spring Boot und der Spring Data Elasticsearch-API entwickelt. Die Frontend-Oberfläche wurde mit Vaadin erstellt.

## Installation

1. Stelle sicher, dass Java und Maven auf deinem System installiert sind.
2. Klone das Repository oder lade die ZIP-Datei herunter und entpacke sie.
3. Navigiere zum Verzeichnis des Projekts und führe den folgenden Befehl aus, um die Abhängigkeiten herunterzuladen und das Projekt zu erstellen:
```bash
mvn clean install
```

## Start der Anwendung

Um die Anwendung zu starten, führen Sie den folgenden Befehl aus:
```bash
mvn spring-boot:run
```

Mit diesem Befehl wird die Anwendung gestartet und ist anschließend unter der entsprechenden URL oder IP-Adresse erreichbar. 
Beachten Sie, dass die Anwendung möglicherweise weitere Konfigurationen oder Umgebungsvariablen benötigt, je nach den spezifischen Anforderungen und Einstellungen.
