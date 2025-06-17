package com.example.hotroid;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.hotroid.bean.Cliente;
import com.example.hotroid.bean.Reserva;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SuperReservasActivity extends AppCompatActivity {

    private static final String TAG = "SuperReservasActivity";

    private TextView tvHotelNombre;
    private LinearLayout llReservasContainer;
    private EditText etSearchUser;
    private Button btnLimpiar;

    private List<Reserva> allReservas;
    private List<Reserva> currentHotelReservas;
    private Map<String, Cliente> clientesMap; // Para almacenar clientes por su ID de Firestore

    private String selectedHotelName;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_reservas_hotel);

        db = FirebaseFirestore.getInstance();
        clientesMap = new HashMap<>(); // Inicializar el mapa de clientes

        tvHotelNombre = findViewById(R.id.tvHotelNombre);
        llReservasContainer = findViewById(R.id.llReservasContainer);
        etSearchUser = findViewById(R.id.etSearchUser);
        btnLimpiar = findViewById(R.id.btnClearSearch);

        Intent intent = getIntent();
        selectedHotelName = "Hotel Desconocido";

        if (intent != null && intent.hasExtra("hotel_name")) {
            String receivedName = intent.getStringExtra("hotel_name");
            if (receivedName != null && !receivedName.isEmpty()) {
                selectedHotelName = receivedName;
            }
        }

        tvHotelNombre.setText("Reservas para " + selectedHotelName);

        // --- PASO CLAVE: Primero carga los clientes, luego las reservas ---
        // Esto es importante porque las reservas referencian los IDs de los clientes.
        // Después de que los clientes se carguen, inicializamos las reservas y mostramos.

        // Opcional: Ejecuta esta función UNA SOLA VEZ para subir tus clientes de prueba
        // DESCOMENTA LA SIGUIENTE LÍNEA SOLO PARA LA PRIMERA EJECUCIÓN (para crear los clientes en Firestore),
        // LUEGO COMENTA DE NUEVO PARA EVITAR DUPLICADOS.
        //saveTestClientesToFirestore();

        // Carga los clientes desde Firestore
        loadClientesFromFirestore(() -> {
            // Callback que se ejecuta cuando los clientes han sido cargados
            initializeReservasData(); // Ahora puedes inicializar las reservas con los IDs de cliente

            // Opcional: Ejecuta esta función UNA SOLA VEZ para subir tus reservas de prueba
            // DESCOMENTA LA SIGUIENTE LÍNEA SOLO PARA LA PRIMERA EJECUCIÓN (para crear las reservas en Firestore),
            // LUEGO COMENTA DE NUEVO PARA EVITAR DUPLICADOS.
            //saveAllReservasToFirestore(); // <--- Aquí la llamarías para guardar las reservas de prueba

            currentHotelReservas = getReservationsForHotel(selectedHotelName);
            displayFilteredReservas(currentHotelReservas); // Y mostrar las reservas
        });


        etSearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterReservas(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnLimpiar.setOnClickListener(v -> {
            etSearchUser.setText("");
            Toast.makeText(this, "Búsqueda limpiada.", Toast.LENGTH_SHORT).show();
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_hoteles);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_hoteles) {
                Intent intentInicio = new Intent(SuperReservasActivity.this, SuperActivity.class);
                startActivity(intentInicio);
                finish();
                return true;
            } else if (itemId == R.id.nav_usuarios) {
                Intent intentUbicacion = new Intent(SuperReservasActivity.this, SuperUsuariosActivity.class);
                startActivity(intentUbicacion);
                finish();
                return true;
            } else if (itemId == R.id.nav_eventos) {
                Intent intentAlertas = new Intent(SuperReservasActivity.this, SuperEventosActivity.class);
                startActivity(intentAlertas);
                finish();
                return true;
            }
            return false;
        });

        CardView cardSuper = findViewById(R.id.cardSuper);
        cardSuper.setOnClickListener(v -> {
            Intent intentAccount = new Intent(SuperReservasActivity.this, SuperCuentaActivity.class);
            startActivity(intentAccount);
        });
    }

    // --- NUEVA FUNCIÓN PARA SUBIR CLIENTES DE PRUEBA A FIRESTORE ---
    // EJECUTAR ESTA FUNCIÓN UNA SOLA VEZ PARA POBLAR TU BASE DE DATOS DE CLIENTES
    private void saveTestClientesToFirestore() {
        if (db == null) {
            Log.e(TAG, "Firestore no inicializado.");
            Toast.makeText(this, "Error: Firestore no está listo.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Cliente> testClientes = new ArrayList<>();
        // Crea los clientes con sus nombres y apellidos completos
        testClientes.add(new Cliente("Juan", "Molleda García", "true", "DNI", "12345678", "1990-01-15", "juan.molleda@example.com", "912345678", "Calle Falsa 123", ""));
        testClientes.add(new Cliente("Leandro", "Pérez Rojas", "true", "DNI", "87654321", "1985-03-20", "leandro.perez@example.com", "987654321", "Av. Siempre Viva 742", ""));
        testClientes.add(new Cliente("Augusto", "Medina Castro", "true", "DNI", "11223344", "1992-07-01", "augusto.medina@example.com", "911223344", "Jr. Los Álamos 45", ""));
        testClientes.add(new Cliente("Gustavo", "Cuya Salazar", "true", "DNI", "55667788", "1988-11-10", "gustavo.cuya@example.com", "955667788", "Psje. Las Flores 99", ""));
        testClientes.add(new Cliente("Roger", "Palomo Días", "true", "DNI", "99001122", "1995-04-25", "roger.palomo@example.com", "999001122", "Urb. El Sol 10", ""));
        testClientes.add(new Cliente("Joaquín", "Álvarez Soto", "true", "DNI", "12312312", "1993-02-14", "joaquin.alvarez@example.com", "912312312", "Res. Los Olivos 50", ""));
        testClientes.add(new Cliente("Nassim", "Ramírez Torres", "true", "DNI", "45645645", "1987-08-08", "nassim.ramirez@example.com", "945645645", "Calle Principal 200", ""));
        testClientes.add(new Cliente("Elias", "Pulgar Vílchez", "true", "DNI", "78978978", "1991-06-30", "elias.pulgar@example.com", "978978978", "Av. Libertad 30", ""));
        testClientes.add(new Cliente("Mirko", "Paz Robles", "true", "DNI", "10111213", "1994-01-01", "mirko.paz@example.com", "910111213", "Calle del Centro 15", ""));
        testClientes.add(new Cliente("Eliezer", "Cruz Sánchez", "true", "DNI", "14151617", "1989-09-12", "eliezer.cruz@example.com", "914151617", "Jr. Unión 77", ""));

        testClientes.add(new Cliente("Joaquín", "Pozo Ramos", "true", "DNI", "18192021", "1990-04-05", "joaquin.pozo@example.com", "918192021", "Av. Las Palmeras 1", ""));
        testClientes.add(new Cliente("Roger", "Albino Gómez", "true", "DNI", "22232425", "1986-07-20", "roger.albino@example.com", "922232425", "Calle Los Sauces 2", ""));
        testClientes.add(new Cliente("Julio", "Uribe Flores", "true", "DNI", "26272829", "1991-03-17", "julio.uribe@example.com", "926272829", "Urb. San Juan 3", ""));
        testClientes.add(new Cliente("Brian", "Ali Paredes", "true", "DNI", "30313233", "1993-10-01", "brian.ali@example.com", "930313233", "Pj. La Luna 4", ""));
        testClientes.add(new Cliente("Miguel", "Jara Rojas", "true", "DNI", "34353637", "1988-05-11", "miguel.jara@example.com", "934353637", "Res. El Cóndor 5", ""));
        testClientes.add(new Cliente("Flavio", "Farro Soto", "true", "DNI", "38394041", "1992-01-22", "flavio.farro@example.com", "938394041", "Av. El Sol 6", ""));
        testClientes.add(new Cliente("Jose", "Phan Díaz", "true", "DNI", "42434445", "1987-12-03", "jose.phan@example.com", "942434445", "Jr. Huáscar 7", ""));

        testClientes.add(new Cliente("Eduardo", "Campos Ruíz", "true", "DNI", "46474849", "1990-09-09", "eduardo.campos@example.com", "946474849", "Calle Bolognesi 8", ""));
        testClientes.add(new Cliente("Rubén", "Cancho Vargas", "true", "DNI", "50515253", "1985-06-19", "ruben.cancho@example.com", "950515253", "Av. San Martín 9", ""));
        testClientes.add(new Cliente("Aaron", "Villa Pérez", "true", "DNI", "54555657", "1994-03-08", "aaron.villa@example.com", "954555657", "Jr. Puno 10", ""));
        testClientes.add(new Cliente("Ollanta", "Humala Tasso", "true", "DNI", "58596061", "1962-06-27", "ollanta.humala@example.com", "958596061", "Av. Arequipa 11", ""));
        testClientes.add(new Cliente("Nadine", "Heredia Alarcón", "true", "DNI", "62636465", "1976-05-25", "nadine.heredia@example.com", "962636465", "Av. Arequipa 12", ""));

        testClientes.add(new Cliente("Sigrid", "Bazán Narro", "true", "DNI", "66676869", "1991-08-20", "sigrid.bazan@example.com", "966676869", "Calle El Polo 13", ""));
        testClientes.add(new Cliente("Daniel", "Abugattás Majluf", "true", "DNI", "70717273", "1955-04-10", "daniel.abugattas@example.com", "970717273", "Av. Primavera 14", ""));
        testClientes.add(new Cliente("Mauricio", "Mulder Bedoya", "true", "DNI", "74757677", "1956-06-08", "mauricio.mulder@example.com", "974757677", "Jr. Junín 15", ""));
        testClientes.add(new Cliente("Manuel", "Merino De Lama", "true", "DNI", "78798081", "1961-08-22", "manuel.merino@example.com", "978798081", "Calle Olmos 16", ""));

        testClientes.add(new Cliente("Pamela", "López Salas", "true", "DNI", "82838485", "1990-03-15", "pamela.lopez@example.com", "982838485", "Urb. Los Pinos 17", ""));
        testClientes.add(new Cliente("Carlos", "Álvarez Rodríguez", "true", "DNI", "86878889", "1978-01-05", "carlos.alvarez@example.com", "986878889", "Av. El Ejército 18", ""));
        testClientes.add(new Cliente("Robert", "Prevost Martínez", "true", "DNI", "90919293", "1955-09-18", "robert.prevost@example.com", "990919293", "Calle La Marina 19", ""));
        testClientes.add(new Cliente("Alan", "García Pérez", "true", "DNI", "94959697", "1949-05-23", "alan.garcia@example.com", "994959697", "Av. Benavides 20", ""));

        testClientes.add(new Cliente("Alejandro", "Toledo Manrique", "true", "DNI", "98990001", "1946-03-28", "alejandro.toledo@example.com", "998990001", "Jr. Cusco 21", ""));
        testClientes.add(new Cliente("Keiko", "Fujimori Higuchi", "true", "DNI", "02030405", "1975-05-25", "keiko.fujimori@example.com", "902030405", "Av. Pardo 22", ""));
        testClientes.add(new Cliente("Pedro", "Castillo Terrones", "true", "DNI", "06070809", "1969-10-19", "pedro.castillo@example.com", "906070809", "Calle El Sol 23", ""));
        testClientes.add(new Cliente("Agustín", "Lozano Saavedra", "true", "DNI", "10111213", "1971-06-25", "agustin.lozano@example.com", "910111213", "Av. La Paz 24", ""));

        testClientes.add(new Cliente("Oscar", "Ibáñez Velarde", "true", "DNI", "14151617", "1977-02-05", "oscar.ibanez@example.com", "914151617", "Jr. Callao 25", ""));
        testClientes.add(new Cliente("Verónica", "Mendoza Frisch", "true", "DNI", "18192021", "1980-12-09", "veronica.mendoza@example.com", "918192021", "Av. Brasil 26", ""));
        testClientes.add(new Cliente("Ismael", "Retes Espinoza", "true", "DNI", "22232425", "1990-07-03", "ismael.retes@example.com", "922232425", "Calle Lima 27", ""));
        testClientes.add(new Cliente("Patricia", "Benavides Vargas", "true", "DNI", "26272829", "1965-05-18", "patricia.benavides@example.com", "926272829", "Av. La Cultura 28", ""));

        // Cliente para MisReservasUser
        testClientes.add(new Cliente("David", "Gómez Pulgar", "true", "DNI", "00000001", "1999-01-01", "david.gomez@example.com", "900000001", "Calle Falsa 456", ""));


        // Guardar cada cliente
        for (Cliente cliente : testClientes) {
            db.collection("clientes").add(cliente) // add() genera un ID automático para el documento
                    .addOnSuccessListener(documentReference -> {
                        // Aquí recuperamos el ID generado por Firestore y lo asignamos al objeto Cliente
                        String firestoreId = documentReference.getId();
                        cliente.setFirestoreId(firestoreId); // Guardamos el ID en el objeto Cliente

                        // Luego, podemos añadir este cliente al mapa para usarlo
                        clientesMap.put(firestoreId, cliente);
                        Log.d(TAG, "Cliente añadido con ID: " + firestoreId + ", Nombre: " + cliente.getNombres() + " " + cliente.getApellidos());
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error al añadir cliente", e);
                        Toast.makeText(this, "Error al guardar cliente: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
        Toast.makeText(this, "Intentando guardar clientes de prueba en Firestore...", Toast.LENGTH_SHORT).show();
    }

    // --- FUNCIÓN PARA CARGAR CLIENTES DESDE FIRESTORE ---
    private void loadClientesFromFirestore(Runnable onCompleteCallback) {
        if (db == null) {
            Log.e(TAG, "Firestore no inicializado para cargar clientes.");
            onCompleteCallback.run(); // Ejecuta el callback incluso en caso de error
            return;
        }

        db.collection("clientes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        clientesMap.clear(); // Limpiar el mapa existente
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Cliente cliente = document.toObject(Cliente.class);
                            cliente.setFirestoreId(document.getId()); // Asignar el ID de Firestore al objeto Cliente
                            clientesMap.put(document.getId(), cliente); // Guardar el cliente en el mapa
                        }
                        Log.d(TAG, "Clientes cargados de Firestore: " + clientesMap.size());
                        // No es necesario un Toast aquí, ya que el usuario no siempre lo verá.
                    } else {
                        Log.e(TAG, "Error al cargar clientes de Firestore: ", task.getException());
                        Toast.makeText(this, "Error al cargar clientes: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                    onCompleteCallback.run(); // Ejecutar el callback cuando la carga de clientes ha terminado
                });
    }

    private void initializeReservasData() {
        allReservas = new ArrayList<>();

        // Obtener los IDs de los clientes del mapa que acabamos de cargar de Firestore
        // Estos IDs son los que usarás en tus objetos Reserva.
        // Asegúrate de que los nombres y apellidos coincidan EXACTAMENTE con los que pusiste en saveTestClientesToFirestore()
        String idJuanMolleda = getClienteIdByNombre("Juan", "Molleda García");
        String idLeandroPerez = getClienteIdByNombre("Leandro", "Pérez Rojas");
        String idAugustoMedina = getClienteIdByNombre("Augusto", "Medina Castro");
        String idGustavoCuya = getClienteIdByNombre("Gustavo", "Cuya Salazar");
        String idRogerPalomo = getClienteIdByNombre("Roger", "Palomo Días");
        String idJoaquinAlvarez = getClienteIdByNombre("Joaquín", "Álvarez Soto");
        String idNassimRamirez = getClienteIdByNombre("Nassim", "Ramírez Torres");
        String idEliasPulgar = getClienteIdByNombre("Elias", "Pulgar Vílchez");
        String idMirkoPaz = getClienteIdByNombre("Mirko", "Paz Robles");
        String idEliezerCruz = getClienteIdByNombre("Eliezer", "Cruz Sánchez");

        String idJoaquinPozo = getClienteIdByNombre("Joaquín", "Pozo Ramos");
        String idRogerAlbino = getClienteIdByNombre("Roger", "Albino Gómez");
        String idJulioUribe = getClienteIdByNombre("Julio", "Uribe Flores");
        String idBrianAli = getClienteIdByNombre("Brian", "Ali Paredes");
        String idMiguelJara = getClienteIdByNombre("Miguel", "Jara Rojas");
        String idFlavioFarro = getClienteIdByNombre("Flavio", "Farro Soto");
        String idJosePhan = getClienteIdByNombre("Jose", "Phan Díaz");

        String idEduardoCampos = getClienteIdByNombre("Eduardo", "Campos Ruíz");
        String idRubenCancho = getClienteIdByNombre("Rubén", "Cancho Vargas");
        String idAaronVilla = getClienteIdByNombre("Aaron", "Villa Pérez");
        String idOllantaHumala = getClienteIdByNombre("Ollanta", "Humala Tasso");
        String idNadineHeredia = getClienteIdByNombre("Nadine", "Heredia Alarcón");

        String idSigridBazan = getClienteIdByNombre("Sigrid", "Bazán Narro");
        String idDanielAbugattas = getClienteIdByNombre("Daniel", "Abugattás Majluf");
        String idMauricioMulder = getClienteIdByNombre("Mauricio", "Mulder Bedoya");
        String idManuelMerino = getClienteIdByNombre("Manuel", "Merino De Lama");

        String idPamelaLopez = getClienteIdByNombre("Pamela", "López Salas");
        String idCarlosAlvarez = getClienteIdByNombre("Carlos", "Álvarez Rodríguez");
        String idRobertPrevost = getClienteIdByNombre("Robert", "Prevost Martínez");
        String idAlanGarcia = getClienteIdByNombre("Alan", "García Pérez");

        String idAlejandroToledo = getClienteIdByNombre("Alejandro", "Toledo Manrique");
        String idKeikoFujimori = getClienteIdByNombre("Keiko", "Fujimori Higuchi");
        String idPedroCastillo = getClienteIdByNombre("Pedro", "Castillo Terrones");
        String idAgustinLozano = getClienteIdByNombre("Agustín", "Lozano Saavedra");

        String idOscarIbanez = getClienteIdByNombre("Oscar", "Ibáñez Velarde");
        String idVeronicaMendoza = getClienteIdByNombre("Verónica", "Mendoza Frisch");
        String idIsmaelRetes = getClienteIdByNombre("Ismael", "Retes Espinoza");
        String idPatriciaBenavides = getClienteIdByNombre("Patricia", "Benavides Vargas");

        // Ahora, al crear las reservas, usa los IDs de los clientes obtenidos
        allReservas.add(new Reserva("R001", "Aranwa", idJuanMolleda, 1, 2, 0, "30/03/2025", "05/04/2025", "activo", 800,
                false, false, 0, false, null, null, "101", false));
        allReservas.add(new Reserva("R002", "Aranwa", idLeandroPerez, 1, 1, 0, "24/03/2025", "29/03/2025", "activo", 500,
                false, false, 0, false, null, null, "102", false));
        allReservas.add(new Reserva("R003", "Aranwa", idAugustoMedina, 2, 3, 1, "22/03/2025", "29/03/2025", "activo", 1200,
                false, false, 0, false, null, null, "103", false));
        allReservas.add(new Reserva("R004", "Aranwa", idGustavoCuya, 1, 2, 0, "20/03/2025", "26/03/2025", "activo", 750,
                false, false, 0, false, null, null, "104", false));
        allReservas.add(new Reserva("R005", "Aranwa", idRogerPalomo, 1, 2, 0, "15/04/2025", "20/04/2025", "activo", 900,
                false, false, 0, false, null, null, "105", false));
        allReservas.add(new Reserva("R025", "Aranwa", idJoaquinAlvarez, 1, 1, 0, "01/06/2025", "03/06/2025", "activo", 600,
                false, false, 0, false, null, null, "106", false));
        allReservas.add(new Reserva("R026", "Aranwa", idNassimRamirez, 2, 4, 2, "05/06/2025", "10/06/2025", "activo", 1500,
                false, false, 0, false, null, null, "107", false));
        allReservas.add(new Reserva("R040", "Aranwa", idEliasPulgar, 1, 2, 0, "12/06/2025", "15/06/2025", "activo", 850,
                false, false, 0, false, null, null, "108", false));
        allReservas.add(new Reserva("R041", "Aranwa", idMirkoPaz, 1, 1, 0, "18/06/2025", "20/06/2025", "activo", 580,
                false, false, 0, false, null, null, "109", false));
        allReservas.add(new Reserva("R042", "Aranwa", idEliezerCruz, 1, 2, 1, "25/06/2025", "30/06/2025", "activo", 1100,
                false, false, 0, false, null, null, "110", false));

        // Reservas de Decameron
        allReservas.add(new Reserva("R006", "Decameron", idJoaquinPozo, 1, 2, 1, "01/05/2025", "07/05/2025", "activo", 1100,
                false, false, 0, false, null, null, "201", false));
        allReservas.add(new Reserva("R007", "Decameron", idRogerAlbino, 1, 1, 0, "10/05/2025", "13/05/2025", "activo", 600,
                false, false, 0, false, null, null, "202", false));
        allReservas.add(new Reserva("R027", "Decameron", idJulioUribe, 1, 2, 0, "15/06/2025", "20/06/2025", "activo", 1000,
                false, false, 0, false, null, null, "203", false));
        allReservas.add(new Reserva("R028", "Decameron", idBrianAli, 2, 3, 1, "22/06/2025", "28/06/2025", "activo", 1800,
                false, false, 0, false, null, null, "204", false));
        allReservas.add(new Reserva("R029", "Decameron", idMiguelJara, 1, 1, 0, "01/07/2025", "03/07/2025", "activo", 700,
                false, false, 0, false, null, null, "205", false));
        allReservas.add(new Reserva("R043", "Decameron", idFlavioFarro, 1, 2, 0, "05/07/2025", "10/07/2025", "activo", 1200,
                false, false, 0, false, null, null, "206", false));
        allReservas.add(new Reserva("R044", "Decameron", idJosePhan, 1, 1, 0, "12/07/2025", "14/07/2025", "activo", 650,
                false, false, 0, false, null, null, "207", false));

        // Reservas de Oro Verde
        allReservas.add(new Reserva("R008", "Oro Verde", idEduardoCampos, 1, 2, 0, "05/06/2025", "08/06/2025", "activo", 950,
                false, false, 0, false, null, null, "301", false));
        allReservas.add(new Reserva("R030", "Oro Verde", idRubenCancho, 1, 2, 2, "10/07/2025", "15/07/2025", "activo", 1300,
                false, false, 0, false, null, null, "302", false));
        allReservas.add(new Reserva("R031", "Oro Verde", idAaronVilla, 1, 1, 0, "20/07/2025", "23/07/2025", "activo", 800,
                false, false, 0, false, null, null, "303", false));
        allReservas.add(new Reserva("R045", "Oro Verde", idOllantaHumala, 2, 3, 1, "01/08/2025", "06/08/2025", "activo", 1500,
                false, false, 0, false, null, null, "304", false));
        allReservas.add(new Reserva("R046", "Oro Verde", idNadineHeredia, 1, 2, 0, "10/08/2025", "13/08/2025", "activo", 900,
                false, false, 0, false, null, null, "305", false));

        // Reservas de Boca Ratón
        allReservas.add(new Reserva("R009", "Boca Ratón", idSigridBazan, 1, 1, 0, "01/07/2025", "04/07/2025", "activo", 550,
                false, false, 0, false, null, null, "401", false));
        allReservas.add(new Reserva("R032", "Boca Ratón", idDanielAbugattas, 2, 3, 0, "08/08/2025", "12/08/2025", "activo", 1000,
                false, false, 0, false, null, null, "402", false));
        allReservas.add(new Reserva("R033", "Boca Ratón", idMauricioMulder, 1, 2, 0, "15/08/2025", "18/08/2025", "activo", 650,
                false, false, 0, false, null, null, "403", false));
        allReservas.add(new Reserva("R047", "Boca Ratón", idManuelMerino, 1, 1, 0, "20/08/2025", "22/08/2025", "activo", 580,
                false, false, 0, false, null, null, "404", false));

        // Reservas de Libertador
        allReservas.add(new Reserva("R010", "Libertador", idPamelaLopez, 1, 2, 0, "25/07/2025", "28/07/2025", "activo", 1300,
                false, false, 0, false, null, null, "501", false));
        allReservas.add(new Reserva("R034", "Libertador", idCarlosAlvarez, 1, 1, 0, "05/09/2025", "07/09/2025", "activo", 900,
                false, false, 0, false, null, null, "502", false));
        allReservas.add(new Reserva("R035", "Libertador", idRobertPrevost, 2, 4, 1, "10/09/2025", "15/09/2025", "activo", 2000,
                false, false, 0, false, null, null, "503", false));
        allReservas.add(new Reserva("R048", "Libertador", idAlanGarcia, 1, 2, 0, "18/09/2025", "22/09/2025", "activo", 1400,
                false, false, 0, false, null, null, "504", false));

        // Reservas de Costa del Sol
        allReservas.add(new Reserva("R011", "Costa del Sol", idAlejandroToledo, 1, 2, 0, "12/08/2025", "15/08/2025", "activo", 700,
                false, false, 0, false, null, null, "601", false));
        allReservas.add(new Reserva("R036", "Costa del Sol", idKeikoFujimori, 1, 1, 0, "20/09/2025", "23/09/2025", "activo", 500,
                false, false, 0, false, null, null, "602", false));
        allReservas.add(new Reserva("R037", "Costa del Sol", idPedroCastillo, 2, 3, 2, "01/10/2025", "07/10/2025", "activo", 1400,
                false, false, 0, false, null, null, "603", false));
        allReservas.add(new Reserva("R049", "Costa del Sol", idAgustinLozano, 1, 2, 1, "10/10/2025", "13/10/2025", "activo", 800,
                false, false, 0, false, null, null, "604", false));

        // Reservas de Sonesta
        allReservas.add(new Reserva("R012", "Sonesta", idOscarIbanez, 1, 2, 0, "01/09/2025", "04/09/2025", "activo", 850,
                false, false, 0, false, null, null, "701", false));
        allReservas.add(new Reserva("R038", "Sonesta", idVeronicaMendoza, 1, 1, 0, "10/10/2025", "12/10/2025", "activo", 600,
                false, false, 0, false, null, null, "702", false));
        allReservas.add(new Reserva("R039", "Sonesta", idIsmaelRetes, 2, 3, 1, "15/10/2025", "20/10/2025", "activo", 1200,
                false, false, 0, false, null, null, "703", false));
        allReservas.add(new Reserva("R050", "Sonesta", idPatriciaBenavides, 1, 2, 0, "22/10/2025", "25/10/2025", "activo", 950,
                false, false, 0, false, null, null, "704", false));
    }


    // Helper para obtener ID de cliente por nombre y apellido
    // Busca en el mapa `clientesMap` que ya se cargó de Firestore.
    private String getClienteIdByNombre(String nombres, String apellidos) {
        Log.d(TAG, "Buscando cliente: " + nombres + " " + apellidos); // Depuración
        for (Map.Entry<String, Cliente> entry : clientesMap.entrySet()) {
            Cliente cliente = entry.getValue();
            // Imprime cada cliente que está en el mapa
            Log.d(TAG, "Comparando con cliente en mapa: " + cliente.getNombres() + " " + cliente.getApellidos() + " (ID: " + entry.getKey() + ")");

            if (cliente.getNombres().equalsIgnoreCase(nombres) && cliente.getApellidos().equalsIgnoreCase(apellidos)) {
                Log.d(TAG, "¡Cliente ENCONTRADO! ID: " + entry.getKey()); // Depuración
                return entry.getKey(); // Retorna el Firestore ID del documento
            }
        }
        Log.w(TAG, "Cliente con nombre: " + nombres + " " + apellidos + " no encontrado en el mapa de clientes.");
        return "ID_NO_ENCONTRADO"; // Retorna un ID por defecto si no lo encuentra (manejar este caso)
    }


    private void saveAllReservasToFirestore() {
        if (db == null) {
            Log.e(TAG, "Firestore no inicializado.");
            Toast.makeText(this, "Error: Firestore no está listo.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (allReservas == null || allReservas.isEmpty()) {
            Log.d(TAG, "No hay reservas para guardar.");
            Toast.makeText(this, "No hay reservas en la lista para guardar.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Reserva reserva : allReservas) {
            Map<String, Object> reservaMap = new HashMap<>();
            reservaMap.put("idReserva", reserva.getIdReserva());
            reservaMap.put("idPersona", reserva.getIdPersona()); // Este ahora será el ID de Firestore del cliente
            reservaMap.put("idHotel", reserva.getIdHotel());
            reservaMap.put("habitaciones", reserva.getHabitaciones());
            reservaMap.put("adultos", reserva.getAdultos());
            reservaMap.put("ninos", reserva.getNinos());
            reservaMap.put("fechaInicio", reserva.getFechaInicio());
            reservaMap.put("fechaFin", reserva.getFechaFin());
            reservaMap.put("estado", reserva.getEstado());
            reservaMap.put("precioTotal", reserva.getPrecioTotal());
            reservaMap.put("checkInRealizado", reserva.isCheckInRealizado());
            reservaMap.put("checkOutRealizado", reserva.isCheckOutRealizado());
            reservaMap.put("cobros_adicionales", reserva.getCobros_adicionales());
            reservaMap.put("estaCancelado", reserva.isEstaCancelado());
            reservaMap.put("fechaCancelacion", reserva.getFechaCancelacion());
            reservaMap.put("idValoracion", reserva.getIdValoracion());
            reservaMap.put("roomNumber", reserva.getRoomNumber());
            reservaMap.put("tieneValoracion", reserva.isTieneValoracion());

            db.collection("reservas").document(reserva.getIdReserva())
                    .set(reservaMap)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Documento de reserva añadido/actualizado con ID: " + reserva.getIdReserva());
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error al añadir/actualizar documento de reserva", e);
                        Toast.makeText(this, "Error al guardar reserva " + reserva.getIdReserva() + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
        Toast.makeText(this, "Intentando guardar todas las reservas en Firestore...", Toast.LENGTH_SHORT).show();
    }


    private List<Reserva> getReservationsForHotel(String hotelIdentifier) {
        List<Reserva> filteredList = new ArrayList<>();
        if (allReservas != null) {
            for (Reserva reserva : allReservas) {
                if (reserva.getIdHotel().equalsIgnoreCase(hotelIdentifier)) {
                    filteredList.add(reserva);
                }
            }
        }
        return filteredList;
    }

    private void filterReservas(String searchText) {
        llReservasContainer.removeAllViews();

        List<Reserva> filteredReservas = new ArrayList<>();
        if (searchText.isEmpty()) {
            filteredReservas.addAll(currentHotelReservas);
        } else {
            String lowerCaseSearchText = searchText.toLowerCase(Locale.getDefault());
            for (Reserva reserva : currentHotelReservas) {
                // Para la búsqueda por nombre de cliente, ahora que tenemos el mapa:
                String idPersona = reserva.getIdPersona();
                Cliente cliente = clientesMap.get(idPersona);
                String nombreCompletoCliente = (cliente != null) ? (cliente.getNombres() + " " + cliente.getApellidos()).toLowerCase(Locale.getDefault()) : "";

                if (nombreCompletoCliente.contains(lowerCaseSearchText) ||
                        reserva.getIdReserva().toLowerCase(Locale.getDefault()).contains(lowerCaseSearchText)) {
                    filteredReservas.add(reserva);
                }
            }
        }

        if (filteredReservas.isEmpty()) {
            TextView noReservationsText = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            noReservationsText.setLayoutParams(lp);
            noReservationsText.setText("No hay reservas encontradas para '" + searchText + "' en " + selectedHotelName + ".");
            noReservationsText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            noReservationsText.setTextColor(Color.GRAY);
            noReservationsText.setGravity(Gravity.CENTER);
            noReservationsText.setPadding(0, dpToPx(32), 0, dpToPx(32));
            llReservasContainer.addView(noReservationsText);
        } else {
            displayFilteredReservas(filteredReservas);
        }
    }

    private void displayFilteredReservas(List<Reserva> reservasToDisplay) {
        llReservasContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (Reserva reserva : reservasToDisplay) {
            View cardView = inflater.inflate(R.layout.item_reserva_card, llReservasContainer, false);

            TextView tvClienteCard = cardView.findViewById(R.id.tvClienteCard);
            TextView tvFechaReservaCard = cardView.findViewById(R.id.tvFechaReservaCard);
            TextView tvHabitacionCard = cardView.findViewById(R.id.tvHabitacionCard);
            TextView tvPrecioTotalCard = cardView.findViewById(R.id.tvPrecioTotalCard);

            // Intentamos obtener el nombre del cliente del mapa
            String idPersona = reserva.getIdPersona();
            Cliente cliente = clientesMap.get(idPersona);
            String nombreCompletoCliente = (cliente != null) ? cliente.getNombres() + " " + cliente.getApellidos() : "Cliente: " + reserva.getIdPersona() + " (Cargando...)";
            tvClienteCard.setText("Cliente: " + nombreCompletoCliente);


            tvFechaReservaCard.setText("Fecha de Reserva: " + reserva.getFechaInicio() + " - " + reserva.getFechaFin());
            tvHabitacionCard.setText("Habitaciones: " + reserva.getHabitaciones());
            tvPrecioTotalCard.setText(String.format(Locale.getDefault(), "Precio Total: $%d", reserva.getPrecioTotal()));

            cardView.setOnClickListener(v -> {
                Toast.makeText(this, "Reserva ID: " + reserva.getIdReserva() + ", Cliente ID: " + reserva.getIdPersona() + " seleccionada.", Toast.LENGTH_SHORT).show();
            });

            llReservasContainer.addView(cardView);
        }
    }

    /**
     * Esta función ya no es necesaria llamarla directamente en `displayFilteredReservas`
     * porque ahora `displayFilteredReservas` intentará obtener el nombre del cliente
     * del `clientesMap` que se cargó al inicio.
     * Solo la mantengo aquí como ejemplo de consulta directa.
     * @param clienteId El ID del documento del cliente en la colección "clientes".
     */
    private void getClienteNameAndLastName(String clienteId) {
        if (db == null) {
            Log.e(TAG, "Firestore no inicializado.");
            return;
        }

        db.collection("clientes").document(clienteId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String nombres = document.getString("nombres");
                                String apellidos = document.getString("apellidos");
                                Log.d(TAG, "Cliente encontrado - ID: " + clienteId + ", Nombre: " + nombres + " " + apellidos);
                                // Toast.makeText(SuperReservasActivity.this, "Cliente: " + nombres + " " + apellidos, Toast.LENGTH_SHORT).show();
                            } else {
                                Log.d(TAG, "No se encontró el documento del cliente con ID: " + clienteId);
                                // Toast.makeText(SuperReservasActivity.this, "Cliente no encontrado.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "Error al obtener documento del cliente: ", task.getException());
                            // Toast.makeText(SuperReservasActivity.this, "Error al obtener cliente: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }
}