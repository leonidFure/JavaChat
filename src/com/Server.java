package com;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            serverSocket= new ServerSocket(ConsoleHelper.readInt());
            ConsoleHelper.writeMessage("Сервер запущен.");

            while (true){
                if (serverSocket != null) {
                    new Handler(serverSocket.accept()).start();
                }
            }
        } catch (Exception e) {
            try {
                serverSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            e.printStackTrace();
        }

    }
    public static void sendBroadcastMessage(Message message){
        for(Map.Entry<String,Connection> c: connectionMap.entrySet()){
            try {
                c.getValue().send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("Ошибка отправки сообщения");
            }
        }
    }

    private static class Handler extends Thread{
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException{
            while (true){
                connection.send(new Message(MessegeType.NAME_REQUEST));
                Message message=connection.receive();
                if(message.getType()==MessegeType.USER_NAME){
                    if(!message.getData().isEmpty()&&message.getData() != null&&!connectionMap.containsKey(message.getData())){
                        connectionMap.put(message.getData(), connection);
                        connection.send(new Message(MessegeType.NAME_ACCEPTED));
                    }
                    else return serverHandshake(connection);
                }
                else return serverHandshake(connection);
                return message.getData();
            }
        }

        private void sendListOfUsers(Connection connection, String userName) throws IOException{
            try {
                for(Map.Entry<String,Connection> c: connectionMap.entrySet()){
                    if(!c.getKey().equals(userName))
                        connection.send(new Message(MessegeType.USER_ADDED,c.getKey()));
                }
            } catch (IOException e) {
                ConsoleHelper.writeMessage("Ошибка отправки сообщения");
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException{
            while (true){
                Message message;
                if((message=connection.receive()).getType()==MessegeType.TEXT){
                    sendBroadcastMessage(new Message(MessegeType.TEXT,String.format("%s: %s",userName,message.getData())));
                }
                else ConsoleHelper.writeMessage("Ошибка отправки сообщения");

            }
        }

        @Override
        public void run() {
            ConsoleHelper.writeMessage("установлено новое соединение с удаленным адресом"+socket.getRemoteSocketAddress());
            String userName = null;
            try (Connection connection = new Connection(socket)){
                userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessegeType.USER_ADDED,userName));
                sendListOfUsers(connection,userName);
                serverMainLoop(connection,userName);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("Произошла ошибка при обмене с адресом"+ socket.getRemoteSocketAddress());
            } catch (ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Произошла ошибка при обмене с адресом"+ socket.getRemoteSocketAddress());
            }
            if(userName!=null){
                connectionMap.remove(userName);
                sendBroadcastMessage(new Message(MessegeType.USER_REMOVED,userName));
            }
            ConsoleHelper.writeMessage("Соединение с удаленным адресом закрыто");
        }
    }
}
