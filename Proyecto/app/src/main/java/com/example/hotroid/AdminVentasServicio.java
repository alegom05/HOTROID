package com.example.hotroid;

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

                // Estilos de fuente
                Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
                Font headerFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
                Font cellFont = new Font(Font.FontFamily.HELVETICA, 12);

                // Título centrado
                Paragraph title = new Paragraph("Venta de Servicios", titleFont);
                title.setAlignment(Paragraph.ALIGN_CENTER);
                title.setSpacingAfter(20f);
                document.add(title);

                // Tabla con 3 columnas
                PdfPTable table = new PdfPTable(3);
                table.setWidthPercentage(100); // que ocupe todo el ancho
                table.setWidths(new float[]{3f, 2f, 3f}); // distribución de columnas
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

                // Mostrar mensaje con ubicación
                Toast.makeText(AdminVentasServicio.this, "PDF Generado en: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

            } catch (DocumentException | IOException e) {
                e.printStackTrace();
                Toast.makeText(AdminVentasServicio.this, "Error generando PDF", Toast.LENGTH_SHORT).show();
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
}