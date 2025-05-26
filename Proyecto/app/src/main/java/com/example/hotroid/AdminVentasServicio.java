package com.example.hotroid;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Document;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AdminVentasServicio extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_ventas_servicio);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        CardView cardAdmin = findViewById(R.id.cardAdmin);
        cardAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(AdminVentasServicio.this, AdminCuentaActivity.class);
            startActivity(intent);
        });
        Button btnGenerarPdf = findViewById(R.id.btnGenerarPdf);

        btnGenerarPdf.setOnClickListener(v -> {
            try {
                Document document = new Document(PageSize.A4);
                File pdfDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!pdfDir.exists()) {
                    pdfDir.mkdir();
                }

                File file = new File(pdfDir, "Venta_de_Servicios.pdf");
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                // Estilos
                Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
                Font headerFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
                Font cellFont = new Font(Font.FontFamily.HELVETICA, 12);

                // Título
                Paragraph title = new Paragraph("Venta de Servicios", titleFont);
                title.setAlignment(Paragraph.ALIGN_CENTER);
                title.setSpacingAfter(20f);
                document.add(title);

                // Tabla
                PdfPTable table = new PdfPTable(3);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{3f, 2f, 3f});
                table.setSpacingBefore(10f);

                table.addCell(createCenteredCell("Servicio", headerFont));
                table.addCell(createCenteredCell("Cantidad", headerFont));
                table.addCell(createCenteredCell("Monto Total (S/.)", headerFont));

                table.addCell(createCenteredCell("Spa y masajes", cellFont));
                table.addCell(createCenteredCell("2", cellFont));
                table.addCell(createCenteredCell("50.00", cellFont));

                table.addCell(createCenteredCell("Alquiler de autos", cellFont));
                table.addCell(createCenteredCell("3", cellFont));
                table.addCell(createCenteredCell("120.00", cellFont));

                table.addCell(createCenteredCell("Tópico médico", cellFont));
                table.addCell(createCenteredCell("6", cellFont));
                table.addCell(createCenteredCell("365.00", cellFont));

                table.addCell(createCenteredCell("Desayuno Buffet", cellFont));
                table.addCell(createCenteredCell("14", cellFont));
                table.addCell(createCenteredCell("2300.00", cellFont));

                document.add(table);
                document.close();

                // Abrir PDF automáticamente
                abrirPdf(file);

                // Mostrar notificación
                mostrarNotificacion(file);

            } catch (DocumentException | IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error generando PDF", Toast.LENGTH_SHORT).show();
            }
        });


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_reportes);


        // BottomNavigationView o Barra inferior de menú
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_registros) {
                Intent intentInicio = new Intent(AdminVentasServicio.this, AdminActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (item.getItemId() == R.id.nav_taxistas) {
                Intent intentUbicacion = new Intent(AdminVentasServicio.this, AdminTaxistas.class);
                startActivity(intentUbicacion);
                return true;
            } else if (item.getItemId() == R.id.nav_checkout) {
                Intent intentAlertas = new Intent(AdminVentasServicio.this, AdminCheckout.class);
                startActivity(intentAlertas);
                return true;
            } else if (item.getItemId() == R.id.nav_reportes) {
                Intent intentAlertas = new Intent(AdminVentasServicio.this, AdminReportes.class);
                startActivity(intentAlertas);
                return true;
            } else {
                return false;
            }
        });

    }
    private PdfPCell createCenteredCell(String content, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(content, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }
    private void abrirPdf(File file) {
        Uri uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                getApplicationContext().getPackageName() + ".provider",
                file
        );

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir el PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarNotificacion(File file) {
        String CHANNEL_ID = "pdf_channel";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Notificación PDF";
            String description = "Canal para notificaciones de PDFs generados";
            int importance = android.app.NotificationManager.IMPORTANCE_DEFAULT;
            android.app.NotificationChannel channel = new android.app.NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            android.app.NotificationManager notificationManager = getSystemService(android.app.NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        Uri uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                getApplicationContext().getPackageName() + ".provider",
                file
        );

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_hotroid_icon)
                .setContentTitle("PDF generado")
                .setContentText("Haz clic para abrir el PDF de ventas")
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        androidx.core.app.NotificationManagerCompat notificationManager = androidx.core.app.NotificationManagerCompat.from(this);
        notificationManager.notify(100, builder.build());
    }

}