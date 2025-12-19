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

lateinit var servidor: MongoServer
lateinit var cliente: MongoClient
lateinit var uri: String
lateinit var coleccionEquipos: MongoCollection<Document>
lateinit var coleccionJugadores: MongoCollection<Document>
lateinit var coleccionLigas: MongoCollection<Document>


const val NOM_BD = "clubes"
const val NOM_COLECCION1 = "equipos"
const val NOM_COLECCION2 = "jugadores"
const val NOM_COLECCION3 = "ligas"

val scanner = Scanner(System.`in`)

data class Equipo(
    val id: Int,
    val nombre: String,
    val fundacion: Int,
    val titulos: Int,
    val valorMercado: Double,
    val idLiga: Int
)

data class Jugador(
    val id: Int,
    val nombre: String,
    val fechaNacimiento: String,
    val posicion: String,
    val idEquipo: Int
)

data class Liga(
    val id: Int,
    val nombre: String,
    val pais: String,
    val division: String
)

fun main() {
    conectarBD()
    importarBD("src/main/resources/equipos.json", coleccionEquipos)
    importarBD("src/main/resources/jugadores.json", coleccionJugadores)
    importarBD("src/main/resources/ligas.json", coleccionLigas)

    do {
        menuMostar(
            listOf(
                "Ligas",
                "Equipos",
                "Jugadores",
                "Clubes por liga",
                "Ligas por valor de mercadp",
                "Jugadores con equipo y liga",
                "Exportar colecciones",
                "Importar colecciones",
                "Salir"
            )
        )
        println("Introduce una opcion:")
        val option = readln()
        when (option) {
            "1" -> crudLiga()
            "2" -> crudEquipo()
            "3" -> crudJugador()
            "4" -> mostrarEquiposLiga()
            "5" -> mostrarLigasPorValorMercado()
            "6" -> mostrarJugadoresConEquipoYLiga()
            "7" -> {
                exportarBD(coleccionEquipos, "src/main/resources/equipos.json")
                exportarBD(coleccionJugadores, "src/main/resources/jugadores.json")
                exportarBD(coleccionLigas, "src/main/resources/ligas.json")
            }
            "8" -> {
                importarBD("src/main/resources/equipos.json", coleccionEquipos)
                importarBD("src/main/resources/jugadores.json", coleccionJugadores)
                importarBD("src/main/resources/ligas.json", coleccionLigas)
            }
            "9" -> {
                println("Saliendo...")
                desconectarBD()
            }
        }
    } while (option != "9")
}

fun conectarBD() {
    servidor = MongoServer(MemoryBackend())
    val address = servidor.bind()
    uri = "mongodb://${address.hostName}:${address.port}"

    cliente = MongoClients.create(uri)
    coleccionEquipos = cliente.getDatabase(NOM_BD).getCollection(NOM_COLECCION1)
    coleccionJugadores = cliente.getDatabase(NOM_BD).getCollection(NOM_COLECCION2)
    coleccionLigas = cliente.getDatabase(NOM_BD).getCollection(NOM_COLECCION3)


    println("Servidor MongoDB en memoria iniciado en $uri")
}

fun desconectarBD() {
    cliente.close()
    servidor.shutdown()
    println("Servidor MongoDB en memoria finalizado")
}


