package mx.tecnm.tepic.ladm_u3_practica2_basededatosfirebasecloudfirestore

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Thread.sleep

class MainActivity : AppCompatActivity() {
    var baseRemota = FirebaseFirestore.getInstance()
    var baseDatos=BaseDatos(this,"evento", null, 1); //Esta es la conexión con SQLite y su construcción en ejecución
    var listaID=ArrayList<String>();
    var idSeleccionadoEnLista=-1;
    var IDFirebase=ArrayList<String>();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        baseRemota.collection("evento")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    mensaje("¡ERROR! No se ha podido verificar los ID's desde Firestore")
                    return@addSnapshotListener
                }
                IDFirebase.clear();
                for (registros in querySnapshot!!) {
                    IDFirebase.add(registros.id);
                }
            }

        btninsertar.setOnClickListener {
            insertar();
        }
        btnRecuperar.setOnClickListener {
            consultar();
        }
        btnsync.setOnClickListener {
            limpiarFirestore();
            sleep(1000);
            subirAFirestore();
        }
        cargarContactos();
    }

    private fun insertar() {
        try {
            /* ORDEN PARA LAS INSERIONES
            1 - Apertura de base de datos ya sea Lectura o ecritura
            2- Construcción de sentencia de SQL
            3- Ejecución y mostrado de resultados*/
            var trans=baseDatos.writableDatabase; //Permite escribir (Y leer obviamente)
            var variables=ContentValues();
            variables.put("lugar", txtLugar.text.toString().toString());
            variables.put("hora",txtHora.text.toString());
            variables.put("fecha",txtFecha.text.toString());
            variables.put("descripcion",txtDescripcion.text.toString());
            var respuesta=trans.insert("agenda", null, variables)//Si regresa un -1 quiere decir que existe un error con la inserción
            if(respuesta==-1L){
                mensaje("Error de inserción, no se pudo insertar");
            }else{
                mensaje("Insertado con éxito :D");
                limpiarCampos();
            }
            trans.close();
        }catch (e: SQLiteException){
            mensaje(e.message!!);
        }
        cargarContactos();
    }

    private fun cargarContactos() {
        try {
            var trans=baseDatos.readableDatabase;
            var eventos= ArrayList<String>() //AQUI va a ir guardada la información recuperada en forma de cadena
            var respuesta=trans.query("agenda", arrayOf("*"),null,null,null,null,null);
            listaID.clear();
            if (respuesta.moveToFirst()){ //Condición para saber si hay por lo menos una persona en base de datos
                do{
                    var concatenacion="ID: ${respuesta.getInt(0)}\n" +
                            "Lugar: ${respuesta.getString(1)}\n" +
                            "Hora: ${respuesta.getString(2)} | Fecha: ${respuesta.getString(3)}\n" +
                            "Descripción: ${respuesta.getString(4)}";
                    eventos.add(concatenacion);
                    listaID.add(respuesta.getInt(0).toString())
                }while (respuesta.moveToNext());
            }else{
                eventos.add("Aun no se agregado ningún evento, sal a hacer amigos :D");
            }
            listaeventos.adapter= ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,eventos);
            this.registerForContextMenu(listaeventos)//Liga el menuppal con listaeventos
            listaeventos.setOnItemClickListener { adapterView, view, i, l ->
                idSeleccionadoEnLista=i;
                Toast.makeText(this, "Se seleccionó el elemento", Toast.LENGTH_SHORT).show()
            }
            trans.close();
        }catch (e:SQLiteException){
            mensaje("Error con la recuperación de la lista de eventos: "+e.message!!);
        }
    }

    private fun limpiarCampos() {
        txtLugar.setText("");
        txtHora.setText("");
        txtFecha.setText("");
        txtDescripcion.setText("");
    }

    private fun mensaje(msj: String) {
        AlertDialog.Builder(this)
                .setTitle("Atención")
                .setMessage("error: ${msj}")
                .setPositiveButton("OK"){d,i-> d.dismiss()}
                .show();
    }

    private fun consultar() {
        var trans=baseDatos.readableDatabase;
        var resultados=trans.query("agenda", arrayOf("id","lugar","hora","fecha","descripcion"),"fecha=?", arrayOf(txtFecha.text.toString()),null,null,null);
        if(resultados.moveToFirst()){ //Move to first regresa true si es que encontró el registro que estaba buscando
            var cadena="ID: "+resultados.getInt(0)+
                    "\nLugar: "+resultados.getString(1)+
                    "\nHora: "+resultados.getString(2)+
                    "\nFecha: "+resultados.getString(3)+
                    "\nDescripción: "+resultados.getString(4);
            mensaje(cadena);
            txtFecha.setText("");
        }else{
            mensaje("No se encontró el dato, socio.");
        }
        trans.close();
    }

    override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        var inflaterOB=menuInflater; //Carga y construye un objeto XML
        inflaterOB.inflate(R.menu.menuppal,menu) //Sube a memoria los objetos
    }

    override fun onContextItemSelected(item: MenuItem): Boolean { //Cuando se selecciona un item del menú contextual ya creado, se dispara este método
        if(idSeleccionadoEnLista==-1){
            mensaje("Recuerda que primero debes darle un toque al item para actualizarlo");
            return true;
        }
        when(item.itemId){
            R.id.itemActualizar->{
                var intent= Intent(this,MainActivity2::class.java)
                intent.putExtra("idActualizar",listaID.get(idSeleccionadoEnLista));
                startActivity(intent);
                idSeleccionadoEnLista=-1;
            }
            R.id.itemEliminar->{
                var IDEliminar=listaID.get(idSeleccionadoEnLista);
                AlertDialog.Builder(this).setTitle("ATENCIÓN")
                        .setMessage("¿Estás seguro que deseas eliminar el ID "+IDEliminar+"?")
                        .setPositiveButton("SI, ELIMINAR"){d,i->
                            eliminar(IDEliminar);
                        }
                        .setNeutralButton("No, no estoy listo"){d,i->}
                        .show();
                idSeleccionadoEnLista=-1;
            }
            R.id.itemSalir->{

            }
        }
        //return super.onContextItemSelected(item)
        return true;
    }
    private fun eliminar(IDeliminar: String) {
        try {
            var cueri=baseDatos.writableDatabase;
            var resultado=cueri.delete("agenda","id=?",arrayOf(IDeliminar));
            if(resultado==0){
                mensaje("Error, no se pudo eliminar");
            }else{
                mensaje("Se logró eliminar con éxito el ID ${IDeliminar}")
            }
            cueri.close();
            cargarContactos();
        }catch(e:SQLiteException){

        }
    }
    //----RELACIONADO CON FIREBASE

    private fun limpiarFirestore(){
        for(i in IDFirebase){
            baseRemota.collection("evento").document(i)
                .delete();
        }
        /*var activador = hashMapOf(
            "id" to "Activador");
        baseRemota.collection("evento")
            .add(activador as Any)*/
    }

    private fun subirAFirestore(){
        var trans=baseDatos.readableDatabase;
        var respuesta=trans.query("agenda", arrayOf("*"),null,null,null,null,null);
        if (respuesta.moveToFirst()){ //Condición para saber si hay por lo menos una persona en base de datos
            do{
                var datosInsertarREMOTOS = hashMapOf(
                    "id" to respuesta.getInt(0),
                    "lugar" to respuesta.getString(1),
                    "hora" to respuesta.getString(2),
                    "fecha" to respuesta.getString(3),
                    "descripcion" to respuesta.getString(4)
                );
                baseRemota.collection("evento")
                    .add(datosInsertarREMOTOS as Any)
                    .addOnSuccessListener {
                        Toast.makeText(this,"SE INSERTÓ CORRECTAMENTE CON ID EN CLOUD FIRESTORE: ${it.id}", Toast.LENGTH_LONG)
                            .show();
                    }
                    .addOnFailureListener {
                        mensaje("NO SE PUDO INSERTAR:\n${it.message!!}")
                    }
                //--
            }while (respuesta.moveToNext());
        }else{
            mensaje("No hay datos disponibles para sincronizar");
        }
    }
}