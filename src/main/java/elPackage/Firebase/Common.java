package elPackage.Firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Common extends JFrame {

    public static JFrame te = new JFrame();

    public static void initFireBase() {
        FileInputStream refreshToken = null;
        try{
            refreshToken = new FileInputStream("/Users/fabiensisca/Documents/Cours/Mspr/src/main/java/elPackage/Firebase/mspr-gosecuri-firebase-adminsdk-77ld0-4c1bf60a2b.json");

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(refreshToken))
                    .setDatabaseUrl("https://mspr-gosecuri.firebaseio.com/")
                    .build();
            FirebaseApp.initializeApp(options);
            System.out.println("ok Firebase");
        }
        catch (FileNotFoundException e){
            Logger.getLogger(Common.class.getName()).log(Level.SEVERE, null, e);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try{
                refreshToken.close();
            }

            catch (IOException e){
                Logger.getLogger(Common.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    public Common(){

    }
}
