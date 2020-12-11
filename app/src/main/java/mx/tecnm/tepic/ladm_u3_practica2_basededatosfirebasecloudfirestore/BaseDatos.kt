package mx.tecnm.tepic.ladm_u3_practica2_basededatosfirebasecloudfirestore

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BaseDatos(
    context: Context?, //Activity que llamará a la conexión
    name: String?, //Nombre del archivo de la base de datos
    factory: SQLiteDatabase.CursorFactory?, //Cursor de una base de datos, actualmente obsoleto
    version: Int //Version de la base de datos
) : SQLiteOpenHelper(context, name, factory, version) {

    //Los dos metodos siguientes sirven para escribir las estructura de las tablas
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("create table agenda(id integer primary key autoincrement, lugar varchar(500), hora varchar(20), fecha varchar(20), descripcion varchar(500))");
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) { //Cuando actualizamos la  ESTRUCTURA DE LA TABLA como un alter table, recuerda cambiar las versión

    }
}