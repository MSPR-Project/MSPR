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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    FirebaseStorage storage;
    StorageReference storageReference;
    //FirebaseVisionImage image;

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


        //runTextRecognition();
        runFaceDetection();

    }

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

            Uri photoUri = getImageUri(getApplicationContext(), photo);
            StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());

            ref.putFile(photoUri);

            //image = FirebaseVisionImage.fromBitmap(photo);
           /* Uri uri = storageReference.child("images/fd3c195e-fe3c-451f-b050-755844fc6807.jpeg").getDownloadUrl();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                image = FirebaseVisionImage.fromBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }*/


        }
    }



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
            if(blocks.get(i).getText().endsWith("No")){
                try{
                    int valeur = i;
                    valeur++;
                    //String a = blocks.get(valeur).getText();
                    numeroCI = blocks.get(valeur).getText();
                    //System.out.println(numeroCI);

                }
                catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            else if(blocks.get(i).getText().contains("IDENTITÉ")){
                try {
                    String[] splits = (blocks.get(i).getText()).split(" ");
                    int c = (splits.length) - 1;
                    numeroCI = splits[c];
                    //System.out.println(numeroCI);

                }
                catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            else if(blocks.get(i).getText().contains("Prénom")){
                try {
                    String[] splits = (blocks.get(i).getText()).split(" ");
                    int c = (splits.length) - 1;
                    prenomCI = splits[c];
                    //System.out.println(prenomCI);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

            }
            else if(blocks.get(i).getText().contains("Nom") || blocks.get(i).getText().contains("Nom:")){
                try {
                    String[] splits = (blocks.get(i).getText()).split(" ");
                    int c = (splits.length) - 1;
                    nomCI = splits[c];
                    //System.out.println(nomCI);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            //lignes par lignes
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                //System.out.println(j);
                //System.out.println(lines.get(j).getText());
                if(lines.get(j).getText().contains("Prénom")){
                    try {
                        String[] splits = (lines.get(j).getText()).split(" ");
                        int c = (splits.length) - 1;
                        prenomCI = splits[c];
                        //System.out.println(prenomCI);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }

                }
                else if(lines.get(j).getText().contains("Nom") || lines.get(j).getText().contains("Nom:")){
                    try {
                        String[] splits = (lines.get(j).getText()).split(" ");
                        int c = (splits.length) - 1;
                        nomCI = splits[c];
                        //System.out.println(nomCI);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }

                // mots par mots
                for (int k = 0; k < elements.size(); k++) {
                    System.out.println(k);
                    System.out.println(elements.get(k).getText());
                    if(elements.get(k).getText().contains("Prénom")){
                        try {
                            int valeur = k;
                            valeur++;
                            prenomCI = elements.get(valeur).getText();
                            //System.out.println(prenomCI);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                    else if(elements.get(k).getText().contains("Nom")){
                        try {
                            int valeur = k;
                            valeur++;
                            nomCI = elements.get(valeur).getText();
                            //System.out.println(nomCI);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                    else if(elements.get(k).getText().endsWith("N")){
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
    }


    public void runFaceDetection() {
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

        String fakeci1 = "https://firebasestorage.googleapis.com/v0/b/mspr-gosecuri.appspot.com/o/images%2Ffake_ci.png?alt=media&token=796668b2-f334-4e39-a0f6-f5ee3e1140cb";
        String fakeci2 = "https://firebasestorage.googleapis.com/v0/b/mspr-gosecuri.appspot.com/o/images%2Ffake_ci2.jpg?alt=media&token=11241a73-5286-4261-9a1c-b197aff63cb0";
        String fakeci3 = "https://firebasestorage.googleapis.com/v0/b/mspr-gosecuri.appspot.com/o/images%2Ffake_ci3.jpg?alt=media&token=af39fd24-afb3-4d67-a354-20d52b80b69a";

        Bitmap bitmap = getBitmapFromURL(fakeci2);

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options);
        Task<List<FirebaseVisionFace>> result =
                detector.detectInImage(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<FirebaseVisionFace>>() {
                                    @Override
                                    public void onSuccess(List<FirebaseVisionFace> faces) {
                                        // Task completed successfully
                                        System.out.println("passe reco visage");
                                        processTextRecognitionResult(faces);
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

    private void processTextRecognitionResult(List<FirebaseVisionFace> faces) {
        for (FirebaseVisionFace face : faces) {
            Rect bounds = face.getBoundingBox();
            float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
            float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees

            // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
            // nose available):
            FirebaseVisionFaceLandmark leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR);
            if (leftEar != null) {
                FirebaseVisionPoint leftEarPos = leftEar.getPosition();
            }

            // If contour detection was enabled:
            List<FirebaseVisionPoint> leftEyeContour =
                    face.getContour(FirebaseVisionFaceContour.LEFT_EYE).getPoints();
            List<FirebaseVisionPoint> upperLipBottomContour =
                    face.getContour(FirebaseVisionFaceContour.UPPER_LIP_BOTTOM).getPoints();

            // If classification was enabled:
            if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                float smileProb = face.getSmilingProbability();
            }
            if (face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                float rightEyeOpenProb = face.getRightEyeOpenProbability();
            }

            // If face tracking was enabled:
            if (face.getTrackingId() != FirebaseVisionFace.INVALID_ID) {
                int id = face.getTrackingId();
            }
        }

    }
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
