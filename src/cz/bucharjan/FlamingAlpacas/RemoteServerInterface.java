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

    public RemoteServerInterface (InetAddress addr, int port) {
        try {
            socket = new DatagramSocket();

            sockaddr = new InetSocketAddress(addr, port);
        } catch (SocketException e) {

        }
    }

    @Override
    public void connect () {
        sendMessage(new ConnectMessage());

        new Thread(()-> {
            try {
                DatagramPacket packet = new DatagramPacket(new byte[1024], 1024, sockaddr);
                while (true) {
                    socket.receive(packet);
                    ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(packet.getData(), 0, packet.getLength()));
                    StatusUpdate update = (StatusUpdate) stream.readObject();

                    if (update.getNumber() > lastUpdateNumber) {
                        notifyListeners(update);
                        lastUpdateNumber = update.getNumber();
                    }
                }
            } catch (IOException e) {

            } catch (ClassNotFoundException e) {

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
