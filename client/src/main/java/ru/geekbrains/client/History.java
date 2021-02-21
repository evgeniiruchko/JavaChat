package ru.geekbrains.client;

import java.io.*;
import java.util.LinkedList;

public class History {
    private static final int MAX_MESSAGES = 100;

    public static LinkedList<String> readHistory(String fileName){
        LinkedList<String> history = new LinkedList<>();
        String line;
        try {
            File file = new File(fileName);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while ((line = reader.readLine()) != null){
                history.add(line);
                if (history.size() > MAX_MESSAGES){
                    history.remove(0);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return history;
    }

    public static void writeHistory(String fileName, String message){
        try {
            FileWriter writer = new FileWriter(fileName, true);
            BufferedWriter bufferWriter = new BufferedWriter(writer);
            bufferWriter.write(message + System.lineSeparator());
            bufferWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
