# Documentacion del Proceso de Solucion

## 1. Analisis del problema

Se requiere automatizar el juego de carrera de caballos con cartas, demostrando:

- Modelo de datos
- Estructuras
- Operadores
- Restricciones
- Metodologia de solucion de problemas completa

Reglas aplicadas en este proyecto:

- Hay hasta 4 caballos (palos de baraja espanola): Oro, Espada, Bastos, Copa.
- Usuario selecciona entre 2 y 4 caballos para una carrera.
- Usuario selecciona la distancia de la meta.
- En cada turno se roba una carta:
  - Si el palo pertenece a un caballo activo, ese caballo avanza una casilla.
  - Si no coincide con un caballo activo, no hay avance.
- Regla de penalizacion:
  - Cada vez que todos los caballos activos alcanzan un nuevo checkpoint comun, se roba una carta adicional.
  - Si su palo corresponde a un caballo activo, ese caballo retrocede 1 casilla (minimo 0).
- Gana el primer caballo en llegar a la distancia.

## 2. Alternativas evaluadas

1. Solo frontend (JavaScript puro)
- Ventaja: rapido de construir.
- Desventaja: no cumple con el requerimiento de usar Java como base principal.

2. Aplicacion de consola Java
- Ventaja: simple logica de juego.
- Desventaja: no cumple objetivo de interfaz grafica web bonita.

3. Java Spring Boot + HTML/CSS/JS (alternativa elegida)
- Ventaja: separa logica de negocio (backend) y presentacion (frontend).
- Ventaja: es desplegable en Render/Railway/Vercel (con adaptacion).
- Ventaja: facilita documentar modelo de datos y pruebas.

## 3. Diseno de solucion

### 3.1 Modelo de datos

- `SuitSpanish` (enum): define palos permitidos.
- `SpanishCard` (record): representa una carta (`suit`, `value`).
- `RaceGame` (clase): estado completo de una carrera:
  - id
  - distancia
  - caballos activos
  - posiciones por caballo
  - historial de turnos
  - mazo barajado
  - turno actual
  - ganador
- `TurnResult` (record): resultado de cada turno.

### 3.2 Estructuras de datos usadas

- `List`: mazo, historial, caballos activos.
- `Map` (EnumMap): posiciones por caballo.
- `ConcurrentHashMap`: almacenamiento de partidas activas en memoria.
- `LinkedHashSet`: validacion para eliminar duplicados y conservar orden en caballos seleccionados.

### 3.3 Operadores aplicados

- Asignacion e incremento (`=`, `+1`, `+=` logico en avance).
- Comparacion (`>=`, `==`, `!=`) para validaciones y meta.
- Logicos (`&&`, `||`) para reglas de restriccion.
- Operadores de colecciones (`containsKey`, `size`, `put`, `remove`).

### 3.4 Restricciones implementadas

- Distancia valida: 3 a 30.
- Caballos validos: minimo 2, maximo 4.
- Sin palos repetidos.
- No existe avance de caballos fuera del conjunto elegido.
- Cuando hay ganador, la carrera se considera finalizada.

## 4. Implementacion

Backend:

- API REST:
  - `POST /api/games` crea carrera.
  - `GET /api/games/{id}` consulta estado.
  - `POST /api/games/{id}/step` avanza un turno.
- Servicio `RaceGameService`:
  - aplica validaciones de negocio.
  - ejecuta turnos y convierte el estado a DTO de respuesta.

Frontend:

- Template `index.html`.
- Estilos `styles.css`.
- Logica cliente `app.js`:
  - crear partida.
  - avanzar turno manual.
  - avance automatico con `setInterval`.
  - renderizar pista, carta actual e historial.

## 5. Pruebas

Pruebas unitarias en `RaceGameServiceTest`:

- Rechaza cantidad invalida de caballos.
- Rechaza palos repetidos.
- Verifica que una carrera termina con ganador en numero razonable de turnos.

Pruebas manuales sugeridas:

1. Crear carrera con distancia 10 y 2 caballos.
2. Avanzar manualmente varios turnos.
3. Cambiar a modo automatico y detener.
4. Crear nueva carrera con 4 caballos y distancia 20.
5. Confirmar anuncio de ganador y bloqueo de botones al finalizar.

## 6. Conclusiones

- Se implemento un juego web funcional en Java, con interfaz grafica y reglas solicitadas.
- El proyecto evidencia la relacion entre modelo de datos, estructuras, operadores y restricciones.
- Se aplico metodologia completa desde analisis hasta pruebas y documentacion.
- La arquitectura elegida permite evolucionar facilmente (persistencia, ranking, multiplayer, estadisticas).
