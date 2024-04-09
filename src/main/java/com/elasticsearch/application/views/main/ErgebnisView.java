package com.elasticsearch.application.views.main;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;

import com.vaadin.flow.dom.Element;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Route("Ergebnis")
@PageTitle("Ergebnis")
@SuppressWarnings({"unused", "serial"})
@Component
public class ErgebnisView extends VerticalLayout {

    private Button zurueckButton, exitButton;
    private static Anchor downloadLink; // Neues Anchor-Element hinzufügen

    private static IFrame htmlFrame;
    private String htmlDateipfad;
    
    void sethtmlDateipfad(String inputhtmlDateipfad) {
    	this.htmlDateipfad = inputhtmlDateipfad;
    }

    public ErgebnisView() {
    	 
        
        setAlignItems(Alignment.CENTER);
        zurueckButton = new Button("Zurück|Back");
        zurueckButton.getStyle().set("background-color", "blue");
        zurueckButton.getStyle().set("color", "white");
        zurueckButton.addClickListener(e -> geheZurueck());
        
        exitButton = new Button("Beenden|Exit");
        exitButton.getStyle().set("background-color", "red");
        exitButton.getStyle().set("color", "white");
        exitButton.addClickListener(e -> geheZurueck());
        
        // Erstelle einen Link, um die zuletzt generierte HTML-Datei herunterzuladen
        downloadLink = new Anchor("", "Download letzte HTML-Datei");
        downloadLink.getStyle().set("color", "black");
        downloadLink.getStyle().set("text-decoration", "none");
        downloadLink.setTarget("_blank"); // Öffnen Sie den Link in einem neuen Tab
        
       
        // Erstelle das IFrame-Element
        htmlFrame = new IFrame();
        htmlFrame.setHeight("600px"); // Setzen Sie die Höhe des Frames
        htmlFrame.setWidth("100%"); // Setzen Sie die Breite des Frames

        add(htmlFrame, zurueckButton, exitButton, downloadLink);
    }
    
    private void geheZurueck() {
    	UI ui = UI.getCurrent();
    	ui.navigate("");
	}
    
    void setzeWerte(String htmlDateipfad) {
    	// Setzen Sie die src des IFrame-Elements auf den Pfad zur HTML-Datei
        htmlFrame.setSrc(htmlDateipfad);
        // Setzen Sie den href-Attribut des Anker-Elements auf den Pfad zur HTML-Datei
        downloadLink.setHref(htmlDateipfad);
    }
    
	public void closeApplication() {
		// Get the ApplicationContext
		ApplicationContext context = getApplicationContext();
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

	private ApplicationContext getApplicationContext() {
		return VaadinService.getCurrent().getContext().getAttribute(ApplicationContext.class);
	}
	
}