//EQUIPO
fun crudEquipo() {
    do {
        menuMostar(
            listOf(
                "Listar equipos",
                "Obtener equipo por ID",
                "Insertar equipo",
                "Actualizar equipo",
                "Eliminar equipo",
                "Equipos con mas titulos que",
                "Nombre y titulos de los equipos",
                "Media de titulos",
                "Mostrar clubes por valor de mercado",
                "Exportar coleccion",
                "Importar coleccion",
                "Salir"
            )
        )
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
                val id = leerEntero("Introduce el id del equipo a insertar:")
                val equipoExistente = getEquipoPorId(id)

                if (equipoExistente == null) {
                    val nombre = leerCadena("Introduce el nombre:")
                    val fundacion = leerEntero("Introduce el año de fundacion:")
                    val titulos = leerEntero("Introduce la cantidad de titulos:")
                    val valorMercado = leerDouble("Introduce el valor de mercado:")
                    val idLiga = leerEntero("Introduce el ID liga:")

                    val ligaExistente = getLigaPorId(idLiga)

                    if (ligaExistente != null) {
                        val equipo = Equipo(id, nombre, fundacion, titulos, valorMercado, idLiga)
                        insertarEquipo(equipo)
                    } else {
                        println("La liga no existe")
                    }

                } else {
                    println("Ya existe un equipo con ese ID")
                }
            }

            "4" -> {
                val id = leerEntero("Introduce el id del equipo a modificar:")
                val equipoExistente = getEquipoPorId(id)

                if (equipoExistente != null) {
                    println("Equipo encontrado: ID: ${equipoExistente.id} - Nombre: ${equipoExistente.nombre} - Fundacion: ${equipoExistente.fundacion} - Titulos: ${equipoExistente.titulos} - Valor de mercado: ${equipoExistente.valorMercado} - ID liga: ${equipoExistente.idLiga}")

                    do {
                        menuMostar(listOf("nombre", "fundacion", "titulos", "valor de mercado", "ID liga", "Salir"))
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

                            "5" -> {
                                val idLiga = leerEntero("Introduce un nuevo ID liga:")
                                val ligaExistente = getLigaPorId(idLiga)

                                if (ligaExistente != null) {
                                    val equipoCopia = equipoExistente.copy(idLiga = idLiga)
                                    actualizarEquipo(equipoCopia)
                                } else {
                                    println("No se ha encontrado una liga con ese ID")
                                }
                            }

                            "6" -> println("Saliendo...")
                            else -> println("Opcion no valida")
                        }
                    } while (optionUpdate != "6")


                } else {
                    println("El equipo con ese ID no existe")
                }
            }

            "5" -> eliminarEquipo()

            "6" -> {
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

            "7" -> {
                getNombresTitulos().forEach {
                    val nombre = it.first
                    val titulos = it.second

                    println("Equipo: $nombre - Títulos: $titulos")
                }
            }

            "8" -> {
                println("La media de titulos es: ${calcularMediaTitulos()}")
            }

            "9" -> mostrarClubesPorValorMercado()

            "10" -> {
                exportarBD(coleccionEquipos, "src/main/resources/equipos.json")
            }

            "11" -> {
                importarBD("src/main/resources/equipos.json", coleccionEquipos)
            }

            "12" -> {
                println("Saliendo...")
            }
        }
    } while (option != "12")
}

fun getEquipos() {
    try {
        coleccionEquipos.find().forEach { doc ->
            val id = doc.getInteger("id")
            val nombre = doc.getString("nombre")
            val fundacion = doc.getInteger("fundacion")
            val titulos = doc.getInteger("titulos")
            val valorMercado = doc.get("valorMercado").toString().toDouble()
            val idLiga = doc.getInteger("id_liga")
            println("id: $id - nombre: $nombre - fundacion: $fundacion - titulos: $titulos - valor mercado: $valorMercado - ID liga: $idLiga")
        }
    } catch (e: Exception) {
        println("Ha ocurrido un error en la consulta")
    }
}

fun insertarEquipo(equipo: Equipo) {
    try {
        val doc = Document("id", equipo.id)
            .append("nombre", equipo.nombre)
            .append("fundacion", equipo.fundacion)
            .append("titulos", equipo.titulos)
            .append("valorMercado", equipo.valorMercado)

        coleccionEquipos.insertOne(doc)
        println("Equipo insertado con ID: ${doc.getObjectId("_id")}")
    } catch (e: Exception) {
        println("Ha ocurrido un error en la consulta")
    }
}

