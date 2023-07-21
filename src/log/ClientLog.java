package log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import controller.connection.ClientChannel;

public final class ClientLog {

    private File f;
    private FileWriter fw;
    private LocalDateTime today = LocalDateTime.now();
    private String mUser;
    private boolean isNewFile = true;

    public ClientLog(ClientChannel cc) {
        mUser = cc.getNick();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd_MM_yyyy");
        File logsDir = new File("./logs");
        File userDir = new File("./logs/" + mUser);

        f = new File("./logs/" + mUser + "/" + dtf.format(today) + ".txt");
        isNewFile = f.exists();

        try {
            logsDir.mkdir();
            userDir.mkdir();
            f.createNewFile();
            f.setWritable(true);
            fw = new FileWriter(f, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logIn() {
        try {
            if (isNewFile) {
                fw.write(mUser + " LOGGED IN at : " + today.getHour() + ":" + today.getMinute() + ":"
                        + today.getSecond() + "\n");
            } else {
                fw.append("\n" + mUser + " LOGGED IN at : " + today.getHour() + ":" + today.getMinute() + ":"
                        + today.getSecond() + "\n");
            }
            fw.flush();
        } catch (IOException e) {
            System.out.println("Could not init the user history file");
        }
    }

    public void logOut() {
        try {
            if (isNewFile) {
                fw.write(mUser + " LOGGED OUT at : " + today.getHour() + ":" + today.getMinute() + ":"
                        + today.getSecond() + "\n");
            } else {
                fw.append("\n" + mUser + " LOGGED OUT at : " + today.getHour() + ":" + today.getMinute() + ":"
                        + today.getSecond() + "\n");
            }
            fw.flush();
        } catch (IOException e) {
            System.out.println("Could not init the user history file");
        }
    }

    public void log(String msg) {
        System.out.println("OUT==>" + msg.toString());
        try {
            fw.append(msg + " [" + today.getHour() + ":" + today.getMinute() + ":" + today.getSecond() + "]\n");
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
