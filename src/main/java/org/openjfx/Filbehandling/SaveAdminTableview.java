package org.openjfx.Filbehandling;

import javafx.concurrent.WorkerStateEvent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import org.openjfx.Component;
import org.openjfx.ComponentRegister;
import org.openjfx.ThreadAdmin;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;

public class SaveAdminTableview {

    private static File selectedFile;
    private AnchorPane anchorpane;
    private ThreadAdmin task;
    private Label errorMsg;
    private boolean failed = false;
    private boolean exited = false;

    public void save(ComponentRegister cr, AnchorPane anchorpane, Label errorMsg) throws InterruptedException {
        this.anchorpane = anchorpane;
        disable();
        FileChooser fc = new FileChooser();
        fc.setTitle("Lagre Komponenter");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("binary files","*.jobj"));
        selectedFile = fc.showSaveDialog(null);
        try (OutputStream os = Files.newOutputStream(selectedFile.toPath());
             ObjectOutputStream out = new ObjectOutputStream(os))
        {
            out.writeObject(cr);
            open();
        } catch (IOException | ClassCastException e) {
            e.printStackTrace();
            failed = true;
            open();
        } catch (NullPointerException e){
            exited = true;
            open();
        }
    }

    public void disable(){
        anchorpane.setDisable(true);
    }

    public void open(){
        task = new ThreadAdmin(selectedFile, failed, exited);
        task.setOnSucceeded(this::threadDone);
        task.setOnFailed(this::threadError);
        Thread thread = new Thread(task);
        thread.start();
    }

    public void threadDone(WorkerStateEvent event) {
        anchorpane.setDisable(false);
        errorMsg.setText("");
    }

    public void threadError(WorkerStateEvent event){
        if (!exited){
            errorMsg.setText("Feil med lagring av fil");
        }
        anchorpane.setDisable(false);
        errorMsg.setText("");
    }
}
