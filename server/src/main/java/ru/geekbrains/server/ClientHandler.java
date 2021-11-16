package ru.geekbrains.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {
    private static Logger logger = LogManager.getLogger();
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickname;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
//            ExecutorService executorService = Executors.newCachedThreadPool();
            new Thread(() -> {
//            executorService.execute(() -> {
                try {
                    while (true) {
                        String str = in.readUTF();
                        // /auth login1 password1
                        if (str.startsWith("/auth")) {
                            String[] subStrings = str.split(" ", 3);
                            if (subStrings.length == 3) {
                                logger.info("Пользователь пытается авторизоваться с логином " + subStrings[1]);
                                String nickFromDB = SQLHandler.getNickByLoginAndPassword(subStrings[1], subStrings[2]);
                                if (nickFromDB != null) {
                                    if (!server.isNickInChat(nickFromDB)) {
                                        nickname = nickFromDB;
                                        sendMsg("/authok " + nickname + " " + subStrings[1]);
                                        server.subscribe(ClientHandler.this);
                                        logger.info("Упешная авторизация ползователя " + subStrings[1]);
                                        break;
                                    } else {
                                        logger.warn("Пользователь " + subStrings[1] + " уже авторизован в чате");
                                        sendMsg("This nick already in use");
                                    }
                                } else {
                                    logger.warn("Неудачная авторизация " + subStrings[1]);
                                    sendMsg("Wrong login/password");
                                }
                            } else {
                                logger.warn("Пользователь ввёл не все данные для входа");
                                sendMsg("Wrong data format");
                            }
                        }
                        if (str.startsWith("/registration")) {
                            String[] subStr = str.split(" ");
                            // /registration login pass nick
                            if (subStr.length == 4) {
                                logger.info("Попытка регистрации. Пользователь ввёл login = " + subStr[1] + " password + subStr[3]");
                                if (SQLHandler.tryToRegister(subStr[1], subStr[2], subStr[3])) {
                                    logger.info("Успешная регистрация пользователя " + subStr[1]);
                                    sendMsg("Registration complete");
                                } else {
                                    logger.warn("Неудачная регистрация пользователя " + subStr[1] + ", с ником " + subStr[3]);
                                    sendMsg("Incorrect login/password/nickname");
                                }
                            }
                        }
                    }

                    while (true) {
                        String str = in.readUTF();
                        System.out.println("Сообщение от клиента: " + str);
                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                logger.info("Пользователь " + nickname + " выходит из чата");
                                break;
                            } else if (str.startsWith("/w")) {
                                // /w nick hello m8! hi
                                final String[] subStrings = str.split(" ", 3);
                                if (subStrings.length == 3) {
                                    final String toUserNick = subStrings[1];
                                    if (server.isNickInChat(toUserNick)) {
                                        server.unicastMsg(toUserNick, "from " + nickname + ": " + subStrings[2]);
                                        sendMsg("to " + toUserNick + ": " + subStrings[2]);
                                    } else {
                                        logger.warn("Неудачная попытка отправки личного сообщения от пользователя " + nickname + ", т.к. пользователя " + toUserNick + " нет в чате");
                                        sendMsg("User with nick '" + toUserNick + "' not found in chat room");
                                    }
                                } else {
                                    logger.warn("Недостаточно параметров для отправки личного сообщения от пользователя" + nickname);
                                    sendMsg("Wrong private message");
                                }
                            }
                        } else {
                            server.broadcastMsg(nickname + ": " + str);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        logger.debug("Попытка закрытия входящего потока");
                        in.close();
                    } catch (IOException e) {
                        logger.error("Закрытие входящего потока завершилось неудачно" , e);
                        e.printStackTrace();
                    }
                    try {
                        logger.debug("Попытка закрытия исхдящего потока");
                        out.close();
                    } catch (IOException e) {
                        logger.error("Закрытие исходящего потока завершилось неудачно" , e);
                        e.printStackTrace();
                    }
                    try {
                        logger.debug("Попытка закрытия сокета");
                        socket.close();
                    } catch (IOException e) {
                        logger.error("Закрытие сокета завершилось неудачно" , e);
                        e.printStackTrace();
                    }
                    server.unsubscribe(ClientHandler.this);
                }
            }).start();
//            });
//            executorService.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            logger.error("Неуданая оправка сообщения" , e);
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }
}
