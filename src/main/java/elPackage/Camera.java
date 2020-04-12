package elPackage;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import elPackage.Firebase.Common;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static javax.imageio.ImageIO.read;
import static elPackage.Firebase.Common.initFireBase;

public class Camera extends JFrame {

    static JFrame frame = new JFrame("GoSecuri");

    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
    JButton startStream = new JButton("Start");
    JCheckBox cb1 = new JCheckBox("Mousqueton");
    JCheckBox cb2 = new JCheckBox("Gants d'intervention");
    JCheckBox cb3 = new JCheckBox("Ceinture de sécurite tactique");
    JCheckBox cb4 = new JCheckBox("Detecteur de metaux");
    JCheckBox cb5 = new JCheckBox("Brassard de securite");
    JCheckBox cb6 = new JCheckBox("Lampe torche");
    JCheckBox cb7 = new JCheckBox("Bandeau Agent cynophile");
    JCheckBox cb8 = new JCheckBox("Gilet pare-balle");
    JCheckBox cb9 = new JCheckBox("Chemise manches courtes");
    JCheckBox cb10 = new JCheckBox("Blouson");
    JCheckBox cb11 = new JCheckBox("Coupe-vent");
    JCheckBox cb12 = new JCheckBox("Talkie-Walkie");
    JCheckBox cb13 = new JCheckBox("Kit oreillette");
    JCheckBox cb14 = new JCheckBox("Taser");


    class DaemonThread implements Runnable{
        protected volatile boolean runnable = false;

        @Override
        public  void run()
        {
            synchronized(this)
            {
                while(runnable)
                {
                    if(webSource.grab())
                    {
                        try
                        {
                            webSource.retrieve(pol);
                            if ( pol.empty() ) continue;
                            Imgcodecs.imencode(".bmp", pol, mem);
                            Image im = ImageIO.read(new ByteArrayInputStream(mem.toArray()));

                            BufferedImage buff = (BufferedImage) im;
                            Graphics g = CameraPanel.getGraphics();
                            int w = getWidth();
                            if (g.drawImage(buff, 0, 0, CameraPanel.getWidth(), CameraPanel.getHeight() , 0, 0, buff.getWidth(), buff.getHeight(), null))

                                if(runnable == false)
                                {
                                    webSource.release();
                                    System.out.println("Runnable = false so we stop the webcam");
                                    this.wait();
                                }
                        }
                        catch(Exception ex)
                        {
                            System.out.println("Error");
                        }
                    }
                }
            }
        }
    }
    public JPanel MainPanelAuth;
    private JPanel CameraPanel;
    private JButton buttonCapture;

    private DaemonThread myThread = null;
    int count = 0;
    VideoCapture webSource = null;
    Mat pol = new Mat();
    MatOfByte mem = new MatOfByte();


    public Camera() throws IOException {

        initFireBase();

        webSource =new VideoCapture(0);
        webSource.open(0);
        myThread = new DaemonThread();
        Thread t = new Thread(myThread);
        t.setDaemon(true);
        myThread.runnable = true;
        t.start();
        buttonCapture.setEnabled(true);  // capture button

        buttonCapture.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);

            myThread.runnable = false;
            buttonCapture.setEnabled(false);
            CameraPanel.setVisible(false);
            CameraPanel.setVisible(false);
            webSource.release();
            formMain();

            }
        });
    }


    public static void main(String[] args) throws IOException {
        setupFrame();
    }

    public void formMain(){
        JPanel panelForm = new JPanel();
        panelForm.setVisible(true);
        frame.setContentPane(panelForm);
        panelForm.setBackground(Color.LIGHT_GRAY);
        panelForm.add(startStream);
        panelForm.add(cb1);
        panelForm.add(cb2);
        panelForm.add(cb3);
        panelForm.add(cb4);
        panelForm.add(cb5);
        panelForm.add(cb6);
        panelForm.add(cb7);
        panelForm.add(cb8);
        panelForm.add(cb9);
        panelForm.add(cb10);
        panelForm.add(cb11);
        panelForm.add(cb12);
        panelForm.add(cb13);
        panelForm.add(cb14);
        firstPush();
    }

    public void firstPush(){
        //table ==>
        //Column ==> myRef
        //Row ==> Value


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");
        myRef.setValue("Hello world test", new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

            }
        });
    }

    public static void setupFrame() throws IOException {

        frame.setSize(1000,700);
        frame.setContentPane(new Camera().MainPanelAuth);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.pack();
        frame.setVisible(true);
    }
}
