package application; /**
 * Created by cb on 17/03/16.
 */


import java.io.File;

import javax.swing.*;

public class Main {


    public static void main(String[] args){
        //Designate source of information
        File file;
        if (args.length > 0){
            file  = new File(args[0]);
        } else {
            file = fileChooser();
        }

        //Parse information from JSON into render information

        //Start render
        new Graphics().run();

    }

    private static File fileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.showOpenDialog(null);
        File file = fileChooser.getSelectedFile();
        System.out.println("File: " + file);

        if (file == null){
            System.out.println("No file selected.");
            System.exit(0);
        }
        return file;
    }
}
