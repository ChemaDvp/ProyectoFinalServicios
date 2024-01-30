package com.example.serverchemayjavi;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HelloController {

    @FXML
    private ListView<String> serverListView;
    private static final int PUERTO_ESCUCHA = 5010;
    private static final String PALABRA_STOP = "STOP";

    // Mapa que asocia la combinación de dirección IP y puerto de origen con nicks
    private Map<ClienteKey, String> mapaClientes = Collections.synchronizedMap(new HashMap<>());
    // Agrega esta variable a la clase para almacenar las direcciones IP y puertos de origen
    private List<ClienteKey> listaClientes = Collections.synchronizedList(new ArrayList<>());

    // Clase auxiliar para representar la combinación de IP y puerto
    private static class ClienteKey {
        private final InetAddress ip;
        private final int puerto;

        public ClienteKey(InetAddress ip, int puerto) {
            this.ip = ip;
            this.puerto = puerto;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClienteKey that = (ClienteKey) o;
            return puerto == that.puerto && ip.equals(that.ip);
        }

        @Override
        public int hashCode() {
            return 31 * ip.hashCode() + puerto;
        }
    }

    // Método para inicializar el controlador
    @FXML
    private void initialize() {
        iniciarServidor();
    }

    // Método para iniciar el servidor
    private void iniciarServidor() {
        new Thread(() -> {
            try {
                boolean bandera = true;
                DatagramSocket socket = new DatagramSocket(PUERTO_ESCUCHA, InetAddress.getByName("0.0.0.0"));
                addMensajeLista("Servidor escuchando en el puerto " + PUERTO_ESCUCHA);

                while (bandera) {
                    byte[] buffer = new byte[1024];
                    DatagramPacket paqueteEntrante = new DatagramPacket(buffer, buffer.length);

                    addMensajeLista("Server: esperando Datagrama .......... ");
                    socket.receive(paqueteEntrante);

                    int bytesRec = paqueteEntrante.getLength();
                    String contenidoPaquete = new String(paqueteEntrante.getData()).trim();
                    int puertoOrigen = paqueteEntrante.getPort();
                    InetAddress ipOrigen = paqueteEntrante.getAddress();
                    int puertoDestino = socket.getLocalPort();

                    // Agrega la dirección IP y el puerto de origen a la lista
                    ClienteKey clienteKey = new ClienteKey(ipOrigen, puertoOrigen);
                    if (!listaClientes.contains(clienteKey)) {
                        listaClientes.add(clienteKey);
                    }

                    // Si el cliente no está en el mapa, se asume que está enviando su nick
                    if (!mapaClientes.containsKey(clienteKey)) {
                        String nick = contenidoPaquete;
                        if (!nick.isEmpty() && !mapaClientes.containsValue(nick)) {
                            mapaClientes.put(clienteKey, nick);
                            Platform.runLater(() -> {
                                addMensajeLista("SERVER: Cliente " + nick + " se ha unido al chat.");
                            });
                        }
                    }

                    // Imprimir información en la ListView
                    Platform.runLater(() -> {
                        addMensajeLista("Server: numero de Bytes recibidos->" + bytesRec);
                        addMensajeLista("Server: contenido del Paquete->" + contenidoPaquete);
                        addMensajeLista("Server: puerto origen del mensaje->" + puertoOrigen);
                        addMensajeLista("Server: IP de origen->" + ipOrigen.getHostAddress());
                        addMensajeLista("Server: puerto destino del mensaje->" + puertoDestino);
                        addMensajeLista("=============================");
                    });

                    if (!contenidoPaquete.equalsIgnoreCase(PALABRA_STOP)) {
                        reenviarMensaje(mapaClientes.get(clienteKey) + ": " + contenidoPaquete);
                    } else {
                        Platform.runLater(() -> addMensajeLista("Servidor detenido por comando STOP"));
                        bandera = false;
                    }
                }

                socket.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    // Método para reenviar mensajes
    private void reenviarMensaje(String mensaje) {
        try (DatagramSocket socket = new DatagramSocket()) {
            for (ClienteKey clienteKey : listaClientes) {
                DatagramPacket paqueteSalida = new DatagramPacket(
                        mensaje.getBytes(),
                        mensaje.length(),
                        clienteKey.ip,
                        clienteKey.puerto
                );
                Platform.runLater(() -> {
                    addMensajeLista("Mensaje reenviado a " + mapaClientes.get(clienteKey) +
                            " en el puerto " + clienteKey.puerto);
                });

                socket.send(paqueteSalida);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addMensajeLista(String message) {
        Platform.runLater(() -> serverListView.getItems().add(message));
    }
}