# Carrera de Caballos (Baraja Espanola)

Juego web en Java + Spring Boot que automatiza la dinamica de carrera con cartas:

- Caballos: `Oro`, `Espada`, `Bastos`, `Copa`.
- Cada turno se revela una carta aleatoria del mazo espanol.
- Si el palo de la carta coincide con un caballo activo, ese caballo avanza 1 posicion.
- Regla extra: cuando todos los caballos activos alcanzan el mismo "checkpoint" (al menos 1 slot mas), se roba carta de penalizacion; si coincide con un caballo activo, retrocede 1 slot.
- Gana el primer caballo en alcanzar la distancia objetivo.

## Funcionalidades implementadas

- Elegir distancia de carrera (3 a 30).
- Elegir entre 2 y 4 caballos.
- Modo manual (turno por turno).
- Modo automatico (simulacion continua).
- Interfaz web responsiva y estilizada.

## Requisitos

- Java 17+
- Maven 3.9+ (o usar `mvnw`)

## Ejecutar en local

```bash
mvn spring-boot:run
```

Abrir: `http://localhost:8080`

## Pruebas

```bash
mvn test
```

## Despliegue recomendado (Render)

1. Subir este proyecto a GitHub.
2. En Render crear un nuevo servicio `Web Service`.
3. Configurar:
   - Build Command: `mvn clean package`
   - Start Command: `java -jar target/CarreraCaballos-0.0.1-SNAPSHOT.jar`
4. Variable de entorno opcional:
   - `PORT` (Render la asigna automaticamente; la app ya la usa con `server.port=${PORT:8080}`).

## Enlace de despliegue

- Colocar aqui tu URL final, por ejemplo:
  - `https://carrera-caballos.onrender.com`

## Entregable metodologico

Revisar el documento [DOCUMENTACION.md](./DOCUMENTACION.md).
