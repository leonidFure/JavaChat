package com.client;

import com.Connection;
import com.ConsoleHelper;
import com.Message;
import com.MessegeType;

import java.io.IOException;
import java.net.Socket;

public class Client {
    protected Connection connection;

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
    private volatile boolean clientConnected = false;

    protected String getServerAddress(){ return ConsoleHelper.readString(); }

    protected int getServerPort(){ return ConsoleHelper.readInt(); }

    protected String getUserName(){ return ConsoleHelper.readString(); }

    protected boolean shouldSendTextFromConsole(){ return true; }

    protected SocketThread getSocketThread(){ return new SocketThread(); }

    protected void sendTextMessage(String text){
        try {
            connection.send(new Message(MessegeType.TEXT,text));
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Ошибка отправки текстового сообщения(");
            clientConnected = false;
        }
    }

    public synchronized void run(){
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        try {
            synchronized (this){
                this.wait();
            }
        } catch (InterruptedException e) {
            ConsoleHelper.writeMessage(e.getMessage());
            return;
        }
        if(clientConnected) {
            ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");
            while (clientConnected){
                String mes = ConsoleHelper.readString();
                if(mes.equals("exit")||!clientConnected)break;
                if(shouldSendTextFromConsole()) sendTextMessage(mes);
            }
        }
        else ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");

    }

    public class SocketThread extends Thread{
        protected void processIncomingMessage(String message){ ConsoleHelper.writeMessage(message);}

        protected void informAboutAddingNewUser(String userName){
            ConsoleHelper.writeMessage(String.format("%s подключился к чату", userName));
        }

        protected void informAboutDeletingNewUser(String userName){
            ConsoleHelper.writeMessage(String.format("%s покинул чат", userName));
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected){
            synchronized (Client.this){
                Client.this.clientConnected = clientConnected;
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException{
            while (true){
                Message message =connection.receive();
                if(message.getType()==MessegeType.NAME_REQUEST) {
                    connection.send(new Message(MessegeType.USER_NAME,getUserName()));
                }else if(message.getType()==MessegeType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    break;
                }else throw new IOException("Unexpected MessageType");
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException{
            while (true){
                Message message =connection.receive();
                if(message.getType()==MessegeType.TEXT){
                    processIncomingMessage(message.getData());
                }else if(message.getType()==MessegeType.USER_ADDED){
                    informAboutAddingNewUser(message.getData());
                }else if(message.getType()==MessegeType.USER_REMOVED){
                    informAboutDeletingNewUser(message.getData());
                }else throw new IOException("Unexpected MessageType");
            }
        }

        @Override
        public void run() {
            try {
                Client.this.connection = new Connection(new Socket(getServerAddress(),getServerPort()));
                clientHandshake();
                clientMainLoop();
            } catch (IOException e) {
                notifyConnectionStatusChanged(false);
            } catch (ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }
    }
}
