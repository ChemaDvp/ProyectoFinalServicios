package com.proyecto2v.clientefjom_proyecto2v;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Optional;

public class HelloController {
    private static final int PUERTO_SERVER = 5010;
    private static final int PUERTO_DESTINO = 6010;

    @FXML
    private ListView<String> chatListView;

    @FXML
    private TextField messageTextField;

    private DatagramSocket socket;
    private String nick = "";

    @FXML
    protected void initialize() {
        obtenerNick();
        try {
            // Inicializa el socket del cliente
            socket = new DatagramSocket(PUERTO_DESTINO);

            // Envía el Nick al servidor
            enviarNickAlServidor();

            // Inicia un hilo para escuchar mensajes del servidor
            Thread listenerThread = new Thread(this::escucharMensajes);
            listenerThread.setDaemon(true);
            listenerThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void obtenerNick() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Elegir Nick");
        dialog.setHeaderText("Ingresa un Nick único:");
        dialog.setContentText("Nick:");

        Optional<String> result = dialog.showAndWait();
        nick = result.orElse("DefaultNick");
    }

    private void enviarNickAlServidor() {
        try {
            // Construye el paquete UDP con el Nick del usuario
            DatagramPacket paqueteNick = new DatagramPacket(
                    nick.getBytes(),
                    nick.length(),
                    InetAddress.getLocalHost(),
                    PUERTO_SERVER
            );

            // Envía el paquete al servidor
            socket.send(paqueteNick);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void enviarMensaje() {
        try {
            String mensaje = messageTextField.getText();

            // Construye el paquete UDP con el Nick del usuario y el mensaje
            String mensajeCompleto = mensaje;
            DatagramPacket paqueteSalida = new DatagramPacket(
                    mensajeCompleto.getBytes(),
                    mensajeCompleto.length(),
                    InetAddress.getLocalHost(),
                    PUERTO_SERVER
            );

            // Envía el paquete al servidor
            socket.send(paqueteSalida);

            // Limpia el campo de texto después de enviar el mensaje
            messageTextField.clear();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void escucharMensajes() {
        try {
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket paqueteEntrante = new DatagramPacket(buffer, buffer.length);
                socket.receive(paqueteEntrante);

                String mensajeRecibido = new String(paqueteEntrante.getData()).trim();

                // Agrega el mensaje recibido al ListView en el hilo de la aplicación JavaFX
                Platform.runLater(() -> {
                    chatListView.getItems().add(mensajeRecibido);
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}