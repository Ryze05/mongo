package org.example

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import org.bson.Document
import java.util.Scanner


const val NOM_SRV = "mongodb://localhost:27017"
const val NOM_BD = "bibliotecaSimple"
const val NOM_COLECCION = "librerias"

val scanner = Scanner(System.`in`)

data class Libreria (
    val id: Int,
    val libreria: String,
    val calle: String,
    val ciudad: String,
    val codigoPostal: String,
    val abierto: Boolean
)

fun main() {
    do {
        menuMostar(listOf("Listar librerias","Insertar libreria", "Actualizar libreria", "Eliminar libreria", "Salir"))
        println("Introduce una opcion")
        val option = readln()
        when (option) {
            "1" -> {
                getLibrerias().forEach {
                    println("id: ${it.id} - nombre: ${it.libreria} - calle: ${it.calle} - ciudad: ${it.ciudad} - cp: ${it.codigoPostal} - abierto: ${it.abierto}")
                }
            }
            "2" -> insertarLibreria()
            "4" -> println("Saliendo...")
        }
    } while (option != "4")
}

fun getLibrerias(): List<Libreria> {
    val cliente = MongoClients.create(NOM_SRV)
    val db = cliente.getDatabase(NOM_BD)
    val coleccion = db.getCollection(NOM_COLECCION)

    val librerias = mutableListOf<Libreria>()

    val cursor = coleccion.find().iterator()
    cursor.use {
        while (it.hasNext()) {
            val doc = it.next()

            val id = doc.getInteger("id")
            val libreria = doc.getString("libreria")
            val calle = doc.getString("calle")
            val ciudad = doc.getString("ciudad")
            val codigoPostal = doc.getString("codigo_postal")
            val abierto = doc.getBoolean("abierto")

            val libreriaData = Libreria(id, libreria, calle, ciudad, codigoPostal, abierto)
            librerias.add(libreriaData)
        }
    }

    cliente.close()
    return librerias
}

fun insertarLibreria() {
    val cliente = MongoClients.create(NOM_SRV)
    val db = cliente.getDatabase(NOM_BD)
    val coleccion = db.getCollection(NOM_COLECCION)

    var id: Int? = null
    while (id == null) {
        print("ID de la libreria: ")
        val entrada = scanner.nextLine()
        id = entrada.toIntOrNull()
        if (id == null) {
            println("El ID debe ser un número !!!")
        }
    }

    print("Nombre libreria: ")
    val libreria = scanner.nextLine()
    print("Nombre calle: ")
    val calle = scanner.nextLine()

    print("Nombre ciudad: ")
    val ciudad = scanner.nextLine()
    val cp = scanner.nextLine()

    var abierto: Boolean? = null
    while (abierto == null) {
        println("¿Esta abierta?: ")
        menuMostar(listOf("Si", "No"))
        val entrada = scanner.nextLine()
        when(entrada) {
            "1" -> abierto = true
            "2" -> abierto = false
            else -> println("Opción no valida")
        }
    }

    val doc = Document("id", id)
        .append("libreria", libreria)
        .append("calle", calle)
        .append("ciudad", ciudad)
        .append("codigo_postal", cp)
        .append("abierto", abierto)

    coleccion.insertOne(doc)
    println("Planta insertada con ID: ${doc.getObjectId("_id")}")

    cliente.close()
    println("Conexión cerrada")
}


fun actualizarAltura() {
    //conectar con la BD
    val cliente = MongoClients.create(NOM_SRV)
    val db = cliente.getDatabase(NOM_BD)
    val coleccion = db.getCollection(NOM_COLECCION)

    var id_planta: Int? = null
    while (id_planta == null) {
        print("ID de la planta a actualizar: ")
        val entrada = scanner.nextLine()
        id_planta = entrada.toIntOrNull()
        if (id_planta == null) {
            println("El ID debe ser un número !!!")
        }
    }

    //comprobar si existe una planta con el id_planta proporcionado por consola
    val planta = coleccion.find(Filters.eq("id_planta", id_planta)).firstOrNull()
    if (planta == null) {
        println("No se encontró ninguna planta con id_planta = \"$id_planta\".")
    }
    else {
        // Mostrar información de la planta encontrada
        println("Planta encontrada: ${planta.getString("nombre_comun")} (altura: ${planta.get("altura")} cm)")

        //pedir nueva altura
        var altura: Int? = null
        while (altura == null) {
            print("Nueva altura (en cm): ")
            val entrada = scanner.nextLine()
            altura = entrada.toIntOrNull()
            if (altura == null) {
                println("¡¡¡ La altura debe ser un número !!!")
            }
        }

        // Actualizar el documento
        val result = coleccion.updateOne(
            Filters.eq("id_planta", id_planta),
            Document("\$set", Document("altura", altura))
        )

        if (result.modifiedCount > 0)
            println("Altura actualizada correctamente (${result.modifiedCount} documento modificado).")
        else
            println("No se modificó ningún documento (la altura quizá ya era la misma).")
    }

    cliente.close()
    println("Conexión cerrada.")
}


fun eliminarPlanta() {
    //conectar con la BD
    val cliente = MongoClients.create(NOM_SRV)
    val db = cliente.getDatabase(NOM_BD)
    val coleccion = db.getCollection(NOM_COLECCION)

    var id_planta: Int? = null
    while (id_planta == null) {
        print("ID de la planta a eliminar: ")
        val entrada = scanner.nextLine()
        id_planta = entrada.toIntOrNull()
        if (id_planta == null) {
            println("El ID debe ser un número !!!")
        }
    }

    val result = coleccion.deleteOne(Filters.eq("id_planta", id_planta))
    if (result.deletedCount > 0)
        println("Planta eliminada correctamente.")
    else
        println("No se encontró ninguna planta con ese nombre.")

    cliente.close()
    println("Conexión cerrada.")
}

fun menuMostar(options: List<String>) {
    options.forEachIndexed { index, option ->
        println("${index+1}. $option")
    }
}