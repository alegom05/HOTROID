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
import com.example.hotroid.bean.Persona;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "MAINACTDEBUG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "oncreate");


    }

    public void incrementarContador (View view) {
        TextView textView = findViewById(R.id.contadorEnVista);
        String contadorStr = textView.getText().toString();
        int contador = Integer.parseInt(contadorStr);
        contador++;
        Log.d ("contador", "" +  String.valueOf(contador));
        textView.setText(String.valueOf(contador));
    }

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

    public void irAVentana2(View view) {

        EditText editText = findViewById(R.id.editTextNombre);
        String texto = editText.getText().toString();

        Persona persona = new Persona("Claudia");

        Intent intent = new Intent(this, MainActivity2.class);
        intent.putExtra("texto", texto);
        //se envia un objeto
        intent.putExtra("alumna", persona);
        startActivity(intent);

    }

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