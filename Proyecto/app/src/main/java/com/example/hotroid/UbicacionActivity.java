package com.example.hotroid;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UbicacionActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "UbicacionActivity";
    private TextView tvSeleccionLugares;
    private AutoCompleteTextView autoCompleteDireccion; // Este es el campo de texto para la dirección detallada
    private Button btnGuardarActualizar;

    // Lugares turísticos de Lima
    private String[] lugaresTuristicos = {
            "Plaza Mayor de Lima",
            "Parque de la Reserva (Circuito Mágico del Agua)",
            "Larcomar",
            "Barrio de Barranco",
            "Huaca Pucllana",
            "Museo Larco",
            "Parque Kennedy (Parque Central de Miraflores)",
            "Malecon de Miraflores",
            "Catedral de Lima",
            "Convento de San Francisco y Catacumbas"
    };
    private boolean[] seleccionados;
    private List<String> lugaresElegidos = new ArrayList<>();

    private boolean modoEdicion = true;

    // Google Maps
    private GoogleMap mMap;
    private Geocoder geocoder;
    private Marker hotelMarker;
    private Map<String, Marker> touristMarkers = new HashMap<>();

    // Google Places API
    private PlacesClient placesClient;
    private AutocompleteSessionToken sessionToken;
    private ArrayAdapter<String> autoCompleteAdapter;

    // Firebase Firestore
    private FirebaseFirestore db;
    private static final String HOTELES_COLLECTION = "hoteles";
    private static final String HOTEL_LIBERTADOR_DOCUMENT_ID = "yqrBR3OPmiHnWB677l5X";
    private DocumentReference hotelDocRef;

    // Texto fijo para la descripción del hotel
    private static final String BASE_DESCRIPCION_HOTEL = "Ubicado estratégicamente en Lima, el Hotel Libertador ofrece comodidad y cercanía a los principales atractivos de la capital, ideal para tu visita.";

    // Coordenadas fijas para el Hotel Libertador (ahora UNMSM)
    // Estas coordenadas no se sobrescribirán desde Firestore para este marcador.
    private LatLng HOTEL_LIBERTADOR_COORDS = new LatLng(-12.0577, -77.0869); // Coordenadas aproximadas de UNMSM en Lima
    private String HOTEL_LIBERTADOR_ADDRESS_DEFAULT = "Av. Venezuela s/n, Lima 15081, Perú"; // Dirección de UNMSM

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubicacion);

        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.verdejade));

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        hotelDocRef = db.collection(HOTELES_COLLECTION).document(HOTEL_LIBERTADOR_DOCUMENT_ID);

        // Inicializar Google Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.Maps_api_key), Locale.US);
        }
        placesClient = Places.createClient(this);
        sessionToken = AutocompleteSessionToken.newInstance();

        // Referencias de la UI
        autoCompleteDireccion = findViewById(R.id.autoCompleteDireccion);
        tvSeleccionLugares = findViewById(R.id.tvSeleccionLugares);
        btnGuardarActualizar = findViewById(R.id.btnGuardarUbicacion);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation); // Única declaración aquí

        seleccionados = new boolean[lugaresTuristicos.length];
        Arrays.fill(seleccionados, false);

        CardView cardAdmin = findViewById(R.id.cardAdmin);
        cardAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(UbicacionActivity.this, AdminCuentaActivity.class);
            startActivity(intent);
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "Error: map_fragment no encontrado en el layout.");
        }

        geocoder = new Geocoder(this, Locale.getDefault());

        // Cargar datos de la ubicación del hotel desde Firestore
        loadHotelLocation();

        autoCompleteAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        autoCompleteDireccion.setAdapter(autoCompleteAdapter);

        // ELIMINAMOS LA GEOLOCALIZACIÓN AUTOMÁTICA EN CADA CAMBIO DE TEXTO
        // El AutoCompleteTextView ahora se usará solo para el texto de la dirección,
        // pero NO para geocodificar la posición del hotel, ya que es fija.
        autoCompleteDireccion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && modoEdicion) {
                    findAutocompletePredictions(s.toString()); // Sigue ofreciendo sugerencias de dirección
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // En ItemClick, solo actualizamos el texto, NO geocodificamos el hotel.
        autoCompleteDireccion.setOnItemClickListener((parent, view, position, id) -> {
            String selectedPrediction = (String) parent.getItemAtPosition(position);
            autoCompleteDireccion.setText(selectedPrediction);
            // Ya no llamamos a geocodeAndAddHotelMarker aquí para el hotel
            sessionToken = AutocompleteSessionToken.newInstance(); // Inicia una nueva sesión
        });

        tvSeleccionLugares.setOnClickListener(v -> {
            if (modoEdicion) {
                mostrarDialogoSeleccion();
            }
        });

        btnGuardarActualizar.setOnClickListener(v -> {
            if (modoEdicion) {
                saveHotelLocation();
            } else {
                setEditMode(true);
            }
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent intent = null;
            if (itemId == R.id.nav_registros) {
                intent = new Intent(UbicacionActivity.this, AdminActivity.class);
            } else if (itemId == R.id.nav_taxistas) {
                intent = new Intent(UbicacionActivity.this, AdminTaxistas.class);
            } else if (itemId == R.id.nav_checkout) {
                intent = new Intent(UbicacionActivity.this, AdminCheckout.class);
            } else if (itemId == R.id.nav_reportes) {
                intent = new Intent(UbicacionActivity.this, AdminReportes.class);
            }

            if (intent != null) {
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        Log.d(TAG, "Mapa listo.");

        // Después de que el mapa esté listo, cargamos la ubicación.
        // loadHotelLocation() se encargará de añadir el marcador del hotel con las coordenadas fijas.
        loadHotelLocation();
    }

    private void findAutocompletePredictions(String query) {
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(sessionToken)
                .setTypeFilter(TypeFilter.ADDRESS)
                .setCountries(Arrays.asList("PE"))
                .setQuery(query)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
            List<String> predictions = new ArrayList<>();
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                predictions.add(prediction.getFullText(null).toString());
            }
            autoCompleteAdapter.clear();
            autoCompleteAdapter.addAll(predictions);
            autoCompleteAdapter.notifyDataSetChanged();
            autoCompleteDireccion.showDropDown();
        }).addOnFailureListener((exception) -> {
            Log.e(TAG, "Error al obtener predicciones: " + exception.getMessage());
        });
    }

    // Esta función maneja la adición de marcadores al mapa.
    // Para el "Hotel Libertador", siempre usa las coordenadas fijas (UNMSM).
    // Para los lugares turísticos, intenta geocodificar la dirección completa.
    private void geocodeAndAddMarker(String addressString, LatLng fixedCoords, String title, float markerColor) {
        if (mMap == null) return;

        LatLng finalLatLng = null;

        if (fixedCoords != null) {
            finalLatLng = fixedCoords; // Usa coordenadas fijas si se proporcionan (para Hotel Libertador)
        } else if (geocoder != null && !addressString.isEmpty()) {
            try {
                List<Address> addresses = geocoder.getFromLocationName(addressString, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    finalLatLng = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
                } else {
                    Log.w(TAG, "No se encontraron coordenadas para: " + addressString);
                    Toast.makeText(this, "No se encontró la ubicación para: " + addressString, Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error de geocodificación para " + addressString + ": " + e.getMessage());
                Toast.makeText(this, "Error de geocodificación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        if (finalLatLng != null) {
            if (title.equals("Hotel Libertador")) { // Manejar el marcador del hotel por separado
                if (hotelMarker != null) {
                    hotelMarker.remove();
                }
                hotelMarker = mMap.addMarker(new MarkerOptions()
                        .position(finalLatLng)
                        .title(title)
                        .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(finalLatLng, 15));
                Log.d(TAG, "Marcador de " + title + " añadido en: " + finalLatLng);
            } else { // Marcadores de lugares turísticos
                Marker newMarker = mMap.addMarker(new MarkerOptions()
                        .position(finalLatLng)
                        .title(title)
                        .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));
                touristMarkers.put(title, newMarker);
                Log.d(TAG, "Marcador de lugar turístico " + title + " añadido en: " + finalLatLng);
            }
        } else {
            Log.w(TAG, "No se pudo añadir marcador para: " + title);
        }
    }


    private void mostrarDialogoSeleccion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Elige lugares turísticos");

        builder.setMultiChoiceItems(lugaresTuristicos, seleccionados, (dialog, indexSelected, isChecked) -> {
            seleccionados[indexSelected] = isChecked;
            if (isChecked) {
                if (!lugaresElegidos.contains(lugaresTuristicos[indexSelected])) {
                    lugaresElegidos.add(lugaresTuristicos[indexSelected]);
                }
            } else {
                lugaresElegidos.remove(lugaresTuristicos[indexSelected]);
            }
            // Llama a updateMapMarkers con la dirección actual del hotel (fija) y los lugares elegidos
            updateMapMarkers(autoCompleteDireccion.getText().toString(), lugaresElegidos);
        });

        builder.setPositiveButton("Aceptar", (dialog, id) -> {
            if (lugaresElegidos.isEmpty()) {
                tvSeleccionLugares.setText("Selecciona lugares turísticos");
            } else {
                tvSeleccionLugares.setText(String.join(", ", lugaresElegidos));
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    /**
     * Guarda la ubicación del hotel y los lugares turísticos en Firestore.
     * Solo actualiza 'direccionDetallada' y construye la 'description'.
     * Las coordenadas del hotel son fijas y no se actualizan aquí.
     */
    private void saveHotelLocation() {
        String direccionDetalladaInput = autoCompleteDireccion.getText().toString().trim();
        if (direccionDetalladaInput.isEmpty()) {
            Toast.makeText(this, "Ingrese la dirección detallada del hotel.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Las coordenadas del hotel son FIJAS, no las obtenemos ni las actualizamos aquí.
        if (hotelMarker == null) {
            Toast.makeText(this, "Error: Marcador del hotel no inicializado. Intente recargar la aplicación.", Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("direccionDetallada", direccionDetalladaInput); // Actualiza solo el texto de la dirección detallada
        // NO actualizamos latitud ni longitud aquí, ya que son fijas para este marcador
        updates.put("lugaresTuristicosCercanos", lugaresElegidos);

        // Construir la descripción según el formato especificado
        StringBuilder descripcionBuilder = new StringBuilder(BASE_DESCRIPCION_HOTEL);
        descripcionBuilder.append(" Sus lugares turísticos cercanos son: ");
        if (lugaresElegidos.isEmpty()) {
            descripcionBuilder.append("No hay lugares turísticos seleccionados.");
        } else {
            descripcionBuilder.append(String.join(", ", lugaresElegidos)).append(".");
        }
        updates.put("description", descripcionBuilder.toString());

        hotelDocRef.set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UbicacionActivity.this, "Ubicación del hotel guardada correctamente.", Toast.LENGTH_SHORT).show();
                    setEditMode(false);
                    Log.d(TAG, "Datos de ubicación guardados en Firestore para Hotel Libertador.");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UbicacionActivity.this, "Error al guardar ubicación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al guardar datos en Firestore para Hotel Libertador: ", e);
                });
    }

    /**
     * Carga los datos del hotel desde Firestore y actualiza la UI.
     * La latitud y longitud del "Hotel Libertador" NO se leen ni se sobrescriben desde Firestore.
     * Siempre se usarán las coordenadas fijas de UNMSM.
     */
    private void loadHotelLocation() {
        hotelDocRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    String direccionDetallada = null;
                    // Ya no leemos latitud y longitud de Firestore para este marcador
                    List<String> lugaresGuardados = null;
                    String descriptionFromDb = null;

                    if (documentSnapshot.exists()) {
                        direccionDetallada = documentSnapshot.getString("direccionDetallada");
                        // IGNORAMOS latitud y longitud del documento para el marcador principal.
                        lugaresGuardados = (List<String>) documentSnapshot.get("lugaresTuristicosCercanos");
                        descriptionFromDb = documentSnapshot.getString("description");
                        Log.d(TAG, "Datos de documento: " + documentSnapshot.getData());
                    } else {
                        Log.d(TAG, "No hay datos para el Hotel Libertador en Firestore. Usando valores predeterminados.");
                    }

                    // Establecer la dirección detallada en el AutoCompleteTextView
                    if (direccionDetallada != null && !direccionDetallada.isEmpty()) {
                        autoCompleteDireccion.setText(direccionDetallada);
                    } else {
                        autoCompleteDireccion.setText(HOTEL_LIBERTADOR_ADDRESS_DEFAULT);
                    }

                    // **IMPORTANTE:** Eliminamos la lógica que sobrescribía HOTEL_LIBERTADOR_COORDS
                    // con valores de Firestore. Ahora, siempre se mantendrán las coordenadas de UNMSM.

                    // Procesar lugares turísticos
                    lugaresElegidos.clear();
                    if (lugaresGuardados != null) { // Añadir verificación de nulo
                        lugaresElegidos.addAll(lugaresGuardados);
                    }
                    if (lugaresElegidos.isEmpty()) {
                        tvSeleccionLugares.setText("Selecciona lugares turísticos");
                    } else {
                        tvSeleccionLugares.setText(String.join(", ", lugaresElegidos));
                    }

                    // Sincronizar el array `seleccionados` para el diálogo
                    for (int i = 0; i < lugaresTuristicos.length; i++) {
                        seleccionados[i] = lugaresElegidos.contains(lugaresTuristicos[i]);
                    }

                    // Actualizar el mapa solo si ya está listo
                    if (mMap != null) {
                        updateMapMarkers(autoCompleteDireccion.getText().toString(), lugaresElegidos);
                    }
                    setEditMode(false); // Por defecto, se carga en modo solo lectura
                    Log.d(TAG, "Datos de ubicación cargados de Firestore para Hotel Libertador.");

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UbicacionActivity.this, "Error al cargar ubicación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al cargar datos de Firestore para Hotel Libertador: ", e);
                    // Si falla la carga, establecer en modo edición y usar valores predeterminados (UNMSM)
                    setEditMode(true);
                    autoCompleteDireccion.setText(HOTEL_LIBERTADOR_ADDRESS_DEFAULT);
                    if (mMap != null) {
                        geocodeAndAddMarker(null, HOTEL_LIBERTADOR_COORDS, "Hotel Libertador", BitmapDescriptorFactory.HUE_GREEN);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(HOTEL_LIBERTADOR_COORDS, 15));
                    }
                });
    }


    /**
     * Parsea la lista de lugares turísticos desde la cadena de descripción.
     */
    private List<String> parseTouristSpotsFromDescription(String description) {
        List<String> parsedSpots = new ArrayList<>();
        if (description == null || description.isEmpty()) {
            return parsedSpots;
        }

        Pattern pattern = Pattern.compile("Sus lugares turísticos cercanos son: (.+?)(?:\\.|$)");
        Matcher matcher = pattern.matcher(description);
        if (matcher.find()) {
            String spotsString = matcher.group(1);
            if (spotsString != null && !spotsString.isEmpty()) {
                String[] spotsArray = spotsString.split(",\\s*");
                for (String spot : spotsArray) {
                    String trimmedSpot = spot.trim();
                    if (!trimmedSpot.isEmpty() && !trimmedSpot.equalsIgnoreCase("No hay lugares turísticos seleccionados.")) {
                        parsedSpots.add(trimmedSpot);
                    }
                }
            }
        }
        return parsedSpots;
    }

    /**
     * Actualiza los marcadores en el mapa basándose en la dirección del hotel y los lugares turísticos seleccionados.
     */
    private void updateMapMarkers(String hotelAddressText, List<String> selectedTouristSpots) {
        if (mMap == null) return;

        mMap.clear(); // Limpia todos los marcadores existentes (excepto la ubicación del usuario si está habilitada)
        touristMarkers.clear(); // Limpia el mapa de marcadores de lugares turísticos

        // Añadir marcador del hotel con las coordenadas FIJAS (UNMSM)
        geocodeAndAddMarker(null, HOTEL_LIBERTADOR_COORDS, "Hotel Libertador", BitmapDescriptorFactory.HUE_GREEN);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(HOTEL_LIBERTADOR_COORDS, 15));


        // Añadir marcadores de lugares turísticos seleccionados
        for (String spotName : selectedTouristSpots) {
            String fullAddress = getTouristSpotFullAddress(spotName);
            if (fullAddress != null) {
                // Aquí sí geocodificamos los lugares turísticos
                geocodeAndAddMarker(fullAddress, null, spotName, BitmapDescriptorFactory.HUE_RED);
            }
        }
    }

    /**
     * Provee una dirección más específica para los lugares turísticos de LIMA.
     */
    private String getTouristSpotFullAddress(String spotName) {
        switch (spotName) {
            case "Plaza Mayor de Lima":
                return "Jirón de la Unión 301, Lima 15001, Perú";
            case "Parque de la Reserva (Circuito Mágico del Agua)":
                return "Jr. Madre de Dios S/N, Cercado de Lima 15046, Perú";
            case "Larcomar":
                return "Malecón de la Reserva 610, Miraflores 15074, Perú";
            case "Barrio de Barranco":
                return "Puente de los Suspiros, Barranco 15063, Perú"; // Punto central en Barranco
            case "Huaca Pucllana":
                return "Calle General Borgoño cuadra 8, Miraflores 15074, Perú";
            case "Museo Larco":
                return "Av. Bolívar 1515, Pueblo Libre 15084, Perú";
            case "Parque Kennedy (Parque Central de Miraflores)":
                return "Diagonal, Miraflores 15074, Perú";
            case "Malecon de Miraflores":
                return "Malecón de la Reserva, Miraflores, Perú"; // Dirección general, pueden ser varias secciones
            case "Catedral de Lima":
                return "Jirón Carabaya, Plaza Mayor, Lima 15001, Perú";
            case "Convento de San Francisco y Catacumbas":
                return "Jr. Ancash 471, Cercado de Lima 15001, Perú";
            default:
                return spotName + ", Lima, Perú"; // Fallback para otros casos o si el nombre es muy genérico
        }
    }

    /**
     * Configura el modo de edición (true) o solo lectura (false) para la UI.
     */
    private void setEditMode(boolean enable) {
        modoEdicion = enable;
        autoCompleteDireccion.setEnabled(enable);
        tvSeleccionLugares.setClickable(enable);
        tvSeleccionLugares.setFocusable(enable);
        btnGuardarActualizar.setText(enable ? "Guardar" : "Editar");

        // Cambiar color según estado
        if (enable) {
            btnGuardarActualizar.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.teal_700)); // verde jade (ajusta el color si es necesario)
        } else {
            btnGuardarActualizar.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue_500)); // azul (ajusta el color si es necesario)
        }

        if (mMap != null) {
            updateMapMarkers(autoCompleteDireccion.getText().toString(), lugaresElegidos);
        }
    }

}