package elPackage;

import autovalue.shaded.com.google$.common.base.$Objects;
import autovalue.shaded.com.squareup.javapoet$.$TypeVariableName;
import com.google.firebase.database.*;
import elPackage.Firebase.Common;
import elPackage.Firebase.Item;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static javax.imageio.ImageIO.read;
import static elPackage.Firebase.Common.initFireBase;

public class Camera extends JFrame {

    static JFrame frame = new JFrame("GoSecuri");
    private DatabaseReference mDatabase;
    private final String idUser = "1";
    private final int counter = 0;
    private final boolean canCheck = false;

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
            synchronized (this) {
                while (runnable) {
                    if (webSource.grab()) {
                        try {
                            webSource.retrieve(pol);
                            if (pol.empty()) continue;
                            Imgcodecs.imencode(".bmp", pol, mem);
                            Image im = ImageIO.read(new ByteArrayInputStream(mem.toArray()));

                            BufferedImage buff = (BufferedImage) im;
                            Graphics g = CameraPanel.getGraphics();
                            int w = getWidth();
                            if (g.drawImage(buff, 0, 0, CameraPanel.getWidth(), CameraPanel.getHeight(), 0, 0, buff.getWidth(), buff.getHeight(), null))

                                if (runnable == false) {
                                    webSource.release();
                                    System.out.println("Runnable = false so we stop the webcam");
                                    this.wait();
                                }
                        } catch (Exception ex) {
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
            try {webSource.release();}
            catch (Exception exception){System.out.println("Bug webcam");}
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
        cb1.setEnabled(false);
        cb2.setEnabled(false);
        cb3.setEnabled(false);
        cb4.setEnabled(false);
        cb5.setEnabled(false);
        cb6.setEnabled(false);
        cb7.setEnabled(false);
        cb8.setEnabled(false);
        cb9.setEnabled(false);
        cb10.setEnabled(false);
        cb11.setEnabled(false);
        cb12.setEnabled(false);
        cb13.setEnabled(false);
        cb14.setEnabled(false);

        readData("mousquetons");
        readData("gants intervention");
        readData("ceinture secu");
        readData("detecteur metaux");
        readData("brassard secu");
        readData("lampe");
        readData("bandeau");
        readData("gilet");
        readData("chemise");
        readData("blouson");
        readData("kway");
        readData("talkie");
        readData("oreillette");
        readData("taser");

        ifCheckChange();

    }

    public void firstPush(){
        //table ==>
        //Column ==> myRef
        //Row ==> Value
        mDatabase = FirebaseDatabase.getInstance().getReference().child("message");

        mDatabase.setValue("Hello world test", new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

            }
        });
    }

    public void writeNewItem(String newId, String newAvailable, String newIdOwner, String elItem){
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Item").child(elItem);

        Item item = new Item();
        item.setId(newId);
        item.setAvailable(newAvailable);
        item.setIdOwner(newIdOwner);

        mDatabase.child(newId).setValue(item, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                System.out.println("Good");
            }
        });

    }

    public Boolean firstRead(){
    	
        //table ==>
        //Column ==> myRef
        //Row ==> Value
        //
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Item");
        // Read from the database
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                System.out.println(value);
                if(value.equals("toto")){
                    cb1.setEnabled(true);
                    System.out.println("value");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            System.out.println("Error");
            }
        });
        System.out.println("Error");
        return true;
    }

    public void selectAnItem(String elItem){
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Item").child(elItem);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Item item = ds.getValue(Item.class);
                    if(item.available.equals("True")){
                        String id = item.getId();
                        updateItem(mDatabase, id, idUser, "False");
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabase.addListenerForSingleValueEvent(valueEventListener);
    }

    public void unselectAnItem(String elItem){

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Item").child(elItem);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Item item = ds.getValue(Item.class);
                    if(item.idOwner.equals(idUser)){
                        String id = item.getId();
                        updateItem(mDatabase, id, "Null", "True");
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabase.addListenerForSingleValueEvent(valueEventListener);
    }

    public void ifCheckChange(){

        cb1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(cb1.isSelected()){
                    selectAnItem("mousquetons");
                }
                else{
                    unselectAnItem("mousquetons");
                }
            }
        });

        cb2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(cb1.isSelected()){
                    selectAnItem("gants intervention");
                }
                else{
                    unselectAnItem("gants intervention");
                }
            }
        });

        cb3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(cb1.isSelected()){
                    selectAnItem("ceinture secu");
                }
                else{
                    unselectAnItem("ceinture secu");
                }
            }
        });

        cb4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(cb1.isSelected()){
                    selectAnItem("detecteur metaux");
                }
                else{
                    unselectAnItem("detecteur metaux");
                }
            }
        });

        cb5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(cb1.isSelected()){
                    selectAnItem("brassard secu");
                }
                else{
                    unselectAnItem("brassard secu");
                }
            }
        });

        cb6.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(cb1.isSelected()){
                    selectAnItem("lampe");
                }
                else{
                    unselectAnItem("lampe");
                }
            }
        });

        cb7.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(cb1.isSelected()){
                    selectAnItem("bandeau");
                }
                else{
                    unselectAnItem("bandeau");
                }
            }
        });

        cb8.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(cb1.isSelected()){
                    selectAnItem("gilet");
                }
                else{
                    unselectAnItem("gilet");
                }
            }
        });


        cb9.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(cb1.isSelected()){
                    selectAnItem("chemise");
                }
                else{
                    unselectAnItem("chemise");
                }
            }
        });

        cb10.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(cb1.isSelected()){
                    selectAnItem("blouson");
                }
                else{
                    unselectAnItem("blouson");
                }
            }
        });

        cb11.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(cb1.isSelected()){
                    selectAnItem("kway");
                }
                else{
                    unselectAnItem("kway");
                }
            }
        });

        cb12.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(cb1.isSelected()){
                    selectAnItem("talkie");
                }
                else{
                    unselectAnItem("talkie");
                }
            }
        });

        cb13.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(cb1.isSelected()){
                    selectAnItem("oreillette");
                }
                else{
                    unselectAnItem("oreillette");
                }
            }
        });

        cb14.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(cb1.isSelected()){
                    selectAnItem("taser");
                }
                else{
                    unselectAnItem("taser");
                }
            }
        });
    }

    public void updateItem(DatabaseReference databaseReference, String id, String newOwner, String newAvailable){

        Map<String, Object> update = new HashMap<String, Object>();

        update.put("available",newAvailable);
        update.put("idOwner", newOwner);

        mDatabase.child(id).updateChildren(update, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

            }
        });

    }

    public void readData(String elItem){
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Item");
        mDatabase.child(elItem).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    Item item = ds.getValue(Item.class);
                    if(item.idOwner.equals(idUser)){
                        if(elItem.equals("mousquetons")){
                            cb1.setSelected(true);
                            cb1.setEnabled(true);
                        }
                        else if(elItem.equals("gants intervention")){
                            cb2.setSelected(true);
                            cb2.setEnabled(true);
                        }
                        else if(elItem.equals("ceinture secu")){
                            cb3.setSelected(true);
                            cb3.setEnabled(true);
                        }
                        else if(elItem.equals("detecteur metaux")){
                            cb4.setSelected(true);
                            cb4.setEnabled(true);
                        }
                        else if(elItem.equals("brassard secu")){
                            cb5.setSelected(true);
                            cb5.setEnabled(true);
                        }
                        else if(elItem.equals("lampe")){
                            cb6.setSelected(true);
                            cb6.setEnabled(true);
                        }
                        else if(elItem.equals("bandeau")){
                            cb7.setSelected(true);
                            cb7.setEnabled(true);
                        }
                        else if(elItem.equals("gilet")){
                            cb8.setSelected(true);
                            cb8.setEnabled(true);
                        }
                        else if(elItem.equals("chemise")){
                            cb9.setSelected(true);
                            cb9.setEnabled(true);
                        }
                        else if(elItem.equals("blouson")){
                            cb10.setSelected(true);
                            cb10.setEnabled(true);
                        }
                        else if(elItem.equals("kway")){
                            cb11.setSelected(true);
                            cb11.setEnabled(true);
                        }
                        else if(elItem.equals("talkie")){
                            cb12.setSelected(true);
                            cb12.setEnabled(true);
                        }
                        else if(elItem.equals("oreillette")){
                            cb13.setSelected(true);
                            cb13.setEnabled(true);
                        }
                        else if(elItem.equals("taser")){
                            cb14.setSelected(true);
                            cb14.setEnabled(true);
                        }
                    }

                    if(item.available.equals("True")){
                        if(elItem.equals("mousquetons")){
                            cb1.setEnabled(true);
                        }
                        else if(elItem.equals("gants intervention")){
                            cb2.setEnabled(true);
                        }
                        else if(elItem.equals("ceinture secu")){
                            cb3.setEnabled(true);
                        }
                        else if(elItem.equals("detecteur metaux")){
                            cb4.setEnabled(true);
                        }
                        else if(elItem.equals("brassard secu")){
                            cb5.setEnabled(true);
                        }
                        else if(elItem.equals("lampe")){
                            cb6.setEnabled(true);
                        }
                        else if(elItem.equals("bandeau")){
                            cb7.setEnabled(true);
                        }
                        else if(elItem.equals("gilet")){
                            cb8.setEnabled(true);
                        }
                        else if(elItem.equals("chemise")){
                            cb9.setEnabled(true);
                        }
                        else if(elItem.equals("blouson")){
                            cb10.setEnabled(true);
                        }
                        else if(elItem.equals("kway")){
                            cb11.setEnabled(true);
                        }
                        else if(elItem.equals("talkie")){
                            cb12.setEnabled(true);
                        }
                        else if(elItem.equals("oreillette")){
                            cb13.setEnabled(true);
                        }
                        else if(elItem.equals("taser")){
                            cb14.setEnabled(true);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Fail");
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
