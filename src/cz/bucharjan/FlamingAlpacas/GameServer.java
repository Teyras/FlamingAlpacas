package cz.bucharjan.FlamingAlpacas;

import cz.bucharjan.FlamingAlpacas.Messages.ConnectMessage;
import cz.bucharjan.FlamingAlpacas.Messages.MoveMessage;
import cz.bucharjan.FlamingAlpacas.Messages.ShootMessage;
import cz.bucharjan.FlamingAlpacas.Messages.SteerMessage;
import cz.bucharjan.FlamingAlpacas.Sprites.Ally;
import cz.bucharjan.FlamingAlpacas.Sprites.Monster;
import cz.bucharjan.FlamingAlpacas.Sprites.Projectile;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by teyras on 11.2.15.
 */
public class GameServer {
    private int port;
    private Map<Client, ClientData> clients = new HashMap<>();
    private long updateNumber;

    private GameController controller;


    public GameServer (int port) {
        this.port = port;
        this.controller = new GameController();
    }

    public void serve () {
        ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);
        executor.scheduleAtFixedRate(this::updateClients, 0, 200, TimeUnit.MILLISECONDS);

        controller.run();

        try {
            DatagramSocket socket = new DatagramSocket(port);
            DatagramPacket packet = new DatagramPacket(new byte[65536], 65536);

            while (true) {
                try {
                    socket.receive(packet);
                    ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(packet.getData(), 0, packet.getLength()));
                    Client from = null;

                    for (ClientData clientData : clients.values()) {
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
            return;
        }

        ClientData data = clients.get(from);
        if (data == null) {
            return;
        }

        if (message instanceof MoveMessage) {
            Ally sprite = data.getSprite();
            sprite.transformPosition(((MoveMessage) message).getDirection());
        } else if (message instanceof SteerMessage) {
            Ally sprite = data.getSprite();
            sprite.setDirection(((SteerMessage) message).getDirection());
        } else if (message instanceof ShootMessage) {
            controller.startShot(data.getSprite(), ((ShootMessage) message).getOrigin());
        }
    }

    public synchronized void updateClients () {
        StatusUpdate update = new StatusUpdate(++updateNumber);
        update.setBoard(controller.getBoard());
        update.setObjects(controller.getMonstersCopy(), controller.getPlayersCopy(), controller.getProjectilesCopy());

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
    private Projectile[] projectiles;

    private int playerId;

    public StatusUpdate (long number) {
        this.number = number;
    }

    public void setBoard (Board board) {
        this.board = board;
    }

    public void setObjects (Monster[] monsters, Ally[] players, Projectile[] projectiles) {
        this.monsters = monsters;
        this.players = players;
        this.projectiles = projectiles;
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

    public Ally[] getPlayers () {
        return players;
    }

    public Projectile[] getProjectiles () {
        return projectiles;
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