package com.client;

import com.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class BotClient extends Client{
    public static void main(String[] args) {
        new BotClient().run();
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();

    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        return String.format("date_bot_%d", (int) (Math.random()*100));
    }

    public class BotSocketThread extends SocketThread{

        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            Calendar c = new GregorianCalendar();

            String[] dates = message.split(": ");
            if(dates.length>1){
                switch (dates[1]){
                    case "дата":
                        sendTextMessage("Информация для "+dates[0]+": "+new SimpleDateFormat("d.MM.YYYY").format(c.getTime()));
                        break;
                    case "день":
                        sendTextMessage("Информация для "+dates[0]+": "+new SimpleDateFormat("d").format(c.getTime()));
                        break;
                    case "месяц":
                        sendTextMessage("Информация для "+dates[0]+": "+new SimpleDateFormat("MMMM").format(c.getTime()));
                        break;
                    case "год":
                        sendTextMessage("Информация для "+dates[0]+": "+new SimpleDateFormat("YYYY").format(c.getTime()));
                        break;
                    case "время":
                        sendTextMessage("Информация для "+dates[0]+": "+new SimpleDateFormat("H:mm:ss").format(c.getTime()));
                        break;
                    case "час":
                        sendTextMessage("Информация для "+dates[0]+": "+new SimpleDateFormat("H").format(c.getTime()));
                        break;
                    case "минуты":
                        sendTextMessage("Информация для "+dates[0]+": "+new SimpleDateFormat("m").format(c.getTime()));
                        break;
                    case "секунды":
                        sendTextMessage("Информация для "+dates[0]+": "+new SimpleDateFormat("s").format(c.getTime()));
                        break;
                }
            }
        }
    }
}