fun actualizarEquipo(equipo: Equipo) {
    try {
        val filtro = Filters.eq("id", equipo.id)

        val actualizacion = Document(
            "\$set", Document()
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
    } catch (e: Exception) {
        println("Ha ocurrido un error en la consulta")
    }
}

fun eliminarEquipo() {
    try {
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
    } catch (e: Exception) {
        println("Ha ocurrido un error en la consulta")
    }
}

fun getEquipoPorId(idEquipo: Int): Equipo? {
    return try {
        val filtro = Filters.eq("id", idEquipo)

        val doc = coleccionEquipos.find(filtro).first()


        return if (doc != null) {
            val id = doc.getInteger("id")
            val nombre = doc.getString("nombre")
            val fundacion = doc.getInteger("fundacion")
            val titulos = doc.getInteger("titulos")
            val valorMercado = doc.get("valorMercado").toString().toDouble()
            val idLiga = doc.getInteger("id_liga")

            Equipo(id, nombre, fundacion, titulos, valorMercado, idLiga)
        } else {
            null
        }
    } catch (e: Exception) {
        println("Ha ocurrido un error en la consulta")
        null
    }
}

fun getEquiposConMasTitulos(minimoTitulos: Int): List<Equipo> {
    val equipos = mutableListOf<Equipo>()

    try {
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
                val idLiga = doc.getInteger("id_liga")

                equipos.add(
                    Equipo(id, nombre, fundacion, titulos, valorMercado, idLiga)
                )
            }
        }
    } catch (e: Exception) {
        println("Ha ocurrido un error en la consulta")
    }

    return equipos
}

fun getNombresTitulos(): List<Pair<String, Int>> {
    val resultados = mutableListOf<Pair<String, Int>>()

    try {
        val proyeccion = Projections.include("nombre", "titulos")
        val cursor = coleccionEquipos.find().projection(proyeccion).iterator()

        cursor.use {
            while (it.hasNext()) {
                val doc = it.next()
                resultados.add(
                    Pair(
                        doc.getString("nombre"),
                        doc.getInteger("titulos")
                    )
                )
            }
        }
    } catch (e: Exception) {
        println("Ha ocurrido un error en la consulta")
    }

    return resultados
}

fun calcularMediaTitulos(): Double {
    return try {
        val pipeline = listOf(
            Document(
                "\$group",
                Document("_id", null)
                    .append("titulosMedia", Document("\$avg", "\$titulos"))
            )
        )

        val resultado = coleccionEquipos.aggregate(pipeline).first()
        resultado?.getDouble("titulosMedia") ?: 0.0

    } catch (e: Exception) {
        println("Ha ocurrido un error en la consulta")
        0.0
    }
}

fun mostrarClubesPorValorMercado() {
    try {
        val cursor = coleccionEquipos
            .find()
            .projection(
                Projections.include("nombre", "valorMercado")
            )
            .sort(
                Document("valorMercado", -1)
            )

        cursor.forEach { doc ->
            val nombre = doc.getString("nombre")
            val valorMercado = doc.get("valorMercado").toString().toDouble()

            println("$nombre - $valorMercado €")
        }

    } catch (e: Exception) {
        println("Error al mostrar los clubes por valor de mercado")
    }
}

