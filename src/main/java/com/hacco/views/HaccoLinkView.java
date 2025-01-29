package com.hacco.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@PageTitle("Hacco Link by Image")
@Route("")
public class HaccoLinkView extends HorizontalLayout {

    private final Grid<String> grid;
    private final List<String> urls;

    public HaccoLinkView() {
        setPadding(true);

        // Initialize components
        var buffer = new MemoryBuffer();
        var upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/png", "image/jpeg");

        grid = new Grid<>(String.class);
        urls = new ArrayList<>();

        // Upload listener
        upload.addSucceededListener(event -> {
            try (InputStream inputStream = buffer.getInputStream()) {
                BufferedImage image = ImageIO.read(inputStream);
                if (image != null) {
                    urls.clear();
                    urls.addAll(extractUrlsFromImage(image));
                    System.out.println(urls);
                    grid.setItems(urls);
                    if (urls.isEmpty()) {
                        Notification.show("No hay urls en la imagen.", 3000, Notification.Position.MIDDLE);
                    }
                    Notification.show("La imagen se leido correctamente.");
                } else {
                    Notification.show("Fallo al leer la imagen.", 3000, Notification.Position.MIDDLE);
                }
            } catch (Exception e) {
                Notification.show("Error al procesar la imagen: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });

        grid.removeAllColumns();
        // Grid setup
        grid.addColumn(new ComponentRenderer<>(url -> {
                    Anchor link = new Anchor(url, url);
                    link.setTarget("_blank");
                    return link;
                }))
                .setHeader("Links")
                .setAutoWidth(true);

        grid.setWidthFull();

        var layout = new VerticalLayout();
        var divUpload = new Div(upload);
        divUpload.setHeight("200px");
        divUpload.setWidthFull();

        layout.add(divUpload);
        layout.add(grid);

        // Layout setup
        add(layout);
    }

    // Extract URLs matching the pattern from the image
    private List<String> extractUrlsFromImage(BufferedImage image) throws Exception {
        List<String> foundUrls = new ArrayList<>();

        // Use Tesseract OCR to extract text from the image
        net.sourceforge.tess4j.Tesseract tesseract = new net.sourceforge.tess4j.Tesseract();
        tesseract.setDatapath("/usr/share/tessdata/");
        tesseract.setLanguage("eng");

        String extractedText = tesseract.doOCR(image);

        // Regex to match the desired URL pattern
        Pattern pattern = Pattern.compile("https://c\\.hacoo\\.pl/\\S+");
        Matcher matcher = pattern.matcher(extractedText);

        while (matcher.find()) {
            foundUrls.add(matcher.group());
        }

        return foundUrls;
    }

}
