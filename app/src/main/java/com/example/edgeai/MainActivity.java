package com.example.edgeai;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import java.io.File;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// Pytorch Imports
import org.pytorch.torchvision.TensorImageUtils;
import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.MemoryFormat;


public class MainActivity extends AppCompatActivity {

    // Variables
    Module classifier;
    ImageView imageView;
    Button capture, select, default_image;
    TextView textView;
    int imageSize = 128;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // UI Elements
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.Results);
        capture = findViewById(R.id.Capture);
        select = findViewById(R.id.SelectPhoto);
        default_image = findViewById(R.id.Test);

        Bitmap defaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image);
        imageView.setImageBitmap(defaultBitmap);

        // Classifier Model
        try {
            classifier = Module.load(assetFilePath(this, "age_gender_emotion_classifier.ptl"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Buttons
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 3);
                } else {
                    requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 100);
                }
            }
        });

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 4); // Use a unique request code (e.g., 4)
            }
        });

        default_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap defaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(defaultBitmap, imageSize, imageSize, true);
                imageView.setImageBitmap(resizedBitmap);
                classifyImage(defaultBitmap, classifier);
            }
        });

    }


    // Main Functions
    @SuppressLint("SetTextI18n")
    public void classifyImage(Bitmap image, Module module) {
        if (module == null) {
            textView.setText("Model not loaded!!!");
            return;
        }

        // Resize the image to match the input size of the model
        Bitmap resizedImage = Bitmap.createScaledBitmap(image, imageSize, imageSize, true);

        // Convert the image to a Tensor with batch dimension
        Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
                resizedImage,
                new float[]{0.485f, 0.456f, 0.406f}, // Mean
                new float[]{0.229f, 0.224f, 0.225f},  // Std
                MemoryFormat.CHANNELS_LAST
        );

        // Run the model and get the output
        IValue output = module.forward(IValue.from(inputTensor));
        IValue[] outputTuple = output.toTuple();

        // Decode the outputs
        decodeModelOutput(outputTuple);
    }

    @SuppressLint("SetTextI18n")
    private void decodeModelOutput(IValue[] outputTuple) {
        // Age
        String[] ageLabels = {"Baby (0-3)", "Child (4-12)", "Teen (13-18)",
                "Adult (19-55)", "Elderly (56+)"};
        Tensor ageTensor = outputTuple[0].toTensor(); // Extract age tensor
        float[] ageScores = ageTensor.getDataAsFloatArray();
        int ageIndex = getMaxIndex(ageScores); // Age Prediction
        String age = ageLabels[ageIndex];


        // Emotion
        String[] emotionLabels = {"Surprise", "Angry", "Happy", "Sad", "Neutral"};
        Tensor emotionTensor = outputTuple[2].toTensor();
        float[] emotionScores = emotionTensor.getDataAsFloatArray();
        int emotionIndex = getMaxIndex(emotionScores);
        String emotion = emotionLabels[emotionIndex];


        // Gender
        Tensor genderTensor = outputTuple[1].toTensor();
        float[] genderScores = genderTensor.getDataAsFloatArray();
        int genderIndex = getMaxIndex(genderScores);
        String gender = (genderIndex == 0) ? "Woman" : "Man";


        // Display the results
        textView.setText(
                "Age: " + age  + "\n" +
                        "Gender: " + gender  + "\n" +
                        "Emotion: " + emotion
        );
    }

    private int getMaxIndex(float[] array) {
        float maxScore = -Float.MAX_VALUE;
        int maxIndex = -1;
        for (int i = 0; i < array.length; i++) {
            if (array[i] > maxScore) {
                maxScore = array[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }


    public String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 3) { // For captured photo
            Bitmap image = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(image);
            Bitmap scaledImage = Bitmap.createScaledBitmap(image, imageSize, imageSize, true);
            classifyImage(scaledImage, classifier);

        } else if (requestCode == 4) { // For selected photo

            if (data != null && data.getData() != null) {
                Uri selectedImageUri = data.getData();

                try {
                    Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    imageView.setImageBitmap(image); // Display the selected image
                    Bitmap scaledImage = Bitmap.createScaledBitmap(image, imageSize, imageSize, true);
                    classifyImage(scaledImage, classifier);

                } catch (IOException e) {
                    textView.setText("Error loading image from gallery.");
                    e.printStackTrace();
                }
            }
        }
    }
}