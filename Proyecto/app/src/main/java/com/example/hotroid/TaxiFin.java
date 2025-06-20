package com.example.hotroid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.TaxiAlertasBeans;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaxiFin extends AppCompatActivity {

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private ExecutorService cameraExecutor;
    private BarcodeScanner barcodeScanner;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private boolean qrScanned = false; // Bandera para evitar múltiples lanzamientos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_fin);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        ImageButton btnAtras = findViewById(R.id.btnAtras);
        previewView = findViewById(R.id.cameraPreview);

        cameraExecutor = Executors.newSingleThreadExecutor();

        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();
        barcodeScanner = BarcodeScanning.getClient(options);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.wifi) {
                Intent intentInicio = new Intent(TaxiFin.this, TaxiActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (item.getItemId() == R.id.location) {
                Intent intentUbicacion = new Intent(TaxiFin.this, TaxiLocation.class);
                startActivity(intentUbicacion);
                return true;
            } else if (item.getItemId() == R.id.notify) {
                Intent intentAlertas = new Intent(TaxiFin.this, TaxiDashboardActivity.class);
                startActivity(intentAlertas);
                return true;
            }
            return false;
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerViajes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<TaxiAlertasBeans> lista = new ArrayList<>();
        long now = System.currentTimeMillis();
        lista.add(new TaxiAlertasBeans("Roberto", "Nuñez Prado", "Hotel Costa del Sol", "Aeropuerto Internacional Jorge Chávez (LIM)", new Date(now - (150 * 60 * 1000)), "Llegó a destino"));

        TaxiFinAdapter adapter = new TaxiFinAdapter(lista);
        recyclerView.setAdapter(adapter);

        btnAtras.setOnClickListener(v -> {
            Intent intent = new Intent(TaxiFin.this, TaxiActivity.class);
            startActivity(intent);
        });

        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST_CODE);
    }

    private void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Error iniciando la cámara: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                // Usando la constante que funciona para ti
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
            // Solo procesa si aún no se ha escaneado un QR
            if (!qrScanned) {
                processImageProxy(imageProxy);
            } else {
                imageProxy.close(); // Cierra el proxy si ya escaneamos para liberar recursos
            }
        });

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
    }

    private void processImageProxy(ImageProxy imageProxy) {
        @SuppressLint("UnsafeOptInUsageError")
        InputImage inputImage = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());

        barcodeScanner.process(inputImage)
                .addOnSuccessListener(barcodes -> {
                    // Verifica si se detectaron códigos QR y si no hemos lanzado la actividad aún
                    if (!barcodes.isEmpty() && !qrScanned) {
                        qrScanned = true; // Marca que ya se escaneó
                        // Lanza la nueva actividad de confirmación
                        Intent intent = new Intent(TaxiFin.this, ConfirmationActivity.class);
                        startActivity(intent);
                        finish(); // Opcional: Finaliza TaxiFin para que no se pueda regresar aquí con el botón "Atrás"
                    }
                })
                .addOnFailureListener(e -> {
                    // Maneja errores de escaneo si es necesario
                    // Log.e("QRScan", "Error al escanear código de barras", e);
                })
                .addOnCompleteListener(task -> {
                    imageProxy.close(); // Importante: Cierra el ImageProxy para liberar el buffer
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Se requiere permiso de cámara para escanear QR",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}
