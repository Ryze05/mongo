package org.example

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import org.bson.Document
import org.bson.json.JsonWriterSettings
import org.json.JSONArray
import java.io.File
import java.util.Scanner


/*const val NOM_SRV = "mongodb://localhost:27017"
const val NOM_BD = "clubes"
const val NOM_COLECCION = "equipos"*/

//variables globales definidas sin inicializar
lateinit var servidor: MongoServer
lateinit var cliente: MongoClient
lateinit var uri: String
lateinit var coleccionEquipos: MongoCollection<Document>

const val NOM_BD = "clubes"
const val NOM_COLECCION = "equipos"

val scanner = Scanner(System.`in`)

data class Equipo(
    val id: Int,
    val nombre: String,
    val fundacion: Int,
    val titulos: Int,
    val valorMercado: Double
)

fun main() {
    conectarBD()
    importarBD("src/main/resources/equipos.json", coleccionEquipos)
    do {
        menuMostar(listOf("Listar equipos", "Obtener equipo por ID", "Equipos con mas titulos que","Insertar equipo", "Actualizar equipo", "Nombre y titulos de los equipos", "Media de titulos","Eliminar equipo", "Exportar a JSON","Salir"))
        println("Introduce una opcion")
        val option = readln()
        when (option) {
            "1" -> {
                getEquipos()
            }
            "2" -> {
                val id = leerEntero("Introduce el ID del equipo")
                val equipo = getEquipoPorId(id)

                if (equipo != null) {
                    println("Equipo encontrado: ID: ${equipo.id} - Nombre: ${equipo.nombre} - Fundacion: ${equipo.fundacion} - Titulos: ${equipo.titulos} - Valor de mercado: ${equipo.valorMercado}")
                } else {
                    println("Equipo con ID $id no encontrado")
                }
            }
            "3" -> {
                val cant = leerEntero("Introduce la cantidad de titulos")
                val equipos = getEquiposConMasTitulos(cant)

                if (!equipos.isEmpty()) {
                    equipos.forEach {
                        println("id: ${it.id} - nombre: ${it.nombre} - fundacion: ${it.fundacion} - titulos: ${it.titulos} - valor mercado: ${it.valorMercado}")
                    }
                } else {
                    println("No se han encontrado equipos con una mayor cantidad de titulos que la especificada")
                }
            }
            "4" -> {
                val id = leerEntero("Introduce el id del quipo a modificar:")
                val equipoExistente = getEquipoPorId(id)

                if (equipoExistente == null) {
                    val nombre = leerCadena("Introduce un nuevo nombre:")
                    val fundacion = leerEntero("Introduce un nuevo año de fundacion:")
                    val titulos = leerEntero("Introduce una nueva cantidad de titulos:")
                    val valorMercado = leerDouble("Introduce un nuevo valor de mercado:")

                    val equipo = Equipo(id, nombre, fundacion, titulos, valorMercado)
                    insertarEquipo(equipo)
                } else {
                    println("Ya existe un equipo con ese ID")
                }
            }
            "5" -> {
                val id = leerEntero("Introduce el id del quipo a modificar:")
                val equipoExistente = getEquipoPorId(id)

                if (equipoExistente != null) {
                    println("Equipo encontrado: ID: ${equipoExistente.id} - Nombre: ${equipoExistente.nombre} - Fundacion: ${equipoExistente.fundacion} - Titulos: ${equipoExistente.titulos} - Valor de mercado: ${equipoExistente.valorMercado}")

                    do {
                        menuMostar(listOf("nombre", "fundacion", "titulos", "valor de mercado", "Salir"))
                        println("Selecciona una opcion:")
                        val optionUpdate = readln()

                        when (optionUpdate) {
                            "1" -> {
                                val nombre = leerCadena("Introduce un nuevo nombre:")
                                val equipoCopia = equipoExistente.copy(nombre = nombre)
                                actualizarEquipo(equipoCopia)
                            }
                            "2" -> {
                                val fundacion = leerEntero("Introduce un nuevo año de fundacion:")
                                val equipoCopia = equipoExistente.copy(fundacion = fundacion)
                                actualizarEquipo(equipoCopia)
                            }
                            "3" -> {
                                val titulos = leerEntero("Introduce una nueva cantidad de titulos:")
                                val equipoCopia = equipoExistente.copy(titulos = titulos)
                                actualizarEquipo(equipoCopia)
                            }
                            "4" -> {
                                val valorMercado = leerDouble("Introduce un nuevo valor de mercado:")
                                val equipoCopia = equipoExistente.copy(valorMercado = valorMercado)
                                actualizarEquipo(equipoCopia)
                            }
                            "5" -> println("Saliendo...")
                            else -> println("Opcion no valida")
                        }
                    } while (optionUpdate != "5")


                } else {
                    println("El equipo con ese ID no existe")
                }
            }
            "6" -> {
                getNombresTitulos().forEach {
                    val nombre = it.first
                    val titulos = it.second

                    println("Equipo: $nombre - Títulos: $titulos")
                }
            }
            "7" -> {
                println("La media de titulos es: ${calcularMediaTitulos()}")
            }
            "8" -> eliminarEquipo()
            "9" -> {
                exportarBD(coleccionEquipos, "src/main/resources/equipos.json")
            }
            "10" -> {
                importarBD("src/main/resources/equipos.json", coleccionEquipos)
            }
            "11" -> {
                println("Saliendo...")
                desconectarBD()
            }
        }
    } while (option != "11")
}

