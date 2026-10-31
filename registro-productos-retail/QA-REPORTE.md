# Reporte QA — Sistema Retail (2026-10-30)

Tres testers independientes (sin conocimiento previo de la app) probaron cada vista ejecutando la app real
contra MySQL local, con datos de prueba aislados (`QA-CRUD-*`, `QA-CAJA-*`). Al final la base quedó idéntica al
respaldo previo (productos `12` y `20` sin cambios).

## Resumen

| Vista | Casos | Fallas alta | media | baja |
|---|---|---|---|---|
| Login | 24 | 1 | 4 | 4 |
| CRUD productos | 33 | 1 | 7 | 5 |
| Sistema de Caja | 45 | 4 | 3 | 2 |

## Severidad alta (corregir primero)

| # | Vista | Bug | Causa (código) |
|---|---|---|---|
| A1 | CRUD | **Actualizar falla siempre** si no se reescribe el precio: la tabla muestra `1500,50` (locale es_CL) y al seleccionar la fila ese texto vuelve al campo; `parseDouble` lo rechaza. Salen 2 diálogos y no actualiza. | `RegistroProductosRetail.java:103` `String.format("$%.2f")` usa el locale del sistema |
| A2 | Caja | **Venta con dinero `NaN` o `Infinity`**: "Venta Exitosa", vuelto `$NaN`, stock descontado. | `SistemaCaja.java:188,202` `parseDouble` acepta NaN y `NaN < total` es false |
| A3 | Caja | **Error de redondeo**: 3 × 0,10 pagando 0.30 exacto da "insuficiente. Total: $0.30000000000000004". | Montos en `double` |
| A4 | Caja | **Venta aplicada al producto equivocado**: buscar A y pegar el código H con el mouse (sin teclas) cobra A pero escribe en H el stock calculado de A. | Vende por el texto del campo, no por el producto buscado |
| A5 | Caja | **Stock incorrecto con 2 cajas** (lost update): escribe un stock absoluto calculado del caché. BD 2, vender 5: stock queda 5. | `SistemaCaja.java:210` `SET stock = ?` en vez de `stock = stock - ?` |
| A6 | Login | **Credenciales `admin`/`1234` dentro del programa**: legibles en `Login.class` y en el `.exe` distribuido. | `Login.java:46` |

## Severidad media

- **Login**:
  - Acepta la contraseña con espacios, tab o salto de línea alrededor (`trim()` en `Login.java:43`).
  - Enter no inicia sesión.
  - No hay límite de intentos.
  - Entra al CRUD aunque la BD falle; el CRUD abre vacío.
- **CRUD**:
  - Mensaje engañoso: todo error de BD al guardar dice "(código duplicado)", aunque sea texto muy largo o precio fuera de rango.
  - Precio `0.001` pasa la validación y queda `0.00` en la BD.
  - Actualizar o eliminar un código inexistente dice "correctamente".
  - Eliminar no exige una fila seleccionada: basta tipear un código, incluso de un producto real.
  - Una categoría que no está en la lista se reemplaza en silencio por la anterior.
  - Las celdas de la tabla son editables y lo editado no se guarda.
  - Al actualizar con un error, salen 2 diálogos ("Seleccione un producto" aunque hay fila seleccionada).
- **Caja**:
  - Venta "exitosa" si el producto fue borrado en la BD después de buscarlo.
  - Cobra el precio viejo si cambió en la BD después de buscar.
  - Enter no cobra.

## Severidad baja

- **Formato de montos**: el formato mezcla coma y punto (`$0.00`, `$1000,00`, ticket `$800.0`), y los campos de entrada no aceptan coma.
- **Validaciones**:
  - Total negativo visible con cantidad negativa.
  - Se acepta `"800d"` y `1e3` como número.
  - Login sin aviso de campos vacíos.
- **Detalles de UI**:
  - El diálogo de bienvenida tiene título "Message".
  - Tras un error de login, la contraseña sigue escrita.
  - "Guardar" sigue activo con una fila seleccionada.
- **Base de datos**:
  - La collation `ai_ci` trata `Ñ` y `N` como iguales en `codigo`.
  - `SELECT *` sin `ORDER BY`.
  - La Caja abre una conexión por cada tecla en el campo código.
  - Errores de búsqueda que solo se imprimen en la consola.
  - No se guarda un registro de ventas: solo se descuenta stock.

## Correcciones aplicadas (2026-10-30)

Contexto de uso: 1 PC, 1 caja, 1 administrador, usuarias sin conocimientos técnicos; el dinero recibido
(efectivo o tarjeta) lo anota la cajera.

| Bug | Estado |
|---|---|
| A1 Actualizar con precio `1.500,50` | Corregido: montos con formato chileno (`$1.500`, `$1.500,50`) que se leen de vuelta (`Moneda.java`) |
| A2 Dinero `NaN`/`Infinity`/`800d`/`1e3` | Corregido: solo se aceptan dígitos, `.`, `,` y `$` |
| A3 Redondeo `double` | Corregido: `BigDecimal` en precio, total y vuelto |
| A4 Venta al producto equivocado | Corregido: al cobrar se vuelve a buscar el código escrito |
| A5 Stock con cambios simultáneos | Corregido: `stock = stock - ?` solo si alcanza (también protege si se edita el stock en el CRUD con la caja abierta) |
| A6 Credenciales en el programa | **No se cambia**: un solo administrador, usuarias sin acceso al código |
| CRUD medias | Corregidas: mensajes claros (código repetido, largo, rango, decimales), éxito falso, eliminar exige selección, categoría desconocida, tabla no editable, doble diálogo, selección con teclado, Guardar desactivado con fila elegida |
| Caja medias | Corregidas: producto borrado, precio desactualizado, Enter (código → cantidad → dinero → cobrar) |
| Login | Corregidos: Enter inicia sesión, aviso de campos vacíos, contraseña se borra tras error, título del diálogo |
| No se cambian | Límite de intentos (1 PC, 1 persona); `trim()` de contraseña (más fácil para la usuaria); registro de ventas (no pedido) |

Nota: con formato chileno, `1.999` se lee como mil novecientos noventa y nueve pesos.

Verificación: `MonedaTest` 29/29, los 3 harness QA re-ejecutados con el código nuevo, BD idéntica al respaldo al final.

## Evidencia

Harness y salidas de cada tester en la carpeta temporal de la sesión de Claude Code (`scratchpad\qa\login|crud|caja`).
