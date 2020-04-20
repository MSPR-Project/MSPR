package elPackage;

import autovalue.shaded.com.google$.common.base.$Objects;
import autovalue.shaded.com.squareup.javapoet$.$TypeVariableName;
import com.google.firebase.database.*;
import elPackage.Firebase.Common;
import elPackage.Firebase.Item;
import org.opencv.core.*;
import org.opencv.face.LBPHFaceRecognizer;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;


import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.transform.Source;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import static javax.imageio.ImageIO.read;
import static elPackage.Firebase.Common.initFireBase;

public class Camera extends JFrame {

    static JFrame frame = new JFrame("Go Securi");
    private DatabaseReference mDatabase;
    private static String idUser = "0";
    private final int counter = 0;
    private final boolean canCheck = false;
    static JPanel panelForm = new JPanel();
    static JPanel panelPicture = new JPanel();
    static private int aCounter = 0;
    final static boolean shouldFill = true;
    final static boolean shouldWeightX = true;
    final static boolean RIGHT_TO_LEFT = false;
    private static String theName;


    Mat face = new Mat();

    private final CascadeClassifier faceCascade;
    CascadeClassifier eyesCascade;

    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
    JButton startStream = new JButton("Start");
    static JCheckBox cb1 = new JCheckBox("Mousqueton");
    static JCheckBox cb2 = new JCheckBox("Gants d'intervention");
    static JCheckBox cb3 = new JCheckBox("Ceinture de sécurite tactique");
    static JCheckBox cb4 = new JCheckBox("Detecteur de metaux");
    static JCheckBox cb5 = new JCheckBox("Brassard de securite");
    static JCheckBox cb6 = new JCheckBox("Lampe torche");
    static JCheckBox cb7 = new JCheckBox("Bandeau Agent cynophile");
    static JCheckBox cb8 = new JCheckBox("Gilet pare-balle");
    static JCheckBox cb9 = new JCheckBox("Chemise manches courtes");
    static JCheckBox cb10 = new JCheckBox("Blouson");
    static JCheckBox cb11 = new JCheckBox("Coupe-vent");
    static JCheckBox cb12 = new JCheckBox("Talkie-Walkie");
    static JCheckBox cb13 = new JCheckBox("Kit oreillette");
    static JCheckBox cb14 = new JCheckBox("Taser");
    static ImageIcon img;
    static JLabel lblImg = new JLabel();
    static JButton comeBackButton = new JButton("Back");
    static JLabel title = new JLabel();



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
                                    System.out.println("Webcam turn off");
                                    this.wait();
                                }
                        } catch (Exception ex) {
                            //System.out.println("Error");
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

        if(aCounter == 0){
            initFireBase();
            aCounter++;
        }
        checkDatabaseImages();
        this.faceCascade = new CascadeClassifier();
        String fileFace = "/Users/fabiensisca/Documents/Cours/Mspr/src/main/resources/haarcascades/haarcascade_frontalface_alt.xml";
        faceCascade.load(fileFace);

        eyesCascade = new CascadeClassifier();
        String fileEyes = "/Users/fabiensisca/Documents/Cours/Mspr/src/main/resources/haarcascades/haarcascade_eye_tree_eyeglasses.xml";
        eyesCascade.load(fileEyes);

        webSource =new VideoCapture(0);
        webSource.open(0);
        myThread = new DaemonThread();
        Thread t1 = new Thread(myThread);
        t1.setDaemon(true);
        myThread.runnable = true;
        t1.start();
        buttonCapture.setEnabled(true);  // capture button

        buttonCapture.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);

            if(faceDetection(pol) == true){

                myThread.runnable = false;

                if(Recognize() == true){
                    buttonCapture.setEnabled(false);
                    CameraPanel.setVisible(false);
                    formMain();
                }
                else{
                    infoBox("Visage détecté mais non identifié, veuillez recommencer", "Echec reconnaissance");

                    CameraPanel.setVisible(false);
                    try {
                        frame.setContentPane(new Camera().MainPanelAuth);
                    } catch (IOException ioException) {

                    }
                    CameraPanel.setVisible(true);
                    myThread.runnable = true;
                    buttonCapture.setEnabled(true);
                }

            }
            else{
                infoBox("Aucun visage détecté, essayez à nouveau.", "Erreur detection");

                myThread.runnable = true;
                buttonCapture.setEnabled(true);
            }
        }
        });
    }


    public static void main(String[] args) throws IOException {
        setupFrame();
    }

    public static void infoBox(String infoMessage, String titleBar){
        JOptionPane.showMessageDialog(null, infoMessage, " InfoBox : "+ titleBar, JOptionPane.INFORMATION_MESSAGE);
    }


    public void buildSecondForm(){
        panelForm.setVisible(true);
        frame.setContentPane(panelForm);
        panelForm.setBackground(Color.WHITE);

        panelForm.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        if(shouldFill){
            c.fill = GridBagConstraints.HORIZONTAL;
        }

        if (shouldWeightX) {
            c.weightx = 0.5;
        }

        c.fill = GridBagConstraints.HORIZONTAL;
        //c.ipady = 40;      //make this component tall
        c.weightx = 0.5;
        c.gridwidth = 4;
        c.gridx = 0;
        c.gridy = 1;
        comeBackButton.setBackground(Color.decode("#379EC1"));
        comeBackButton.setForeground(Color.decode("#379EC1"));
        panelForm.add(comeBackButton, c);

        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 3;
        panelForm.add(cb1, c);

        c.gridx = 1;
        c.gridy = 3;
        panelForm.add(cb2, c);

        c.gridx = 2;
        c.gridy = 3;
        panelForm.add(cb3, c);

        c.gridx = 0;
        c.gridy = 4;
        panelForm.add(cb4, c);

        c.gridx = 1;
        c.gridy = 4;
        panelForm.add(cb5, c);

        c.gridx = 2;
        c.gridy = 4;
        panelForm.add(cb6, c);

        c.gridx = 0;
        c.gridy = 5;
        panelForm.add(cb7, c);

        c.gridx = 1;
        c.gridy = 5;
        panelForm.add(cb8, c);


        c.gridx = 2;
        c.gridy = 5;
        panelForm.add(cb9, c);

        c.gridx = 0;
        c.gridy = 6;
        panelForm.add(cb10, c);

        c.gridx = 1;
        c.gridy = 6;
        panelForm.add(cb11, c);

        c.gridx = 2;
        c.gridy = 6;
        panelForm.add(cb12, c);

        c.gridx = 0;
        c.gridy = 7;
        panelForm.add(cb13, c);

        c.gridx = 2;
        c.gridy = 7;
        panelForm.add(cb14, c);

        c.gridx = 2;
        c.gridy = 2;
        //c.gridwidth = 2;
        //c.gridheight = 6;
        c.ipadx = 50;
        c.ipady = 50;
        c.anchor = GridBagConstraints.PAGE_END;
        panelPicture.setBackground(Color.WHITE);

        lblImg.setIcon(new ImageIcon("/Users/fabiensisca/Documents/Cours/Mspr/images/pictureUnknow/faceCaptured.png"));
        //lblImg.setText("toto");
        lblImg.setSize(50,50);
        panelPicture.add(lblImg);
        validate();
        panelForm.add(panelPicture, c);

        c.anchor = GridBagConstraints.PAGE_START;
        c.gridy = 0;
        c.gridx = 0;
        title.setText("Gestion de l'equipement de l'utilisateur : " + theName);
        title.setForeground(Color.decode("#379EC1"));
        panelForm.add(title);

    }


    public void formMain(){
        buildSecondForm();

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

        comeBackButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                panelForm.setVisible(false);
                try {
                    frame.setContentPane(new Camera().MainPanelAuth);
                } catch (IOException ioException) {

                }
                idUser = "0";
                CameraPanel.setVisible(true);
                myThread.runnable = true;
                buttonCapture.setEnabled(true);
            }
        });

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
                if(cb2.isSelected()){
                    System.out.println("Testtest tes");
                    selectAnItem("gants intervention");
                }
                else{
                    System.out.println("Testtest tes 2");
                    unselectAnItem("gants intervention");
                }
            }
        });

        cb3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(cb3.isSelected()){
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
                if(cb4.isSelected()){
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
                if(cb5.isSelected()){
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
                if(cb6.isSelected()){
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
                if(cb7.isSelected()){
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
                if(cb8.isSelected()){
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
                if(cb9.isSelected()){
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
                if(cb10.isSelected()){
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
                if(cb11.isSelected()){
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
                if(cb12.isSelected()){
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
                if(cb13.isSelected()){
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
                if(cb14.isSelected()){
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

        frame.setSize(1000,750);
        frame.setContentPane(new Camera().MainPanelAuth);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.pack();
        frame.setVisible(true);
    }

    public boolean faceDetection(Mat frame){
        MatOfRect faces = new MatOfRect();
        faceCascade.detectMultiScale(frame, faces);
        if(faces.toArray().length > 0) {
            Rect rectCrop = new Rect(faces.toArray()[0].tl(), faces.toArray()[0].br());
            face = new Mat(frame, rectCrop);
            File file = new File("/Users/fabiensisca/Documents/Cours/Mspr/images/pictureUnknow/faceCaptured.png");
            Imgcodecs.imwrite(file.getPath(), face);
            return true;
        }
        return false;
    }

    public static boolean Recognize(){
        try {
            File root = new File("/Users/fabiensisca/Documents/Cours/Mspr/images/pictureKnow");
            FilenameFilter imgFilter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    name = name.toLowerCase();
                    return name.endsWith(".png");
                }
            };
            File[] imageFiles = root.listFiles(imgFilter);
            List<Mat> images = new ArrayList<Mat>();
            Mat labels = new Mat(imageFiles.length, 1, CvType.CV_32SC1);
            int counter = 0;

            for (File image : imageFiles) {
                Mat img = Imgcodecs.imread(image.getAbsolutePath());
                Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);
                Imgproc.equalizeHist(img, img);
                int label = Integer.parseInt(image.getName().split("\\-")[0]);
                String labnname = image.getName().split("\\_")[0];
                theName = labnname.split("\\-")[1];
                String lastName = image.getName().split("\\_")[1];
                images.add(img);
                labels.put(counter, 0, label);
                counter++;
            }
            LBPHFaceRecognizer model = LBPHFaceRecognizer.create();
            model.train(images, labels);
            model.save("MyTrainnedData");

            Mat fileUnKnow = new Mat();
            fileUnKnow = Imgcodecs.imread("/Users/fabiensisca/Documents/Cours/Mspr/images/pictureUnknow/faceCaptured.png");
            Imgproc.cvtColor(fileUnKnow, fileUnKnow, Imgproc.COLOR_BGR2GRAY);
            Imgproc.equalizeHist(fileUnKnow, fileUnKnow);

            model.read("MyTrainnedData");
            int predict = model.predict_label(fileUnKnow);
            int[] predLabel = new int[1];
            double[] trueScale = new double[1];

            model.predict(fileUnKnow,predLabel,trueScale);
            System.out.println("Degres de reconnaissance : " + trueScale[0]);
            if(trueScale[0] < 33) {
                idUser = Integer.toString(predict);
                //System.out.println(predict);
                return true;
            }
            else
                return false;
        }catch (Exception e){
            return false;
        }
    }

    public void checkDatabaseImages(){



    }
}
