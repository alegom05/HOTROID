const admin = require("firebase-admin");
const fs = require("fs");
const path = require("path");

// Cargar las credenciales
const serviceAccount = require("./credenciales.json"); // usa el archivo válido

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  projectId: serviceAccount.project_id
});

const db = admin.firestore();

async function exportarColecciones() {
  const collections = await db.listCollections();

  for (const col of collections) {
    const colId = col.id;
    const snapshot = await col.get();

    const documentos = snapshot.docs.map(doc => ({
      id: doc.id,
      ...doc.data()
    }));

    const filePath = path.join(__dirname, `${colId}.json`);
    fs.writeFileSync(filePath, JSON.stringify(documentos, null, 2));

    console.log(`✅ Exportado: ${colId} → ${filePath}`);
  }

  console.log("🎉 Exportación completada.");
}

exportarColecciones().catch(console.error);
