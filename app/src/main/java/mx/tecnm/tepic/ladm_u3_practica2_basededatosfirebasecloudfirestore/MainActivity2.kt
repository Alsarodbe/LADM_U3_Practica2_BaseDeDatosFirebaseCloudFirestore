package mx.tecnm.tepic.ladm_u3_practica2_basededatosfirebasecloudfirestore

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main2.*

class MainActivity2 : AppCompatActivity() {
    var baseDatos=BaseDatos(this,"evento", null, 1); //Esta es la conexión con SQLite y su construcción en ejecución
    var id="";
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        var extra= intent.extras;
        id=extra?.getString("idActualizar")!!;
        muestraID.setText(muestraID.text.toString()+" ${id}")
        try{
            var base=baseDatos.readableDatabase;
            var respuesta=base.query("agenda",arrayOf("lugar,hora,fecha,descripcion"),"id=?",arrayOf(id),null,null,null);
            if(respuesta.moveToFirst()){
                txtactualizarlugar.setText(respuesta.getString(0));
                txtactualizarhora.setText(respuesta.getString(1));
                txtactualizarfecha.setText(respuesta.getString(2));
                txtactualizardescripcion.setText(respuesta.getString(3));
            }else{
                mensaje("No se encontró ID");
            }
            base.close();
        }catch(e:SQLiteException){
            mensaje(e.message!!)
        }
        btnactualizar.setOnClickListener {
            actualizar(id);
        }
        btnregresar.setOnClickListener {
            finish();
        }
    }

    private fun actualizar(id: String) {
        try{
            var cueri=baseDatos.writableDatabase;
            var valores=ContentValues();
            valores.put("lugar",txtactualizarlugar.text.toString());
            valores.put("hora",txtactualizarhora.text.toString());
            valores.put("fecha",txtactualizarfecha.text.toString());
            valores.put("descripcion",txtactualizardescripcion.text.toString());
            var res=cueri.update("agenda",valores,"ID=?", arrayOf(id));
            if(res>0){
                mensaje("Se insertó con éxito");
            }else{
                mensaje("Un error ha ocurrido durante la actualización del dato")
            }
            cueri.close();
        }catch(e:SQLiteException){
            mensaje(e.message!!);
        }
    }

    private fun mensaje(msj: String) {
        AlertDialog.Builder(this)
            .setTitle("Atención")
            .setMessage(msj)
            .setPositiveButton("OK"){d,i-> d.dismiss()}
            .show();
    }
}