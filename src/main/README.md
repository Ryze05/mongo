# Gestión de Ligas, Equipos y Jugadores – MongoDB

## 1. Descripción general

Este programa es una aplicación de consola desarrollada en **Kotlin** que permite gestionar información relacionada con **ligas de fútbol, equipos y jugadores**.

La aplicación utiliza una base de datos **MongoDB en memoria**, por lo que no es necesario instalar un servidor de MongoDB externo.  

El programa se ejecuta mediante un menú interactivo por consola.

---

## 2. Requisitos

Para ejecutar la aplicación es necesario disponer de:

- Kotlin
- Gradle junto con sus respectivas dependecias (incluido en el proyecto)
- Carpeta resources con los JSON de las colecciones

---

## 3. Base de datos

La aplicación utiliza una base de datos MongoDB en memoria llamada **clubes**

### Colecciones

- **ligas**
  - `id_liga`
  - `nombre`
  - `pais`
  - `division`

- **equipos**
  - `id`
  - `nombre`
  - `fundacion`
  - `titulos`
  - `valorMercado`
  - `id_liga`

- **jugadores**
  - `id_jugador`
  - `nombre`
  - `fecha_nacimiento`
  - `posicion`
  - `id_equipo`

Los datos iniciales se cargan desde los ficheros JSON ubicados en:

```bash
src/main/resources/
```

---

## 4. Cómo ejecutar

1. Abrir el proyecto en **IntelliJ IDEA**
2. Esperar a que Gradle descargue las dependencias
3. Ejecutar la función **main()** del fichero principal
4. El servidor MongoDB en memoria se iniciará automáticamente
5. Aparecerá el menú principal por consola
6. Para guardar los cambios es necesario usar la opción de exportar

Al salir del programa, el servidor MongoDB en memoria se detiene automáticamente.

---

## 5. Opciones del programa y ejemplos de uso

### Menú principal

1. Ligas
2. Equipos
3. Jugadores
4. Clubes por liga
5. Ligas por valor de mercado
6. Jugadores con equipo y liga
7. Exportar colecciones
8. Importar colecciones
9. Salir

### Ejemplo de salida de uso

```bash
1. Ligas
2. Equipos
3. Jugadores
4. Clubes por liga
5. Ligas por valor de mercadp
6. Jugadores con equipo y liga
7. Exportar colecciones
8. Importar colecciones
9. Salir
Introduce una opcion:
4
ID de la liga: 
2
===============================================================
LIGA: Liga Inglesa
PAÍS: Inglaterra | DIVISIÓN: Premier League
---------------------------------------------------------------
Equipo               Fundación    Títulos    Valor mercado  
---------------------------------------------------------------
Manchester United    1878         23         580,70         
Liverpool            1892         25         540,12         
Chelsea              1905         8          480,67         
===============================================================
1. Ligas
2. Equipos
3. Jugadores
4. Clubes por liga
5. Ligas por valor de mercadp
6. Jugadores con equipo y liga
7. Exportar colecciones
8. Importar colecciones
9. Salir
Introduce una opcion:
9
Saliendo...
Servidor MongoDB en memoria finalizado
```

## 6. Notas importantes

Estamos trabajando con una base de datos MongoDB en memoria, por lo que cualquier cambio realizado (altas, bajas o modificaciones) se borrará al cerrar el proceso de ejecución.

Uso de Exportar/Importar:

- Utiliza la opción 7 (Exportar) para persistir tus cambios en la carpeta `resources`.
- La opción 8 (Importar) sobreescribirá los datos actuales en memoria con el contenido de los archivos JSON.
