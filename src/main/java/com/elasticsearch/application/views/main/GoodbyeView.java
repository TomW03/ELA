package com.elasticsearch.application.views.main;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;

import com.vaadin.flow.dom.Element;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Route("goodbye")
@PageTitle("Goodbye")
@Component
public class GoodbyeView extends VerticalLayout {

    private Label countdownLabel;
    private Button cancelButton;

    public GoodbyeView() {
        setAlignItems(Alignment.CENTER);

        countdownLabel = new Label("10");
        countdownLabel.getStyle().set("font-size", "3em");
        countdownLabel.getStyle().set("color", "green");

        cancelButton = new Button("Cancel");
        cancelButton.getStyle().set("background-color", "red");
        cancelButton.getStyle().set("color", "white");
        cancelButton.addClickListener(e -> stopCountdown());

        add(countdownLabel, cancelButton);

        startCountdown();
    }
    
    private void startCountdown() {
        int interval = 1000; // Update every 1 second
        int duration = 10; // Total duration in seconds

        String jsScript = "var remaining = " + duration + ";" +
                "function updateCountdown() {" +
                "  $0.innerText = remaining;" +
                "  if (remaining > 0) {" +
                "    remaining--;" +
                "    setTimeout(updateCountdown, " + interval + ");" +
                "  }" +
                "}" +
                "updateCountdown();";

        UI ui = UI.getCurrent();
        if (ui != null) {
            Element countdownElement = countdownLabel.getElement();
            ui.getPage().executeJs(jsScript, countdownElement);
        }
    }
    
    private void stopCountdown() {
        // Stop the client-side countdown by clearing the client-side timer
        UI ui = UI.getCurrent();
        if (ui != null) {
            Element countdownElement = countdownLabel.getElement();
            ui.getPage().executeJs("clearTimeout(updateCountdown)", countdownElement);

            // Redirect back to the index page ("/")
            ui.navigate("");
        }
    }
    
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        // Initialize and start the countdown only when the view is attached to the UI
        startCountdown();
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
	
	/*public static void main(String[] args) {
        String currentDir = System.getProperty("user.dir");
        System.out.println("Current project path: " + currentDir);
    }*/
}
