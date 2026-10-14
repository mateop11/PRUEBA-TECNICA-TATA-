# Reto técnico TCS – Backend Java Spring Boot (microservicios)

Solución al ejercicio de arquitectura de microservicios. Está hecha al nivel **Senior** (F1–F7), que incluye
todo lo pedido para Junior y Semi Senior: dos microservicios con comunicación asincrónica, reporte, pruebas
unitarias y de integración, y despliegue en Docker.

## Arquitectura

```
 Postman ──► customer-service :8081 ──(evento cliente.*)──► RabbitMQ ──► account-service :8082 ◄── Postman
              Persona, Cliente                                            Cuenta, Movimiento, Reportes
              BD: customer_db                                             BD: account_db
```

- **customer-service**: `Persona` (clase base) y `Cliente` (hereda de Persona, herencia JPA `JOINED`).
- **account-service**: `Cuenta`, `Movimiento` y el reporte de estado de cuenta.
- **Comunicación asincrónica**: cada vez que un cliente se crea, actualiza o elimina, customer-service publica un
  evento en RabbitMQ. account-service lo consume y mantiene una copia mínima (`cliente_replica`), así valida
  clientes y arma reportes sin llamadas HTTP entre servicios. Una cuenta nunca depende de que customer-service
  esté arriba.
- **Una base por servicio** (PostgreSQL, dos databases en el mismo contenedor para simplificar).

## Cómo ejecutarlo

Requisitos: Docker y Docker Compose.

```bash
docker compose up --build
```

- customer-service: http://localhost:8081/swagger-ui.html
- account-service: http://localhost:8082/swagger-ui.html
- RabbitMQ (consola): http://localhost:15672 (guest / guest)

`BaseDatos.sql` se ejecuta automáticamente al crear el volumen de Postgres. Para reiniciar desde cero:
`docker compose down -v`.

Pruebas con Postman: importar `postman/Reto_TCS.postman_collection.json` y correr la colección completa
(Collection Runner) en orden. Reproduce los casos de uso del enunciado (usuarios, cuentas, movimientos y reporte).

Pruebas automáticas (Java 17 + Maven):

```bash
cd customer-service && mvn test
cd ../account-service && mvn test
```

## Endpoints

| Servicio | Verbo y ruta | Descripción |
|---|---|---|
| customer | `GET/POST /api/clientes`, `GET/PUT/DELETE /api/clientes/{clienteId}`, `PATCH /api/clientes/{clienteId}/estado` | CRUD de clientes |
| account | `GET/POST /api/cuentas`, `GET/PUT/DELETE /api/cuentas/{numeroCuenta}`, `PATCH .../estado` | CRUD de cuentas |
| account | `GET/POST /api/movimientos`, `GET/PUT/DELETE /api/movimientos/{id}` | Movimientos |
| account | `GET /api/reportes?fecha=2022-02-01,2022-02-28&clienteId=mmontalvo` | Estado de cuenta (JSON) |

### Movimientos (F2, F3)
- `valor` positivo = depósito, negativo = retiro. El tipo se deriva del signo (opcionalmente se puede enviar `tipoMovimiento`).
- El saldo disponible se actualiza en la cuenta y queda guardado en cada movimiento.
- Si un retiro supera el saldo: HTTP 400 con `"message": "Saldo no disponible"`.
- `fecha` es opcional (por defecto, ahora); sirve para cargar los datos históricos del enunciado.
- Para no romper la cadena de saldos, solo se puede modificar o eliminar el **último** movimiento de una cuenta.

### Reporte (F4)
`fecha` recibe `yyyy-MM-dd,yyyy-MM-dd` (ambos extremos inclusive) y `clienteId` identifica al cliente. Devuelve
las cuentas con su saldo y el detalle de movimientos del rango, más totales de créditos y débitos:

```json
{
  "clienteId": "mmontalvo", "cliente": "Marianela Montalvo",
  "desde": "2022-02-01", "hasta": "2022-02-28",
  "cuentas": [{
    "numeroCuenta": "225487", "tipoCuenta": "Corriente", "estado": true,
    "saldoInicial": 100.00, "saldoDisponible": 700.00, "totalCreditos": 600.00, "totalDebitos": 0,
    "movimientos": [{ "fecha": "2022-02-10T10:00:00", "tipoMovimiento": "Deposito", "valor": 600.00, "saldo": 700.00 }]
  }]
}
```

### Errores
Un `@RestControllerAdvice` por servicio devuelve siempre el mismo formato
(`timestamp, status, error, message, path, details`): 400 validación/negocio, 404 no encontrado, 409 duplicado, 500 genérico sin filtrar detalles internos.

## Decisiones técnicas (para la entrevista)

- **Patrones**: Repository (Spring Data), DTOs con `record`, capa de servicio transaccional, lógica de negocio en el dominio (`Cuenta.aplicarMovimiento`), manejo centralizado de excepciones.
- **Concurrencia**: el movimiento bloquea la fila de la cuenta (`PESSIMISTIC_WRITE`) para que dos retiros simultáneos no dejen saldo negativo.
- **Publicación segura de eventos**: se envían con `@TransactionalEventListener(AFTER_COMMIT)`, es decir, solo si la transacción de BD se confirmó.
- **Resiliencia**: consumidor idempotente, reintentos (3, con backoff) y cola de errores (DLQ) para mensajes que fallen.
- **Contraseñas**: se guardan con BCrypt y nunca se devuelven en las respuestas.
- **Escalabilidad**: servicios sin estado (se pueden replicar detrás de un balanceador), cada uno con su base; la lectura de cuentas/reportes no depende de customer-service.
- **Tipos de cuenta**: el enunciado usa "Ahorro" y "Ahorros"; ambos se aceptan y se normalizan.

## Limitaciones conocidas / mejoras
- Consistencia eventual: justo después de crear un cliente, la cuenta puede tardar milisegundos en poder asociarse (Postman incluye una espera corta).
- Si RabbitMQ está caído al crear un cliente, el evento se pierde (se registra el error). La mejora productiva es el patrón **Outbox**.
- Si account-service se levanta después de que ya existían clientes, necesita una carga inicial (snapshot) o reenvío de eventos.
- Falta API Gateway, autenticación en los endpoints y trazabilidad distribuida, que serían los siguientes pasos.
- `ddl-auto: update` facilita el arranque; en producción se usarían migraciones (Flyway) con `validate`.
