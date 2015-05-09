package cz.bucharjan.FlamingAlpacas;

import cz.bucharjan.FlamingAlpacas.Messages.ConnectMessage;
import cz.bucharjan.FlamingAlpacas.Messages.Message;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by teyras on 11.2.15.
 */
public class RemoteServerInterface implements ServerInterface {
    private DatagramSocket socket;
    private SocketAddress sockaddr;
    private List<UpdateAction> updateListeners = new ArrayList<>();
    private long lastUpdateNumber = 0;

    public RemoteServerInterface (InetAddress addr, int port) throws SocketException {
        socket = new DatagramSocket();
        sockaddr = new InetSocketAddress(addr, port);
    }

    @Override
    public void connect (String nickname) {
        sendMessage(new ConnectMessage(nickname));

        new Thread(()-> {
            DatagramPacket packet = new DatagramPacket(new byte[65536], 65536, sockaddr);
            boolean finished = false;

            while (!finished) {
                try {
                    socket.receive(packet);
                    ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(packet.getData(), 0, packet.getLength()));
                    StatusUpdate update = (StatusUpdate) stream.readObject();

                    if (update.getNumber() > lastUpdateNumber) {
                        notifyListeners(update);
                        lastUpdateNumber = update.getNumber();
                        if (update.getState() == GameState.FINISHED) {
                            finished = true;
                        }
                    }
                } catch (IOException e) {
                    System.err.println("IOException");
                } catch (ClassNotFoundException e) {
                    System.err.println("ClassNotFoundException");
                }
            }
        }).start();
    }

    @Override
    public void sendMessage (Message msg) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
            new ObjectOutputStream(stream).writeObject(msg);
            DatagramPacket msgPacket = new DatagramPacket(stream.toByteArray(), stream.size(), sockaddr);
            socket.send(msgPacket);
        } catch (IOException e) {
            System.err.println("IO error when sending message");
        }
    }

    private void notifyListeners (StatusUpdate update) {
        for (UpdateAction action: updateListeners) {
            action.run(update);
        }
    }

    @Override
    public void addUpdateListener (UpdateAction action) {
        updateListeners.add(action);
    }
}
