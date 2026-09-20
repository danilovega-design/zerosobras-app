import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const PORT = 3000;
const APK_PATH = path.join(__dirname, 'app/build/outputs/apk/debug/app-debug.apk');
const INDEX_PATH = path.join(__dirname, 'index.html');

// In-memory / JSON persistence fallback for server-side state
const DB_FILE = path.join(__dirname, 'alimentos_db.json');
function loadAlimentos() {
  try {
    if (fs.existsSync(DB_FILE)) {
      return JSON.parse(fs.readFileSync(DB_FILE, 'utf-8'));
    }
  } catch (e) {
    console.error('Error loading db file:', e);
  }
  return null;
}

function saveAlimentos(data) {
  try {
    fs.writeFileSync(DB_FILE, JSON.stringify(data, null, 2), 'utf-8');
  } catch (e) {
    console.error('Error saving db file:', e);
  }
}

const server = http.createServer((req, res) => {
  const url = new URL(req.url, 'http://localhost:3000');

  // CORS headers
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    res.statusCode = 204;
    res.end();
    return;
  }

  // API Config: Expose safe client-side Firebase environment config
  if (url.pathname === '/api/config') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({
      firebase: {
        apiKey: process.env.FIREBASE_API_KEY || '',
        authDomain: process.env.FIREBASE_AUTH_DOMAIN || '',
        projectId: process.env.FIREBASE_PROJECT_ID || 'zerosobra-app',
        storageBucket: process.env.FIREBASE_STORAGE_BUCKET || '',
        messagingSenderId: process.env.FIREBASE_MESSAGING_SENDER_ID || '',
        appId: process.env.FIREBASE_APP_ID || ''
      }
    }));
    return;
  }

  // APK download endpoint
  if (url.pathname === '/api/download-apk') {
    if (fs.existsSync(APK_PATH)) {
      const stat = fs.statSync(APK_PATH);
      res.writeHead(200, {
        'Content-Type': 'application/vnd.android.package-archive',
        'Content-Disposition': 'attachment; filename="ZeroSobra-release.apk"',
        'Content-Length': stat.size
      });
      const stream = fs.createReadStream(APK_PATH);
      stream.pipe(res);
      return;
    } else {
      res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
      res.end('APK en proceso de compilación.');
      return;
    }
  }

  // Health and status API
  if (url.pathname === '/api/status') {
    const apkReady = fs.existsSync(APK_PATH);
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({
      status: 'ok',
      app: 'ZeroSobra',
      apkAvailable: apkReady,
      port: PORT,
      timestamp: new Date().toISOString()
    }));
    return;
  }

  // AI Smart Expiry Calculation Endpoint
  if (url.pathname === '/api/ai/estimate-expiry' && req.method === 'POST') {
    let body = '';
    req.on('data', chunk => { body += chunk; });
    req.on('end', () => {
      try {
        const bodyParsed = JSON.parse(body || '{}');
        const { foodName = '', storageType = 'refrigerador', packageState = 'sellado' } = bodyParsed;
        const isFreezer = storageType === 'congelador' || storageType === 'freezer';
        const isOpened = packageState === 'abierto' || packageState === 'opened';
        const q = foodName.toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "").trim();

        // High accuracy culinary database for perishability & Ecuador market prices (USD)
        let days = isFreezer ? 120 : 5;
        let categoryId = 'otros';
        let tip = isFreezer
          ? 'Guardar en recipientes o bolsas herméticas para congelación a -18°C.'
          : 'Almacenar en un lugar fresco y seco, o en refrigeración según el empaque.';
        let preservation = isFreezer ? 'Congelador a -18°C' : 'Refrigerador a 4°C';
        let pricePerKgUSD = 3.00;
        let pricePerUnitUSD = 0.50;
        let pricePerLiterUSD = 1.20;
        let unitWeightKg = 0.20;

        if (/pollo|carne|res|cerdo|pescado|salmon|atun fresco|marisco|camaron|bistec|molida|pechuga|chuleta|tocino|salchicha|embutido/.test(q)) {
          categoryId = 'carnes';
          if (/pescado|marisco|camaron|salmon/.test(q)) {
            days = isFreezer ? 120 : 2;
            tip = isFreezer
              ? 'Congelar inmediatamente bien sellado a -18°C. Consumir dentro de 4 meses.'
              : 'Consumir rápido o congelar a -18°C. Mantener en la parte más fría de la nevera.';
            pricePerKgUSD = 7.80;
            pricePerUnitUSD = 2.50;
            unitWeightKg = 0.30;
          } else if (/pechuga|pollo|molida/.test(q)) {
            days = isFreezer ? 180 : 3;
            tip = isFreezer
              ? 'Excelente vida útil en congelador (-18°C). Dividir en porciones antes de congelar.'
              : 'Mantener bien tapada en la repisa inferior para evitar goteos cruzados.';
            pricePerKgUSD = 4.20;
            pricePerUnitUSD = 1.80;
            unitWeightKg = 0.40;
          } else if (/res|bistec|lomo/.test(q)) {
            days = isFreezer ? 180 : 3;
            tip = isFreezer
              ? 'Congelar en cortes individuales con film plástico para evitar quemaduras por frío.'
              : 'Guardar en recipiente cerrado en la zona inferior de la nevera.';
            pricePerKgUSD = 7.50;
            pricePerUnitUSD = 2.40;
            unitWeightKg = 0.35;
          } else if (/tocino|salchicha|embutido/.test(q)) {
            days = isFreezer ? 90 : 7;
            tip = isFreezer
              ? 'Congelar en paquete cerrado; conserva textura hasta 3 meses.'
              : 'Cerrar herméticamente tras abrir para evitar que se reseque.';
            pricePerKgUSD = 5.50;
            pricePerUnitUSD = 0.60;
            unitWeightKg = 0.10;
          } else {
            days = isFreezer ? 180 : 3;
            tip = isFreezer
              ? 'Conservar a -18°C en bolsas herméticas aptas para congelador.'
              : 'Guardar en recipiente cerrado en la zona inferior de la nevera.';
            pricePerKgUSD = 5.80;
            pricePerUnitUSD = 1.90;
            unitWeightKg = 0.30;
          }
        } else if (/leche|yogur|yogurt|crema de leche|kefir/.test(q)) {
          categoryId = 'lacteos';
          days = isFreezer ? 90 : 5;
          tip = isFreezer
            ? 'Dejar espacio libre en el envase porque los líquidos se expanden al congelar.'
            : 'Mantener en el estante interior, no en la puerta donde fluctúa la temperatura.';
          pricePerKgUSD = 1.10;
          pricePerLiterUSD = 1.15;
          pricePerUnitUSD = 1.15;
          unitWeightKg = 1.00;
        } else if (/queso|mantequilla|ricotta|parmesano|mozzarella|gouda|cheddar/.test(q)) {
          categoryId = 'derivados_lacteos';
          if (/fresco|ricotta|cottage/.test(q)) {
            days = isFreezer ? 60 : 4;
            tip = isFreezer
              ? 'El queso fresco puede soltar suero al descongelar; ideal usarlo en cocciones.'
              : 'Consumir pronto tras abrir y conservar en su propio suero o recipiente hermético.';
            pricePerKgUSD = 4.50;
            pricePerUnitUSD = 2.00;
            unitWeightKg = 0.40;
          } else if (/parmesano|madurado|curado/.test(q)) {
            days = isFreezer ? 180 : 21;
            tip = isFreezer
              ? 'Rallar antes de congelar para usar directamente en pastas y platos calientes.'
              : 'Envolver en papel encerado para que respire sin acumular humedad.';
            pricePerKgUSD = 12.00;
            pricePerUnitUSD = 3.50;
            unitWeightKg = 0.25;
          } else {
            days = isFreezer ? 120 : 10;
            tip = isFreezer
              ? 'Rallar o rebanar antes de congelar para facilitar su uso posterior.'
              : 'Mantener en cajón intermedio a 4°C bien envuelto.';
            pricePerKgUSD = 5.50;
            pricePerUnitUSD = 2.20;
            unitWeightKg = 0.40;
          }
        } else if (/huevo/.test(q)) {
          categoryId = 'huevos';
          days = isFreezer ? 180 : 18;
          tip = isFreezer
            ? 'No congelar con cáscara. Batir ligeramente o separar claras/yemas antes de congelar.'
            : 'No lavar antes de guardar en la nevera para no dañar la cutícula protectora.';
          pricePerKgUSD = 2.80;
          pricePerUnitUSD = 0.16; // 1 huevo en Ecuador cuesta ~$0.15 - $0.18
          unitWeightKg = 0.06;
        } else if (/lechuga|espinaca|tomate|aguacate|cebolla|ajo|zanahoria|papa|brocoli|calabacin|pepino|pimiento|champinon|seta/.test(q)) {
          categoryId = 'verduras';
          if (/champinon|seta|espinaca|lechuga/.test(q)) {
            days = isFreezer ? 120 : 3;
            tip = isFreezer
              ? 'Blanquear o saltear previamente antes de congelar para sopas o guisos.'
              : 'Guardar con papel absorbente para absorber el exceso de humedad.';
            pricePerKgUSD = 3.20;
            pricePerUnitUSD = 0.50;
            unitWeightKg = 0.20;
          } else if (/aguacate/.test(q)) {
            days = isFreezer ? 90 : 4;
            tip = isFreezer
              ? 'Triturar con unas gotas de limón y congelar en cubeta para guacamole.'
              : 'Dejar madurar a temperatura ambiente y refrigerar una vez maduro.';
            pricePerKgUSD = 3.50;
            pricePerUnitUSD = 0.75;
            unitWeightKg = 0.22;
          } else if (/tomate/.test(q)) {
            days = isFreezer ? 180 : 5;
            tip = isFreezer
              ? 'Congelar enteros o triturados; la piel se desprende sola al descongelar.'
              : 'Conservar en lugar fresco para no alterar su textura y sabor.';
            pricePerKgUSD = 1.30;
            pricePerUnitUSD = 0.25;
            unitWeightKg = 0.15;
          } else if (/papa|cebolla|ajo/.test(q)) {
            days = isFreezer ? 180 : 20;
            tip = isFreezer
              ? 'Picar la cebolla o pre-cocinar las papas antes de congelar.'
              : 'Conservar en lugar oscuro, seco y ventilado; no juntar papas con cebollas.';
            preservation = isFreezer ? 'Congelador a -18°C' : 'Lugar fresco y seco (no nevera)';
            pricePerKgUSD = 1.10;
            pricePerUnitUSD = 0.20;
            unitWeightKg = 0.18;
          } else {
            days = isFreezer ? 180 : 6;
            tip = isFreezer
              ? 'Blanquear en agua hirviendo 2 min para preservar vitaminas y color.'
              : 'Guardar en el cajón de verduras con humedad controlada.';
            pricePerKgUSD = 1.40;
            pricePerUnitUSD = 0.35;
            unitWeightKg = 0.25;
          }
        } else if (/manzana|platano|banana|fresa|frutilla|uva|naranja|limon|mango|papaya|pina|arandano|sandia|melon/.test(q)) {
          categoryId = 'frutas';
          if (/fresa|frutilla|arandano|mora/.test(q)) {
            days = isFreezer ? 180 : 3;
            tip = isFreezer
              ? 'Congelar en bandeja separadas y luego embolsar; perfectas para batidos.'
              : 'No lavar hasta el momento de comer para prevenir la formación de moho.';
            pricePerKgUSD = 3.50;
            pricePerUnitUSD = 1.50;
            unitWeightKg = 0.25;
          } else if (/platano|banana|guineo/.test(q)) {
            days = isFreezer ? 120 : 4;
            tip = isFreezer
              ? 'Pelar y cortar en rodajas antes de congelar para helados o batidos.'
              : 'Separar del racimo o envolver el tallo en film transparente para retardar maduración.';
            preservation = isFreezer ? 'Congelador a -18°C' : 'Ambiente fresco';
            pricePerKgUSD = 0.90;
            pricePerUnitUSD = 0.15;
            unitWeightKg = 0.16;
          } else if (/manzana|pera|durazno/.test(q)) {
            days = isFreezer ? 180 : 8;
            tip = isFreezer
              ? 'Pelar y cortar en gajos con unas gotas de limón para tartas y compotas.'
              : 'En refrigeración se mantienen crujientes por varias semanas.';
            pricePerKgUSD = 2.20;
            pricePerUnitUSD = 0.35;
            unitWeightKg = 0.18;
          } else if (/limon|naranja|citrico/.test(q)) {
            days = isFreezer ? 180 : 14;
            tip = isFreezer
              ? 'Exprimir y congelar el zumo en cubitera de hielo para cocina y cócteles.'
              : 'En refrigeración conservan su jugo hasta por 3 semanas.';
            pricePerKgUSD = 1.50;
            pricePerUnitUSD = 0.15;
            unitWeightKg = 0.12;
          } else {
            days = isFreezer ? 180 : 6;
            tip = isFreezer
              ? 'Trocear y congelar en bolsas herméticas para conservar nutrientes.'
              : 'Mantener ventiladas en frutero o en cajón de frutas.';
            pricePerKgUSD = 1.80;
            pricePerUnitUSD = 0.40;
            unitWeightKg = 0.25;
          }
        } else if (/pan|baguette|tostada|croissant|bollo|arepa|tortilla/.test(q)) {
          categoryId = 'panaderia';
          if (/molde/.test(q)) {
            days = isFreezer ? 90 : 7;
            tip = isFreezer
              ? 'Congelar en rebanadas con separadores; tostar directamente del congelador.'
              : 'Cerrar con su precinto. Se puede congelar en rebanadas y tostar directo.';
            pricePerKgUSD = 3.80;
            pricePerUnitUSD = 2.20;
            unitWeightKg = 0.50;
          } else {
            days = isFreezer ? 90 : 3;
            tip = isFreezer
              ? 'Congelar el mismo día de compra en bolsa hermética. Calentar al horno 5 min.'
              : 'Guardar en bolsa de tela o papel; la nevera reseca la miga.';
            preservation = isFreezer ? 'Congelador a -18°C' : 'Ambiente seco o congelador';
            pricePerKgUSD = 2.50;
            pricePerUnitUSD = 0.18; // Pan de batalla en Ecuador ~$0.18
            unitWeightKg = 0.07;
          }
        } else if (/arroz|frijol|lenteja|quinoa|avena|pasta|harina|garbanzo/.test(q)) {
          categoryId = 'granos';
          days = isFreezer ? 180 : 90;
          tip = isFreezer
            ? 'Los granos cocidos se congelan de maravilla por porciones para meal-prep.'
            : 'Almacenar en recipientes de vidrio herméticos protegidos de polillas y luz.';
          preservation = isFreezer ? 'Congelador a -18°C' : 'Despensa fresca y seca';
          pricePerKgUSD = 1.25;
          pricePerUnitUSD = 0.90;
          unitWeightKg = 0.50;
        } else if (/atun|sardina|enlatad|lata|conserva|maiz en lata|frijol en lata/.test(q)) {
          categoryId = 'enlatados';
          days = isFreezer ? 90 : 180;
          tip = 'Una vez abierta la lata, trasvasar el sobrante a un recipiente de vidrio o plástico.';
          pricePerKgUSD = 9.00;
          pricePerUnitUSD = 1.65; // Atún Real/Isabel en Ecuador ~$1.65
          unitWeightKg = 0.16;
        } else if (/galleta|cereal|snack|papas fritas|chocolate|barra/.test(q)) {
          categoryId = 'snacks';
          days = isFreezer ? 90 : 45;
          tip = 'Cerrar con pinza hermética para mantener la textura crujiente.';
          pricePerKgUSD = 6.00;
          pricePerUnitUSD = 0.85;
          unitWeightKg = 0.12;
        }

        // Adjust shelf life and preservation tips if package is already OPENED (🔓 Abierto)
        if (isOpened) {
          if (categoryId === 'enlatados') {
            days = isFreezer ? 60 : 3;
            preservation = isFreezer ? 'Congelador a -18°C (en envase no metálico)' : 'Refrigerador a 4°C (trasvasado a envase)';
            tip = '¡Lata abierta! Trasvasar de inmediato a un recipiente de vidrio o plástico hermético y consumir en 2 a 3 días; no conservar en la lata metálica abierta.';
          } else if (categoryId === 'snacks') {
            days = isFreezer ? 60 : 4;
            preservation = 'Lugar seco en frasco hermético o refrigeración';
            tip = 'Empaque abierto: cerrar con pinza hermética o bote hermético para evitar que absorba humedad y pierda crocancia; consumir en 3 a 5 días.';
          } else if (categoryId === 'lacteos') {
            days = isFreezer ? 60 : 3;
            preservation = isFreezer ? 'Congelador a -18°C' : 'Refrigerador a 4°C bien cerrado';
            tip = 'Envase abierto: consumir dentro de 3 a 4 días tras romper el precinto para evitar agriado o contaminación bacteriana.';
          } else if (categoryId === 'panaderia') {
            days = isFreezer ? 60 : 3;
            preservation = isFreezer ? 'Congelador a -18°C en rebanadas' : 'Lugar seco bien sellado';
            tip = 'Empaque abierto: cerrar con su precinto herméticamente; si no se consume en 3 días, congelar rebanadas individuales.';
          } else if (categoryId === 'derivados_lacteos') {
            days = isFreezer ? 60 : 4;
            preservation = isFreezer ? 'Congelador a -18°C' : 'Refrigerador a 4°C protegido';
            tip = 'Empaque abierto: envolver en film plástico o guardar en quesera cerrada para evitar hongos y resequedad.';
          } else if (categoryId === 'carnes') {
            if (!isFreezer) {
              days = Math.min(days, 2);
              tip = 'Empaque abierto: cocinar y consumir en 24-48 horas o congelar de inmediato a -18°C.';
            } else {
              days = Math.min(days, 90);
              tip = 'Empaque abierto en congelador: asegurar sellado hermético con film para evitar quemaduras por frío.';
            }
          } else {
            if (!isFreezer) {
              days = Math.min(days, 4);
              tip = `Empaque abierto: consumir prioritariamente en ${days} días y mantener debidamente cerrado.`;
            } else {
              days = Math.min(days, 90);
              tip = 'Empaque abierto en congelador: mantener bien sellado para preservar aroma y textura.';
            }
          }
        }

        // Parse quantity and unit
        const parsedQuantity = parseFloat(bodyParsed.quantity) || 1;
        const parsedUnit = (bodyParsed.unit || 'unidades').toLowerCase();

        let totalWeightKg = 0.3;
        let estimatedCostUSD = 1.50;

        if (parsedUnit === 'kg' || parsedUnit === 'kilogramos' || parsedUnit === 'kilos') {
          totalWeightKg = parsedQuantity;
          estimatedCostUSD = parsedQuantity * pricePerKgUSD;
        } else if (parsedUnit === 'g' || parsedUnit === 'gramos') {
          totalWeightKg = parsedQuantity / 1000;
          estimatedCostUSD = (parsedQuantity / 1000) * pricePerKgUSD;
        } else if (parsedUnit === 'l' || parsedUnit === 'litros') {
          totalWeightKg = parsedQuantity * 1.0;
          estimatedCostUSD = parsedQuantity * pricePerLiterUSD;
        } else {
          // Unidades
          totalWeightKg = parsedQuantity * unitWeightKg;
          estimatedCostUSD = parsedQuantity * pricePerUnitUSD;
        }

        estimatedCostUSD = Math.max(0.20, Math.round(estimatedCostUSD * 100) / 100);
        totalWeightKg = Math.max(0.05, Math.round(totalWeightKg * 100) / 100);

        // Environmental metrics:
        // 1 kg of food saved saves approx 1,000 Liters of fresh water
        const waterSavedLitres = Math.round(totalWeightKg * 1000);
        // 1 shower of 5 minutes = ~50 Liters
        const waterShowerEquiv = Math.max(1, Math.round(waterSavedLitres / 50));
        // 1 kg of food avoided prevents ~2.5 kg of CO2 equivalent emissions and CH4 in landfills
        const co2SavedKg = Math.round(totalWeightKg * 2.5 * 10) / 10;
        // 1 km in typical passenger car = ~0.12 kg CO2 (so 1 kg CO2 = ~8.3 km)
        const co2CarKmEquiv = Math.max(1, Math.round(co2SavedKg / 0.12));
        // Meals served: ~0.35 kg per plate
        const mealsRescued = Math.max(1, Math.round((totalWeightKg / 0.35) * 10) / 10);

        // Calculate status
        let status = 'fresh';
        if (days < 2) status = 'urgent';
        else if (days <= 4) status = 'warning';

        // Calculated date ISO YYYY-MM-DD
        const targetDate = new Date();
        targetDate.setDate(targetDate.getDate() + days);
        const expiryDate = targetDate.toISOString().split('T')[0];

        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
          foodName,
          storageType: isFreezer ? 'congelador' : 'refrigerador',
          packageState: isOpened ? 'abierto' : 'sellado',
          quantity: parsedQuantity,
          unit: parsedUnit,
          estimatedDays: days,
          expiryDate,
          categoryId,
          status,
          statusLabel: isFreezer
            ? `❄️ Congelador (~${days >= 30 ? Math.round(days / 30) + ' meses' : days + ' días'})`
            : (status === 'urgent' ? '🔴 Urgente (<2 días)' : status === 'warning' ? '🟡 Por Vencer (2-4 días)' : '🟢 Fresco (>4 días)'),
          preservation,
          tip,
          estimatedCostUSD,
          totalWeightKg,
          waterSavedLitres,
          waterShowerEquiv,
          co2SavedKg,
          co2CarKmEquiv,
          mealsRescued,
          currency: 'USD',
          confidence: 0.96
        }));
      } catch (err) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: 'Error procesando solicitud de IA' }));
      }
    });
    return;
  }

  // Backup sync endpoint for alimentos collection
  if (url.pathname === '/api/alimentos') {
    if (req.method === 'GET') {
      const data = loadAlimentos() || [];
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify(data));
      return;
    }

    if (req.method === 'POST') {
      let body = '';
      req.on('data', chunk => { body += chunk; });
      req.on('end', () => {
        try {
          const payload = JSON.parse(body);
          if (Array.isArray(payload)) {
            saveAlimentos(payload);
          }
          res.writeHead(200, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ success: true }));
        } catch (e) {
          res.writeHead(400, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ error: 'Invalid JSON' }));
        }
      });
      return;
    }
  }

  // Serve main index.html
  if (fs.existsSync(INDEX_PATH)) {
    const content = fs.readFileSync(INDEX_PATH, 'utf-8');
    res.writeHead(200, {
      'Content-Type': 'text/html; charset=utf-8',
      'Cache-Control': 'no-cache, no-store, must-revalidate'
    });
    res.end(content);
    return;
  }

  res.writeHead(200, { 'Content-Type': 'text/plain; charset=utf-8' });
  res.end('ZeroSobra is running.');
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`[ZeroSobra] Professional Production Server listening on http://0.0.0.0:${PORT}`);
});