fun conectarBD() {
    servidor = MongoServer(MemoryBackend())
    val address = servidor.bind()
    uri = "mongodb://${address.hostName}:${address.port}"

    cliente = MongoClients.create(uri)
    coleccionEquipos = cliente.getDatabase(NOM_BD).getCollection(NOM_COLECCION)

    println("Servidor MongoDB en memoria iniciado en $uri")
}

fun desconectarBD() {
    cliente.close()
    servidor.shutdown()
    println("Servidor MongoDB en memoria finalizado")
}

fun getEquipos() {
    coleccionEquipos.find().forEach { doc ->
        val id = doc.getInteger("id")
        val nombre = doc.getString("nombre")
        val fundacion = doc.getInteger("fundacion")
        val titulos = doc.getInteger("titulos")
        val valorMercado = doc.get("valorMercado").toString().toDouble()
        println("id: ${id} - nombre: ${nombre} - fundacion: ${fundacion} - titulos: ${titulos} - valor mercado: ${valorMercado}")
    }
}

fun insertarEquipo(equipo: Equipo) {
    val doc = Document("id", equipo.id)
        .append("nombre", equipo.nombre)
        .append("fundacion", equipo.fundacion)
        .append("titulos", equipo.titulos)
        .append("valorMercado", equipo.valorMercado)

    coleccionEquipos.insertOne(doc)
    println("Equipo insertado con ID: ${doc.getObjectId("_id")}")

    cliente.close()
    println("Conexión cerrada")
}

fun actualizarEquipo(equipo: Equipo) {
    val filtro = Filters.eq("id", equipo.id)

    val actualizacion = Document("\$set", Document()
        .append("nombre", equipo.nombre)
        .append("fundacion", equipo.fundacion)
        .append("titulos", equipo.titulos)
        .append("valorMercado", equipo.valorMercado)
    )

    val result = coleccionEquipos.updateOne(filtro, actualizacion)

    if (result.modifiedCount > 0) {
        println("Equipo con ID ${equipo.id} actualizado correctamente (${result.modifiedCount} documento modificado).")
    } else {
        println("No se modificó el equipo con ID ${equipo.id}. Puede que el ID no exista o los datos fueran los mismos.")
    }

    cliente.close()
    println("Conexión cerrada.")
}

fun eliminarEquipo() {
    var id: Int? = null
    while (id == null) {
        print("ID del equipo a eliminar: ")
        val entrada = scanner.nextLine()
        id = entrada.toIntOrNull()
        if (id == null) {
            println("El ID debe ser un número !!!")
        }
    }

    val result = coleccionEquipos.deleteOne(Filters.eq("id", id))
    if (result.deletedCount > 0)
        println("Equipo eliminado correctamente.")
    else
        println("No se encontró ningun equipo con ese nombre.")

    cliente.close()
    println("Conexión cerrada.")
}

fun getEquipoPorId(idEquipo: Int): Equipo? {

    val filtro = Filters.eq("id", idEquipo)

    val doc = coleccionEquipos.find(filtro).first()

    cliente.close()

    return if (doc != null) {
        val id = doc.getInteger("id")
        val nombre = doc.getString("nombre")
        val fundacion = doc.getInteger("fundacion")
        val titulos = doc.getInteger("titulos")
        val valorMercado = doc.get("valorMercado").toString().toDouble()

        Equipo(id, nombre, fundacion, titulos, valorMercado)
    } else {
        null
    }
}

