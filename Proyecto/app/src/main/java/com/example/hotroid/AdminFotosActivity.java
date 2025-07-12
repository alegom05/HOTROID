package com.example.hotroid;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions; // <-- AÑADIR ESTE IMPORT

// Cloudinary imports
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AdminFotosActivity extends AppCompatActivity {

    private static final int PICK_IMAGES = 100;
    private final List<ImageView> imageViews = new ArrayList<>();
    private final List<FrameLayout> frameLayouts = new ArrayList<>();
    private final List<ImageView> removeButtons = new ArrayList<>();
    private final List<Object> currentDisplayImages = new ArrayList<>(); // Master list

    private Button btnSelectImages;
    private Button btnSave;
    private Button btnClearAll;

    private FirebaseFirestore db;

    // EL ID DEL HOTEL ES FIJO Y SE DECLARA AQUÍ.
    private String hotelDocumentId = "yqrBR3OPmiHnWB677l5X"; // Confirmed from your screenshots

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_fotos);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        // Referencias a los elementos de la UI
        btnSelectImages = findViewById(R.id.btnSeleccionarImagenes);
        btnSave = findViewById(R.id.btnGuardarUbicacion);
        btnClearAll = findViewById(R.id.btnLimpiar);

        // Inicializar listas de ImageViews, FrameLayouts y botones de eliminar
        imageViews.add(findViewById(R.id.img1));
        imageViews.add(findViewById(R.id.img2));
        imageViews.add(findViewById(R.id.img3));
        imageViews.add(findViewById(R.id.img4));

        frameLayouts.add(findViewById(R.id.frame1));
        frameLayouts.add(findViewById(R.id.frame2));
        frameLayouts.add(findViewById(R.id.frame3));
        frameLayouts.add(findViewById(R.id.frame4));

        removeButtons.add(findViewById(R.id.btnRemove1));
        removeButtons.add(findViewById(R.id.btnRemove2));
        removeButtons.add(findViewById(R.id.btnRemove3));
        removeButtons.add(findViewById(R.id.btnRemove4));

        // Configurar listeners para los botones de eliminar
        for (int i = 0; i < removeButtons.size(); i++) {
            final int index = i;
            removeButtons.get(i).setOnClickListener(v -> removeImage(index));
        }

        // Cargar imágenes existentes del hotel al iniciar la actividad
        loadHotelImages();

        // Listener para seleccionar nuevas imágenes
        btnSelectImages.setOnClickListener(v -> abrirGaleria());

        // Listener para limpiar todas las imágenes de la UI (no de Firestore hasta que se guarde)
        btnClearAll.setOnClickListener(v -> clearAllImages());

        // Listener para guardar/actualizar imágenes en Firestore (a través de Cloudinary)
        btnSave.setOnClickListener(v -> uploadImagesToCloudinary());

        // Configuración de la BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_registros) {
                // Ya estás en registros, no hacer nada o refrescar
                return true;
            } else if (itemId == R.id.nav_taxistas) {
                startActivity(new Intent(AdminFotosActivity.this, AdminTaxistas.class));
                return true;
            } else if (itemId == R.id.nav_checkout) {
                startActivity(new Intent(AdminFotosActivity.this, AdminCheckout.class));
                return true;
            } else if (itemId == R.id.nav_reportes) {
                startActivity(new Intent(AdminFotosActivity.this, AdminReportes.class));
                return true;
            }
            return false;
        });
    }

    /**
     * Carga las URLs de las imágenes existentes del hotel desde Firestore
     * y las añade a la lista maestra `currentDisplayImages`.
     */
    private void loadHotelImages() {
        db.collection("hoteles").document(hotelDocumentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    currentDisplayImages.clear(); // Limpiar lista maestra antes de cargar

                    if (documentSnapshot.exists()) {
                        List<String> urls = (List<String>) documentSnapshot.get("imageUrls");
                        if (urls != null && !urls.isEmpty()) {
                            currentDisplayImages.addAll(urls); // Añadir URLs existentes
                            Log.d("AdminFotosActivity", "Imágenes cargadas de Firestore: " + currentDisplayImages.size());
                        } else {
                            Log.d("AdminFotosActivity", "El documento no tiene el campo 'imageUrls' o está vacío/nulo.");
                        }
                    } else {
                        Log.d("AdminFotosActivity", "Documento del hotel no encontrado. Se asume que no hay imágenes previas.");
                        // No es un error crítico si el documento no existe al cargar,
                        // ya que `set(merge:true)` lo creará al guardar.
                    }
                    displayAllImages(); // Mostrar las imágenes cargadas (o limpiar si no hay)
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminFotosActivity.this, "Error al cargar fotos del hotel: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("AdminFotosActivity", "Error al cargar fotos", e);
                    currentDisplayImages.clear(); // Limpiar UI en caso de error
                    displayAllImages();
                });
    }

    /**
     * Abre la galería de imágenes para que el usuario seleccione nuevas fotos.
     * Limita el número de selecciones para no exceder el máximo de 4 imágenes totales.
     */
    private void abrirGaleria() {
        if (currentDisplayImages.size() >= 4) {
            Toast.makeText(this, "Ya tienes el máximo de 4 imágenes.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        int remainingSlots = 4 - currentDisplayImages.size();
        startActivityForResult(Intent.createChooser(intent, "Selecciona imágenes (Máx. " + remainingSlots + ")"), PICK_IMAGES);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGES && resultCode == RESULT_OK && data != null) {
            int currentCount = currentDisplayImages.size();

            if (data.getClipData() != null) { // Selección múltiple de imágenes
                ClipData clipData = data.getClipData();
                int count = clipData.getItemCount();
                for (int i = 0; i < count; i++) {
                    if (currentCount + i < 4) { // Límite de 4 imágenes en total (existentes + nuevas)
                        Uri imageUri = clipData.getItemAt(i).getUri();
                        getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        currentDisplayImages.add(imageUri); // Añadir a la lista maestra
                    } else {
                        Toast.makeText(this, "Solo puedes tener hasta 4 imágenes en total.", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            } else if (data.getData() != null) { // Selección única de imagen
                if (currentCount < 4) {
                    Uri imageUri = data.getData();
                    getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    currentDisplayImages.add(imageUri); // Añadir a la lista maestra
                } else {
                    Toast.makeText(this, "Máximo 4 imágenes permitidas.", Toast.LENGTH_SHORT).show();
                }
            }
            displayAllImages(); // Vuelve a mostrar todas las imágenes después de la selección
        }
    }

    /**
     * Muestra todas las imágenes presentes en la lista maestra `currentDisplayImages` en los ImageViews.
     * Limpia la UI antes de mostrar para asegurar una representación correcta del estado actual.
     */
    private void displayAllImages() {
        // Primero, oculta y limpia todos los slots de imagen en la UI
        for (int i = 0; i < imageViews.size(); i++) {
            frameLayouts.get(i).setVisibility(View.GONE);
            imageViews.get(i).setImageDrawable(null); // Limpiar imagen
            removeButtons.get(i).setVisibility(View.GONE);
        }

        // Luego, itera sobre la lista maestra y muestra las imágenes en los slots disponibles
        for (int i = 0; i < currentDisplayImages.size(); i++) {
            if (i < imageViews.size()) { // Asegurarse de no exceder el número de ImageViews disponibles
                ImageView imageView = imageViews.get(i);
                FrameLayout frameLayout = frameLayouts.get(i);
                ImageView removeBtn = removeButtons.get(i);

                Object imageSource = currentDisplayImages.get(i);

                // Cargar la imagen usando Glide, manejando tanto Uri local como String URL
                if (imageSource instanceof Uri) {
                    // Es una imagen local recién seleccionada (Uri)
                    Glide.with(this).load((Uri) imageSource).into(imageView);
                } else if (imageSource instanceof String) {
                    // Es una URL de Cloudinary existente (String)
                    Glide.with(this).load((String) imageSource).into(imageView);
                }

                frameLayout.setVisibility(View.VISIBLE);
                removeBtn.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Elimina una imagen de la lista maestra en el índice especificado y actualiza la UI.
     * @param index El índice de la imagen a eliminar.
     */
    private void removeImage(int index) {
        if (index >= 0 && index < currentDisplayImages.size()) {
            currentDisplayImages.remove(index); // Elimina de la lista maestra
            Toast.makeText(this, "Imagen eliminada de la vista.", Toast.LENGTH_SHORT).show();
            displayAllImages(); // Vuelve a mostrar las imágenes actualizadas en la UI
        }
    }

    /**
     * Limpia todas las imágenes de la UI y de la lista maestra.
     * Esto NO elimina las imágenes de Cloudinary o Firestore hasta que se presione "Guardar".
     */
    private void clearAllImages() {
        currentDisplayImages.clear(); // Limpia la lista maestra
        displayAllImages(); // Actualiza la UI para mostrar vacío
        Toast.makeText(this, "Todas las imágenes han sido limpiadas de la vista.", Toast.LENGTH_SHORT).show();
    }

    /**
     * Sube las nuevas imágenes (Uri) a Cloudinary y luego actualiza Firestore
     * con la lista completa de URLs de Cloudinary (incluyendo las nuevas y las preexistentes).
     */
    private void uploadImagesToCloudinary() {
        if (hotelDocumentId == null || hotelDocumentId.isEmpty()) {
            Toast.makeText(this, "ID del hotel no encontrado. No se pueden subir las fotos.", Toast.LENGTH_LONG).show();
            return;
        }

        if (currentDisplayImages.isEmpty()) {
            // Si la lista maestra está vacía, significa que el usuario quiere borrar todas las imágenes.
            updateFirestoreImageUrls(new ArrayList<>()); // Actualiza Firestore con una lista vacía
            Toast.makeText(this, "No hay imágenes para subir o guardar. Se eliminarán todas las existentes.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Procesando imágenes...", Toast.LENGTH_LONG).show();
        Log.d("AdminFotosActivity", "Total de imágenes a procesar: " + currentDisplayImages.size());

        List<Uri> imagesToUpload = new ArrayList<>();
        final List<String> finalCloudinaryUrls = new ArrayList<>(); // Acumulará todas las URLs finales (existentes + nuevas)

        // Separar imágenes nuevas (Uri) de las existentes (String URL)
        for (Object item : currentDisplayImages) {
            if (item instanceof Uri) {
                imagesToUpload.add((Uri) item); // Son imágenes locales que necesitan subirse
            } else if (item instanceof String) {
                finalCloudinaryUrls.add((String) item); // Ya son URLs de Cloudinary, solo las agregamos
            }
        }

        // Si no hay imágenes nuevas para subir (solo se reorganizaron o eliminaron existentes)
        if (imagesToUpload.isEmpty()) {
            updateFirestoreImageUrls(finalCloudinaryUrls); // Simplemente actualiza Firestore con la lista actual
            return;
        }

        final int[] uploadCount = {0}; // Contador para rastrear cuántas subidas se han completado
        final int totalNewUploads = imagesToUpload.size(); // Total de imágenes locales a subir

        // Inicia el proceso de subida para cada Uri local
        for (Uri imageUri : imagesToUpload) {
            String requestId = UUID.randomUUID().toString(); // Genera un ID único para cada solicitud de subida

            MediaManager.get().upload(imageUri)
                    .option("folder", "hotel_images") // Carpeta donde se guardarán en Cloudinary
                    .option("public_id", "hotel_" + UUID.randomUUID().toString()) // Public ID único para la imagen
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            Log.d("CloudinaryUpload", "Subida iniciada para: " + imageUri.getLastPathSegment());
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            double progress = (double) bytes / totalBytes;
                            Log.d("CloudinaryUpload", "Progreso: " + (int)(progress * 100) + "% para " + imageUri.getLastPathSegment());
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String url = (String) resultData.get("secure_url"); // Obtener la URL segura (HTTPS)
                            if (url != null) {
                                synchronized (finalCloudinaryUrls) { // Proteger la lista si es accedida por múltiples hilos
                                    finalCloudinaryUrls.add(url);
                                }
                                Log.d("CloudinaryUpload", "Subida exitosa: " + url);
                            } else {
                                Log.e("CloudinaryUpload", "URL nula en respuesta de Cloudinary para " + imageUri.getLastPathSegment());
                            }
                            uploadCount[0]++; // Incrementa el contador de subidas completadas
                            checkAllUploadsComplete(totalNewUploads, uploadCount[0], finalCloudinaryUrls);
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            Log.e("CloudinaryUpload", "Error en subida para " + imageUri.getLastPathSegment() + ": " + error.getDescription());
                            Toast.makeText(AdminFotosActivity.this, "Error al subir imagen: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                            uploadCount[0]++; // Incrementa el contador incluso en caso de error
                            checkAllUploadsComplete(totalNewUploads, uploadCount[0], finalCloudinaryUrls);
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            Log.w("CloudinaryUpload", "Subida re-programada para " + imageUri.getLastPathSegment() + ": " + error.getDescription());
                        }
                    }).dispatch();
        }
    }

    /**
     * Verifica si todas las subidas de Cloudinary han terminado.
     * Si es así, actualiza el documento del hotel en Firestore con la lista de URLs finales.
     * @param totalNewUploads El número total de imágenes locales que se intentaron subir.
     * @param completedUploads El número de subidas (exitosas o fallidas) que ya finalizaron.
     * @param finalCloudinaryUrls La lista acumulada de URLs de Cloudinary (tanto las nuevas como las preexistentes).
     */
    private void checkAllUploadsComplete(int totalNewUploads, int completedUploads, List<String> finalCloudinaryUrls) {
        if (completedUploads == totalNewUploads) {
            Log.d("AdminFotosActivity", "Todas las subidas de Cloudinary (nuevas) han finalizado. Iniciando actualización de Firestore.");
            updateFirestoreImageUrls(finalCloudinaryUrls);
        }
    }

    /**
     * Actualiza el campo 'imageUrls' del documento del hotel en Firestore con la lista proporcionada.
     * Se utiliza `set(SetOptions.merge())` para crear el documento si no existe o fusionar si ya existe.
     * @param imageUrls La lista final de URLs de imágenes de Cloudinary para el hotel.
     */
    private void updateFirestoreImageUrls(List<String> imageUrls) {
        DocumentReference hotelRef = db.collection("hoteles").document(hotelDocumentId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("imageUrls", imageUrls); // Sobreescribe la lista completa de URLs en Firestore

        // *** CAMBIO CLAVE AQUÍ: usar set() con SetOptions.merge() ***
        hotelRef.set(updates, SetOptions.merge()) // Usa set() para crear o actualizar el documento
                .addOnSuccessListener(aVoid -> {
                    // Si Firestore se actualiza con éxito, también actualiza la lista maestra local
                    // y vuelve a dibujar la UI para reflejar el estado guardado.
                    currentDisplayImages.clear();
                    currentDisplayImages.addAll(imageUrls);
                    displayAllImages(); // Vuelve a dibujar la UI con las URLs guardadas (todas son String ahora)

                    Toast.makeText(AdminFotosActivity.this, "Imágenes actualizadas en Firestore!", Toast.LENGTH_SHORT).show();
                    Log.d("AdminFotosActivity", "Firestore actualizado con éxito. Total URLs guardadas: " + imageUrls.size());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminFotosActivity.this, "Error al actualizar imágenes en Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("AdminFotosActivity", "Error al actualizar Firestore", e);
                });
    }
}