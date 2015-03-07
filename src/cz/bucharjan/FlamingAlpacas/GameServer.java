package cz.bucharjan.FlamingAlpacas;

import cz.bucharjan.FlamingAlpacas.Messages.ConnectMessage;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by teyras on 11.2.15.
 */
public class GameServer {
    private int port;
    private List<Client> clients = new ArrayList<>();
    private long updateNumber;
    Random random = new Random();

    public GameServer (int port) {
        this.port = port;
    }

    public void serve () {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    updateClients();
                } catch (InterruptedException e) {

                }
            }
        }).start();

        try {
            DatagramSocket socket = new DatagramSocket(port);
            DatagramPacket packet = new DatagramPacket(new byte[65536], 65536);

            while (true) {
                try {
                    socket.receive(packet);
                    ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(packet.getData(), 0, packet.getLength()));
                    Client from = null;

                    for (Client client: clients) {
                        if (client instanceof RemoteClient && ((RemoteClient) client).getAddress().equals(packet.getSocketAddress())) {
                            from = client;
                            break;
                        }
                    }

                    if (from == null) {
                        from = new RemoteClient(packet.getSocketAddress());
                    }

                    receiveMessage(stream.readObject(), from);
                } catch (IOException e) {

                } catch (ClassNotFoundException e) {

                }
            }
        } catch (SocketException e) {

        }
    }

    public synchronized void receiveMessage (Object message, Client from) {
        if (message instanceof ConnectMessage) {
            clients.add(from);
        }
    }

    public synchronized void updateClients () {
        int width = 50;
        int height = 30;

        Coords[] monsters = new Coords[10];

        for (int i = 0; i < monsters.length; i++) {
            monsters[i] = new Coords(random.nextInt(Integer.MAX_VALUE) % width, random.nextInt(Integer.MAX_VALUE) % height);
        }

        StatusUpdate update = new StatusUpdate(++updateNumber, width, height, monsters);

        for (Client client : clients) {
            client.update(update);
        }
    }
}

class Coords implements Serializable {
    private int x;
    private int y;

    public Coords (int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX () {
        return x;
    }

    public int getY () {
        return y;
    }
}

class StatusUpdate implements Serializable {
    private long number;

    private int boardWidth;
    private int boardHeight;

    private Coords[] monsters;

    public StatusUpdate (long number, int boardWidth, int boardHeight, Coords[] monsters) {
        this.number = number;
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        this.monsters = monsters;
    }

    public long getNumber () {
        return number;
    }

    public int getBoardWidth () {
        return boardWidth;
    }

    public int getBoardHeight () {
        return boardHeight;
    }

    public Coords[] getMonsters () {
        return monsters;
    }
}

interface UpdateAction {
    public void run (StatusUpdate update);
}

interface Client {
    public void update (StatusUpdate update);
}

class RemoteClient implements Client {
    private DatagramSocket socket;
    private SocketAddress sockaddr;

    public RemoteClient (SocketAddress address) {
        try {
            socket = new DatagramSocket();
            sockaddr = address;
        } catch (SocketException e) {

        }
    }

    public SocketAddress getAddress () {
        return sockaddr;
    }

    @Override
    public void update (StatusUpdate update) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
            new ObjectOutputStream(stream).writeObject(update);
            DatagramPacket packet = new DatagramPacket(stream.toByteArray(), stream.size(), sockaddr);

            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}