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

    private List<Monster> monsters = new ArrayList<>();
    private List<Ally> players = new ArrayList<>();
    private int width = 50;
    private int height = 30;

    private int nextSpriteId = 0;

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

        for (int i = 0; i < height / 2; i++) {
            Monster monster = new Monster(getSpriteId());
            monster.setPosition(new Coords(width - 1, i * 2));
            monster.setDirection(Direction.Left);
            monsters.add(monster);
        }

        new Thread(() -> {
            Map<Sprite, Integer> remaining = new HashMap<Sprite, Integer>();

            for (Monster monster : monsters) {
                remaining.put(monster, monster.getTimePerSquare());
            }

            while (true) {
                Integer time = null;

                for (Map.Entry<Sprite, Integer> entry : remaining.entrySet()) {
                    if (time == null || entry.getValue() < time) {
                        time = entry.getValue();
                    }
                }

                try {
                    Thread.sleep(time);
                } catch (InterruptedException e) {}

                for (Map.Entry<Sprite, Integer> entry : remaining.entrySet()) {
                    Integer newTime = entry.getValue() - time;

                    Sprite sprite = entry.getKey();
                    if (newTime.equals(0)) {
                        newTime = sprite.getTimePerSquare();
                        sprite.setPosition(sprite.getPosition().transform(sprite.getDirection()));
                    }

                    remaining.put(sprite, newTime);
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
            Ally sprite = new Ally(getSpriteId());
            players.add(sprite);
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
        Monster[] monstersArray = new Monster[monsters.size()];
        int i = 0;

        for (Monster monster : monsters) {
            monstersArray[i++] = new Monster(monster);
        }

        Ally[] playersArray = new Ally[players.size()];
        i = 0;

        for (Ally player : players) {
            playersArray[i++] = new Ally(player);
        }

        StatusUpdate update = new StatusUpdate(++updateNumber, width, height, monstersArray, playersArray);

        for (ClientData data : clients.values()) {
            update.setPlayerId(data.getSprite().getId());
            data.getClient().update(update);
        }
    }

    protected int getSpriteId () {
        return nextSpriteId++;
    }
}

class StatusUpdate implements Serializable {
    private long number;

    private int boardWidth;
    private int boardHeight;

    private Monster[] monsters;
    private int playerId;
    private Ally[] players;

    public StatusUpdate (long number, int boardWidth, int boardHeight, Monster[] monsters, Ally[] players) {
        this.number = number;
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        this.monsters = monsters;
        this.players = players;
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

    public Monster[] getMonsters () {
        return monsters;
    }

    public void setPlayerId (int playerId) {
        this.playerId = playerId;
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