//JUGADOR
fun crudJugador() {
    do {
        menuMostar(
            listOf(
                "Listar jugadores",
                "Obtener jugador por ID",
                "Insertar jugador",
                "Actualizar jugador",
                "Eliminar jugador",
                "Exportar coleccion",
                "Importar coleccion",
                "Salir"
            )
        )
        println("Introduce una opcion")
        val option = readln()
        when (option) {
            "1" -> getJugadores()
            "2" -> {
                val id = leerEntero("Introduce el ID del jugador")
                val jugador = getJugadorPorId(id)

                if (jugador != null) {
                    println("Equipo encontrado: ID: ${jugador.id} - nombre: ${jugador.nombre} - fecha nacimiento: ${jugador.fechaNacimiento} - posicion: ${jugador.posicion} - ID equipo: ${jugador.idEquipo}")
                } else {
                    println("Jugador con ID $id no encontrado")
                }
            }

            "3" -> {
                val id = leerEntero("Introduce el id del jugador a insertar:")
                val jugadorExistente = getJugadorPorId(id)

                if (jugadorExistente == null) {
                    val nombre = leerCadena("Introduce un nombre:")
                    val fecha_nacimiento = leerCadena("Introduce una fecha de nacimiento:")
                    val posicion = leerCadena("Introduce una posicion:")
                    val id_equipo = leerEntero("Introduce un ID de equipo:")

                    val equipo = getEquipoPorId(id_equipo)

                    if (equipo != null) {
                        val jugador = Jugador(id, nombre, fecha_nacimiento, posicion, id_equipo)
                        insertarJugador(jugador)
                    } else {
                        println("No se ha encontrado un equipo con ese ID")
                    }

                } else {
                    println("Ya existe un jugador con ese ID")
                }
            }

            "4" -> {
                val id = leerEntero("Introduce el id del jugador a modificar:")
                val jugadorExistente = getJugadorPorId(id)

                if (jugadorExistente != null) {
                    println("Jugador encontrado: ID: ${jugadorExistente.id} - Nombre: ${jugadorExistente.nombre} - Fecha nacimiento: ${jugadorExistente.fechaNacimiento} - Posicion: ${jugadorExistente.posicion} - ID equipo: ${jugadorExistente.idEquipo}")

                    do {
                        menuMostar(listOf("Nombre", "Fecha nacimiento", "Posicion", "ID equipo", "Salir"))
                        println("Selecciona una opcion:")
                        val optionUpdate = readln()

                        when (optionUpdate) {
                            "1" -> {
                                val nombre = leerCadena("Introduce un nuevo nombre:")
                                val jugadorCopia = jugadorExistente.copy(nombre = nombre)
                                actualizarJugador(jugadorCopia)
                            }

                            "2" -> {
                                val fechaNacimiento = leerCadena("Introduce una nueva fecha de nacimiento:")
                                val jugadorCopia = jugadorExistente.copy(fechaNacimiento = fechaNacimiento)
                                actualizarJugador(jugadorCopia)
                            }

                            "3" -> {
                                val posicion = leerCadena("Introduce una nueva posicion:")
                                val jugadorCopia = jugadorExistente.copy(posicion = posicion)
                                actualizarJugador(jugadorCopia)
                            }

                            "4" -> {
                                val idEquipo = leerEntero("Introduce el nuevo ID de equipo:")
                                val equipo = getEquipoPorId(idEquipo)

                                if (equipo != null) {
                                    val jugadorCopia = jugadorExistente.copy(idEquipo = idEquipo)
                                    actualizarJugador(jugadorCopia)
                                } else {
                                    println("No se ha encontrado un equipo con ese ID")
                                }
                            }

                            "5" -> println("Saliendo...")
                            else -> println("Opcion no valida")
                        }
                    } while (optionUpdate != "5")


                } else {
                    println("El equipo con ese ID no existe")
                }
            }

            "5" -> eliminarJugador()
            "6" -> {
                exportarBD(coleccionJugadores, "src/main/resources/jugadores.json")
            }

            "7" -> {
                importarBD("src/main/resources/jugadores.json", coleccionJugadores)
            }

            "8" -> println("Saliendo...")
        }

    } while (option != "8")
}

fun getJugadores() {
    try {
        coleccionJugadores.find().forEach { doc ->
            val id = doc.getInteger("id_jugador")
            val nombre = doc.getString("nombre")
            val fecha_nacimiento = doc.getString("fecha_nacimiento")
            val posicion = doc.getString("posicion")
            val id_equipo = doc.getInteger("id_equipo")
            println("id: ${id} - nombre: ${nombre} - fecha nacimiento: ${fecha_nacimiento} - posicion: ${posicion} - ID equipo: ${id_equipo}")
        }
    } catch (e: Exception) {
        println("Ha ocurrido un error en la consulta")
    }
}

fun getJugadorPorId(idJugador: Int): Jugador? {
    return try {
        val filtro = Filters.eq("id_jugador", idJugador)
        val doc = coleccionJugadores.find(filtro).first()

        if (doc != null) {
            Jugador(
                doc.getInteger("id_jugador"),
                doc.getString("nombre"),
                doc.getString("fecha_nacimiento"),
                doc.getString("posicion"),
                doc.getInteger("id_equipo")
            )
        } else null

    } catch (e: Exception) {
        println("Ha ocurrido un error en la consulta")
        null
    }
}