fun getEquiposConMasTitulos(minimoTitulos: Int): List<Equipo> {

    val equipos = mutableListOf<Equipo>()

    val filtro = Filters.gt("titulos", minimoTitulos)

    val cursor = coleccionEquipos.find(filtro).iterator()
    cursor.use {
        while (it.hasNext()) {
            val doc = it.next()

            val id = doc.getInteger("id")
            val nombre = doc.getString("nombre")
            val fundacion = doc.getInteger("fundacion")
            val titulos = doc.getInteger("titulos")
            val valorMercado = doc.get("valorMercado").toString().toDouble()

            val equipo = Equipo(id, nombre, fundacion, titulos, valorMercado)
            equipos.add(equipo)
        }
    }

    cliente.close()
    return equipos
}

fun getNombresTitulos(): List<Pair<String, Int>> {
    val resultados = mutableListOf<Pair<String, Int>>()

    val proyeccion = Projections.include("nombre", "titulos")
    val cursor = coleccionEquipos.find().projection(proyeccion).iterator()

    cursor.use {
        while (it.hasNext()) {
            val doc = it.next()

            val nombre = doc.getString("nombre")
            val titulos = doc.getInteger("titulos")

            resultados.add(Pair(nombre, titulos))
        }
    }

    cliente.close()
    return resultados
}

fun calcularMediaTitulos(): Double {
    val pipeline = listOf(
        Document("\$group",
            Document("_id", null)
                .append("titulosMedia", Document("\$avg", "\$titulos"))
        )
    )

    val resultado = coleccionEquipos.aggregate(pipeline).first()

    cliente.close()

    return if (resultado != null) {
        resultado.getDouble("titulosMedia") ?: 0.0
    } else {
        0.0
    }
}

fun exportarBD(coleccion: MongoCollection<Document>, rutaJSON: String) {
    val settings = JsonWriterSettings.builder().indent(true).build()
    val file = File(rutaJSON)
    file.printWriter().use { out ->
        out.println("[")
        val cursor = coleccion.find().iterator()
        var first = true
        while (cursor.hasNext()) {
            if (!first) out.println(",")
            val doc = cursor.next()
            out.print(doc.toJson(settings))
            first = false
        }
        out.println("]")
        cursor.close()
    }

    println("Exportación de ${coleccion.namespace.collectionName} completada")
}

fun importarBD(rutaJSON: String, coleccion: MongoCollection<Document>) {
    println("Iniciando importación de datos desde JSON...")

    val jsonFile = File(rutaJSON)
    if (!jsonFile.exists()) {
        println("No se encontró el archivo JSON a importar")
        return
    }

    // Leer JSON del archivo
    val jsonText = try {
        jsonFile.readText()
    } catch (e: Exception) {
        println("Error leyendo el archivo JSON: ${e.message}")
        return
    }

    val array = try {
        JSONArray(jsonText)
    } catch (e: Exception) {
        println("Error al parsear JSON: ${e.message}")
        return
    }

    // Convertir JSON a Document y eliminar _id si existe
    val documentos = mutableListOf<Document>()
    for (i in 0 until array.length()) {
        val doc = Document.parse(array.getJSONObject(i).toString())
        doc.remove("_id")  // <-- eliminar _id para que MongoDB genere uno nuevo
        documentos.add(doc)
    }

    if (documentos.isEmpty()) {
        println("El archivo JSON está vacío")
        return
    }

    val db = cliente.getDatabase(NOM_BD)

    val nombreColeccion =coleccion.namespace.collectionName

    // Borrar colección si existe
    if (db.listCollectionNames().contains(nombreColeccion)) {
        db.getCollection(nombreColeccion).drop()
        println("Colección '$nombreColeccion' eliminada antes de importar.")
    }

    // Insertar documentos
    try {
        coleccion.insertMany(documentos)
        println("Importación completada: ${documentos.size} documentos de $nombreColeccion.")
    } catch (e: Exception) {
        println("Error importando documentos: ${e.message}")
    }
}

fun menuMostar(options: List<String>) {
    options.forEachIndexed { index, option ->
        println("${index + 1}. $option")
    }
}

fun leerEntero(mensaje: String): Int {

    while (true) {

        println(mensaje)

        try {
            return readLine()!!.toInt()
        } catch (e: NumberFormatException) {
            println("Formato no válido")
        }
    }
}

fun leerCadena(mensaje: String): String {
    var valido = false
    var cadena = ""

    while (!valido) {

        println(mensaje)

        cadena = readLine()!!

        if (cadena.isNotEmpty()) {
            valido = true
        } else {
            println("La cadena no puede estar vacía")
        }

    }

    return cadena
}

fun leerDouble(mensaje: String): Double {

    while(true) {

        println(mensaje)

        try {
            return readLine()!!.toDouble()
        } catch (e: NumberFormatException) {
            println("Formato no válido")
        }
    }

}