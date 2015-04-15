package cz.bucharjan.FlamingAlpacas;

import cz.bucharjan.FlamingAlpacas.Messages.ConnectMessage;
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
    private List<Client> clients = new ArrayList<>();
    private long updateNumber;
    Random random = new Random();

    private List<Monster> monsters = new ArrayList<>();
    private int width = 50;
    private int height = 30;

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
            Monster monster = new Monster(i);
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
        Monster[] monstersArray = new Monster[monsters.size()];
        int i = 0;

        for (Monster monster : monsters) {
            monstersArray[i++] = new Monster(monster);
        }

        StatusUpdate update = new StatusUpdate(++updateNumber, width, height, monstersArray);

        for (Client client : clients) {
            client.update(update);
        }
    }
}

class StatusUpdate implements Serializable {
    private long number;

    private int boardWidth;
    private int boardHeight;

    private Monster[] monsters;

    public StatusUpdate (long number, int boardWidth, int boardHeight, Monster[] monsters) {
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

    public Monster[] getMonsters () {
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