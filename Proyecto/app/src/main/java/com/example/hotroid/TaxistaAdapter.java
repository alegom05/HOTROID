package com.example.hotroid;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.Taxista;

import java.util.List;

public class TaxistaAdapter extends RecyclerView.Adapter<TaxistaAdapter.TaxistaViewHolder> {

    private List<Taxista> lista;
    private Context context;

    public TaxistaAdapter(List<Taxista> lista, Context context) {
        this.lista = lista;
        this.context = context;
    }

    @NonNull
    @Override
    public TaxistaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_taxista, parent, false);
        return new TaxistaViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TaxistaViewHolder holder, int position) {
        Taxista taxista = lista.get(position);
        holder.tvNombre.setText(taxista.getNombre());
        holder.tvEstado.setText(taxista.getEstado());
        holder.imgTaxista.setImageResource(taxista.getImagenResId());

        holder.itemView.setOnClickListener(v -> {
            switch (taxista.getNombre()) {
                case "Carlos Alvarez":
                    taxista.setDni("72341235");
                    taxista.setNacimiento("11 de agosto de 2000");
                    taxista.setCorreo("c.alvarez@gmail.com");
                    taxista.setTelefono("912356124");
                    taxista.setDireccion("Av. Perú 243, San Martín de Porres");
                    taxista.setPlaca("ABC-1234");
                    taxista.setVehiculoImagenResId(R.drawable.carrito);
                    break;
                case "Alex Russo":
                    taxista.setDni("82456122");
                    taxista.setNacimiento("5 de febrero de 1998");
                    taxista.setCorreo("alex.russo@gmail.com");
                    taxista.setTelefono("987654321");
                    taxista.setDireccion("Calle Luna 456, Surco");
                    taxista.setPlaca("XYZ-5678");
                    taxista.setVehiculoImagenResId(R.drawable.carrito);
                    break;
                case "Marcelo Vilca":
                    taxista.setDni("74581236");
                    taxista.setNacimiento("20 de junio de 1995");
                    taxista.setCorreo("m.vilca@gmail.com");
                    taxista.setTelefono("911223344");
                    taxista.setDireccion("Jr. Cusco 123, San Juan de Lurigancho");
                    taxista.setPlaca("MNO-2345");
                    taxista.setVehiculoImagenResId(R.drawable.carrito);
                    break;
                case "Jaime Mora":
                    taxista.setDni("75963218");
                    taxista.setNacimiento("15 de marzo de 1990");
                    taxista.setCorreo("jaime.mora@gmail.com");
                    taxista.setTelefono("900112233");
                    taxista.setDireccion("Av. Arequipa 1500, Miraflores");
                    taxista.setPlaca("JKL-8765");
                    taxista.setVehiculoImagenResId(R.drawable.carrito);
                    break;
                case "Arturo Delgado":
                    taxista.setDni("73592614");
                    taxista.setNacimiento("29 de abril de 1985");
                    taxista.setCorreo("arturo.d@gmail.com");
                    taxista.setTelefono("933445566");
                    taxista.setDireccion("Av. Colonial 980, Cercado de Lima");
                    taxista.setPlaca("TAX-1122");
                    taxista.setVehiculoImagenResId(R.drawable.carrito);
                    break;
                case "Farith Puente":
                    taxista.setDni("76893421");
                    taxista.setNacimiento("9 de septiembre de 1992");
                    taxista.setCorreo("f.puente@gmail.com");
                    taxista.setTelefono("922334455");
                    taxista.setDireccion("Pasaje El Sol 88, Los Olivos");
                    taxista.setPlaca("FPT-4321");
                    taxista.setVehiculoImagenResId(R.drawable.carrito);
                    break;
            }
            Intent intent = new Intent(context, AdminTaxistaDetalles.class);
            intent.putExtra("nombre", taxista.getNombre());
            intent.putExtra("estado", taxista.getEstado());
            intent.putExtra("imagen", taxista.getImagenResId());
            intent.putExtra("dni", taxista.getDni());
            intent.putExtra("nacimiento", taxista.getNacimiento());
            intent.putExtra("correo", taxista.getCorreo());
            intent.putExtra("telefono", taxista.getTelefono());
            intent.putExtra("direccion", taxista.getDireccion());
            intent.putExtra("placa", taxista.getPlaca());
            intent.putExtra("fotoVehiculo", taxista.getVehiculoImagenResId());
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return lista.size();
    }
    public void actualizarLista(List<Taxista> nuevaLista) {
        lista.clear();
        lista.addAll(nuevaLista);
        notifyDataSetChanged();
    }


    static class TaxistaViewHolder extends RecyclerView.ViewHolder {
        ImageView imgTaxista;
        TextView tvNombre, tvEstado;

        public TaxistaViewHolder(@NonNull View itemView) {
            super(itemView);
            imgTaxista = itemView.findViewById(R.id.imgTaxista);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvEstado = itemView.findViewById(R.id.tvEstado);
        }
    }
}
