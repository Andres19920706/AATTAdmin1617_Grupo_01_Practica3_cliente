/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Gestion;

import java.io.*;

/**
 * Esta clase, se ha creado con el único de fin de mostrar por la interfaz,
 * el arhivo de texto que se ha creado con los datos del DNI, los cuales, pueden
 * ser copiados a un excel.
 * @author Andres Ruiz Peñuela
 * @author Luis Jesús Montes Pérez
 * @version 3.0.0
 */
public class Fichero {
    
    public Fichero(){
    }
    /**
     * Abrir fichero de texto
     * @param ruta
     * @return contenido
     * @throws IOException 
     */
    public String AbrirTexto (String ruta) throws IOException{
        String contenido="";
        FileReader f =null;//Contiene el fichero.
        BufferedReader b=null;//Flujo de datos para la lectura.
        try{
            String cadena;//Es usada para leer el fichero.
            f = new FileReader(ruta);
            b = new BufferedReader(f);
            while((cadena = b.readLine())!=null) {
                contenido=contenido+cadena+"\n";//Concatemos la lecutra del fichero.
            }
        }catch (Exception e){}finally{
            try{//Cerramos el fichero.
                b.close();
            }catch(Exception e){}
        }
        //Devovlemos el contenido que va ha estar dentro del textárea.
        return contenido;
    }
}
