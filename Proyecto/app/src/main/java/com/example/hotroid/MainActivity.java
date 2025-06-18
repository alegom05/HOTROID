package com.example.hotroid;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hotroid.R;
import com.example.hotroid.databinding.ActivityMainBinding;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;


import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "MAINACTDEBUG";

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "oncreate");
        Button btnCliente = findViewById(R.id.btnCliente);
        Log.d(TAG, "se ingresa a cliente");
        Button btnTaxista = findViewById(R.id.btnTaxista);
        Button btnAdmin = findViewById(R.id.btnAdmin);
        Button btnSuperAdmin = findViewById(R.id.btnSuperAdmin);

        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setLogo(R.drawable.ic_hotroid_icon)
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build();
        signInLauncher.launch(signInIntent);


        btnCliente.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ClienteActivity.class);
            startActivity(intent);
        });

        btnTaxista.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TaxiActivity.class);
            startActivity(intent);
        });

        btnAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdminActivity.class);
            startActivity(intent);
        });

        btnSuperAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SuperActivity.class);
            startActivity(intent);
        });

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.setLanguageCode("es-419");

        if (auth.getCurrentUser() != null) {
            // already signed in
            Log.d("msg", "uid: " + auth.getCurrentUser().getUid());
            Log.d("msg", "name: " + auth.getCurrentUser().getDisplayName());
            Log.d("msg", "email: " + auth.getCurrentUser().getEmail());
            if (auth.getCurrentUser().isEmailVerified()) {
                Log.d("msg", "correo autenticado");
                // continuar al siguiente activity
            } else {
                auth.getCurrentUser().sendEmailVerification()
                        .addOnCompleteListener(task -> Log.d("msg", "correo enviado"));
            }

        } else {
            // not signed in
            signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(Arrays.asList(
                            new AuthUI.IdpConfig.EmailBuilder().build()
                    ))
                    .setIsSmartLockEnabled(false)
                    .build();

            signInLauncher.launch(signInIntent);

        }



    }

   /* public void incrementarContador (View view) {
        TextView textView = findViewById(R.id.contadorEnVista);
        String contadorStr = textView.getText().toString();
        int contador = Integer.parseInt(contadorStr);
        contador++;
        Log.d ("contador", "" +  String.valueOf(contador));
        textView.setText(String.valueOf(contador));
    }*/

    List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build()
    );

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    if (auth.getCurrentUser() != null) {
                        if (auth.getCurrentUser().isEmailVerified()) {
                            Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show();
                        } else {
                            auth.getCurrentUser().sendEmailVerification()
                                    .addOnCompleteListener(task -> Toast.makeText(this, "Verifica tu correo", Toast.LENGTH_LONG).show());
                        }
                    }
                } else {
                    Toast.makeText(this, "Inicio de sesión cancelado", Toast.LENGTH_SHORT).show();
                }
            }
    );












    /**** App Bar - Start ****/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_act,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        if (itemId == R.id.wifi) {
            Toast.makeText(this, "btn wifi presionado", Toast.LENGTH_LONG).show();
        } else if (itemId == R.id.add) {
            Toast.makeText(this, "btn add presionado", Toast.LENGTH_LONG).show();
        } else if (itemId == R.id.notify) {
            //Toast.makeText(this, "btn notify presionado", Toast.LENGTH_SHORT).show();
            Log.d ("msgOptAppBar", "App Bar onclik");
            View menuItemView = findViewById(R.id.notify);

            /**** Popup Menu - Start ****/
            PopupMenu popupMenu =  new PopupMenu(this, menuItemView);
            popupMenu.getMenuInflater().inflate(R.menu.menu_popup, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.reply_all) {
                        Log.d ("msgPopup", "replyAll");
                        return true;
                    } else if (item.getItemId() == R.id.forward) {
                        Log.d ("msgPopup", "forward");
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            popupMenu.show();
            /**** Popup Menu - End ****/

        }

        return super.onOptionsItemSelected(item);
    }

    /*public void wifiBtn(MenuItem menuItem){
        Toast.makeText(this, "btn wifi presionado", Toast.LENGTH_SHORT).show();
    }

    public void addBtn(MenuItem menuItem){
        Toast.makeText(this, "btn add presionado", Toast.LENGTH_SHORT).show();
    }

    public void notifyBtn(MenuItem menuItem){
        Toast.makeText(this, "btn notify presionado", Toast.LENGTH_SHORT).show();
    }*/

    /**** App Bar - End ****/

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onstart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onresumed");
    }

    /**** Context Menu - Start ****/
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_context, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item){
        if (item.getItemId() == R.id.context_edit) {
            Log.d("msgContextMenu", "edit");
            return true;
        } else if (item.getItemId() == R.id.context_delete) {
            Log.d("msgContextMenu", "delete");
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }
    /**** Context Menu - End ****/



    /*
        El callback (lo que ejecutará al regreso)
     */
    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                //aquí llegamos luego del setResult(RESULT_OK,intent);
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    String apellido = data.getStringExtra("apellido");
                    Toast.makeText(MainActivity.this,
                            "el apellido recibido es: " + apellido,
                            Toast.LENGTH_LONG).show();
                }
            }
    );
    //**************************ADMINISTRADOR DE HOTEL**************************************************************************
    public void irAAdmin(View view) {
        Intent intent = new Intent(this, AdminActivity.class);
        startActivity(intent);
    }

}