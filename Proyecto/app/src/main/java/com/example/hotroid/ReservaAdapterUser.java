package com.example.hotroid;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.Hotel;
import com.example.hotroid.bean.Reserva;
import com.example.hotroid.bean.ReservaConHotel;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReservaAdapterUser extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ACTIVO = 0;
    private static final int VIEW_TYPE_PASADO = 1;
    private static final int VIEW_TYPE_CANCELADO = 2;
    private final int tipoLista; // 0 = activos, 1 = pasados, 2 = cancelados

    private final List<ReservaConHotel> lista;
    private final Context context;

    public ReservaAdapterUser(List<ReservaConHotel> lista, Context context, int tipoLista){
        this.lista=lista;
        this.context = context;
        this.tipoLista=tipoLista;
    }

    @Override
    public int getItemViewType(int position) {
        String estado = lista.get(position).getReserva().getEstado().trim().toLowerCase();
        Log.d("AdapterTipo", "Estado detectado: " + estado + " en posición " + position);
        switch (estado) {
            case "activo":
                return VIEW_TYPE_ACTIVO;
            case "pasado":
                return VIEW_TYPE_PASADO;
            case "cancelado":
                return VIEW_TYPE_CANCELADO;
            default:
                return -1;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_ACTIVO) {
            View view = inflater.inflate(R.layout.user_item_reserva_activo, parent, false);
            return new ActivoViewHolder(view);
        } else if (viewType == VIEW_TYPE_PASADO) {
            View view = inflater.inflate(R.layout.user_item_reserva_cancelados, parent, false);
            return new PasadoViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.user_item_reserva_cancelados, parent, false);
            return new CanceladoViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ReservaConHotel reservaConHotel = lista.get(position);
        Reserva reserva = reservaConHotel.getReserva();
        Hotel hotel = reservaConHotel.getHotel();

        if (holder instanceof ActivoViewHolder) {
            ((ActivoViewHolder) holder).bind(reserva, hotel, context, tipoLista);
        } else if (holder instanceof PasadoViewHolder) {
            ((PasadoViewHolder) holder).bind(reserva, hotel);
        } else if (holder instanceof CanceladoViewHolder) {
            ((CanceladoViewHolder) holder).bind(reserva, hotel);
        }
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    // ViewHolders
    static class ActivoViewHolder extends RecyclerView.ViewHolder {
        TextView tvHotelName, tvRoomDetails, tvStatus;
        CircleImageView profileImage;
        CardView cardHotelActivo;


        public ActivoViewHolder(View itemView) {
            super(itemView);
            tvHotelName = itemView.findViewById(R.id.tvHotelName);
            tvRoomDetails = itemView.findViewById(R.id.tvRoomDetails);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            profileImage = itemView.findViewById(R.id.profileImage);
            cardHotelActivo = itemView.findViewById(R.id.cardHotelActivo);
        }

        void bind(Reserva r, Hotel h, Context context, int tipoLista) {
            tvHotelName.setText(h.getName() + " - " + h.getDireccion());
            tvRoomDetails.setText(r.getHabitaciones() + " hab., " + r.getAdultos() + " adultos, " + r.getNinos() + " niños");
            tvStatus.setText("Estado: Confirmado");
            profileImage.setImageResource(h.getImageResourceId());

            if (tipoLista == 0) {
                Log.d("ReservaAdapter", "bind() llamado para ACTIVO");
                cardHotelActivo.setOnClickListener(v -> {
                    Log.d("ReservaAdapter", "Iniciando DetalleReservaActivo...");
                    Intent intent = new Intent(context, DetalleReservaActivo.class);
                    intent.putExtra("hotel_nombre", h.getName());
                    intent.putExtra("city",h.getDireccion());
                    intent.putExtra("hotel_location", h.getDireccionDetallada());
                    intent.putExtra("room_details", r.getHabitaciones() + " hab., " + r.getAdultos() + " adultos, " + r.getNinos() + " niños");
                    intent.putExtra("status", r.getEstado());
                    intent.putExtra("checkInDate", r.getFechaInicio());
                    intent.putExtra("checkOutDate", r.getFechaFin());
                    Log.d("ReservaAdapter", "Extras enviados: " + h.getName() + ", estado: " + r.getEstado());
                    context.startActivity(intent);
                });
            }
        }

    }

    static class PasadoViewHolder extends RecyclerView.ViewHolder {
        TextView tvHotelName, tvRoomDetails, tvStatus;
        CircleImageView profileImage;

        public PasadoViewHolder(View itemView) {
            super(itemView);
            tvHotelName = itemView.findViewById(R.id.tvHotelName);
            tvRoomDetails = itemView.findViewById(R.id.tvRoomDetails);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            profileImage = itemView.findViewById(R.id.profileImage);
        }

        void bind(Reserva r,  Hotel h) {
            tvHotelName.setText(h.getName() + " - " + h.getDireccion());
            tvRoomDetails.setText(r.getHabitaciones() + " hab., " + r.getAdultos() + " adultos, " + r.getNinos() + " niños");
            tvStatus.setText("Estado: Finalizado");
            profileImage.setImageResource(h.getImageResourceId());
            Log.d("ReservaAdapter", "Extras enviados: " + h.getName() + ", estado: " + r.getEstado());
        }
    }
    static class CanceladoViewHolder extends RecyclerView.ViewHolder {
        TextView tvHotelName, tvRoomDetails, tvStatus;
        CircleImageView profileImage;

        public CanceladoViewHolder(View itemView) {
            super(itemView);
            tvHotelName = itemView.findViewById(R.id.tvHotelName);
            tvRoomDetails = itemView.findViewById(R.id.tvRoomDetails);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            profileImage = itemView.findViewById(R.id.profileImage);
        }

        void bind(Reserva r,  Hotel h) {
            tvHotelName.setText(h.getName() + " - " + h.getDireccion());
            tvRoomDetails.setText(r.getHabitaciones() + " hab., " + r.getAdultos() + " adultos, " + r.getNinos() + " niños");
            tvStatus.setText("Estado: Cancelado");
            profileImage.setImageResource(h.getImageResourceId());
            Log.d("ReservaAdapter", "Extras enviados: " + h.getName() + ", estado: " + r.getEstado());
        }
    }
}
