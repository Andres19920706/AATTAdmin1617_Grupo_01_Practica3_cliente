/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aatt.practica03.accesoDNI;

import aatt.practica03.model.Usuario;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;
import javax.swing.JOptionPane;

/**
 * La clase ObtenerDatos implementa cuatro métodos públicos que permiten obtener
 * determinados datos de los certificados de tarjetas DNIe, Izenpe y Ona.
 *
 * @author Juan Carlos Cuevas Martínez
 * @author Andres Ruiz Peñuela
 * @author Luis Jesús Montes Pérez
 * @version 3.0.0
 * * falta termina javadoc
 */
public class ObtenerDatos {
    /**
     * Consantes
     */
    private static final String ruta = "Source/datos.txt";
    private static final byte[] dnie_v_1_0_Atr = {
        (byte) 0x3B, (byte) 0x7F, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x6A, (byte) 0x44,
        (byte) 0x4E, (byte) 0x49, (byte) 0x65, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x90, (byte) 0x00};
    private static final byte[] dnie_v_1_0_Mask = {
        (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
        (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFF};
    /**
     * Constructor por defecto
     */
    public ObtenerDatos() {
    }
    
    /**
     * Método para leer los datos del DNI y generar un documento de texto con
     * los datos leidos.
     * 
     * @return objeto tipo Usuario
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public Usuario LeerNIF() throws FileNotFoundException, IOException {

        Usuario user = null; //Objeto de tipo usuario.
        byte[] datos=null; //Array de byte para lamacenar los datos del DNI.
        
        /**
         * Establecemos contacto con la tarjeta.
         */
        try {
            Card c = ConexionTarjeta(); //Establecemos una conexión.
            
            if (c == null) {//Si no se ha podedio establecer una conexión.
                throw new Exception("ACCESO DNIe: No se ha encontrado ninguna tarjeta");
            }
            
            //Si la conexión fue existosa.
            byte[] atr = c.getATR().getBytes(); //Obtenmos el ATR de la tarjeta y lo convertios de String a Bytes.
                                                //El ATR (Answer To Reset) contine el formato y contenido establecidos 
                                                //en el estándar ISO-7816 de la tarjeta.
                                                //Más información en https://www.dnielectronico.es/PDFs/ATR1.pdf
            CardChannel ch = c.getBasicChannel(); //Obtenmos el canal basico de la tardeja.

            if (esDNIe(atr)) { //Si la tarjeta ledia es un DNI
                datos = leerCertificado(ch); //Obtenemos el certificado.
                if(datos!=null){ //Si no se ha producido ningún error al leer el
                                 //certificado.
                    user = leerDatosUsuario(datos); //Leemos los datos del usuairo
                }
            }else{
                throw new Exception("ACCESO DNIe: No es un DNI.");
            }
            
            //Desconectamos.
            c.disconnect(false);

        } catch (Exception ex) { //Si se produce alguna excepción.
            Logger.getLogger(ObtenerDatos.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
        /**
         * Creamos el fichero con los datos leidos.
         */
        FileWriter archivo = null;//new File(ruta);
        PrintWriter bw = null;
        /*Guardamos el resultado de la lectura del DNI en un txt.
        */
        if(datos!=null){
            try {
                archivo = new FileWriter(ruta);
                //Creamos la instanica para copiar los datos al txt.
                bw = new PrintWriter(archivo);
                //Realizamo la copia.
                bw.println("Pos \t Byte \tDec \tCaracter");
                byte[] n = new byte[1];
                for(int i=0;i<datos.length;i++)
                {
                    n[0]=datos[i];
                    bw.println(i+"\t"+String.format(" %2X", datos[i])+"\t"+datos[i]+"\t"+new String(n));
                }
            } 
            catch (FileNotFoundException e) {
                //Execepion al no encontrar el archivo.
                e.printStackTrace();
                return null;
            }
            catch (IOException ex) {
                //Error en la escritura del archivo.
                ex.printStackTrace();
                return null;
            } 
            finally {
                //Cerramos el BufferedWriter.
                try {
                    if (bw != null) {
                        bw.flush();
                        bw.close();
                        archivo.close();
                    }       
                }
                catch (IOException e2) {
                    //Error en el archvio.
                    e2.printStackTrace();
                    return null;
                }
            }
        }
        
        //Deovlemos el objeto Usuario, con los datos leidos
        return user;
    }

    /**
     * El método leerCertificado, permite estraer los datos del DNI, así como 
     * nombre, apellidos, nif, dirección, etc.
     * 
     * Referencia Seccion 4 del Manual de Comandos para Desarrolladores 102
     * 
     * @param ch de tipo CardChannel
     * @return array de bytes
     * @throws CardException
     * @throws CertificateException 
     */
    public byte[] leerCertificado(CardChannel ch) throws CardException, CertificateException, IOException {
        
        //[1] PRÁCTICA 3. Punto 1.a
        /**
         * Comando: Select
         * Función: Selecciona directa de un fichero dedicado (DF) por nombre.
         * Estructura: 0x00 0xa4 0x04 0x00 0x0b 0x4D 0x61 0x73 0x74 .... 0x65
         *  - 1º octecto: 0x00 "CLA"
         *  - 2º octecto: 0xA4 "INS"
         *  - 3º octecto: 0x04 "P1" -> Selección directa de DF por nombre
         *  - 4º octecto: 0x00 "P2"
         *  - 5º octecto: 0x0b "LC" o longitud de los datos en bytes
         *  - Resto de cotectos: "Datos de acuerdo a P1-P2"
         * 
         * @see Apartado 4.8. Comando Select del Manual de Comandos para Desarrolladores 102
         * @return null si el byte de estado no es correcto.
         */
        byte[] command = new byte[]{(byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, (byte) 0x0b, (byte) 0x4D, (byte) 0x61, (byte) 0x73, (byte) 0x74, (byte) 0x65, (byte) 0x72, (byte) 0x2E, (byte) 0x46, (byte) 0x69, (byte) 0x6C, (byte) 0x65};
        ResponseAPDU r = ch.transmit(new CommandAPDU(command));
        if ((byte) r.getSW() != (byte) 0x9000) {//Comprobamos si el byte de estado es correcto o no.
            System.out.println("ACCESO DNIe: SW incorrecto"); 
            return null;//No es correcto
        }

        //[2] PRÁCTICA 3. Punto 1.a
        /**
         * Comando: Select
         * Función: Selecciona de un fichero dedicado (DF) o elemental (EF)
         * Estructura: 0x00 0xA4 0x00 0x00 0x02 0x50 0x15
         *  - 1º octecto: 0x00 "CLA"
         *  - 2º octecto: 0xA4 "INS"
         *  - 3º octecto: 0x00 "P1" -> Selecciona DF o EF por Id (data field = id)
         *  - 4º octecto: 0x00 "P2"
         *  - 5º octecto: 0x02 "LC" o longitud de los datos en bytes
         *  - Resto de cotectos: "Datos de aceurdo a P1-P2"
         * 
         * @see Apartado 4.8. Comando Select del Manual de Comandos para Desarrolladores 102
         * @return Null
         */
        command = new byte[]{(byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x50, (byte) 0x15};
        r = ch.transmit(new CommandAPDU(command));

        if ((byte) r.getSW() != (byte) 0x9000) {
            System.out.println("ACCESO DNIe: SW incorrecto");
            return null;
        }

        //[3] PRÁCTICA 3. Punto 1.a
        /**
         * Comando: Select
         * Función: Selecciona de un fichero dedicado (DF) o elemental (EF)
         * Estructura: 0x00 0xA4 0x00 0x00 0x02 0x50 0x15
         *  - 1º octecto: 0x00 "CLA"
         *  - 2º octecto: 0xA4 "INS"
         *  - 3º octecto: 0x00 "P1" -> Selecciona DF o EF por Id (data field = id)
         *  - 4º octecto: 0x00 "P2"
         *  - 5º octecto: 0x02 "LC" o longitud de los datos en bytes
         *  - Resto de cotectos: "Datos de aceurdo a P1-P2"
         * 
         * @see Apartado 4.8. Comando Select del Manual de Comandos para Desarrolladores 102
         * @return Null
         */
        command = new byte[]{(byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x60, (byte) 0x04};
        r = ch.transmit(new CommandAPDU(command));

        byte[] responseData = null;
        if ((byte) r.getSW() != (byte) 0x9000) {
            System.out.println("ACCESO DNIe: SW incorrecto");
            return null;
        } else {
            responseData = r.getData();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] r2 = null; 
        int bloque = 0; 
        do {
             //[4] PRÁCTICA 3. Punto 1.b
            /**
             * Comando: Read Binary
             * Función: Obtener el contenido (o parte) de un fichero elemental transparente en el mensaje de respuesta este comando.
             * Estructura: 
             *  - 1º          octecto: 0x0X "CLA"
             *  - 2º          octecto: 0xB0 "INS"
             *  - 3º y 4º     octecto: 0xXX "P1-P2"
             *  - 5º          octecto: 0xXX "LC"             -> Vacío.
             *  - 6º hasta Nº octecto                        -> Vacío.
             *  - (N+1)º      octecto: 0xXX "LE"
             * 
             * @see Apartado 4.9. Comando Read  Binary del Manual de Comandos para Desarrolladores 102
             * @return Null
             */           
            final byte CLA = (byte) 0x00;//El valor aceptado puede ser desde 0x00 hasta 0x0F
            final byte INS = (byte) 0xB0;//El valor para especificar el comando es 0xB0
            final byte LE = (byte) 0xFF;// Número de bytes a leer, si este esta a 0, el nº de bytes a leer es 256.

            //[4] PRÁCTICA 3. Punto 1.b
            //Construimos el comando 'read binary', con un onffset del 1º byte a leer desde el principio del fichero de 0.
            command = new byte[]{CLA, INS, (byte) bloque/*P1*/, (byte) 0x00/*P2*/, LE};
            //P1 y P2, indica el byte del fichero, por el cual, empezamos para leer.
            r = ch.transmit(new CommandAPDU(command));
            //System.out.println("ACCESO DNIe: Response SW1=" + String.format("%X", r.getSW1()) + " SW2=" + String.format("%X", r.getSW2()));
            
            if ((byte) r.getSW() == (byte) 0x9000) {
                r2 = r.getData();
                
                baos.write(r2, 0, r2.length);
                
                //Escribimos por pantalla conforme vamos leyendo.
                for (int i = 0; i < r2.length; i++) {
                    byte[] t = new byte[1];
                    t[0] = r2[i];
                    String eco = i + (0xff * bloque) + String.format(" %2X", r2[i]) + " " + String.format(" %d", r2[i])+" "+new String(t);
                    System.out.println(eco);
                }
               bloque++;
            } else {
                return null;
            }

        } while (r2.length >= 0xfe);
        
        return baos.toByteArray(); //Devolvmeos el certificado.
        
    }

    /**
     * Este método establece la conexión con la tarjeta. La función busca el
     * Terminal que contenga una tarjeta, independientemente del tipo de tarjeta
     * que sea.
     *
     * @return objeto Card con conexión establecida
     * @throws Exception
     */
    private Card ConexionTarjeta() throws Exception {

        Card card = null;
        TerminalFactory factory = TerminalFactory.getDefault();
        List<CardTerminal> terminals = factory.terminals().list();
        System.out.println("Terminals: " + terminals);

        for (int i = 0; i < terminals.size(); i++) {

            // get terminal
            CardTerminal terminal = terminals.get(i);

            try {
                if (terminal.isCardPresent()) {
                    card = terminal.connect("*"); //T=0, T=1 or T=CL(not needed)
                    System.out.println("Terminals Conect: "+card);
                }
            } catch (Exception e) {//Si no se puede establcer conexión con la tarjeta.
                
                System.out.println("Exception catched: " + e.getMessage());
                card = null;
            }
        }
        return card;
    }

    /**
     * Este método nos permite saber el tipo de tarjeta que estamos leyendo del
     * Terminal, a partir del ATR de ésta.
     *
     * @param atrCard ATR de la tarjeta que estamos leyendo
     * @return tipo de la tarjeta. 1 si es DNIe, 2 si es Starcos y 0 para los
     * demás tipos
     */
    private boolean esDNIe(byte[] atrCard) {
        int j = 0;
        boolean found = false;

        //Es una tarjeta DNIe?
        if (atrCard.length == dnie_v_1_0_Atr.length) {
            found = true;
            while (j < dnie_v_1_0_Atr.length && found) {
                if ((atrCard[j] & dnie_v_1_0_Mask[j]) != (dnie_v_1_0_Atr[j] & dnie_v_1_0_Mask[j])) {
                    found = false; //No es una tarjeta DNIe
                }
                j++;
            }
        }
        if (found == true) {//Es un DNI
            return true;
        } else {//No es un DNI
            return false;
        }

    }

    /**
     * Analizar los datos leídos del DNIe para obtener
     *   - nombre
     *   - apellidos
     *   - NIF
     * @param datos
     * @return objeto de tipo Usuario
     */ 
    private Usuario leerDatosUsuario(byte[] datos) {
       //Creamos las variables necesarias.
       Usuario user = new Usuario(); //Objeto que devolvemos y contendra la info.
       String dni=""; //Variable que contendra el número del DNI.
       String[] usu = new String[3]; //Array que permitira extraer el nombre y los apellidos.
       
       usu[0]="";//Inicializamos.
       usu[1]="";
       usu[2]="";
       
       /**
        * Nº decimal que marcan el inicio del dni y del nombre y apellidos:
        *   9, corresponde al decimal 9, y si le sigue un nuemro indica el DNI
        *   30, corresponde al byte 27 (' en ASCII), le presigue Apellidos, Nombre.
        *   
       */
       int posi=0;  //Vector que contien las posición de los bytes desados.
       int i=0,j=0; //Variables de centinela.
       byte[] n = new byte[1]; // Variable para almanecar bytes, es necesario que sea de tipo arry para poder convertirlo a String.
       int Set=49; //Marca el inicio de un Set, en hexadecimal -> 31
       int [] OidDNI={85,4,5};//OID DNI, en decimal-> {}
       int [] OidApN={85,4,3};//OID Apellidos y Nombre en decimal-> {55,4,3}
               
       //Extremos el DNI.
       while(j<datos.length){
            //Buscamos el SET del DNI.
            if(datos[j]==Set)
            {//Se encontro un SET.
                //Buscamos la estrucutra del OID del DNI.
                if(datos[j+6]==OidDNI[0] && datos[j+7]==OidDNI[1] && datos[j+8]==OidDNI[2]){
                    
                    posi=(j+11);
                   //Extraemos el DNI.                
                    for(j=posi ; j<posi+9;j++){
                        n[0] = datos[j]; //Extremos el bytes
                        dni = dni + new String(n);//Lo convertimos a Sring y lo concatemos
                    }
                    j=datos.length; //Salimos de buscar.
                }
            }
            j++;
        }
       
        //Extraemos los apellidos y el nombre.
        j=0; //Iniciazliamos a 0.
        i=0; //Iniciazliamos a 0.
        while(j<datos.length)
        {
            
            if(datos[j]==Set){//Se econtro un set.
                if(datos[j+6]==OidApN[0] && datos[j+7]==OidApN[1] && datos[j+8]==OidApN[2]){
                    //Encontramos el OID que corresponde al Apellidos, Nombre (....)
                    j=(j+11);//Inicio del primer apellido.
                    while(datos[j]!=40)
                    {//Estamos dentro hasta que encontramos el caracter '(', que corresponde al 28 en dec al 40 en byte.
                        
                        if(datos[j]!=32 && datos[j]!=44){
                            //Si no es un espacio (en hexa el 20 o 32 en decimal) ni
                            //y una ',' (2C en Hexa o 32 en decimal), extraemos el caracter.
                            n[0] = datos[j]; //Extremos el bytes
                            usu[i] = usu[i]+ new String(n);//Lo convertimos a Sring y lo concatemos
                            
                        }
                        if(datos[j]==32 && i==0){
                            //Hemos extradio ya el 1º apellido.
                            i++;
                                
                        }
                        if(datos[j]==44 && i!=0){
                            //Hemos extradio ya el 2º apellido.
                            i++;
                        }
                        j++;
                        
                    }
                    j=datos.length;
                }
            }
            j++;
        }
            
       //Guardamos los datos al usuario.
       user.setApellido1(usu[0]);
       user.setApellido2(usu[1]);
       user.setNombre(usu[2]);
       user.setNif(dni);
       
       //Devolmeos los datos del usuario extraidos.
       return user;
    }
}