fun actualizarJugador(jugador: Jugador) {
    try {
        val filtro = Filters.eq("id_jugador", jugador.id)

        val actualizacion = Document(
            "\$set", Document()
                .append("nombre", jugador.nombre)
                .append("fecha_nacimiento", jugador.fechaNacimiento)
                .append("posicion", jugador.posicion)
                .append("id_equipo", jugador.idEquipo)
        )

        val result = coleccionJugadores.updateOne(filtro, actualizacion)

        if (result.modifiedCount > 0) {
            println("Jugador con ID ${jugador.id} actualizado correctamente (${result.modifiedCount} documento modificado).")
        } else {
            println("No se modificó el jugador con ID ${jugador.id}. Puede que el ID no exista o los datos fueran los mismos.")
        }
    } catch (e: Exception) {
        println("Ha ocurrido un error en la consulta")
    }

}

fun insertarJugador(jugador: Jugador) {
    try {
        val doc = Document("id_jugador", jugador.id)
            .append("nombre", jugador.nombre)
            .append("fecha_nacimiento", jugador.fechaNacimiento)
            .append("posicion", jugador.posicion)
            .append("id_equipo", jugador.idEquipo)

        coleccionJugadores.insertOne(doc)
        println("Jugador insertado con ID: ${doc.getObjectId("_id")}")
    } catch (e: Exception) {
        println("Ha ocurrido un error en la consulta")
    }
}

fun eliminarJugador() {
    try {
        var id: Int? = null
        while (id == null) {
            print("ID del jugador a eliminar: ")
            val entrada = scanner.nextLine()
            id = entrada.toIntOrNull()
            if (id == null) {
                println("El ID debe ser un número !!!")
            }
        }

        val result = coleccionEquipos.deleteOne(Filters.eq("id_jugador", id))
        if (result.deletedCount > 0)
            println("Jugador eliminado correctamente.")
        else
            println("No se encontró ningun jugador con ese nombre.")
    } catch (e: Exception) {
        println("Ha ocurrido un error en la consulta")
    }
}

//LIGA
fun crudLiga() {
    do {
        menuMostar(
            listOf(
                "Listar ligas",
                "Obtener liga por ID",
                "Insertar liga",
                "Actualizar liga",
                "Eliminar liga",
                "Exportar coleccion",
                "Importar coleccion",
                "Salir"
            )
        )
        println("Introduce una opcion")
        val option = readln()
        when (option) {
            "1" -> getLigas()
            "2" -> {
                val id = leerEntero("Introduce el ID de la liga")
                val liga = getLigaPorId(id)

                if (liga != null) {
                    println("Liga encontrada: ID: ${liga.id} - nombre: ${liga.nombre} - Pais: ${liga.pais} - Division: ${liga.division}")
                } else {
                    println("Liga con ID $id no encontrada")
                }
            }

            "3" -> {
                val id = leerEntero("Introduce el id de la liga a insertar:")
                val ligaExistente = getLigaPorId(id)

                if (ligaExistente == null) {
                    val nombre = leerCadena("Introduce un nombre:")
                    val pais = leerCadena("Introduce un pais:")
                    val division = leerCadena("Introduce una division:")

                    val liga = Liga(id, nombre, pais, division)
                    insertarLiga(liga)

                } else {
                    println("Ya existe una liga con ese ID")
                }
            }

            "4" -> {
                val id = leerEntero("Introduce el id de la liga a modificar:")
                val ligaExistente = getLigaPorId(id)

                if (ligaExistente != null) {
                    println("Liga encontrada: ID: ${ligaExistente.id} - Nombre: ${ligaExistente.nombre} - Pais: ${ligaExistente.pais} - Division: ${ligaExistente.division}")

                    do {
                        menuMostar(listOf("Nombre", "Pais", "Division", "Salir"))
                        println("Selecciona una opcion:")
                        val optionUpdate = readln()

                        when (optionUpdate) {
                            "1" -> {
                                val nombre = leerCadena("Introduce un nuevo nombre:")
                                val ligaCopia = ligaExistente.copy(nombre = nombre)
                                actualizarLiga(ligaCopia)
                            }

                            "2" -> {
                                val pais = leerCadena("Introduce un nuevo pais:")
                                val ligaCopia = ligaExistente.copy(pais = pais)
                                actualizarLiga(ligaCopia)
                            }

                            "3" -> {
                                val division = leerCadena("Introduce una nueva division:")
                                val ligaCopia = ligaExistente.copy(division = division)
                                actualizarLiga(ligaCopia)
                            }

                            "4" -> println("Saliendo...")
                            else -> println("Opcion no valida")
                        }
                    } while (optionUpdate != "4")


                } else {
                    println("El equipo con ese ID no existe")
                }
            }

            "5" -> eliminarLiga()

            "6" -> {
                exportarBD(coleccionLigas, "src/main/resources/ligas.json")
            }

            "7" -> {
                importarBD("src/main/resources/ligas.json", coleccionLigas)
            }

            "8" -> println("Saliendo...")
        }

    } while (option != "8")
}

