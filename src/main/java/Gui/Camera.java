package Gui;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.face.EigenFaceRecognizer;
import org.opencv.face.FaceRecognizer;
import org.opencv.face.FisherFaceRecognizer;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax.imageio.ImageIO.read;

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


    public Camera() {
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

    public static void main(String[] args) {
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

    }

    public static void setupFrame(){

        frame.setSize(1000,700);
        frame.setContentPane(new Camera().MainPanelAuth);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.pack();
        frame.setVisible(true);
    }
}
