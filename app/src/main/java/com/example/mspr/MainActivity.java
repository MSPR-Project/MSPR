package com.example.mspr;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions;

import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    public MainActivity(){

    }

    FirebaseStorage storage;
    StorageReference storageReference;
    //FirebaseVisionImage image;
    private DatabaseReference mDatabase;

    List<FirebaseVisionPoint> pointsFace1 = new ArrayList<>();
    List<FirebaseVisionPoint> pointsFace2 = new ArrayList<>();

    String fakeci1 = "https://firebasestorage.googleapis.com/v0/b/mspr-gosecuri.appspot.com/o/images%2Ffake_ci.png?alt=media&token=796668b2-f334-4e39-a0f6-f5ee3e1140cb";
    String fakeci2 = "https://firebasestorage.googleapis.com/v0/b/mspr-gosecuri.appspot.com/o/images%2Ffake_ci2.jpg?alt=media&token=11241a73-5286-4261-9a1c-b197aff63cb0";
    String fakeci3 = "https://firebasestorage.googleapis.com/v0/b/mspr-gosecuri.appspot.com/o/images%2Ffake_ci3.jpg?alt=media&token=af39fd24-afb3-4d67-a354-20d52b80b69a";
    String imageci = "https://firebasestorage.googleapis.com/v0/b/mspr-gosecuri.appspot.com/o/images%2Fimageci.jpg?alt=media&token=af315935-b40a-49e3-914f-6ee22da17585";

    Bitmap photoCI = null;
    Bitmap photoVisage = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //accessing the firebase storage
        storage = FirebaseStorage.getInstance();

        //creates a storage reference
        storageReference = storage.getReference();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("IdCard");

        //runTextRecognition();
        //runFaceDetection(fakeci1);
        //runFaceDetection(imageci);
    }

    //photo carte identité
    static int REQUEST_IMAGE_CAPTURE = 0;

    public void dispatchTakePictureIntent(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        REQUEST_IMAGE_CAPTURE = 1;
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

    }
    //photo visage
    static int REQUEST_FACE_CAPTURE = 2;

    public void dispatchTakeFacePictureIntent() {
        Intent takeFacePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        REQUEST_FACE_CAPTURE = 3;
        if (takeFacePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeFacePictureIntent, REQUEST_FACE_CAPTURE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            REQUEST_IMAGE_CAPTURE = 0;
            photoCI = (Bitmap) data.getExtras().get("data");


            runTextRecognition(photoCI);
        }
        else if (requestCode == 3 && resultCode == RESULT_OK) {
            REQUEST_FACE_CAPTURE = 2;
            photoVisage = (Bitmap) data.getExtras().get("data");


            //stockage photo
            Uri photoUri = getImageUri(getApplicationContext(), photoVisage);
            StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());
            ref.putFile(photoUri);

            //Toast.makeText(MainActivity.this, "Photo du visage push sur Firebase", Toast.LENGTH_SHORT).show();
            runFaceDetection(photoCI);
            runFaceDetection(photoVisage);

        }
    }


    // détection texte
    private void runTextRecognition(Bitmap photo) {
        Toast.makeText(MainActivity.this, "Reconnaissance texte début ...", Toast.LENGTH_SHORT).show();

        //Bitmap bitmap = getBitmapFromURL(fakeci3);

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(photo);
        FirebaseVisionTextRecognizer recognizer = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();

        recognizer.processImage(image)
                .addOnSuccessListener(
                        new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText texts) {
                                processTextRecognitionResult(texts);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                e.printStackTrace();
                            }
                        });
    }

    private void processTextRecognitionResult(FirebaseVisionText texts) {
        String numeroCI = null;
        String nomCI = null;
        String prenomCI = null;

        List<FirebaseVisionText.TextBlock> blocks = texts.getTextBlocks();
        if (blocks.size() == 0) {
            System.out.println("No text found");
            return;
        }
        //bloc de texte
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            //System.out.println(i);
            //System.out.println(blocks.get(i).getText());
            /*
            if (blocks.get(i).getText().endsWith("No")) {
                try {
                    int valeur = i;
                    valeur++;
                    //String a = blocks.get(valeur).getText();
                    numeroCI = blocks.get(valeur).getText();
                    //System.out.println(numeroCI);

                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            } else if (blocks.get(i).getText().contains("IDENTITÉ")) {
                try {
                    String[] splits = (blocks.get(i).getText()).split(" ");
                    int c = (splits.length) - 1;
                    numeroCI = splits[c];
                    //System.out.println(numeroCI);

                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            } else if (blocks.get(i).getText().contains("Prénom")) {
                try {
                    String[] splits = (blocks.get(i).getText()).split(" ");
                    int c = (splits.length) - 1;
                    prenomCI = splits[c];
                    //System.out.println(prenomCI);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

            } else if (blocks.get(i).getText().contains("Nom") || blocks.get(i).getText().contains("Nom:")) {
                try {
                    String[] splits = (blocks.get(i).getText()).split(" ");
                    int c = (splits.length) - 1;
                    nomCI = splits[c];
                    //System.out.println(nomCI);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }*/
            //lignes par lignes

            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                //System.out.println(j);
                //System.out.println(lines.get(j).getText());
                /*
                if (lines.get(j).getText().contains("Prénom")) {
                    try {
                        String[] splits = (lines.get(j).getText()).split(" ");
                        int c = (splits.length) - 1;
                        prenomCI = splits[c];
                        //System.out.println(prenomCI);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }

                } else if (lines.get(j).getText().contains("Nom") || lines.get(j).getText().contains("Nom:")) {
                    try {
                        String[] splits = (lines.get(j).getText()).split(" ");
                        int c = (splits.length) - 1;
                        nomCI = splits[c];
                        //System.out.println(nomCI);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
                */
                // mots par mots
                for (int k = 0; k < elements.size(); k++) {
                    //System.out.println("j : " + j);
                    //System.out.println(elements.size());
                    System.out.println(k);
                    System.out.println(elements.get(k).getText());

                    if (elements.get(k).getText().contains("Prénom") || elements.get(k).getText().contains("Prenom") || elements.get(k).getText().contains("Prbnom") || elements.get(k).getText().contains("Prenem") || elements.get(k).getText().contains("Prnem")) {
                        try {
                            int valeur = k;
                            if(valeur < (elements.size()) -1) {
                                valeur++;
                                prenomCI = elements.get(valeur).getText();
                            }
                            else if (lines.get(j).getText().contains("Prénom") || lines.get(j).getText().contains("Prenom") || lines.get(j).getText().contains("Prbnom") || lines.get(j).getText().contains("Prenem") || lines.get(j).getText().contains("Prnem")) {
                                String[] splits = (lines.get(j).getText()).split(":");
                                int c = (splits.length) - 1;
                                prenomCI = splits[c];
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                    } else if (elements.get(k).getText().contains("Nom") || elements.get(k).getText().contains("Non")) {
                        try {
                            int valeur = k;
                            if(valeur < (elements.size()) -1) {
                                valeur++;
                                nomCI = elements.get(valeur).getText();
                            } else if (lines.get(j).getText().contains("Nom") || lines.get(j).getText().contains("Non")) {
                                String[] splits = (lines.get(j).getText()).split(":");
                                int c = (splits.length) - 1;
                                nomCI = splits[c];
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                    } else if ((lines.get(j).getText().contains("CART") || lines.get(j).getText().contains("NATIONAL") || lines.get(j).getText().contains("IDENTIT")) && (elements.get(k).getText().endsWith("N") || elements.get(k).getText().endsWith("N:") || elements.get(k).getText().endsWith("Ne") || elements.get(k).getText().endsWith("Ne:") || elements.get(k).getText().endsWith("N°") || elements.get(k).getText().endsWith("N°:") || elements.get(k).getText().endsWith(":"))) {
                        try {
                            int valeur = k;
                            if(valeur < (elements.size()) -1) {
                                valeur++;
                                numeroCI = elements.get(valeur).getText();
                            }
                            else if(valeur == (elements.size()) -1){
                                if (blocks.get(i).getText().contains("IDENTIT")) {
                                    String[] splits = (blocks.get(i).getText()).split(" ");
                                    int c = (splits.length) - 1;
                                    numeroCI = splits[c];
                                    //System.out.println(numeroCI);
                                }
                            }
                            else if (lines.get(j).getText().endsWith("N") || lines.get(j).getText().endsWith("No") || lines.get(j).getText().endsWith("N°") || lines.get(j).getText().endsWith(":")) {
                                String[] splits = (lines.get(j).getText()).split(":");
                                int c = (splits.length) - 1;
                                numeroCI = splits[c];
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                }
            }
        }

        System.out.println("Numéro CI trouvé : " + numeroCI);
        System.out.println("Nom CI trouvé : " + nomCI);
        System.out.println("Prénom CI trouvé : " + prenomCI);
        // comparer les 3 champs avec les données en base
        // si infos trouvé alors ok
        // sinon : push les 3 champs + image
        // + photo et comparaison visage
        //dispatchTakeFacePictureIntent();

        if(numeroCI != null && nomCI != null && prenomCI != null) {
            Toast.makeText(MainActivity.this, "Reconnaissance du texte passé avec succès", Toast.LENGTH_SHORT).show();
            readData(numeroCI, prenomCI, nomCI);
        }
        else{
            Toast.makeText(MainActivity.this, "Echec de la reconnaissance du texte", Toast.LENGTH_SHORT).show();
        }



    }


    public void readData(final String nocard, final String prenom, final String nom){

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    IdCard card = ds.getValue(IdCard.class);
                    if (card.getNoIdCard().equals(nocard)) {
                        Toast.makeText(MainActivity.this, "Utilisateur connu, accès autorisé !", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(600);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Toast.makeText(MainActivity.this, "Utilisateur non connu, enregistrement des données", Toast.LENGTH_SHORT).show();
                writeNewIdCard(prenom, nom, nocard);

                //stockage photo
                Uri photoUri = getImageUri(getApplicationContext(), photoCI);
                StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());
                ref.putFile(photoUri);
                //Toast.makeText(MainActivity.this, "Photo de la carte push sur Firebase", Toast.LENGTH_SHORT).show();

                Toast.makeText(MainActivity.this, "Lancement prise photo du visage", Toast.LENGTH_SHORT).show();

                dispatchTakeFacePictureIntent();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Fail");
            }
        });
    }
    private void writeNewIdCard(String firstname, String lastname, String nocard) {
        IdCard idcard = new IdCard(firstname, lastname, nocard);

        mDatabase.child(nocard).setValue(idcard);
        System.out.println("push infos base ok");
    }



    public void runFaceDetection(Bitmap visagePhoto) {
        Toast.makeText(MainActivity.this, "Début comparaison de visage ...", Toast.LENGTH_SHORT).show();

        // High-accuracy landmark detection and face classification
        FirebaseVisionFaceDetectorOptions highAccuracyOpts =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .build();

        // contour detection
        FirebaseVisionFaceDetectorOptions options = new FirebaseVisionFaceDetectorOptions.Builder()
                .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                .build();

        //Bitmap bitmap = getBitmapFromURL(fakeci);

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(visagePhoto);
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(highAccuracyOpts);
        Task<List<FirebaseVisionFace>> result =
                detector.detectInImage(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<FirebaseVisionFace>>() {
                                    @Override
                                    public void onSuccess(List<FirebaseVisionFace> faces) {
                                        // Task completed successfully
                                        //System.out.println("passe reco visage");
                                        processFaceDetectionResult(faces);
                                        //return pointFace;
                                        //matchVisagePoints(pointFace);


                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        e.printStackTrace();
                                    }
                                });
        //System.out.println(result);
    }

    private void processFaceDetectionResult(List<FirebaseVisionFace> faces) {
        List<FirebaseVisionPoint> listPointVisage = new ArrayList<>();
        for (FirebaseVisionFace face : faces) {
            System.out.println("-- Image --");
            Rect bounds = face.getBoundingBox();
            System.out.println(bounds);
            float rotY = face.getHeadEulerAngleY();
            System.out.println(rotY);// Head is rotated to the right rotY degrees
            float rotZ = face.getHeadEulerAngleZ();
            System.out.println(rotZ);// Head is tilted sideways rotZ degrees

            // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
            // nose available):
            System.out.println("- Mouth -");
            FirebaseVisionFaceLandmark mouthBottom = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_BOTTOM);
            if (mouthBottom != null) {
                FirebaseVisionPoint mouthBottomPos = mouthBottom.getPosition();
                System.out.println(mouthBottomPos);
                listPointVisage.add(mouthBottomPos);
            }
            FirebaseVisionFaceLandmark mouthLeft = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_LEFT);
            if (mouthLeft != null) {
                FirebaseVisionPoint mouthLeftPos = mouthLeft.getPosition();
                System.out.println(mouthLeftPos);
                listPointVisage.add(mouthLeftPos);
            }
            FirebaseVisionFaceLandmark mouthRight = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_RIGHT);
            if (mouthRight != null) {
                FirebaseVisionPoint mouthRightPos = mouthRight.getPosition();
                System.out.println(mouthRightPos);
                listPointVisage.add(mouthRightPos);
            }
            System.out.println("- Ears -");
            FirebaseVisionFaceLandmark leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR);
            if (leftEar != null) {
                FirebaseVisionPoint leftEarPos = leftEar.getPosition();
                System.out.println(leftEarPos);
                listPointVisage.add(leftEarPos);
            }
            FirebaseVisionFaceLandmark rightEar = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EAR);
            if (rightEar != null) {
                FirebaseVisionPoint rightEarPos = rightEar.getPosition();
                System.out.println(rightEarPos);
                listPointVisage.add(rightEarPos);
            }
            System.out.println("- Eyes -");
            FirebaseVisionFaceLandmark leftEye = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE);
            if (leftEye != null) {
                FirebaseVisionPoint leftEyePos = leftEye.getPosition();
                System.out.println(leftEyePos);
                listPointVisage.add(leftEyePos);
            }
            FirebaseVisionFaceLandmark rightEye = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE);
            if (rightEye != null) {
                FirebaseVisionPoint rightEyePos = rightEye.getPosition();
                System.out.println(rightEyePos);
                listPointVisage.add(rightEyePos);
            }
            System.out.println("- Cheeks -");
            FirebaseVisionFaceLandmark cheekLeft = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_CHEEK);
            if (cheekLeft != null) {
                FirebaseVisionPoint cheekLeftPos = cheekLeft.getPosition();
                System.out.println(cheekLeftPos);
                listPointVisage.add(cheekLeftPos);
            }
            FirebaseVisionFaceLandmark cheekRight = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_CHEEK);
            if (cheekRight != null) {
                FirebaseVisionPoint cheekRightPos = cheekRight.getPosition();
                System.out.println(cheekRightPos);
                listPointVisage.add(cheekRightPos);
            }
            System.out.println("- Nose -");
            FirebaseVisionFaceLandmark noseBase = face.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE);
            if (noseBase != null) {
                FirebaseVisionPoint noseBasePos = noseBase.getPosition();
                System.out.println(noseBasePos);
                listPointVisage.add(noseBasePos);
            }

            // If contour detection was enabled:
            List<FirebaseVisionPoint> leftEyeContour =
                    face.getContour(FirebaseVisionFaceContour.LEFT_EYE).getPoints();
            //System.out.println(leftEyeContour);
            List<FirebaseVisionPoint> upperLipBottomContour =
                    face.getContour(FirebaseVisionFaceContour.UPPER_LIP_BOTTOM).getPoints();
            //System.out.println(upperLipBottomContour);
            // If classification was enabled:
            if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                float smileProb = face.getSmilingProbability();
            }
            if (face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                float rightEyeOpenProb = face.getRightEyeOpenProbability();
                //System.out.println(rightEyeOpenProb);
            }

            // If face tracking was enabled:
            if (face.getTrackingId() != FirebaseVisionFace.INVALID_ID) {
                int id = face.getTrackingId();
            }
        }
        if (pointsFace1.isEmpty()) {
            pointsFace1 = listPointVisage;
        } else if (!pointsFace1.isEmpty() && pointsFace2.isEmpty()) {
            pointsFace2 = listPointVisage;
            if (!pointsFace2.isEmpty()) {
                boolean reco = matchVisagePoints();
                if(reco){
                    Toast.makeText(MainActivity.this, "Correspondance visage trouvé, accès autorisé !", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(MainActivity.this, "Echec correspondance visage, accès décliné !", Toast.LENGTH_LONG).show();

                }


            }
        }


    }


    public boolean matchVisagePoints() {
        int i = 0;
        if (pointsFace1.equals(pointsFace2)){
            return true;
        }
        else {
            float nosebase1X = pointsFace1.get(9).getX();
            float nosebase1Y = pointsFace1.get(9).getY();

            float nosebase2X = pointsFace2.get(9).getX();
            float nosebase2Y = pointsFace2.get(9).getY();

            for (FirebaseVisionPoint face1 : pointsFace1) {

                float gapnose1X = nosebase1X - face1.getX();
                float gapnose1Y = nosebase1Y - face1.getY();

                for (FirebaseVisionPoint face2 : pointsFace2) {
                    float gapnose2X = nosebase2X - face2.getX();
                    float gapnose2Y = nosebase2Y - face2.getY();

                    float gapnoseXTotal = gapnose1X - gapnose2X;
                    float gapnoseYTotal = gapnose1Y - gapnose2Y;
                    if(gapnoseXTotal <= 9 && gapnoseXTotal >= -9 && gapnoseYTotal <= 9 && gapnoseYTotal >= -9){
                        i++;
                    }
                }

            }
            System.out.println(i);
            if(i>=10){
                System.out.println("Visage reconnu");
                return true;
            }
            else{
                System.out.println("Echec, visage non reconnu");
                return false;
            }


        }

    }

    // fonctions utilitaire
    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }


    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }










}