fun getLigas() {
    try {
        coleccionLigas.find().forEach { doc ->
            val id = doc.getInteger("id_liga")
            val nombre = doc.getString("nombre")
            val pais = doc.getString("pais")
            val division = doc.getString("division")
            println("id: ${id} - nombre: ${nombre} - pais: ${pais} - division: ${division}")
        }
    } catch (e: Exception) {
        println("Ha ocurrido un error en la consulta")
    }
}

fun getLigaPorId(idLiga: Int): Liga? {
    return try {
        val doc = coleccionLigas.find(Filters.eq("id_liga", idLiga)).first()

        if (doc != null) {
            Liga(
                doc.getInteger("id_liga"),
                doc.getString("nombre"),
                doc.getString("pais"),
                doc.getString("division")
            )
        } else null

    } catch (e: Exception) {
        println("Ha ocurrido un error en la consulta")
        null
    }
}

fun insertarLiga(liga: Liga) {
    try {
        val doc = Document("id_liga", liga.id)
            .append("nombre", liga.nombre)
            .append("pais", liga.pais)
            .append("division", liga.division)

        coleccionLigas.insertOne(doc)
        println("Liga insertada con ID: ${doc.getObjectId("_id")}")
    } catch (e: Exception) {
        println("Ha ocurrido un error en la consulta")
    }
}

fun actualizarLiga(liga: Liga) {
    try {
        val filtro = Filters.eq("id_liga", liga.id)

        val actualizacion = Document(
            "\$set", Document()
                .append("nombre", liga.nombre)
                .append("pais", liga.pais)
                .append("division", liga.division)
        )

        val result = coleccionLigas.updateOne(filtro, actualizacion)

        if (result.modifiedCount > 0) {
            println("Liga con ID ${liga.id} actualiza correctamente (${result.modifiedCount} documento modificado).")
        } else {
            println("No se modificó la liga con ID ${liga.id}. Puede que el ID no exista o los datos fueran los mismos.")
        }
    } catch (e: Exception) {
        println("Ha ocurrido un error en la consulta")
    }
}

fun eliminarLiga() {
    try {
        var id: Int? = null
        while (id == null) {
            print("ID de la liga a eliminar: ")
            val entrada = scanner.nextLine()
            id = entrada.toIntOrNull()
            if (id == null) {
                println("El ID debe ser un número !!!")
            }
        }

        val result = coleccionLigas.deleteOne(Filters.eq("id_liga", id))
        if (result.deletedCount > 0)
            println("Liga eliminada correctamente.")
        else
            println("No se encontró ninguna liga con ese nombre.")

    } catch (e: Exception) {
        println("Ha ocurrido un error en la consulta")
    }
}

