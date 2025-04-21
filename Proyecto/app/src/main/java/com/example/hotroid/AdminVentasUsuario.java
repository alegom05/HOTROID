package com.example.hotroid;

import android.content.Intent;
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
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AdminVentasUsuario extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_ventas_usuario);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        CardView cardAdmin = findViewById(R.id.cardAdmin);
        cardAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(AdminVentasUsuario.this, AdminCuentaActivity.class);
            startActivity(intent);
        });
        Button btnGenerarPdf = findViewById(R.id.btnGenerarPdf);

        btnGenerarPdf.setOnClickListener(v -> {
            try {
                Document document = new Document(PageSize.A4);
                File pdfDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!pdfDir.exists()) pdfDir.mkdir();

                File file = new File(pdfDir, "Venta_por_Usuarios.pdf");
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                // Estilos
                Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
                Font headerFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
                Font cellFont = new Font(Font.FontFamily.HELVETICA, 12);

                // Título centrado
                Paragraph title = new Paragraph("Venta por usuarios", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(20f);
                document.add(title);

                // Tabla
                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{3f, 2f});
                table.setSpacingBefore(10f);

                // Encabezados
                table.addCell(createCenteredCell("Cliente", headerFont));
                table.addCell(createCenteredCell("Monto total (S/.)", headerFont));

                // Datos
                table.addCell(createCenteredCell("Alonso Correa", cellFont));
                table.addCell(createCenteredCell("2235.00", cellFont));

                table.addCell(createCenteredCell("Robert Ramos", cellFont));
                table.addCell(createCenteredCell("1821.20", cellFont));

                table.addCell(createCenteredCell("Rubén Cancho", cellFont));
                table.addCell(createCenteredCell("1532.50", cellFont));

                table.addCell(createCenteredCell("Gumercindo Bartra", cellFont));
                table.addCell(createCenteredCell("1200.00", cellFont));

                document.add(table);
                document.close();

                Toast.makeText(this, "PDF generado en: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

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
                Intent intentInicio = new Intent(AdminVentasUsuario.this, AdminActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (item.getItemId() == R.id.nav_taxistas) {
                Intent intentUbicacion = new Intent(AdminVentasUsuario.this, AdminTaxistas.class);
                startActivity(intentUbicacion);
                return true;
            } else if (item.getItemId() == R.id.nav_checkout) {
                Intent intentAlertas = new Intent(AdminVentasUsuario.this, AdminCheckout.class);
                startActivity(intentAlertas);
                return true;
            } else if (item.getItemId() == R.id.nav_reportes) {
                Intent intentAlertas = new Intent(AdminVentasUsuario.this, AdminReportes.class);
                startActivity(intentAlertas);
                return true;
            } else {
                return false;
            }
        });
    }
    private PdfPCell createCenteredCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }
}