/**
 * Copyright (c) 2013, 2014, ControlsFX All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. * Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. * Neither the name of ControlsFX, any associated
 * website, nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.fxyz;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import javafx.application.Application;
import javafx.scene.ParallelCamera;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.fxyz.client.HiddenSidesClient;
import org.fxyz.client.SimpleWindowFrame;
import org.fxyz.model.Project;

public class FXyzClient extends Application {

    public static final String 
            BACKGROUNDS = FXyzClient.class.getResource("clientBackgrounds.css").toExternalForm(),
            GLASS_BLACK_SMOKE = FXyzClient.class.getResource("smokeBlackGlass.css").toExternalForm();
            //CUSTOM_WINDOW = FXyzClient.class.getResource("images/customWindow.css").toExternalForm();
            
    private Map<String, Project> projectsMap;

    private Stage stage;
    
    private HBox rootContainer;
    private VBox leftContainer; 
    private StackPane centerContainer;
    private VBox rightContainer;
    

    private FXyzSample selectedSample;

    private TreeView<FXyzSample> samplesTreeView;
    private TreeItem<FXyzSample> root;

    public static void main(String[] args) {
        launch(args);        
    }

    @Override
    public void start(final Stage stage) throws Exception {
        Application.setUserAgentStylesheet(GLASS_BLACK_SMOKE);
        this.stage = stage;        
        stage.getIcons().add(new Image(getClass().getResource("images/logo2.png").toExternalForm()));
        /*/SimpleSamplerClient client = new SimpleSamplerClient(stage);  
        
        //Look at the clientBackgrounds.css file in resources for others
        //client.getStyleClass().add("comp-fade-background");
        
        SimpleSliderClient ssc = new SimpleSliderClient(stage, true);
        ssc.getStyleClass().add("blue-fade-background");
        
        Scene scene = new Scene(ssc, 1200, 800);//client, client.getPrefWidth(), client.getPrefHeight(), true, SceneAntialiasing.BALANCED);    
        scene.setCamera(new ParallelCamera());
        scene.setFill(null);
        scene.getStylesheets().addAll(BACKGROUNDS);
        
        stage.setScene(scene);
        stage.show();
        */
        HiddenSidesClient client = new HiddenSidesClient();
        client.setContent(new Rectangle(400,300, Color.AQUA));
        client.setLeft(new Rectangle(100,300, Color.BLUE));
        client.setTriggerDistance(15);
        
        SimpleWindowFrame frame = new SimpleWindowFrame(stage, 400,300);
        frame.setIconImage(new Image(getClass().getResource("images/logo2.png").toExternalForm()));
        frame.setText("Fxyz-SamplerApp ver: 0.0.1a");
        frame.setRootContent(client);
               
        Scene scene = new Scene(frame);
        scene.setFill(Color.TRANSPARENT);
        scene.setCamera(new ParallelCamera());
        
        this.stage.setScene(scene);
        this.stage.initStyle(StageStyle.TRANSPARENT);        
        this.stage.show();
        
        //CustomWindow window = new CustomWindow(stage);
    }

    
    /*==========================================================================
    *                           Source Code Methods
    ==========================================================================*/

    private String getResource(String resourceName, Class<?> baseClass) {
        Class<?> clz = baseClass == null ? getClass() : baseClass;
        return getResource(clz.getResourceAsStream(resourceName));
    }

    private String getResource(InputStream is) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String getSourceCode(FXyzSample sample) {
        String sourceURL = sample.getSampleSourceURL();

        try {
            // try loading via the web or local file system
            URL url = new URL(sourceURL);
            InputStream is = url.openStream();
            return getResource(is);
        } catch (IOException e) {
            // no-op - the URL may not be valid, no biggy
        }

        return getResource(sourceURL, sample.getClass());
    }

    private String formatSourceCode(FXyzSample sample) {
        String sourceURL = sample.getSampleSourceURL();
        String src;
        if (sourceURL == null) {
            src = "No sample source available";
        } else {
            src = "Sample Source not found";
            try {
                src = getSourceCode(sample);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }

        // Escape '<' by "&lt;" to ensure correct rendering by SyntaxHighlighter
        src = src.replace("<", "&lt;");

        String template = getResource("/fxsampler/util/SourceCodeTemplate.html", null);
        return template.replace("<source/>", src);
    }

    private String formatCss(FXyzSample sample) {
        String cssUrl = sample.getControlStylesheetURL();
        String src;
        if (cssUrl == null) {
            src = "No CSS source available";
        } else {
            src = "Css not found";
            try {
                src = new String(
                        Files.readAllBytes(Paths.get(getClass().getResource(cssUrl).toURI()))
                );
            } catch (URISyntaxException | IOException ex) {
                ex.printStackTrace();
            }
        }

        // Escape '<' by "&lt;" to ensure correct rendering by SyntaxHighlighter
        src = src.replace("<", "&lt;");

        String template = getResource("/fxsampler/util/CssTemplate.html", null);
        return template.replace("<source/>", src);
    }  

    
}