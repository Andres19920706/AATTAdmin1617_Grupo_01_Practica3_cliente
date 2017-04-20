package aatt.practica03.metodosHTTP;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * La clase PeticionPost, contiene métodos y atributos necesarios, para realizar
 * una petición POST en JAVA y poder así simular un método de HTTP.
 * @author Andres Ruiz Peñuela
 * @author Luis Jesús Montes Pérez
 * @version 3.0.0
 */
public class PeticionPost {
    /**
     * Codigos de error del cliente.
     */
    public final static String error[] = {"500 Servidor Desconectado.", "300 Error Interno."};
    /**
     * Atributos.
     */
    public final String metodo = "POST"; //Constante
    private String request ;//Dirección del servidor, al cual, realizamos la 
                            //petición.
    private String parametros; //Contiene el usuario y la clave, con el formato:
                               //usuario=APRUIZ&clave=00000000x
    
    /**
     * Constructor con parámetros.
     * @param direccion de tipo String
     * @param usu de tipo String
     * @param pass de tipo String
     */
    public PeticionPost(String direccion,String usu,String pass){
        this.request =direccion;
        this.parametros="usuario="+usu+"&clave="+pass;
    }
    
    /**
     * Metodos de acceso.
     */
    /**
     * Metodo que devuelve la direccion del serivor (url)
     * @return request de tipo String
     */
    public String getDireccion() {
	return this.request ;
    }
    /**
     * Metodo que inserta la direcion del servidor
     * @param direccion de tipo String
     */
    public void setDireccion(String direccion){
        this.request =direccion;
    }
    /**
     * Metodo que devuelve los parametros de la peticion
     * @return parametros de tipo String
     */
    public String getParametros() {
	return this.parametros;
    }
    /**
     * Metodo para insertar los parametros de la peticion
     * @param usu de tipo String
     * @param pass de tipo String
     */
    public void setParametros(String usu,String pass){
        this.request ="usuario="+usu+"&clave="+pass;
    }

    
     /**
     * Método: Autentica
     * Objetivo: Enviar petición post, para autenticar.
     * @return objeto de tipo String
     * @exception IOException
     */ 
     public  String autentica( ) throws MalformedURLException, IOException{
         byte[] msg = this.parametros.getBytes( StandardCharsets.UTF_8); //Contine los datos a enviar.
         String response = ""; //Almacena la respuesta
         DataOutputStream out = null; //Para capturar datos
         Reader in = null; //Para leer datos
         
         try{
              URL url = new URL( request ); /** Instancia e tipo URL */
              HttpURLConnection conect= (HttpURLConnection) url.openConnection(); /** Instnaica de tipo HttpURLConnection */
              conect.setDoOutput(true);//Especificamos que vamos a escribir.
              conect.setInstanceFollowRedirects(false);//En caso de recibir una respuesta con el código 3xx, no redireccionamos. (Por defecto es true)
              conect.setRequestMethod(metodo);//Indiamos el método HTTP de la peticion (Por defecto es Gest).
              //Definimos las propiedades de la petción
              conect.setRequestProperty("Content-Type","application/x-www-form-urlencoded");//Tipo de contenido.
              conect.setRequestProperty("charset","UTF-8");//Codificación.
              conect.setRequestProperty("Conent-length", Integer.toString(msg.length));//Longitud del contenido.
              conect.setUseCaches(false);//Indicamos que ignores las caches.
              
              //Preparamos el flujo de datos.
              out = new  DataOutputStream(conect.getOutputStream());
              out.write(msg);
              out.flush();
              
              // //Recibimos la respuesta.
              in = new BufferedReader(new InputStreamReader(conect.getInputStream(), "UTF-8"));
              //Guradmos la respuesta, para devolverla.
              int j=in.read(); //leemos el primer caracter
              do{
                  
                  response=response+(char)j; //La concatemos.
                  j=in.read(); //leemos el siguiente caracter.
              }
              while(j>=0);//Salimos cuando no haya mas caracteres.

        }catch (Exception e){//Auto-generado.
            return error[0]; //Se produjo un error.
        }finally{//Independientemente si se produce un error cerramos el lector y escritor.
            try{
                if(out != null){
                    out.close();
                }
                if(in != null){
                    in.close();
                }
            }catch(IOException ex){
                System.out.println("Exception al cerrar el lector o el escritor.");
                return error[1];
            }     
        }
         return response; //Devolvemos la respuesta.
     }

}