//Joins
fun mostrarEquiposLiga() {
    try {
        val idLiga = leerEntero("ID de la liga: ")

        val ligaDoc = coleccionLigas
            .find(Document("id_liga", idLiga))
            .first()

        if (ligaDoc == null) {
            println("No existe ninguna liga con ID $idLiga")
            return
        }

        val nombreLiga = ligaDoc["nombre"]
        val pais = ligaDoc["pais"]
        val division = ligaDoc["division"]

        val pipeline = listOf(
            Document("\$match", Document("id_liga", idLiga)),
            Document(
                "\$lookup", Document()
                    .append("from", "equipos")
                    .append("localField", "id_liga")
                    .append("foreignField", "id_liga")
                    .append("as", "equipos")
            ),
            Document("\$unwind", "\$equipos"),
            Document(
                "\$project", Document()
                    .append("nombre_equipo", "\$equipos.nombre")
                    .append("fundacion", "\$equipos.fundacion")
                    .append("titulos", "\$equipos.titulos")
                    .append("valorMercado", "\$equipos.valorMercado")
            )
        )

        val equipos = coleccionLigas.aggregate(pipeline).toList()

        if (equipos.isEmpty()) {
            println("La liga no tiene equipos asociados")
            return
        }

        println("===============================================================")
        println("LIGA: $nombreLiga")
        println("PAÍS: $pais | DIVISIÓN: $division")
        println("---------------------------------------------------------------")
        println(
            String.format(
                "%-20s %-12s %-10s %-15s",
                "Equipo", "Fundación", "Títulos", "Valor mercado"
            )
        )
        println("---------------------------------------------------------------")

        equipos.forEach { equipo ->
            val nombre = equipo["nombre_equipo"] as String
            val fundacion = equipo["fundacion"] as Int
            val titulos = equipo["titulos"] as Int
            val valorMercado = equipo["valorMercado"].toString().toDouble()

            println(
                String.format(
                    "%-20s %-12d %-10d %-15.2f",
                    nombre, fundacion, titulos, valorMercado
                )
            )
        }

        println("===============================================================")
    } catch (e: Exception) {
        println("Ha ocurrido un error en la consulta")
    }
}

fun mostrarLigasPorValorMercado() {
    try {
        val pipeline = listOf(

            Document(
                "\$lookup", Document()
                    .append("from", "ligas")
                    .append("localField", "id_liga")
                    .append("foreignField", "id_liga")
                    .append("as", "liga")
            ),

            Document("\$unwind", "\$liga"),

            Document(
                "\$group", Document()
                    .append("_id", "\$liga.nombre")
                    .append("valor_total", Document("\$sum", "\$valorMercado"))
            ),

            Document("\$sort", Document("valor_total", -1))
        )

        val resultado = coleccionEquipos.aggregate(pipeline)

        println("=================================================")
        println("LIGAS ORDENADAS POR VALOR TOTAL DE MERCADO")
        println("=================================================")

        resultado.forEach { doc ->
            val nombreLiga = doc.getString("_id")
            val valorTotal = doc.getDouble("valor_total")

            println("Liga: $nombreLiga - Valor total: $valorTotal €")
        }
    } catch (e: Exception) {
        println("Ha ocurrido un error en la consulta")
    }
}

fun mostrarJugadoresConEquipoYLiga() {
    try {
        val pipeline = listOf(

            Document(
                "\$lookup", Document()
                    .append("from", "equipos")
                    .append("localField", "id_equipo")
                    .append("foreignField", "id")
                    .append("as", "equipo")
            ),

            Document("\$unwind", "\$equipo"),

            Document(
                "\$lookup", Document()
                    .append("from", "ligas")
                    .append("localField", "equipo.id_liga")
                    .append("foreignField", "id_liga")
                    .append("as", "liga")
            ),

            Document("\$unwind", "\$liga"),

            Document(
                "\$project", Document()
                    .append("_id", 0)
                    .append("jugador", "\$nombre")
                    .append("equipo", "\$equipo.nombre")
                    .append("liga", "\$liga.nombre")
                    .append("posicion", "\$posicion")
            )
        )

        val resultado = coleccionJugadores.aggregate(pipeline)

        println("=================================================")
        println("JUGADORES CON SU EQUIPO Y LIGA")
        println("=================================================")

        resultado.forEach { doc ->
            println(
                "Jugador: ${doc.getString("jugador")} | " +
                        "Equipo: ${doc.getString("equipo")} | " +
                        "Liga: ${doc.getString("liga")} | " +
                        "Posición: ${doc.getString("posicion")}"
            )
        }
    } catch (e: Exception) {
        println("Ha ocurrido un error en la consulta")
    }
}


//BD
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

    val nombreColeccion = coleccion.namespace.collectionName

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


//MENU
fun menuMostar(options: List<String>) {
    options.forEachIndexed { index, option ->
        println("${index + 1}. $option")
    }
}


//VALIDACION INPUTS
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

    while (true) {

        println(mensaje)

        try {
            return readLine()!!.toDouble()
        } catch (e: NumberFormatException) {
            println("Formato no válido")
        }
    }

}