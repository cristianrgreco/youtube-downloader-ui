package com.cristianrgreco.ytdlui;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;

class AlertFactory {
    private static final int EXCEPTION_ALERT_WIDTH = 675;
    private static final int EXCEPTION_ALERT_HEIGHT = 400;

    private Optional<List<String>> styleSheets;

    public AlertFactory(Optional<List<String>> styleSheets) {
        this.styleSheets = styleSheets;
    }

    public Alert alertForType(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = this.baseAlert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert;
    }

    public Alert errorForException(Exception e, Optional<String> title, Optional<String> header) {
        Alert alert = this.baseAlert(Alert.AlertType.ERROR);

        if (title.isPresent()) {
            alert.setTitle(title.get());
        } else {
            alert.setTitle(e.getMessage());
        }
        if (header.isPresent()) {
            alert.setHeaderText(header.get());
        } else {
            alert.setHeaderText(e.getMessage());
        }

        Label label = new Label("The exception stacktrace was:");
        label.setPadding(new Insets(0, 0, 15, 0));

        String exceptionText = exceptionToString(e);
        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane contentPane = new GridPane();
        contentPane.setMaxWidth(Double.MAX_VALUE);
        contentPane.add(label, 0, 0);
        contentPane.add(textArea, 0, 1);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setContent(contentPane);
        dialogPane.setPrefSize(EXCEPTION_ALERT_WIDTH, EXCEPTION_ALERT_HEIGHT);

        return alert;
    }

    private Alert baseAlert(Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        this.styleSheets.ifPresent(styleSheets -> styleSheets.forEach(
                styleSheet -> alert.getDialogPane().getStylesheets().add(getClass().getResource(styleSheet).toExternalForm())));
        return alert;
    }

    private static String exceptionToString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString().trim();
    }
}
