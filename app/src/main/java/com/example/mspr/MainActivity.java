package com.example.mspr;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

public class MainActivity extends AppCompatActivity {


    FirebaseStorage storage;
    StorageReference storageReference;
    //FirebaseVisionImage image;

    List<FirebaseVisionPoint> pointsFace1 = new ArrayList<>();
    List<FirebaseVisionPoint> pointsFace2 = new ArrayList<>();

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

        String fakeci1 = "https://firebasestorage.googleapis.com/v0/b/mspr-gosecuri.appspot.com/o/images%2Ffake_ci.png?alt=media&token=796668b2-f334-4e39-a0f6-f5ee3e1140cb";
        String fakeci2 = "https://firebasestorage.googleapis.com/v0/b/mspr-gosecuri.appspot.com/o/images%2Ffake_ci2.jpg?alt=media&token=11241a73-5286-4261-9a1c-b197aff63cb0";
        String fakeci3 = "https://firebasestorage.googleapis.com/v0/b/mspr-gosecuri.appspot.com/o/images%2Ffake_ci3.jpg?alt=media&token=af39fd24-afb3-4d67-a354-20d52b80b69a";
        String imageci = "https://firebasestorage.googleapis.com/v0/b/mspr-gosecuri.appspot.com/o/images%2Fimageci.jpg?alt=media&token=af315935-b40a-49e3-914f-6ee22da17585";


        //runTextRecognition();
        runFaceDetection(fakeci1);
        runFaceDetection(imageci);


    }

    //photo carte identité
    static final int REQUEST_IMAGE_CAPTURE = 1;

    public void dispatchTakePictureIntent(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Bitmap photo = (Bitmap) data.getExtras().get("data");

            //runTextRecognition(photo);


            //stockage photo
            Uri photoUri = getImageUri(getApplicationContext(), photo);
            StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());
            ref.putFile(photoUri);
        }
    }

    // détection texte
    private void runTextRecognition() {
        String fakeci1 = "https://firebasestorage.googleapis.com/v0/b/mspr-gosecuri.appspot.com/o/images%2Ffake_ci.png?alt=media&token=796668b2-f334-4e39-a0f6-f5ee3e1140cb";
        String fakeci2 = "https://firebasestorage.googleapis.com/v0/b/mspr-gosecuri.appspot.com/o/images%2Ffake_ci2.jpg?alt=media&token=11241a73-5286-4261-9a1c-b197aff63cb0";
        String fakeci3 = "https://firebasestorage.googleapis.com/v0/b/mspr-gosecuri.appspot.com/o/images%2Ffake_ci3.jpg?alt=media&token=af39fd24-afb3-4d67-a354-20d52b80b69a";

        Bitmap bitmap = getBitmapFromURL(fakeci1);

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
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
            }
            //lignes par lignes
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                //System.out.println(j);
                //System.out.println(lines.get(j).getText());
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

                // mots par mots
                for (int k = 0; k < elements.size(); k++) {
                    System.out.println(k);
                    System.out.println(elements.get(k).getText());
                    if (elements.get(k).getText().contains("Prénom")) {
                        try {
                            int valeur = k;
                            valeur++;
                            prenomCI = elements.get(valeur).getText();
                            //System.out.println(prenomCI);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                    } else if (elements.get(k).getText().contains("Nom")) {
                        try {
                            int valeur = k;
                            valeur++;
                            nomCI = elements.get(valeur).getText();
                            //System.out.println(nomCI);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                    } else if (elements.get(k).getText().endsWith("N")) {
                       /* try {
                            int valeur = k;
                            if(valeur<=elements.size()) {
                                valeur++;
                                if (isInteger(elements.get(valeur).getText())) {
                                    //numeroCI = elements.get(valeur).getText();
                                    //System.out.println(numeroCI);
                                }
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }*/
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


    }

    //photo visage
    static final int REQUEST_FACE_CAPTURE = 1;

    public void dispatchTakeFacePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_FACE_CAPTURE);
        }

    }
