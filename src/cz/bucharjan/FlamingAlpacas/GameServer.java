package cz.bucharjan.FlamingAlpacas;

import cz.bucharjan.FlamingAlpacas.Messages.ConnectMessage;
import cz.bucharjan.FlamingAlpacas.Messages.MoveMessage;
import cz.bucharjan.FlamingAlpacas.Messages.SteerMessage;
import cz.bucharjan.FlamingAlpacas.Sprites.Ally;
import cz.bucharjan.FlamingAlpacas.Sprites.Monster;
import cz.bucharjan.FlamingAlpacas.Sprites.Sprite;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Created by teyras on 11.2.15.
 */
public class GameServer {
    private int port;
    private Map<Client, ClientData> clients = new HashMap<>();
    private long updateNumber;

    private final List<Monster> monsters = new ArrayList<>();
    private final List<Ally> players = new ArrayList<>();
    private Board board;
    private GameController controller;


    public GameServer (int port) {
        this.port = port;
    }

    public void serve () {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {}

                updateClients();
            }
        }).start();

        int width = 50;
        int height = 30;
        boolean[][] walls = new boolean[width][height];

        for (int i = 0; i < width; i++) {
            walls[i][0] = true;
            walls[i][height - 1] = true;
        }

        this.board = new Board(width, height, walls);
        this.controller = new GameController(board, monsters, players);
        this.controller.run();

        try {
            DatagramSocket socket = new DatagramSocket(port);
            DatagramPacket packet = new DatagramPacket(new byte[65536], 65536);

            while (true) {
                try {
                    socket.receive(packet);
                    ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(packet.getData(), 0, packet.getLength()));
                    Client from = null;

                    for (ClientData clientData: clients.values()) {
                        Client client = clientData.getClient();
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
            Ally sprite = controller.spawnPlayer();
            clients.put(from, new ClientData(from, sprite));
        } else if (message instanceof MoveMessage) {
            ClientData data = clients.get(from);
            if (data == null) {
                return;
            }

            Ally sprite = data.getSprite();
            sprite.transformPosition(((MoveMessage) message).getDirection());
        } else if (message instanceof SteerMessage) {
            ClientData data = clients.get(from);
            if (data == null) {
                return;
            }

            Ally sprite = data.getSprite();
            sprite.setDirection(((SteerMessage) message).getDirection());
        }
    }

    public synchronized void updateClients () {
        StatusUpdate update = new StatusUpdate(++updateNumber);
        update.setBoard(board);
        update.setObjects(controller.getMonstersCopy(), controller.getPlayersCopy());

        for (ClientData data : clients.values()) {
            update.setPlayerId(data.getSprite().getId());
            data.getClient().update(update);
        }
    }
}

class StatusUpdate implements Serializable {
    private long number;

    private Board board;
    private Monster[] monsters;
    private Ally[] players;

    private int playerId;

    public StatusUpdate (long number) {
        this.number = number;
    }

    public void setBoard (Board board) {
        this.board = board;
    }

    public void setObjects (Monster[] monsters, Ally[] players) {
        this.monsters = monsters;
        this.players = players;
    }

    public void setPlayerId (int playerId) {
        this.playerId = playerId;
    }

    public long getNumber () {
        return number;
    }

    public Board getBoard () {
        return board;
    }

    public Monster[] getMonsters () {
        return monsters;
    }

    public int getPlayerId () {
        return playerId;
    }

    public Ally[] getPlayers() {
        return players;
    }
}

class ClientData {
    private Client client;
    private Ally sprite;

    public ClientData (Client client, Ally sprite) {
        this.client = client;
        this.sprite = sprite;
    }

    public Client getClient () {
        return client;
    }

    public Ally getSprite () {
        return sprite;
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