/*
    @Override
    protected void onFaceActivityResult(int requestFaceCode, int resultFaceCode, Intent data) {
        super.onActivityResult(requestFaceCode, resultFaceCode, data);
        if (requestFaceCode == REQUEST_FACE_CAPTURE && resultFaceCode == RESULT_OK) {

            Bitmap photo = (Bitmap) data.getExtras().get("data");

            //runFaceDetection(photo);
            //runFaceDetection(photo_base);


            //stockage photo
            Uri photoUri = getImageUri(getApplicationContext(), photo);
            StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());
            ref.putFile(photoUri);


        }
    }*/


    public void runFaceDetection(String fakeci) {
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

        Bitmap bitmap = getBitmapFromURL(fakeci);

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(highAccuracyOpts);
        Task<List<FirebaseVisionFace>> result =
                detector.detectInImage(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<FirebaseVisionFace>>() {
                                    @Override
                                    public void onSuccess(List<FirebaseVisionFace> faces) {
                                        // Task completed successfully
                                        System.out.println("passe reco visage");
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
        System.out.println(result);
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


            }
        }


    }


    public boolean matchVisagePoints() {
        int i = 0;
        if (pointsFace1.equals(pointsFace2)){
            return true;
        }
        else {
            /*
            float mouthbotomX = pointsFace1.get(0).getX();
            float mouthbotomY = pointsFace1.get(0).getY();
            float earleftX = pointsFace1.get(3).getX();
            float earleftY = pointsFace1.get(3).getY();
            float mbelXCompare = mouthbotomX - earleftX;
            float mbelYCompare = mouthbotomY - earleftY;
            if(mbelXCompare <= 40 && mbelXCompare >= -40 && mbelYCompare <= 40 && mbelYCompare >= -40){
                j++;
            }*/

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
            if(i>=9){
                System.out.println("Reconnaissance ok");
                return true;
            }
            else{
                System.out.println("Reconnaissance ko");
                return false;
            }



                //face1.getX();
                //face1.getY();
                /*
                for (FirebaseVisionPoint face2 : pointsFace2) {
                    //face2.getX();
                    //face2.getY();
                    j++;
                    float f1x = face1.getX();
                    float f2x = face2.getX();
                    float f1f2xcompare = f1x - f2x;
                    float f1y = face1.getY();
                    float f2y = face2.getY();
                    float f1f2ycompare = f1y - f2y;
                    if(f1f2xcompare <= 20 && f1f2xcompare >= -20 && f1f2ycompare <= 20 && f1f2ycompare >= -20) {
                        //System.out.println("ok");
                        i++;


                    }

                }
            }*/

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


    /**
     * Get the angle by which an image must be rotated given the device's current
     * orientation.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private int getRotationCompensation(String cameraId, Activity activity, Context context)
            throws CameraAccessException {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        int deviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int rotationCompensation = ORIENTATIONS.get(deviceRotation);

        // On most devices, the sensor orientation is 90 degrees, but for some
        // devices it is 270 degrees. For devices with a sensor orientation of
        // 270, rotate the image an additional 180 ((270 + 270) % 360) degrees.
        CameraManager cameraManager = (CameraManager) context.getSystemService(CAMERA_SERVICE);
        int sensorOrientation = cameraManager
                .getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SENSOR_ORIENTATION);
        rotationCompensation = (rotationCompensation + sensorOrientation + 270) % 360;

        // Return the corresponding FirebaseVisionImageMetadata rotation value.
        int result;
        switch (rotationCompensation) {
            case 0:
                result = FirebaseVisionImageMetadata.ROTATION_0;
                break;
            case 90:
                result = FirebaseVisionImageMetadata.ROTATION_90;
                break;
            case 180:
                result = FirebaseVisionImageMetadata.ROTATION_180;
                break;
            case 270:
                result = FirebaseVisionImageMetadata.ROTATION_270;
                break;
            default:
                result = FirebaseVisionImageMetadata.ROTATION_0;
                System.out.println("||| Bad rotation value: " + rotationCompensation);
        }
        return result;
    }



    // Or, to provide language hints to assist with language detection:
// See https://cloud.google.com/vision/docs/languages for supported languages
    FirebaseVisionCloudTextRecognizerOptions options = new FirebaseVisionCloudTextRecognizerOptions.Builder()
            .setLanguageHints(Arrays.asList("fr"))
            .build();






    /*
    public FirebaseVisionImage testBitmap(Uri uri) throws IOException {
        FirebaseVisionImage image = null;
        //this.grantUriPermission("com.example.mspr", uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        image = FirebaseVisionImage.fromFilePath(getApplicationContext(), uri);
        return image;

    }
*/

/*
    public void test() {
        storageReference.child("images/fd3c195e-fe3c-451f-b050-755844fc6807").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //System.out.println(uri);
                Bitmap bitmap = getBitmapFromURL("https://firebasestorage.googleapis.com/v0/b/mspr-gosecuri.appspot.com/o/images%2Ffd3c195e-fe3c-451f-b050-755844fc6807?alt=media&token=d60ae870-bc1b-491f-84c0-d95523001dc1");

                FirebaseVisionImage image = null;
                Bitmap bitmap = null;
                try {
                    bitmap = Media.getBitmap(MainActivity.this.getContentResolver(), uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    image = testBitmap(uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    image = FirebaseVisionImage.fromFilePath(MainActivity.this, uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    image = testBitmap(uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
                FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                        .getOnDeviceTextRecognizer();
                detector.processImage(image)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText texts) {
                                System.out.println(" ||| ___ ||| test reco succès");
                                processTextRecognitionResult(texts);
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        mTextButton.setEnabled(true);
                                        e.printStackTrace();
                                    }
                                });
                                // Task completed successfully
                                //String resultText = result.getText();
                        for (FirebaseVisionText.TextBlock block: result.getTextBlocks()) {
                            String blockText = block.getText();
                            Float blockConfidence = block.getConfidence();
                            List<RecognizedLanguage> blockLanguages = block.getRecognizedLanguages();
                            Point[] blockCornerPoints = block.getCornerPoints();
                            Rect blockFrame = block.getBoundingBox();
                            for (FirebaseVisionText.Line line: block.getLines()) {
                                String lineText = line.getText();
                                Float lineConfidence = line.getConfidence();
                                List<RecognizedLanguage> lineLanguages = line.getRecognizedLanguages();
                                Point[] lineCornerPoints = line.getCornerPoints();
                                Rect lineFrame = line.getBoundingBox();
                                for (FirebaseVisionText.Element element: line.getElements()) {
                                    String elementText = element.getText();
                                    Float elementConfidence = element.getConfidence();
                                    List<RecognizedLanguage> elementLanguages = element.getRecognizedLanguages();
                                    Point[] elementCornerPoints = element.getCornerPoints();
                                    Rect elementFrame = element.getBoundingBox();
                                }
                            }
                        }

                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                        System.out.println(" ||| ___ ||| test reco échec");

                                    }
                                });

            }


        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });


    }
    */

    /*
public void test2(){
    Task<URL> url = storageReference.child("images/fd3c195e-fe3c-451f-b050-755844fc6807").getDownloadUrl();
    FirebaseVisionImage image = null;
    try {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), url);
        image = FirebaseVisionImage.fromBitmap(bitmap);

    } catch (IOException e) {
        e.printStackTrace();
    }
}*/










    /*
    String currentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    static final int REQUEST_TAKE_PHOTO = 1;


    public void dispatchTakePictureIntent(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }


    }*/







}
